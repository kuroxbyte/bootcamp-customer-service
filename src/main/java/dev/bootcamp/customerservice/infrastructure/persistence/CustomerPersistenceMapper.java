package dev.bootcamp.customerservice.infrastructure.persistence;

import dev.bootcamp.customerservice.domain.model.customer.Customer;
import dev.bootcamp.customerservice.domain.model.customer.CustomerProfile;
import dev.bootcamp.customerservice.domain.model.customer.CustomerType;

/**
 * Conversión entre el dominio y el documento de MongoDB.
 */
final class CustomerPersistenceMapper {
    private CustomerPersistenceMapper() {}

    static CustomerDocument toDocument(Customer customer) {
        return new CustomerDocument(
                customer.getId(),
                customer.getName(),
                customer.getType().name(),
                customer.getProfile().name());
    }

    static Customer toDomain(CustomerDocument document) {
        Customer customer = new Customer();
        customer.setId(document.getId());
        customer.setName(document.getName());
        customer.setType(CustomerType.valueOf(document.getType()));
        customer.setProfile(CustomerProfile.valueOf(document.getProfile()));
        return customer;
    }
}
