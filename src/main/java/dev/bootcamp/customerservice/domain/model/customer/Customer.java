package dev.bootcamp.customerservice.domain.model.customer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Customer {
    private Long id;
    private String name;
    private CustomerType type;
}
