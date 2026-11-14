package dev.bootcamp.customerservice.application.usecase;

import dev.bootcamp.customerservice.application.port.CustomerEventPublisher;
import dev.bootcamp.customerservice.application.port.CustomerRepository;
import dev.bootcamp.customerservice.application.port.ProductClient;
import dev.bootcamp.customerservice.domain.exception.BusinessRuleException;
import dev.bootcamp.customerservice.domain.model.customer.Customer;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Implementación de {@link CloseCustomerUseCase}.
 */
@RequiredArgsConstructor
public class CloseCustomerUseCaseImpl implements CloseCustomerUseCase {
    private final CustomerRepository customerRepository;
    private final ProductClient productClient;
    private final CustomerEventPublisher customerEventPublisher;
    private final Clock clock;

    @Override
    public Completable execute(String customerId) {
        return GetCustomerUseCaseImpl.findActive(customerRepository, customerId)
                .flatMap(customer -> productClient.hasActiveProducts(customerId)
                        .flatMap(hasProducts -> hasProducts
                                ? Single.<Customer>error(new BusinessRuleException(
                                        "El cliente tiene productos activos; primero deben darse"
                                                + " de baja"))
                                : Single.just(customer)))
                .flatMap(customer -> {
                    customer.close(LocalDate.now(clock));
                    return customerRepository.save(customer);
                })
                .flatMapCompletable(customerEventPublisher::publishCustomerClosed);
    }
}
