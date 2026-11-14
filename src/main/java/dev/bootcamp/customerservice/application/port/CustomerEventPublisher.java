package dev.bootcamp.customerservice.application.port;

import dev.bootcamp.customerservice.domain.model.customer.Customer;
import io.reactivex.rxjava3.core.Completable;

/**
 * Publicación de eventos de clientes para otros microservicios.
 */
public interface CustomerEventPublisher {

    /**
     * Publica que se creó un cliente.
     *
     * @param customer cliente ya guardado, con su id
     * @return termina cuando el evento se publicó, o sin error si no se pudo publicar (el adaptador
     *     registra el error)
     */
    Completable publishCustomerCreated(Customer customer);

    /**
     * Publica que se modificó un cliente (nombre o perfil).
     *
     * @return termina cuando se publicó, o sin error si no se pudo (el adaptador lo registra)
     */
    Completable publishCustomerUpdated(Customer customer);

    /**
     * Publica que se dio de baja un cliente.
     *
     * @return termina cuando se publicó, o sin error si no se pudo (el adaptador lo registra)
     */
    Completable publishCustomerClosed(Customer customer);
}
