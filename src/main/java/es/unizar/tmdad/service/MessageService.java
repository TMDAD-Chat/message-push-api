package es.unizar.tmdad.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import es.unizar.tmdad.adt.MessageIn;

import java.io.IOException;

public interface MessageService {

    void addSseEmmiter(String topic, SseEmitter emitter);
    void removeSseEmmiter(String topic, SseEmitter emitter);
    void processMessage(MessageIn msg) throws IOException;

}
