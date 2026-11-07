package dev.bootcamp.customerservice.infrastructure.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.bootcamp.customerservice.application.usecase.CreateCustomerUseCase;
import dev.bootcamp.customerservice.application.usecase.GetCustomerUseCase;
import dev.bootcamp.customerservice.domain.exception.NotFoundException;
import dev.bootcamp.customerservice.domain.model.customer.Customer;
import dev.bootcamp.customerservice.domain.model.customer.CustomerType;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CreateCustomerUseCase createCustomerUseCase;

    @MockitoBean
    private GetCustomerUseCase getCustomerUseCase;

    private static Customer customer(String id, String name, CustomerType type) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(name);
        customer.setType(type);
        return customer;
    }

    @Test
    void createsCustomer() {
        when(createCustomerUseCase.execute("Ana Torres", CustomerType.PERSONAL))
                .thenReturn(Single.just(customer("1", "Ana Torres", CustomerType.PERSONAL)));

        webTestClient.post().uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Ana Torres\",\"type\":\"PERSONAL\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("1")
                .jsonPath("$.name").isEqualTo("Ana Torres")
                .jsonPath("$.type").isEqualTo("PERSONAL");
    }

    @Test
    void rejectsInvalidCustomerRequest() {
        webTestClient.post().uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"\"}")
                .exchange()
                .expectStatus().isBadRequest();

        verify(createCustomerUseCase, never()).execute(any(), any());
    }

    @Test
    void returnsCustomerById() {
        when(getCustomerUseCase.execute("1"))
                .thenReturn(Single.just(customer("1", "Comercial Andina SAC", CustomerType.BUSINESS)));

        webTestClient.get().uri("/api/v1/customers/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.type").isEqualTo("BUSINESS");
    }

    @Test
    void returnsNotFoundProblemWhenCustomerDoesNotExist() {
        when(getCustomerUseCase.execute("99")).thenReturn(Single.error(new NotFoundException("El cliente no existe")));

        webTestClient.get().uri("/api/v1/customers/99")
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.detail").isEqualTo("El cliente no existe");
    }
}
