package es.unizar.tmdad.config;

import es.unizar.tmdad.listener.MessageListener;
import es.unizar.tmdad.listener.UserMessageListener;
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

    @Value("${chat.exchanges.users}")
    private String usersExchangeName;

    @Value("${spring.application.name}")
    private String appName;

    @Bean("rabbitQueue")
    String queueName(){
        return getQueueName(appName, exchangeName, true);
    }

    @Bean("users-exchange")
    FanoutExchange usersExchange() {
        return new FanoutExchange(usersExchangeName);
    }

    @Bean
    Queue queue(@Qualifier("rabbitQueue") String queueName) {
        return new Queue(queueName, false, true, false);
    }

    @Bean("users-queue")
    Queue usersQueue(){
        return new Queue(getQueueName(appName, usersExchangeName, false), true, false, false);
    }

    @Bean
    Binding usersBinding(@Qualifier("users-queue") Queue usersQueue, @Qualifier("users-exchange") FanoutExchange usersExchange){
        return BindingBuilder.bind(usersQueue).to(usersExchange);
    }

    private String getQueueName(String appName, String exchangeName, boolean random) {
        return appName + "." + exchangeName + (random ? "." + UUID.randomUUID() : "");
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

    @Bean
    SimpleMessageListenerContainer usersContainer(ConnectionFactory connectionFactory,
                                                  @Qualifier("users-message-listener") MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(getQueueName(appName, usersExchangeName, false));
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean("users-message-listener")
    MessageListenerAdapter usersListenerAdapter(UserMessageListener receiver) {
        return new MessageListenerAdapter(receiver, "apply");
    }
}
