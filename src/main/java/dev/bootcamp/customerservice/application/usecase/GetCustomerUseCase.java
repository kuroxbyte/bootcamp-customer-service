package dev.bootcamp.customerservice.application.usecase;

import dev.bootcamp.customerservice.domain.exception.NotFoundException;
import dev.bootcamp.customerservice.domain.model.customer.Customer;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Caso de uso para consultar un cliente por su id.
 *
 * <p>Es reactivo: no hace nada hasta que se suscribe al {@link Single}. Termina con error {@link
 * NotFoundException} si el cliente no existe o fue dado de baja.
 */
public interface GetCustomerUseCase {

    /**
     * Consulta un cliente por su id.
     *
     * @param customerId id del cliente
     * @return el cliente; error {@link NotFoundException} si no existe
     */
    Single<Customer> execute(String customerId);

    /**
     * Lista los clientes activos del banco (los dados de baja no aparecen).
     *
     * @return los clientes; ninguno si el banco no tiene clientes
     */
    Flowable<Customer> findAll();
}
