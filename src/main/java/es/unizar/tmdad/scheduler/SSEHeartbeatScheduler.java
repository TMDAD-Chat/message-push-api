package es.unizar.tmdad.scheduler;

import es.unizar.tmdad.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SSEHeartbeatScheduler {

    private final MessageService messageService;

    public SSEHeartbeatScheduler(MessageService messageService) {
        this.messageService = messageService;
    }

    @Scheduled(fixedRate = 30000)
    public void runScheduledTask(){
        log.info("Running emitterSSE HeartBeat scheduled tasks");
        messageService.sendHeartBeatToEmitters();
    }

}
