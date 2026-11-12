package dev.bootcamp.customerservice.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.time.Clock;

/**
 * Configuración de la mensajería: customer-service es dueño del tópico de eventos de clientes.
 */
@Configuration
public class MessagingConfig {

    /**
     * Tópico de eventos de clientes; la clave de cada mensaje es el id del cliente.
     */
    @Bean
    public NewTopic customerEventsTopic(
            @Value("${messaging.topics.customer-events}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }

    /**
     * Reloj del sistema; con él se fecha cada evento.
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
