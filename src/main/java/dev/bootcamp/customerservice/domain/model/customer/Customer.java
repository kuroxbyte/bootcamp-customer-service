package dev.bootcamp.customerservice.domain.model.customer;

import dev.bootcamp.customerservice.domain.exception.BusinessRuleException;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Cliente del banco: personal (estándar o VIP) o empresarial (estándar o PYME).
 *
 * <p>Un cliente dado de baja conserva sus datos con la fecha de baja ({@code closedAt}); para el
 * resto del sistema ya no existe.
 */
@Getter
@Setter
public class Customer {
    private String id;
    private String name;
    private CustomerType type;
    private CustomerProfile profile;

    /**
     * Fecha de baja; null mientras el cliente está activo.
     */
    private LocalDate closedAt;

    /**
     * Indica si el cliente fue dado de baja.
     */
    public boolean isClosed() {
        return closedAt != null;
    }

    /**
     * Cambia el nombre y el perfil del cliente. El tipo no cambia: los productos ya contratados
     * dependen de él.
     *
     * @throws BusinessRuleException si el nombre está vacío o el perfil no corresponde al tipo
     */
    public void update(String newName, CustomerProfile newProfile) {
        if (newName == null || newName.isBlank()) {
            throw new BusinessRuleException("El nombre del cliente no puede estar vacío");
        }
        if (!newProfile.allows(type)) {
            throw new BusinessRuleException(
                    "El perfil " + newProfile + " no corresponde a un cliente " + type);
        }
        this.name = newName;
        this.profile = newProfile;
    }

    /**
     * Da de baja al cliente.
     *
     * @param today fecha de baja
     */
    public void close(LocalDate today) {
        this.closedAt = today;
    }
}
