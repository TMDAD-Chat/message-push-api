package es.unizar.tmdad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MessagePushAPIApplication {

	public static void main(String[] args) {
		SpringApplication.run(MessagePushAPIApplication.class, args);
	}

}
