package dev.bootcamp.customerservice.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/** Repositorio de Spring Data para {@link CustomerDocument}. */
public interface CustomerMongoRepository
        extends ReactiveMongoRepository<CustomerDocument, String> {}
