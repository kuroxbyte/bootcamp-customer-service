package dev.bootcamp.customerservice.infrastructure.persistence;

import dev.bootcamp.customerservice.domain.model.customer.Customer;
import dev.bootcamp.customerservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.customerservice.domain.model.customer.CustomerType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataMongoTest
@Import(CustomerPersistenceAdapter.class)
class CustomerPersistenceAdapterTest {

    @Container @ServiceConnection static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @Autowired private CustomerPersistenceAdapter adapter;

    @Test
    void savesAndFindsCustomer() {
        Customer customer = new Customer();
        customer.setName("Comercial Andina SAC");
        customer.setType(CustomerType.BUSINESS);
        customer.setProfile(CustomerProfile.PYME);

        Customer saved = adapter.save(customer).blockingGet();

        adapter.findById(saved.getId())
                .test()
                .awaitDone(5, TimeUnit.SECONDS)
                .assertValue(
                        found ->
                                found.getId().equals(saved.getId())
                                        && found.getName().equals("Comercial Andina SAC")
                                        && found.getType() == CustomerType.BUSINESS
                                        && found.getProfile() == CustomerProfile.PYME);
    }

    @Test
    void returnsEmptyWhenCustomerDoesNotExist() {
        adapter.findById("000000000000000000000000")
                .test()
                .awaitDone(5, TimeUnit.SECONDS)
                .assertComplete()
                .assertNoValues();
    }

    @Test
    void listsOnlyActiveCustomersAndKeepsTheClosingDate() {
        Customer active = adapter.save(customer("Ana")).blockingGet();
        Customer closed = customer("Luis");
        closed.close(LocalDate.of(2026, 11, 16));
        Customer savedClosed = adapter.save(closed).blockingGet();

        assertThat(adapter.findAllActive().map(Customer::getId).toList().blockingGet())
                .contains(active.getId())
                .doesNotContain(savedClosed.getId());
        assertThat(adapter.findById(savedClosed.getId()).blockingGet().getClosedAt())
                .isEqualTo(LocalDate.of(2026, 11, 16));
    }

    private static Customer customer(String name) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setType(CustomerType.PERSONAL);
        customer.setProfile(CustomerProfile.STANDARD);
        return customer;
    }
}
