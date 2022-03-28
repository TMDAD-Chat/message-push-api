package es.unizar.tmdad.controller.impl;

import es.unizar.tmdad.controller.RoomController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/room")
public class RoomControllerImpl implements RoomController {

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<Void> sendNewTextMessage(@PathVariable("id") String userId) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
