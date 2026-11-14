package dev.bootcamp.customerservice.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

/**
 * Repositorio de Spring Data para {@link CustomerDocument}.
 */
public interface CustomerMongoRepository
        extends ReactiveMongoRepository<CustomerDocument, String> {

    /**
     * Clientes sin fecha de baja.
     */
    Flux<CustomerDocument> findByClosedAtIsNull();
}
