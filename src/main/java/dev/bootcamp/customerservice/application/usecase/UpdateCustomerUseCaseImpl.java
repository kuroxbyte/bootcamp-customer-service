package dev.bootcamp.customerservice.application.usecase;

import dev.bootcamp.customerservice.application.port.CustomerEventPublisher;
import dev.bootcamp.customerservice.application.port.CustomerRepository;
import dev.bootcamp.customerservice.domain.exception.ForbiddenException;
import dev.bootcamp.customerservice.domain.model.customer.Customer;
import dev.bootcamp.customerservice.domain.model.customer.CustomerProfile;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

/**
 * Implementación de {@link UpdateCustomerUseCase}.
 */
@RequiredArgsConstructor
public class UpdateCustomerUseCaseImpl implements UpdateCustomerUseCase {
    private final CustomerRepository customerRepository;
    private final CustomerEventPublisher customerEventPublisher;

    @Override
    public Single<Customer> execute(
            String customerId,
            String requesterCustomerId,
            String name,
            CustomerProfile profile) {
        boolean byCustomer = requesterCustomerId != null;
        if (byCustomer && !requesterCustomerId.equals(customerId)) {
            return Single.error(new ForbiddenException("Solo puede modificar sus propios datos"));
        }
        return GetCustomerUseCaseImpl.findActive(customerRepository, customerId)
                .map(customer -> {
                    if (byCustomer && customer.getProfile() != profile) {
                        throw new ForbiddenException(
                                "Solo un empleado del banco puede cambiar el perfil del cliente");
                    }
                    customer.update(name, profile);
                    return customer;
                })
                .flatMap(customerRepository::save)
                .flatMap(saved -> customerEventPublisher.publishCustomerUpdated(saved)
                        .andThen(Single.just(saved)));
    }
}
