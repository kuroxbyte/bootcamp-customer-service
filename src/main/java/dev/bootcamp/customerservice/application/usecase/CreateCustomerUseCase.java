package dev.bootcamp.customerservice.application.usecase;

import dev.bootcamp.customerservice.domain.exception.BusinessRuleException;
import dev.bootcamp.customerservice.domain.model.customer.Customer;
import dev.bootcamp.customerservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.customerservice.domain.model.customer.CustomerType;
import io.reactivex.rxjava3.core.Single;

/**
 * Caso de uso para registrar un cliente del banco, personal o empresarial.
 *
 * <p>El perfil es obligatorio y debe corresponder al tipo: un cliente personal es estándar o VIP;
 * uno empresarial, estándar o PYME.
 *
 * <p>Es reactivo: no hace nada hasta que se suscribe al {@link Single}. Termina con error {@link
 * BusinessRuleException} si el perfil no corresponde al tipo.
 */
public interface CreateCustomerUseCase {

    /**
     * Crea y guarda un cliente.
     *
     * @param name nombre del cliente
     * @param type tipo de cliente
     * @param profile perfil del cliente
     * @return el cliente guardado, con su id asignado
     */
    Single<Customer> execute(String name, CustomerType type, CustomerProfile profile);
}
