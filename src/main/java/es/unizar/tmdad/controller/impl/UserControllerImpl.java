package es.unizar.tmdad.controller.impl;

import es.unizar.tmdad.controller.UserController;
import es.unizar.tmdad.controller.exception.UnauthorizedException;
import es.unizar.tmdad.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Objects;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserControllerImpl implements UserController {

    private final MessageService messageService;

    public UserControllerImpl(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    @GetMapping("/{email}")
    public SseEmitter getMessagesOfUser(@PathVariable("email") String email, @RequestHeader("X-Auth-User") String authEmail) throws UnauthorizedException {

        if(!Objects.equals(authEmail, email)){
            throw new UnauthorizedException("Auth email does not match sender email.");
        }

        final SseEmitter emitter = new SseEmitter();
        final String topic = "user." + email;
        messageService.addSseEmitter(topic, email, emitter);

        emitter.onError((callback)->messageService.removeSseEmitter(topic, email));
        emitter.onCompletion(()->messageService.removeSseEmitter(topic, email));
        emitter.onTimeout(()->messageService.removeSseEmitter(topic, email));

        log.info("Logging user {}", topic);

        return emitter;
    }

}
