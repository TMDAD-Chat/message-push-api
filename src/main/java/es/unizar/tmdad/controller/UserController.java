package es.unizar.tmdad.controller;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface UserController {

    SseEmitter getMessagesOfUser(String userId);

}
