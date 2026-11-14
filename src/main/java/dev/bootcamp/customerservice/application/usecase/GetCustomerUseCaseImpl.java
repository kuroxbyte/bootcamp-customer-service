package dev.bootcamp.customerservice.application.usecase;

import dev.bootcamp.customerservice.application.port.CustomerRepository;
import dev.bootcamp.customerservice.domain.exception.NotFoundException;
import dev.bootcamp.customerservice.domain.model.customer.Customer;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

/**
 * Implementación de {@link GetCustomerUseCase}.
 */
@RequiredArgsConstructor
public class GetCustomerUseCaseImpl implements GetCustomerUseCase {
    private final CustomerRepository customerRepository;

    @Override
    public Single<Customer> execute(String customerId) {
        return findActive(customerRepository, customerId);
    }

    @Override
    public Flowable<Customer> findAll() {
        return customerRepository.findAllActive();
    }

    /**
     * Busca un cliente activo.
     *
     * @return el cliente; error {@link NotFoundException} si no existe o fue dado de baja
     */
    static Single<Customer> findActive(CustomerRepository customerRepository, String customerId) {
        return customerRepository
                .findById(customerId)
                .filter(customer -> !customer.isClosed())
                .switchIfEmpty(Single.error(() -> new NotFoundException("El cliente no existe")));
    }
}
