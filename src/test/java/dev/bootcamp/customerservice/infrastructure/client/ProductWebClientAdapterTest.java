package dev.bootcamp.customerservice.infrastructure.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ProductWebClientAdapterTest {

    private static ProductWebClientAdapter adapter(StubWebClient stub) {
        return new ProductWebClientAdapter(
                stub.builder(),
                "http://account-service",
                "http://credit-service",
                TestCircuitBreakers.remoteLookup(Duration.ofSeconds(1)));
    }

    @Test
    void customerWithoutProductsHasNone() {
        StubWebClient stub = StubWebClient.respondingByPath(Map.of(
                "/api/v1/accounts", "[]",
                "/api/v1/credits", "[]",
                "/api/v1/credit-cards", "[]"));

        adapter(stub).hasActiveProducts("c1").test().awaitDone(2, TimeUnit.SECONDS)
                .assertValue(false);
        assertThat(stub.requestCount()).isEqualTo(3);
        assertThat(stub.lastHeader("X-Customer-Id")).isEqualTo("c1");
    }

    @Test
    void stopsAtTheFirstProductFound() {
        StubWebClient stub = StubWebClient.respondingByPath(Map.of(
                "/api/v1/accounts", "[{\"id\":\"a1\"}]",
                "/api/v1/credits", "[]",
                "/api/v1/credit-cards", "[]"));

        adapter(stub).hasActiveProducts("c1").test().awaitDone(2, TimeUnit.SECONDS)
                .assertValue(true);
        assertThat(stub.requestCount()).isEqualTo(1);
    }

    @Test
    void failsAsUnavailableWhenServiceFails() {
        adapter(StubWebClient.respondingWith(HttpStatus.INTERNAL_SERVER_ERROR, "{}"))
                .hasActiveProducts("c1")
                .test()
                .awaitDone(2, TimeUnit.SECONDS)
                .assertError(ServiceUnavailableException.class);
    }
}
