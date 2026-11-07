package dev.bootcamp.customerservice.infrastructure.web.mapper;

import dev.bootcamp.customerservice.domain.model.customer.Customer;
import dev.bootcamp.customerservice.domain.model.customer.CustomerType;
import dev.bootcamp.customerservice.infrastructure.web.dto.CustomerResponseDto;
import dev.bootcamp.customerservice.infrastructure.web.dto.CustomerTypeDto;

/** Conversión entre los DTOs del contrato OpenAPI y el dominio. */
public final class CustomerWebMapper {

    private CustomerWebMapper() {
    }

    public static CustomerType toDomain(CustomerTypeDto type) {
        return CustomerType.valueOf(type.name());
    }

    public static CustomerResponseDto toResponse(Customer customer) {
        return new CustomerResponseDto(
                customer.getId(), customer.getName(), CustomerTypeDto.valueOf(customer.getType().name()));
    }
}
