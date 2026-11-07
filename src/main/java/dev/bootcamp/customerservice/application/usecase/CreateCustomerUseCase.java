package dev.bootcamp.customerservice.application.usecase;

import dev.bootcamp.customerservice.domain.model.customer.Customer;
import dev.bootcamp.customerservice.domain.model.customer.CustomerType;
import io.reactivex.rxjava3.core.Single;

/**
 * Caso de uso para registrar un cliente del banco, personal o empresarial.
 *
 * <p>Es reactivo: no hace nada hasta que se suscribe al {@link Single}.
 */
public interface CreateCustomerUseCase {

    /**
     * Crea y guarda un cliente.
     *
     * @param name nombre del cliente
     * @param type tipo de cliente
     * @return el cliente guardado, con su id asignado
     */
    Single<Customer> execute(String name, CustomerType type);
}
