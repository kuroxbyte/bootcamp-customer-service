package dev.bootcamp.customerservice.application.usecase;

import dev.bootcamp.customerservice.application.port.CustomerRepository;
import dev.bootcamp.customerservice.domain.model.customer.Customer;
import dev.bootcamp.customerservice.domain.model.customer.CustomerType;
import io.reactivex.rxjava3.core.Single;

/** Implementación de {@link CreateCustomerUseCase}. */
public class CreateCustomerUseCaseImpl implements CreateCustomerUseCase {
    private final CustomerRepository customerRepository;

    /** Crea el caso de uso con los adaptadores de sus puertos. */
    public CreateCustomerUseCaseImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Single<Customer> execute(String name, CustomerType type) {
        return Single.fromCallable(
                        () -> {
                            Customer customer = new Customer();
                            customer.setName(name);
                            customer.setType(type);
                            return customer;
                        })
                .flatMap(customerRepository::save);
    }
}
