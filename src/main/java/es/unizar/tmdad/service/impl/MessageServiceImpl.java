package es.unizar.tmdad.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.unizar.tmdad.adt.MessageIn;
import es.unizar.tmdad.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final Map<String, List<SseEmitter>> sseEmmiterList = new HashMap<>();
    private final ObjectMapper objectMapper;

    public MessageServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void addSseEmmiter(String topic, SseEmitter emitter) {
        List<SseEmitter> emmitersForTopic = this.sseEmmiterList.get(topic);
        if(Objects.isNull(emmitersForTopic)){
            emmitersForTopic = new ArrayList<>();
        }
        emmitersForTopic.add(emitter);
        this.sseEmmiterList.put(topic, emmitersForTopic);
    }

    @Override
    public void removeSseEmmiter(String topic, SseEmitter emitter) {
        List<SseEmitter> emmitersForTopic = this.sseEmmiterList.get(topic);
        if(!Objects.isNull(emmitersForTopic)){
            emmitersForTopic.remove(emitter);
        }
        this.sseEmmiterList.put(topic, emmitersForTopic);
    }

    @Override
    public void processMessage(MessageIn msg) throws IOException {
        String topic = null;
        boolean globalMessage = false;
        switch (msg.getRecipientType()){
            case ROOM:
                topic = "room." + msg.getRecipient();
                break;
            case USER:
                topic = "user." + msg.getRecipient();
                break;
            case GLOBAL:
                globalMessage = true;
                break;
        }

        if(Objects.nonNull(topic)) {
            forwardMessageToTopic(msg, topic);
        }else if(globalMessage){
            forwardMessageToAllUsers(msg);
        }

    }

    private void forwardMessageToTopic(MessageIn msg, String topic) throws JsonProcessingException {
        List<SseEmitter> emmitersForTopic = this.sseEmmiterList.get(topic);
        String msgAsString = objectMapper.writeValueAsString(msg);
        if(!Objects.isNull(emmitersForTopic)){
            sendMessageToEmitters(msgAsString, topic, emmitersForTopic);
        }
    }

    private void forwardMessageToAllUsers(MessageIn msg) throws JsonProcessingException {
        String msgAsString = objectMapper.writeValueAsString(msg);
        this.sseEmmiterList.forEach((key, value) -> {
            if (key.startsWith("user")) {
                sendMessageToEmitters(msgAsString, key, value);
            }
        });
    }

    private void sendMessageToEmitters(String msgAsString, String key, List<SseEmitter> value) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        value.forEach(emitter -> {
            try {
                emitter.send(msgAsString);
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        });
        if(!deadEmitters.isEmpty()){
            log.info("Could not send message to {} emmiter/s. Removing them from list...", deadEmitters.size());
            value.removeAll(deadEmitters);
            this.sseEmmiterList.put(key, value);
        }
    }
}
