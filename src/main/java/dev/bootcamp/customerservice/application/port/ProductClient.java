package dev.bootcamp.customerservice.application.port;

import io.reactivex.rxjava3.core.Single;

/**
 * Consulta de los productos del cliente en account-service y credit-service.
 */
public interface ProductClient {

    /**
     * Indica si el cliente tiene alguna cuenta bancaria, crédito o tarjeta de crédito activa.
     *
     * @return true si tiene al menos uno; error si algún servicio no responde
     */
    Single<Boolean> hasActiveProducts(String customerId);
}
