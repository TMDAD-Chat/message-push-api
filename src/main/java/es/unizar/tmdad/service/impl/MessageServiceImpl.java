package es.unizar.tmdad.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.unizar.tmdad.adt.MessageIn;
import es.unizar.tmdad.adt.MessageListIn;
import es.unizar.tmdad.adt.MessageRequest;
import es.unizar.tmdad.adt.RecipientType;
import es.unizar.tmdad.config.Constants;
import es.unizar.tmdad.repository.MessageTimestampRepository;
import es.unizar.tmdad.repository.entity.MessageTimestampEntity;
import es.unizar.tmdad.service.MessageService;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final Map<String, List<ClientConnectedToTopic>> sseEmmiterList = new HashMap<>();
    private final ObjectMapper objectMapper;
    private final MessageTimestampRepository messageTimestampRepository;
    private final RabbitTemplate rabbitTemplate;
    private final AtomicInteger onlineUsersGauge;

    @Value("${chat.exchanges.old-messages}")
    private String oldMessagesExchangeName;

    public MessageServiceImpl(ObjectMapper objectMapper, MessageTimestampRepository messageTimestampRepository, RabbitTemplate rabbitTemplate, AtomicInteger onlineUsersGauge) {
        this.objectMapper = objectMapper;
        this.messageTimestampRepository = messageTimestampRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.onlineUsersGauge = onlineUsersGauge;
    }

    @Override
    public void addSseEmmiter(String topic, String user, SseEmitter emitter) {
        List<String> requests = new ArrayList<>();
        String requestUUID = requestUnreadMessagesForTopicAndUser(topic, user);
        requests.add(requestUUID);
        if(StringUtils.startsWithIgnoreCase(topic, "user")) {
            String globalUUIDRequest = requestUnreadMessagesForTopicAndUser(Constants.GLOBAL_MESSAGE_DB_TYPE, user);
            requests.add(globalUUIDRequest);
        }

        var emmitersForTopic = this.sseEmmiterList.get(topic);
        if(Objects.isNull(emmitersForTopic)){
            emmitersForTopic = Collections.synchronizedList(new ArrayList<>());
        }

        synchronized (emmitersForTopic) {
            emmitersForTopic.add(ClientConnectedToTopic.builder()
                            .user(user)
                            .emitter(emitter)
                            .pendingMessageRequests(requests)
                    .build());
            this.sseEmmiterList.put(topic, emmitersForTopic);
        }
        this.onlineUsersGauge.incrementAndGet();
    }

    private String requestUnreadMessagesForTopicAndUser(String topic, String user) {
        var messageTimestampEntity = messageTimestampRepository.findById(MessageTimestampEntity.MessageTimestampCompositeKey
                .builder()
                        .topic(topic)
                        .username(user)
                .build());

        String requestUUID = UUID.randomUUID().toString();
        Date startingPoint = new Date(0);
        if(messageTimestampEntity.isPresent()){
            startingPoint = messageTimestampEntity.get().getMessageTimestamp();
        }

        RecipientType actualRecipientType = RecipientType.GLOBAL;
        if(topic.startsWith("user")){
            actualRecipientType = RecipientType.USER;
        }else if(topic.startsWith("room")){
            actualRecipientType = RecipientType.ROOM;
        }

        MessageRequest messageRequest = MessageRequest.builder()
                .requestId(requestUUID)
                .recipient(user)
                .recipientType(actualRecipientType)
                .since(startingPoint)
                .build();
        log.info("Requesting messages of user {} in topic {} since {}", user, messageRequest.getRecipientType(), messageRequest.getSince());

        String messageRequestStr = null;
        try {
            messageRequestStr = this.objectMapper.writeValueAsString(messageRequest);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if(Objects.nonNull(messageRequestStr)) {
            this.rabbitTemplate.convertAndSend(oldMessagesExchangeName, "", messageRequestStr);
        }

        return requestUUID;
    }

    @Override
    public void removeSseEmmiter(String topic, String user) {
        log.info("Removing emitter for topic {}.", topic);
        List<ClientConnectedToTopic> emmitersForTopic = this.sseEmmiterList.get(topic);
        if(!Objects.isNull(emmitersForTopic)){
            var newEmmiters = emmitersForTopic.stream()
                    .filter(clientConnectedToTopic -> !Objects.equals(user, clientConnectedToTopic.getUser()))
                    .collect(Collectors.toList());
            if(!Objects.equals(newEmmiters.size(), emmitersForTopic.size())){
                this.onlineUsersGauge.decrementAndGet();
                this.sseEmmiterList.put(topic, newEmmiters);
            }
        }
    }

    @Override
    public void processMessage(MessageListIn msg) throws IOException {
        if(!msg.getMessages().isEmpty()) {
            switch (msg.getRecipientType()){
                case ROOM:
                    String topic = "room." + msg.getRecipient();
                    String user = getUserWhoRequestedMessage(topic, msg.getRequestId());
                    forwardMessageToTopic(msg, user, topic);
                    log.info("Received {} messages for user {} in topic room.{}.",
                            msg.getMessages().size(), user, msg.getRecipient());
                    break;
                case USER:
                    forwardMessageToTopic(msg, msg.getRecipient(), "user." + msg.getRecipient());
                    log.info("Received {} messages for user {} in topic user.{}.",
                            msg.getMessages().size(), msg.getRecipient(), msg.getRecipient());
                    break;
                case GLOBAL:
                    forwardMessageToAllUsers(msg);
                    break;
            }
        }

    }

    private String getUserWhoRequestedMessage(String topic, String requestId) {
        var list = this.sseEmmiterList.get(topic);
        if(Objects.nonNull(list)){
            var user = list.stream()
                    .filter(clientConnectedToTopic -> clientConnectedToTopic.getPendingMessageRequests().contains(requestId))
                    .collect(Collectors.toList());
            if(user.size() > 1){
                log.error("More than one user with the same request Id {} in the same room... Picking only the first one.", requestId);
            }
            if(!user.isEmpty()){
                return user.get(0).getUser();
            }
        }
        log.error("No user found for request {}", requestId);
        return null;
    }

    private void forwardMessageToTopic(MessageListIn msg, String user, String topic) throws JsonProcessingException {
        String msgAsString = objectMapper.writeValueAsString(msg);

        List<ClientConnectedToTopic> emmitersForTopic = this.sseEmmiterList.get(topic);
        if(!Objects.isNull(emmitersForTopic)){
            sendMessageToEmitters(msgAsString, msg.getRequestId(), user, topic, emmitersForTopic);
            updateLastReadMessage(topic, user, msg);
        }
    }

    private void updateLastReadMessage(String topic, String user, MessageListIn messageList) {
        var messageTimestampEntity = messageTimestampRepository.findById(MessageTimestampEntity.MessageTimestampCompositeKey
                .builder()
                .topic(topic)
                .username(user)
                .build())
                .orElseGet(() -> new MessageTimestampEntity(topic, user, new Date(0)));

        var newestDate = messageList.getMessages().stream()
                .map(MessageIn::getCreationTimestamp)
                .map(this::parseDate)
                .filter(Objects::nonNull)
                .max(Date::compareTo)
                .orElse(new Date(0));

        if(messageTimestampEntity.getMessageTimestamp().compareTo(newestDate) < 0){
            messageTimestampEntity.setMessageTimestamp(newestDate);
        }

        messageTimestampRepository.save(messageTimestampEntity);
    }

    @SneakyThrows
    private Date parseDate(String s) {
        if(Objects.isNull(s)){
            return null;
        }
        TemporalAccessor ta = DateTimeFormatter.ISO_DATE_TIME.parse(s);
        Instant i = Instant.from(ta);
        return Date.from(i);
    }

    private void forwardMessageToAllUsers(MessageListIn msg) throws JsonProcessingException {
        String msgAsString = objectMapper.writeValueAsString(msg);
        if(Objects.isNull(msg.getRecipient())) {
            this.sseEmmiterList.forEach((key, value) -> {
                if (key.startsWith("user")) {
                    sendMessageToEmitters(msgAsString, msg.getRequestId(), null, key, value);
                    updateLastReadMessage(Constants.GLOBAL_MESSAGE_DB_TYPE, key.split("\\.")[1], msg);
                }
            });
        }else{
            String user = msg.getRecipient();
            String fakeGlobalTopic = "user." + user;
            List<ClientConnectedToTopic> emmitersForTopic = this.sseEmmiterList.get(fakeGlobalTopic);
            if(!Objects.isNull(emmitersForTopic)){
                sendMessageToEmitters(msgAsString, msg.getRequestId(), user, fakeGlobalTopic, emmitersForTopic);
                updateLastReadMessage(Constants.GLOBAL_MESSAGE_DB_TYPE, user, msg);
            }
        }
    }

    private void sendMessageToEmitters(String msgAsString, String requestId, String user, String key, List<ClientConnectedToTopic> value) {
        List<ClientConnectedToTopic> deadEmitters = new ArrayList<>();
        synchronized (value) {
            value.forEach(emitter -> {
                try {
                    if(Objects.isNull(user) || Objects.equals(user, emitter.getUser())) {
                        emitter.getEmitter().send(msgAsString);
                    }
                    emitter.getPendingMessageRequests().remove(requestId);
                } catch (IOException e) {
                    deadEmitters.add(emitter);
                }
            });
        }
        if(!deadEmitters.isEmpty()){
            log.info("Could not send message to {} emmiter/s. Removing them from list...", deadEmitters.size());
            value.removeAll(deadEmitters);
            this.sseEmmiterList.put(key, value);
        }
    }

    @Data
    @Builder
    private static class ClientConnectedToTopic{
        private SseEmitter emitter;
        private String user;
        private List<String> pendingMessageRequests;
    }
}
