package es.unizar.tmdad.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface RoomController {

    SseEmitter sendNewTextMessage(String groupId);
}
