package dev.bootcamp.customerservice.domain.model.customer;

import java.util.EnumSet;
import java.util.Set;

/**
 * Perfil del cliente dentro de su tipo.
 */
public enum CustomerProfile {
    /**
     * Perfil estándar, personal o empresarial.
     */
    STANDARD(EnumSet.of(CustomerType.PERSONAL, CustomerType.BUSINESS)),
    /**
     * Cliente personal VIP.
     */
    VIP(EnumSet.of(CustomerType.PERSONAL)),
    /**
     * Cliente empresarial PYME.
     */
    PYME(EnumSet.of(CustomerType.BUSINESS));

    private final Set<CustomerType> allowedTypes;

    CustomerProfile(Set<CustomerType> allowedTypes) {
        this.allowedTypes = allowedTypes;
    }

    /**
     * Indica si este perfil corresponde al tipo de cliente indicado.
     */
    public boolean allows(CustomerType type) {
        return allowedTypes.contains(type);
    }
}
