package dev.bootcamp.customerservice.application.usecase;

import dev.bootcamp.customerservice.domain.exception.BusinessRuleException;
import dev.bootcamp.customerservice.domain.exception.ForbiddenException;
import dev.bootcamp.customerservice.domain.exception.NotFoundException;
import dev.bootcamp.customerservice.domain.model.customer.Customer;
import dev.bootcamp.customerservice.domain.model.customer.CustomerProfile;
import io.reactivex.rxjava3.core.Single;

/**
 * Caso de uso para modificar el nombre y el perfil de un cliente.
 *
 * <ul>
 *   <li>Un empleado ({@code ADMIN}) modifica a cualquier cliente.
 *   <li>Un cliente solo se modifica a sí mismo y solo su nombre: cambiar el perfil (VIP, PYME)
 *       cambia las condiciones de sus productos y lo decide el banco.
 *   <li>El tipo (personal o empresarial) no cambia.
 * </ul>
 *
 * <p>Publica {@code CustomerUpdated}. Es reactivo: no hace nada hasta que se suscribe.
 */
public interface UpdateCustomerUseCase {

    /**
     * Modifica el cliente.
     *
     * @param customerId          cliente a modificar
     * @param requesterCustomerId cliente que hace la petición; null si la hace un empleado
     * @param name                nuevo nombre
     * @param profile             nuevo perfil
     * @return el cliente modificado; error {@link NotFoundException} si no existe o fue dado de
     *     baja, {@link ForbiddenException} si el cliente no puede hacer el cambio o
     *     {@link BusinessRuleException} si el perfil no corresponde al tipo
     */
    Single<Customer> execute(
            String customerId,
            String requesterCustomerId,
            String name,
            CustomerProfile profile);
}
