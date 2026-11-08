package dev.bootcamp.customerservice.infrastructure.web.controller;

import dev.bootcamp.customerservice.application.usecase.CreateCustomerUseCase;
import dev.bootcamp.customerservice.application.usecase.GetCustomerUseCase;
import dev.bootcamp.customerservice.infrastructure.web.api.CustomersApi;
import dev.bootcamp.customerservice.infrastructure.web.dto.CustomerRequestDto;
import dev.bootcamp.customerservice.infrastructure.web.dto.CustomerResponseDto;
import dev.bootcamp.customerservice.infrastructure.web.mapper.CustomerWebMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Endpoints de clientes definidos en el contrato OpenAPI. */
@Slf4j
@RestController
@RequiredArgsConstructor
public class CustomerController implements CustomersApi {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final GetCustomerUseCase getCustomerUseCase;

    @Override
    public Mono<ResponseEntity<CustomerResponseDto>> createCustomer(
            Mono<CustomerRequestDto> customerRequestDto, ServerWebExchange exchange) {
        return customerRequestDto
                .flatMap(
                        request ->
                                Mono.from(
                                        createCustomerUseCase
                                                .execute(
                                                        request.getName(),
                                                        CustomerWebMapper.toDomain(
                                                                request.getType()))
                                                .toFlowable()))
                .doOnNext(
                        customer ->
                                log.info(
                                        "Customer created customerId={} type={}",
                                        customer.getId(),
                                        customer.getType()))
                .map(
                        customer ->
                                ResponseEntity.status(HttpStatus.CREATED)
                                        .body(CustomerWebMapper.toResponse(customer)));
    }

    @Override
    public Mono<ResponseEntity<CustomerResponseDto>> getCustomerById(
            String customerId, ServerWebExchange exchange) {
        log.debug("Fetching customer customerId={}", customerId);
        return Mono.from(getCustomerUseCase.execute(customerId).toFlowable())
                .map(customer -> ResponseEntity.ok(CustomerWebMapper.toResponse(customer)));
    }
}
