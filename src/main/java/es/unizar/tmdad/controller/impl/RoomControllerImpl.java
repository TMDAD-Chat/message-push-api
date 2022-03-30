package es.unizar.tmdad.controller.impl;

import es.unizar.tmdad.controller.RoomController;
import es.unizar.tmdad.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @GetMapping("/{id}")
    public SseEmitter sendNewTextMessage(@PathVariable("id") String userId) {
        final SseEmitter emitter = new SseEmitter();
        final String topic = "user." + userId;
        messageService.addSseEmmiter(topic, emitter);

        emitter.onCompletion(()->messageService.removeSseEmmiter(topic, emitter));
        emitter.onTimeout(()->messageService.removeSseEmmiter(topic, emitter));

        log.info("Room {}", topic);
        return emitter;
    }
}
