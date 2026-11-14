package dev.bootcamp.customerservice.infrastructure.web.controller;

import dev.bootcamp.customerservice.application.usecase.CloseCustomerUseCase;
import dev.bootcamp.customerservice.application.usecase.CreateCustomerUseCase;
import dev.bootcamp.customerservice.application.usecase.GetCustomerUseCase;
import dev.bootcamp.customerservice.application.usecase.UpdateCustomerUseCase;
import dev.bootcamp.customerservice.domain.exception.BusinessRuleException;
import dev.bootcamp.customerservice.domain.exception.ForbiddenException;
import dev.bootcamp.customerservice.domain.exception.NotFoundException;
import dev.bootcamp.customerservice.domain.model.customer.Customer;
import dev.bootcamp.customerservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.customerservice.domain.model.customer.CustomerType;
import dev.bootcamp.customerservice.infrastructure.client.ServiceUnavailableException;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired private WebTestClient webTestClient;

    @MockitoBean private CreateCustomerUseCase createCustomerUseCase;

    @MockitoBean private GetCustomerUseCase getCustomerUseCase;

    @MockitoBean private UpdateCustomerUseCase updateCustomerUseCase;

    @MockitoBean private CloseCustomerUseCase closeCustomerUseCase;

    private static Customer customer(String id, String name, CustomerType type) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(name);
        customer.setType(type);
        customer.setProfile(
                type == CustomerType.PERSONAL ? CustomerProfile.VIP : CustomerProfile.PYME);
        return customer;
    }

    @Test
    void createsCustomer() {
        when(createCustomerUseCase.execute(
                        "Ana Torres",
                        CustomerType.PERSONAL,
                        CustomerProfile.VIP))
                .thenReturn(Single.just(customer("1", "Ana Torres", CustomerType.PERSONAL)));

        webTestClient
                .post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Ana Torres\",\"type\":\"PERSONAL\",\"profile\":\"VIP\"}")
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody()
                .jsonPath("$.id")
                .isEqualTo("1")
                .jsonPath("$.name")
                .isEqualTo("Ana Torres")
                .jsonPath("$.type")
                .isEqualTo("PERSONAL");
    }

    @Test
    void rejectsInvalidCustomerRequest() {
        webTestClient
                .post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"\"}")
                .exchange()
                .expectStatus()
                .isBadRequest();

        verify(createCustomerUseCase, never()).execute(any(), any(), any());
    }

    @Test
    void returnsCustomerById() {
        when(getCustomerUseCase.execute("1"))
                .thenReturn(
                        Single.just(customer("1", "Comercial Andina SAC", CustomerType.BUSINESS)));

        webTestClient
                .get()
                .uri("/api/v1/customers/1")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("BUSINESS");
    }

    @Test
    void returnsNotFoundProblemWhenCustomerDoesNotExist() {
        when(getCustomerUseCase.execute("99"))
                .thenReturn(Single.error(new NotFoundException("El cliente no existe")));

        webTestClient
                .get()
                .uri("/api/v1/customers/99")
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectHeader()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo(404)
                .jsonPath("$.detail")
                .isEqualTo("El cliente no existe");
    }

    @Test
    void requiresProfile() {
        webTestClient.post().uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Ana Torres\",\"type\":\"PERSONAL\"}")
                .exchange()
                .expectStatus().isBadRequest();

        verify(createCustomerUseCase, never()).execute(any(), any(), any());
    }

    @Test
    void returnsUnprocessableWhenProfileDoesNotMatchType() {
        when(
                createCustomerUseCase.execute(
                        "Ana Torres",
                        CustomerType.PERSONAL,
                        CustomerProfile.PYME))
                .thenReturn(Single.error(new BusinessRuleException(
                        "El perfil PYME no corresponde a un cliente PERSONAL")));

        webTestClient.post().uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Ana Torres\",\"type\":\"PERSONAL\",\"profile\":\"PYME\"}")
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void returnsProfileOfCustomer() {
        when(getCustomerUseCase.execute("1"))
                .thenReturn(Single.just(customer("1", "Ana Torres", CustomerType.PERSONAL)));

        webTestClient.get().uri("/api/v1/customers/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.profile").isEqualTo("VIP");
    }

    @Test
    void listsActiveCustomersForAdmin() {
        when(getCustomerUseCase.findAll()).thenReturn(Flowable.just(
                customer("1", "Ana Torres", CustomerType.PERSONAL),
                customer("2", "Andina SAC", CustomerType.BUSINESS)));

        webTestClient.get().uri("/api/v1/customers").header("X-Admin-Id", "admin")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[1].name").isEqualTo("Andina SAC");
        webTestClient.get().uri("/api/v1/customers").exchange().expectStatus().isBadRequest();
    }

    @Test
    void updatesCustomerAsCustomerOrAsAdmin() {
        Customer updated = customer("1", "Ana María Torres", CustomerType.PERSONAL);
        when(updateCustomerUseCase.execute("1", "1", "Ana María Torres", CustomerProfile.VIP))
                .thenReturn(Single.just(updated));
        when(updateCustomerUseCase.execute("1", null, "Ana María Torres", CustomerProfile.VIP))
                .thenReturn(Single.just(updated));

        for (String header : new String[] {"X-Customer-Id", "X-Admin-Id"}) {
            webTestClient.put().uri("/api/v1/customers/1")
                    .header(header, header.equals("X-Customer-Id") ? "1" : "admin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(UPDATE)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody().jsonPath("$.name").isEqualTo("Ana María Torres");
        }
    }

    @Test
    void updateWithoutIdentityOrForbiddenChangeIsForbidden() {
        when(updateCustomerUseCase.execute("1", "2", "Ana María Torres", CustomerProfile.VIP))
                .thenReturn(Single.error(new ForbiddenException("Solo sus propios datos")));

        webTestClient.put().uri("/api/v1/customers/1")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(UPDATE)
                .exchange().expectStatus().isForbidden();
        webTestClient.put().uri("/api/v1/customers/1").header("X-Customer-Id", "2")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(UPDATE)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody().jsonPath("$.detail").isEqualTo("Solo sus propios datos");
    }

    @Test
    void deletesCustomerOrExplainsWhyNot() {
        when(closeCustomerUseCase.execute("1")).thenReturn(Completable.complete());
        when(closeCustomerUseCase.execute("2"))
                .thenReturn(Completable.error(new BusinessRuleException("Tiene productos")));
        when(closeCustomerUseCase.execute("3")).thenReturn(Completable.error(
                new ServiceUnavailableException("account-service no está disponible", null)));

        delete("1").expectStatus().isNoContent();
        delete("2").expectStatus().isEqualTo(422);
        delete("3").expectStatus().isEqualTo(503);
    }

    private WebTestClient.ResponseSpec delete(String customerId) {
        return webTestClient.delete().uri("/api/v1/customers/" + customerId)
                .header("X-Admin-Id", "admin")
                .exchange();
    }

    private static final String UPDATE = "{\"name\":\"Ana María Torres\",\"profile\":\"VIP\"}";
}
