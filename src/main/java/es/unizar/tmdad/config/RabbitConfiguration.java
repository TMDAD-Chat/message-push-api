package es.unizar.tmdad.config;

import es.unizar.tmdad.listener.MessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
@Slf4j
public class RabbitConfiguration {

    @Value("${chat.exchanges.input}")
    private String exchangeName;

    @Value("${chat.exchanges.old-messages}")
    private String oldMessagesExchangeName;

    @Value("${spring.application.name}")
    private String appName;

    @Bean("rabbitQueue")
    String queueName(){
        return getQueueName(appName, exchangeName);
    }

    @Bean
    Queue queue(@Qualifier("rabbitQueue") String queueName) {
        return new Queue(queueName, false, true, false);
    }

    private String getQueueName(String appName, String exchangeName) {
        return appName + "." + exchangeName + "." + UUID.randomUUID();
    }

    @Bean("inputExchange")
    FanoutExchange inputExchange() {
        return new FanoutExchange(exchangeName, true, false);
    }

    @Bean
    FanoutExchange oldMessageExchange() {
        return new FanoutExchange(oldMessagesExchangeName, true, false);
    }

    @Bean
    Binding binding(Queue queue, @Qualifier("inputExchange") FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }

    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
                                             MessageListenerAdapter listenerAdapter,
                                             @Qualifier("rabbitQueue") String queueName) {
        log.info("Binding to queue {}", queueName);
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName);
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(MessageListener receiver) {
        return new MessageListenerAdapter(receiver, "apply");
    }
}
