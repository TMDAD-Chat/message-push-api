package es.unizar.tmdad.controller;

import es.unizar.tmdad.controller.exception.UserNotInTheRoomException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface RoomController {

    SseEmitter sendNewTextMessage(Long roomId, String username) throws UserNotInTheRoomException;
}
