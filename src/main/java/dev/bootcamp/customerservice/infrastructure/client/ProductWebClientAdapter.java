package dev.bootcamp.customerservice.infrastructure.client;

import dev.bootcamp.customerservice.application.port.ProductClient;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * Implementa el puerto {@link ProductClient} con los listados de productos del cliente de
 * account-service ({@code /api/v1/accounts}) y credit-service ({@code /api/v1/credits} y
 * {@code /api/v1/credit-cards}), que solo devuelven productos activos.
 *
 * <p>Cada llamada pasa por el circuit breaker del servicio consultado. Las tarjetas de débito no
 * se consultan: necesitan una cuenta activa.
 */
@Component
public class ProductWebClientAdapter implements ProductClient {

    private static final String CUSTOMER_ID_HEADER = "X-Customer-Id";

    private final List<ProductList> productLists;
    private final RemoteLookup remoteLookup;

    /**
     * Crea el cliente con las URL base configuradas.
     */
    public ProductWebClientAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${clients.account-service.base-url}") String accountBaseUrl,
            @Value("${clients.credit-service.base-url}") String creditBaseUrl,
            RemoteLookup remoteLookup) {
        WebClient accounts = webClientBuilder.clone().baseUrl(accountBaseUrl).build();
        WebClient credits = webClientBuilder.clone().baseUrl(creditBaseUrl).build();
        this.productLists = List.of(
                new ProductList("account-service", accounts, "/api/v1/accounts"),
                new ProductList("credit-service", credits, "/api/v1/credits"),
                new ProductList("credit-service", credits, "/api/v1/credit-cards"));
        this.remoteLookup = remoteLookup;
    }

    /**
     * Consulta los listados en orden y se detiene en el primer producto que encuentra.
     */
    @Override
    public Single<Boolean> hasActiveProducts(String customerId) {
        return Flowable.fromIterable(productLists)
                .concatMap(list -> remoteLookup.findAll(
                        list.webClient().get()
                                .uri(list.path())
                                .header(CUSTOMER_ID_HEADER, customerId)
                                .retrieve()
                                .bodyToFlux(Object.class),
                        list.serviceName()))
                .any(product -> true);
    }

    /**
     * Listado de productos de un cliente en otro microservicio.
     */
    private record ProductList(String serviceName, WebClient webClient, String path) {}
}
