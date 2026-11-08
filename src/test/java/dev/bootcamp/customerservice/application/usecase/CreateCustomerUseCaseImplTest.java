package dev.bootcamp.customerservice.application.usecase;

import dev.bootcamp.customerservice.application.port.CustomerRepository;
import dev.bootcamp.customerservice.domain.model.customer.CustomerType;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCustomerUseCaseImplTest {

    @Mock private CustomerRepository customerRepository;

    @InjectMocks private CreateCustomerUseCaseImpl useCase;

    @Test
    void savesCustomerWithNameAndType() {
        when(customerRepository.save(any())).thenAnswer(inv -> Single.just(inv.getArgument(0)));

        useCase.execute("Comercial Andina SAC", CustomerType.BUSINESS)
                .test()
                .assertValue(
                        customer ->
                                customer.getName().equals("Comercial Andina SAC")
                                        && customer.getType() == CustomerType.BUSINESS);
    }
}
