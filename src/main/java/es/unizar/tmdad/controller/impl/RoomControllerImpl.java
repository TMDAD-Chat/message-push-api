package es.unizar.tmdad.controller.impl;

import es.unizar.tmdad.controller.RoomController;
import es.unizar.tmdad.controller.exception.UserNotInTheRoomException;
import es.unizar.tmdad.service.MessageService;
import es.unizar.tmdad.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/room")
@Slf4j
public class RoomControllerImpl implements RoomController {
    private final MessageService messageService;
    private final RoomService roomService;

    public RoomControllerImpl(MessageService messageService, RoomService roomService){
        this.messageService = messageService;
        this.roomService = roomService;
    }

    @Override
    @GetMapping("/{id}/messages/{user}")
    public SseEmitter sendNewTextMessage(@PathVariable("id") Long roomId, @PathVariable("user") String username) throws UserNotInTheRoomException {

        if(!roomService.isUserInTheRoom(username,roomId)){
            throw new UserNotInTheRoomException(roomId,username);
        }

        final SseEmitter emitter = new SseEmitter();
        final String topic = "room." + roomId;
        messageService.addSseEmitter(topic, username, emitter);

        emitter.onError((callback)->messageService.removeSseEmitter(topic, username));
        emitter.onCompletion(()->messageService.removeSseEmitter(topic, username));
        emitter.onTimeout(()->messageService.removeSseEmitter(topic, username));

        log.info("Room {}", topic);
        return emitter;
    }
}
