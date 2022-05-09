package es.unizar.tmdad.scheduler;

import es.unizar.tmdad.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OnlineStatusScheduler {

    private final MessageService messageService;

    public OnlineStatusScheduler(MessageService messageService) {
        this.messageService = messageService;
    }

    @Scheduled(fixedRate = 5000)
    public void runScheduledTask(){
        log.info("Running online users scheduled tasks");
        messageService.updateUsersOnline();
    }

}
