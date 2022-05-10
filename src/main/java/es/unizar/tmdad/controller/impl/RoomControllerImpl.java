package es.unizar.tmdad.controller.impl;

import es.unizar.tmdad.controller.RoomController;
import es.unizar.tmdad.service.MessageService;
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

    public RoomControllerImpl(MessageService messageService){
        this.messageService = messageService;
    }

    @Override
    @GetMapping("/{id}/messages/{user}")
    public SseEmitter sendNewTextMessage(@PathVariable("id") Long roomId, @PathVariable("user") String username) {
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
