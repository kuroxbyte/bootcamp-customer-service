package dev.bootcamp.customerservice.application.usecase;

import dev.bootcamp.customerservice.domain.exception.BusinessRuleException;
import dev.bootcamp.customerservice.domain.exception.NotFoundException;
import io.reactivex.rxjava3.core.Completable;

/**
 * Caso de uso para dar de baja a un cliente (baja lógica).
 *
 * <p>Solo se da de baja a un cliente sin cuentas bancarias, créditos ni tarjetas de crédito
 * activos. El cliente queda con su fecha de baja: deja de listarse, no puede adquirir productos y
 * {@code CustomerClosed} hace que auth-service elimine su usuario. Es reactivo: no hace nada hasta
 * que se suscribe.
 */
public interface CloseCustomerUseCase {

    /**
     * Da de baja al cliente.
     *
     * @return termina sin error si se dio de baja; error {@link NotFoundException} si no existe o
     *     ya fue dado de baja, o {@link BusinessRuleException} si tiene productos activos
     */
    Completable execute(String customerId);
}
