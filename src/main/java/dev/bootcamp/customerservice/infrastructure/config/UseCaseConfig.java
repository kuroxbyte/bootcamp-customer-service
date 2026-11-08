package dev.bootcamp.customerservice.infrastructure.config;

import dev.bootcamp.customerservice.application.port.CustomerRepository;
import dev.bootcamp.customerservice.application.usecase.CreateCustomerUseCase;
import dev.bootcamp.customerservice.application.usecase.CreateCustomerUseCaseImpl;
import dev.bootcamp.customerservice.application.usecase.GetCustomerUseCase;
import dev.bootcamp.customerservice.application.usecase.GetCustomerUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra los casos de uso como beans. Los casos de uso no dependen de Spring; aquí se les
 * inyectan los adaptadores de sus puertos.
 */
@Configuration
public class UseCaseConfig {

    /** Registra el caso de uso {@link CreateCustomerUseCase}. */
    @Bean
    public CreateCustomerUseCase createCustomerUseCase(CustomerRepository customerRepository) {
        return new CreateCustomerUseCaseImpl(customerRepository);
    }

    /** Registra el caso de uso {@link GetCustomerUseCase}. */
    @Bean
    public GetCustomerUseCase getCustomerUseCase(CustomerRepository customerRepository) {
        return new GetCustomerUseCaseImpl(customerRepository);
    }
}
