package es.unizar.tmdad.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.unizar.tmdad.adt.MessageListIn;
import es.unizar.tmdad.service.MessageService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class MessageListener {

    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    public MessageListener(MessageService messageService, ObjectMapper objectMapper) {
        this.messageService = messageService;
        this.objectMapper = objectMapper;
    }

    @SneakyThrows
    public void apply(String input) {
        MessageListIn msg = objectMapper.readValue(input, MessageListIn.class);
        this.apply(msg);
    }

    public void apply(MessageListIn messageInFlux) {
        log.info("Processing msg {}.", messageInFlux);
        try {
            messageService.processMessage(messageInFlux);
        } catch (IOException e) {
            log.info("Error while processing message {}", messageInFlux);
        }
    }
}
