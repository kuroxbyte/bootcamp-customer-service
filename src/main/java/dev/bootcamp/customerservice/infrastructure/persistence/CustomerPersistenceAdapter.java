package dev.bootcamp.customerservice.infrastructure.persistence;

import dev.bootcamp.customerservice.application.port.CustomerRepository;
import dev.bootcamp.customerservice.domain.model.customer.Customer;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Implementa el puerto {@link CustomerRepository} con MongoDB reactivo. */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerPersistenceAdapter implements CustomerRepository {

    private final CustomerMongoRepository mongoRepository;

    @Override
    public Single<Customer> save(Customer customer) {
        return Single.fromPublisher(mongoRepository.save(CustomerPersistenceMapper.toDocument(customer)))
                .doOnSuccess(saved -> log.debug("Customer saved customerId={}", saved.getId()))
                .map(CustomerPersistenceMapper::toDomain);
    }

    @Override
    public Maybe<Customer> findById(String customerId) {
        return Maybe.fromPublisher(mongoRepository.findById(customerId))
                .map(CustomerPersistenceMapper::toDomain);
    }
}
