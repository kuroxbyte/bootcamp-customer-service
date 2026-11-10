package dev.bootcamp.customerservice.application.usecase;

import dev.bootcamp.customerservice.application.port.CustomerRepository;
import dev.bootcamp.customerservice.domain.exception.BusinessRuleException;
import dev.bootcamp.customerservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.customerservice.domain.model.customer.CustomerType;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCustomerUseCaseImplTest {

    @Mock private CustomerRepository customerRepository;

    @InjectMocks private CreateCustomerUseCaseImpl useCase;

    @Test
    void savesCustomerWithNameTypeAndProfile() {
        when(customerRepository.save(any())).thenAnswer(inv -> Single.just(inv.getArgument(0)));

        useCase.execute("Comercial Andina SAC", CustomerType.BUSINESS, CustomerProfile.PYME)
                .test()
                .assertValue(customer -> customer.getName().equals("Comercial Andina SAC")
                        && customer.getType() == CustomerType.BUSINESS
                        && customer.getProfile() == CustomerProfile.PYME);
    }

    @ParameterizedTest(name = "{0} {1}")
    @CsvSource({"PERSONAL, STANDARD", "PERSONAL, VIP", "BUSINESS, STANDARD", "BUSINESS, PYME"})
    void acceptsProfilesOfEachType(CustomerType type, CustomerProfile profile) {
        when(customerRepository.save(any())).thenAnswer(inv -> Single.just(inv.getArgument(0)));

        useCase.execute("Cliente", type, profile).test().assertComplete();
    }

    @ParameterizedTest(name = "{0} {1}")
    @CsvSource({"PERSONAL, PYME", "BUSINESS, VIP"})
    void rejectsProfileOfAnotherType(CustomerType type, CustomerProfile profile) {
        useCase.execute("Cliente", type, profile).test().assertError(BusinessRuleException.class);

        verify(customerRepository, never()).save(any());
    }
}
