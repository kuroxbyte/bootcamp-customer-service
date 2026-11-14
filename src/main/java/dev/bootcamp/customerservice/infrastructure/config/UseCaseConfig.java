package dev.bootcamp.customerservice.infrastructure.config;

import dev.bootcamp.customerservice.application.port.CustomerEventPublisher;
import dev.bootcamp.customerservice.application.port.CustomerRepository;
import dev.bootcamp.customerservice.application.port.ProductClient;
import dev.bootcamp.customerservice.application.usecase.CloseCustomerUseCase;
import dev.bootcamp.customerservice.application.usecase.CloseCustomerUseCaseImpl;
import dev.bootcamp.customerservice.application.usecase.CreateCustomerUseCase;
import dev.bootcamp.customerservice.application.usecase.CreateCustomerUseCaseImpl;
import dev.bootcamp.customerservice.application.usecase.GetCustomerUseCase;
import dev.bootcamp.customerservice.application.usecase.GetCustomerUseCaseImpl;
import dev.bootcamp.customerservice.application.usecase.UpdateCustomerUseCase;
import dev.bootcamp.customerservice.application.usecase.UpdateCustomerUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Registra los casos de uso como beans. Los casos de uso no dependen de Spring; aquí se les
 * inyectan los adaptadores de sus puertos.
 */
@Configuration
public class UseCaseConfig {

    /**
     * Registra el caso de uso {@link CreateCustomerUseCase}.
     */
    @Bean
    public CreateCustomerUseCase createCustomerUseCase(
            CustomerRepository customerRepository,
            CustomerEventPublisher customerEventPublisher) {
        return new CreateCustomerUseCaseImpl(customerRepository, customerEventPublisher);
    }

    /**
     * Registra el caso de uso {@link GetCustomerUseCase}.
     */
    @Bean
    public GetCustomerUseCase getCustomerUseCase(CustomerRepository customerRepository) {
        return new GetCustomerUseCaseImpl(customerRepository);
    }

    /**
     * Registra el caso de uso {@link UpdateCustomerUseCase}.
     */
    @Bean
    public UpdateCustomerUseCase updateCustomerUseCase(
            CustomerRepository customerRepository,
            CustomerEventPublisher customerEventPublisher) {
        return new UpdateCustomerUseCaseImpl(customerRepository, customerEventPublisher);
    }

    /**
     * Registra el caso de uso {@link CloseCustomerUseCase}.
     */
    @Bean
    public CloseCustomerUseCase closeCustomerUseCase(
            CustomerRepository customerRepository,
            ProductClient productClient,
            CustomerEventPublisher customerEventPublisher,
            Clock clock) {
        return new CloseCustomerUseCaseImpl(
                customerRepository,
                productClient,
                customerEventPublisher,
                clock);
    }
}
