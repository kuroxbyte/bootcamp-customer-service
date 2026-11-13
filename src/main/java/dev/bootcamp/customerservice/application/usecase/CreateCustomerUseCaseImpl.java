package dev.bootcamp.customerservice.application.usecase;

import dev.bootcamp.customerservice.application.port.CustomerEventPublisher;
import dev.bootcamp.customerservice.application.port.CustomerRepository;
import dev.bootcamp.customerservice.domain.exception.BusinessRuleException;
import dev.bootcamp.customerservice.domain.model.customer.Customer;
import dev.bootcamp.customerservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.customerservice.domain.model.customer.CustomerType;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

/**
 * Implementación de {@link CreateCustomerUseCase}.
 */
@RequiredArgsConstructor
public class CreateCustomerUseCaseImpl implements CreateCustomerUseCase {
    private final CustomerRepository customerRepository;
    private final CustomerEventPublisher customerEventPublisher;

    @Override
    public Single<Customer> execute(String name, CustomerType type, CustomerProfile profile) {
        return Single.fromCallable(() -> {
            if (!profile.allows(type)) {
                throw new BusinessRuleException(
                        "El perfil " + profile + " no corresponde a un cliente " + type);
            }
            Customer customer = new Customer();
            customer.setName(name);
            customer.setType(type);
            customer.setProfile(profile);
            return customer;
        })
                .flatMap(customerRepository::save)
                .flatMap(saved -> customerEventPublisher
                        .publishCustomerCreated(saved)
                        .andThen(Single.just(saved)));
    }
}
