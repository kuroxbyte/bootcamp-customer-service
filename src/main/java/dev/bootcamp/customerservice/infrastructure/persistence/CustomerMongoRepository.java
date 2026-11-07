package dev.bootcamp.customerservice.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CustomerMongoRepository extends ReactiveMongoRepository<CustomerDocument, String> {
}
