package es.unizar.tmdad.controller;

import org.springframework.http.ResponseEntity;

public interface RoomController {

    ResponseEntity<Void> sendNewTextMessage(String groupId);

}
