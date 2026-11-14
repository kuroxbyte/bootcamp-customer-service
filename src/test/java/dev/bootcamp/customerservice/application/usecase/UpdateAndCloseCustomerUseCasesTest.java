package dev.bootcamp.customerservice.application.usecase;

import dev.bootcamp.customerservice.application.port.CustomerEventPublisher;
import dev.bootcamp.customerservice.application.port.CustomerRepository;
import dev.bootcamp.customerservice.application.port.ProductClient;
import dev.bootcamp.customerservice.domain.exception.BusinessRuleException;
import dev.bootcamp.customerservice.domain.exception.ForbiddenException;
import dev.bootcamp.customerservice.domain.exception.NotFoundException;
import dev.bootcamp.customerservice.domain.model.customer.Customer;
import dev.bootcamp.customerservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.customerservice.domain.model.customer.CustomerType;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateAndCloseCustomerUseCasesTest {

    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-11-16T12:00:00Z"), ZoneOffset.UTC);

    @Mock private CustomerRepository customerRepository;

    @Mock private CustomerEventPublisher customerEventPublisher;

    @Mock private ProductClient productClient;

    private static Customer customer(String id) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName("Ana");
        customer.setType(CustomerType.PERSONAL);
        customer.setProfile(CustomerProfile.STANDARD);
        return customer;
    }

    private UpdateCustomerUseCaseImpl update() {
        return new UpdateCustomerUseCaseImpl(customerRepository, customerEventPublisher);
    }

    private CloseCustomerUseCaseImpl close() {
        return new CloseCustomerUseCaseImpl(
                customerRepository, productClient, customerEventPublisher, CLOCK);
    }

    private void savesAndPublishes() {
        when(customerRepository.save(any()))
                .thenAnswer(invocation -> Single.just(invocation.getArgument(0)));
    }

    @Test
    void customerChangesOwnNameAndPublishesTheChange() {
        when(customerRepository.findById("c1")).thenReturn(Maybe.just(customer("c1")));
        savesAndPublishes();
        when(customerEventPublisher.publishCustomerUpdated(any()))
                .thenReturn(Completable.complete());

        update().execute("c1", "c1", "Ana Torres", CustomerProfile.STANDARD).test()
                .assertValue(updated -> updated.getName().equals("Ana Torres"));
    }

    @Test
    void adminChangesTheProfile() {
        when(customerRepository.findById("c1")).thenReturn(Maybe.just(customer("c1")));
        savesAndPublishes();
        when(customerEventPublisher.publishCustomerUpdated(any()))
                .thenReturn(Completable.complete());

        update().execute("c1", null, "Ana", CustomerProfile.VIP).test()
                .assertValue(updated -> updated.getProfile() == CustomerProfile.VIP);
    }

    @Test
    void customerCannotUpdateOthersOrChangeOwnProfile() {
        when(customerRepository.findById("c1")).thenReturn(Maybe.just(customer("c1")));

        update().execute("c1", "c2", "Ana", CustomerProfile.STANDARD).test()
                .assertError(ForbiddenException.class);
        update().execute("c1", "c1", "Ana", CustomerProfile.VIP).test()
                .assertError(ForbiddenException.class);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void closedOrUnknownCustomerIsNotFound() {
        Customer closed = customer("c1");
        closed.close(LocalDate.of(2026, 1, 1));
        when(customerRepository.findById("c1")).thenReturn(Maybe.just(closed));
        when(customerRepository.findById("c9")).thenReturn(Maybe.empty());

        update().execute("c1", null, "Ana", CustomerProfile.STANDARD).test()
                .assertError(NotFoundException.class);
        close().execute("c9").test().assertError(NotFoundException.class);
        new GetCustomerUseCaseImpl(customerRepository).execute("c1").test()
                .assertError(NotFoundException.class);
    }

    @Test
    void closesCustomerWithoutProductsAndPublishesIt() {
        Customer customer = customer("c1");
        when(customerRepository.findById("c1")).thenReturn(Maybe.just(customer));
        when(productClient.hasActiveProducts("c1")).thenReturn(Single.just(false));
        savesAndPublishes();
        when(customerEventPublisher.publishCustomerClosed(customer))
                .thenReturn(Completable.complete());

        close().execute("c1").test().assertComplete();

        assertThat(customer.getClosedAt()).isEqualTo(LocalDate.of(2026, 11, 16));
    }

    @Test
    void doesNotCloseCustomerWithActiveProducts() {
        when(customerRepository.findById("c1")).thenReturn(Maybe.just(customer("c1")));
        when(productClient.hasActiveProducts("c1")).thenReturn(Single.just(true));

        close().execute("c1").test().assertError(BusinessRuleException.class);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void listsActiveCustomers() {
        when(customerRepository.findAllActive()).thenReturn(Flowable.just(customer("c1")));

        new GetCustomerUseCaseImpl(customerRepository).findAll().test().assertValueCount(1);
    }
}
