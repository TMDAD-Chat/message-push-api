package es.unizar.tmdad.controller.impl;

import es.unizar.tmdad.controller.UserController;
import es.unizar.tmdad.service.MessageService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/user")
public class UserControllerImpl implements UserController {

    private final MessageService messageService;

    public UserControllerImpl(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    @RequestMapping("/{id}")
    public SseEmitter getMessagesOfUser(@PathVariable("id") String userId) {
        final SseEmitter emitter = new SseEmitter();
        final String topic = "user." + userId;
        messageService.addSseEmmiter(topic, emitter);

        emitter.onCompletion(()->messageService.removeSseEmmiter(topic, emitter));
        emitter.onTimeout(()->messageService.removeSseEmmiter(topic, emitter));


        return emitter;
    }

}
