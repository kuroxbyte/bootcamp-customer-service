package dev.bootcamp.customerservice.application.port;

import dev.bootcamp.customerservice.domain.model.customer.Customer;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * Puerto de persistencia de los clientes.
 */
public interface CustomerRepository {

    /**
     * Guarda el cliente y lo emite con su id asignado.
     */
    Single<Customer> save(Customer customer);

    /**
     * Busca el recurso por su id.
     *
     * @return el cliente, o vacío si no existe
     */
    Maybe<Customer> findById(String customerId);
}
