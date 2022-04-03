package es.unizar.tmdad.controller;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface RoomController {

    SseEmitter sendNewTextMessage(Long roomId, String username);
}
