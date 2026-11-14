package dev.bootcamp.customerservice.infrastructure.messaging;

import dev.bootcamp.customerservice.application.port.CustomerEventPublisher;
import dev.bootcamp.customerservice.domain.model.customer.Customer;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
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
 * <p>Publica {@code CustomerCreated}, {@code CustomerUpdated} y {@code CustomerClosed} después de
 * guardar el cliente. Si Kafka no está disponible (se espera como máximo {@code max.block.ms}, 1 s
 * en config-repo), el error se registra y la operación no falla: el evento se pierde (limitación
 * conocida; evitarla requiere el patrón outbox).
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
        return publish(CustomerEvent.CUSTOMER_CREATED, customer);
    }

    @Override
    public Completable publishCustomerUpdated(Customer customer) {
        return publish(CustomerEvent.CUSTOMER_UPDATED, customer);
    }

    @Override
    public Completable publishCustomerClosed(Customer customer) {
        return publish(CustomerEvent.CUSTOMER_CLOSED, customer);
    }

    private Completable publish(String eventType, Customer customer) {
        return Completable.defer(() -> {
            CustomerEvent event = new CustomerEvent(
                    eventType,
                    customer.getId(),
                    customer.getType().name(),
                    customer.getProfile().name(),
                    Instant.now(clock));
            return Completable.fromCompletionStage(kafkaTemplate.send(
                    topic,
                    customer.getId(),
                    jsonMapper.writeValueAsString(event)));
        })
                // send() puede bloquear mientras busca al broker (hasta max.block.ms): fuera del
                // hilo de eventos de WebFlux.
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> log.info(
                        "{} published customerId={}",
                        eventType,
                        customer.getId()))
                .onErrorComplete(error -> {
                    log.error(
                            "{} could not be published customerId={}",
                            eventType,
                            customer.getId(),
                            error);
                    return true;
                });
    }
}
