package dev.bootcamp.customerservice.infrastructure.web.mapper;

import dev.bootcamp.customerservice.domain.model.customer.Customer;
import dev.bootcamp.customerservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.customerservice.domain.model.customer.CustomerType;
import dev.bootcamp.customerservice.infrastructure.web.dto.CustomerProfileDto;
import dev.bootcamp.customerservice.infrastructure.web.dto.CustomerResponseDto;
import dev.bootcamp.customerservice.infrastructure.web.dto.CustomerTypeDto;

/**
 * Conversión entre los DTOs del contrato OpenAPI y el dominio.
 */
public final class CustomerWebMapper {

    private CustomerWebMapper() {}

    /**
     * Convierte el tipo del contrato OpenAPI al tipo del dominio.
     */
    public static CustomerType toDomain(CustomerTypeDto type) {
        return CustomerType.valueOf(type.name());
    }

    /**
     * Convierte el perfil del contrato OpenAPI al perfil del dominio.
     */
    public static CustomerProfile toDomain(CustomerProfileDto profile) {
        return CustomerProfile.valueOf(profile.name());
    }

    /**
     * Convierte {@link Customer} en su DTO de respuesta.
     */
    public static CustomerResponseDto toResponse(Customer customer) {
        return new CustomerResponseDto(
                customer.getId(),
                customer.getName(),
                CustomerTypeDto.valueOf(customer.getType().name()),
                CustomerProfileDto.valueOf(customer.getProfile().name()));
    }
}
