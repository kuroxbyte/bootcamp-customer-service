package dev.bootcamp.customerservice.infrastructure.messaging;

import dev.bootcamp.customerservice.application.port.CustomerEventPublisher;
import dev.bootcamp.customerservice.domain.model.customer.Customer;
import io.reactivex.rxjava3.core.Completable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;

/**
 * Implementa el puerto {@link CustomerEventPublisher} publicando en Kafka.
 *
 * <p>El evento se publica después de guardar el cliente. Si Kafka no está disponible, el error se
 * registra y la creación del cliente no falla: el evento se pierde (limitación conocida; evitarla
 * requiere el patrón outbox).
 */
@Slf4j
@Component
public class CustomerKafkaEventPublisher implements CustomerEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;
    private final Clock clock;
    private final String topic;

    /**
     * Crea el publicador con el tópico configurado.
     */
    public CustomerKafkaEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            JsonMapper jsonMapper,
            Clock clock,
            @Value("${messaging.topics.customer-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
        this.clock = clock;
        this.topic = topic;
    }

    @Override
    public Completable publishCustomerCreated(Customer customer) {
        return Completable.defer(() -> {
            CustomerCreatedEvent event = new CustomerCreatedEvent(
                    CustomerCreatedEvent.EVENT_TYPE,
                    customer.getId(),
                    customer.getType().name(),
                    customer.getProfile().name(),
                    Instant.now(clock));
            return Completable.fromCompletionStage(kafkaTemplate.send(
                    topic,
                    customer.getId(),
                    jsonMapper.writeValueAsString(event)));
        })
                .doOnComplete(() -> log.info(
                        "CustomerCreated published customerId={}",
                        customer.getId()))
                .onErrorComplete(error -> {
                    log.error(
                            "CustomerCreated could not be published customerId={}",
                            customer.getId(),
                            error);
                    return true;
                });
    }
}
