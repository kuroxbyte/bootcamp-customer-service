package dev.bootcamp.customerservice.application.usecase;

import static org.mockito.Mockito.when;

import dev.bootcamp.customerservice.application.port.CustomerRepository;
import dev.bootcamp.customerservice.domain.exception.NotFoundException;
import dev.bootcamp.customerservice.domain.model.customer.Customer;
import io.reactivex.rxjava3.core.Maybe;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetCustomerUseCaseImplTest {

    @Mock private CustomerRepository customerRepository;

    @InjectMocks private GetCustomerUseCaseImpl useCase;

    @Test
    void returnsExistingCustomer() {
        Customer customer = new Customer();
        customer.setId("1");
        when(customerRepository.findById("1")).thenReturn(Maybe.just(customer));

        useCase.execute("1").test().assertValue(customer);
    }

    @Test
    void failsWhenCustomerDoesNotExist() {
        when(customerRepository.findById("99")).thenReturn(Maybe.empty());

        useCase.execute("99").test().assertError(NotFoundException.class);
    }
}
