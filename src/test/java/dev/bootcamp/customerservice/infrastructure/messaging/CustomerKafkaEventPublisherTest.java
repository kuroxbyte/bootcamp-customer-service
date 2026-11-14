package dev.bootcamp.customerservice.infrastructure.messaging;

import dev.bootcamp.customerservice.domain.model.customer.Customer;
import dev.bootcamp.customerservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.customerservice.domain.model.customer.CustomerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerKafkaEventPublisherTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Mock private KafkaTemplate<String, String> kafkaTemplate;

    private CustomerKafkaEventPublisher publisher;

    private Customer customer;

    @BeforeEach
    void createPublisher() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-15T10:00:00Z"), ZoneOffset.UTC);
        publisher = new CustomerKafkaEventPublisher(
                kafkaTemplate,
                jsonMapper,
                clock,
                "customer-events");
        customer = new Customer();
        customer.setId("c1");
        customer.setName("Lucía Paredes");
        customer.setType(CustomerType.PERSONAL);
        customer.setProfile(CustomerProfile.VIP);
    }

    @Test
    void publishesCustomerCreatedWithCustomerIdAsKey() {
        when(kafkaTemplate.send(eq("customer-events"), eq("c1"), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        publisher.publishCustomerCreated(customer).test().awaitDone(
                2,
                TimeUnit.SECONDS).assertComplete();

        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("customer-events"), eq("c1"), json.capture());
        JsonNode event = jsonMapper.readTree(json.getValue());
        assertThat(event.get("eventType").asString()).isEqualTo("CustomerCreated");
        assertThat(event.get("customerId").asString()).isEqualTo("c1");
        assertThat(event.get("type").asString()).isEqualTo("PERSONAL");
        assertThat(event.get("profile").asString()).isEqualTo("VIP");
        assertThat(event.get("occurredAt").asString()).isEqualTo("2026-11-15T10:00:00Z");
    }

    @Test
    void doesNotFailWhenKafkaIsUnavailable() {
        when(kafkaTemplate.send(eq("customer-events"), eq("c1"), anyString()))
                .thenReturn(
                        CompletableFuture.failedFuture(new IllegalStateException("broker down")));

        publisher.publishCustomerCreated(customer).test().awaitDone(
                2,
                TimeUnit.SECONDS).assertComplete();
    }

    @Test
    void publishesCustomerUpdatedAndCustomerClosed() {
        when(kafkaTemplate.send(eq("customer-events"), eq("c1"), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        publisher.publishCustomerUpdated(customer).test().awaitDone(2, TimeUnit.SECONDS)
                .assertComplete();
        publisher.publishCustomerClosed(customer).test().awaitDone(2, TimeUnit.SECONDS)
                .assertComplete();

        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate, times(2)).send(eq("customer-events"), eq("c1"), json.capture());
        assertThat(json.getAllValues())
                .extracting(value -> jsonMapper.readTree(value).get("eventType").asString())
                .containsExactly("CustomerUpdated", "CustomerClosed");
    }
}
