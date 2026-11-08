package dev.bootcamp.customerservice.infrastructure.persistence;

import dev.bootcamp.customerservice.domain.model.customer.Customer;
import dev.bootcamp.customerservice.domain.model.customer.CustomerType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import java.util.concurrent.TimeUnit;

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

        Customer saved = adapter.save(customer).blockingGet();

        adapter.findById(saved.getId())
                .test()
                .awaitDone(5, TimeUnit.SECONDS)
                .assertValue(
                        found ->
                                found.getId().equals(saved.getId())
                                        && found.getName().equals("Comercial Andina SAC")
                                        && found.getType() == CustomerType.BUSINESS);
    }

    @Test
    void returnsEmptyWhenCustomerDoesNotExist() {
        adapter.findById("000000000000000000000000")
                .test()
                .awaitDone(5, TimeUnit.SECONDS)
                .assertComplete()
                .assertNoValues();
    }
}
