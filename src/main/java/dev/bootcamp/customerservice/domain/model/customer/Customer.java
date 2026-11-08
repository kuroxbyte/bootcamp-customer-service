package dev.bootcamp.customerservice.domain.model.customer;

import lombok.Getter;
import lombok.Setter;

/**
 * Cliente del banco, personal o empresarial.
 */
@Getter
@Setter
public class Customer {
    private String id;
    private String name;
    private CustomerType type;
}
