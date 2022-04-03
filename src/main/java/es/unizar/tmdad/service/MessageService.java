package es.unizar.tmdad.service;

import es.unizar.tmdad.adt.MessageListIn;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

public interface MessageService {

    void addSseEmmiter(String topic, String user, SseEmitter emitter);
    void removeSseEmmiter(String topic, SseEmitter emitter);
    void processMessage(MessageListIn msg) throws IOException;

}
