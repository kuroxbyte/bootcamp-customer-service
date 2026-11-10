package dev.bootcamp.customerservice.domain.model.customer;

import lombok.Getter;
import lombok.Setter;

/**
 * Cliente del banco: personal (estándar o VIP) o empresarial (estándar o PYME).
 */
@Getter
@Setter
public class Customer {
    private String id;
    private String name;
    private CustomerType type;
    private CustomerProfile profile;
}
