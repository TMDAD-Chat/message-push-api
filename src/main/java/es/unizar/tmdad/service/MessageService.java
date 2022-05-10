package es.unizar.tmdad.service;

import es.unizar.tmdad.adt.MessageListIn;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

public interface MessageService {

    void addSseEmitter(String topic, String user, SseEmitter emitter);
    void removeSseEmitter(String topic, String user);

    void updateUsersOnline();
    void processMessage(MessageListIn msg) throws IOException;

    void sendHeartBeatToEmitters();
}
