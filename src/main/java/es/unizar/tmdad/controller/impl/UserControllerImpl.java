package es.unizar.tmdad.controller.impl;

import es.unizar.tmdad.controller.UserController;
import es.unizar.tmdad.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserControllerImpl implements UserController {

    private final MessageService messageService;

    public UserControllerImpl(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    @RequestMapping("/{id}")
    public SseEmitter getMessagesOfUser(@PathVariable("id") String username) {
        final SseEmitter emitter = new SseEmitter();
        final String topic = "user." + username;
        messageService.addSseEmmiter(topic, username, emitter);

        emitter.onError((callback)->messageService.removeSseEmmiter(topic, username));
        emitter.onCompletion(()->messageService.removeSseEmmiter(topic, username));
        emitter.onTimeout(()->messageService.removeSseEmmiter(topic, username));

        log.info("Logging user {}", topic);

        return emitter;
    }

}
