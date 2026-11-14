package dev.bootcamp.customerservice.infrastructure.web.controller;

import dev.bootcamp.customerservice.application.usecase.CloseCustomerUseCase;
import dev.bootcamp.customerservice.application.usecase.CreateCustomerUseCase;
import dev.bootcamp.customerservice.application.usecase.GetCustomerUseCase;
import dev.bootcamp.customerservice.application.usecase.UpdateCustomerUseCase;
import dev.bootcamp.customerservice.domain.exception.ForbiddenException;
import dev.bootcamp.customerservice.infrastructure.web.api.CustomersApi;
import dev.bootcamp.customerservice.infrastructure.web.dto.CustomerRequestDto;
import dev.bootcamp.customerservice.infrastructure.web.dto.CustomerResponseDto;
import dev.bootcamp.customerservice.infrastructure.web.dto.CustomerUpdateRequestDto;
import dev.bootcamp.customerservice.infrastructure.web.mapper.CustomerWebMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Endpoints de clientes definidos en el contrato OpenAPI.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class CustomerController implements CustomersApi {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final GetCustomerUseCase getCustomerUseCase;
    private final UpdateCustomerUseCase updateCustomerUseCase;
    private final CloseCustomerUseCase closeCustomerUseCase;

    @Override
    public Mono<ResponseEntity<CustomerResponseDto>> createCustomer(
            Mono<CustomerRequestDto> customerRequestDto,
            ServerWebExchange exchange) {
        return customerRequestDto
                .flatMap(request -> Mono.from(createCustomerUseCase
                        .execute(
                                request.getName(),
                                CustomerWebMapper.toDomain(request.getType()),
                                CustomerWebMapper.toDomain(request.getProfile()))
                        .toFlowable()))
                .doOnNext(customer -> log.info(
                        "Customer created customerId={} type={} profile={}",
                        customer.getId(),
                        customer.getType(),
                        customer.getProfile()))
                .map(
                        customer ->
                                ResponseEntity.status(HttpStatus.CREATED)
                                        .body(CustomerWebMapper.toResponse(customer)));
    }

    @Override
    public Mono<ResponseEntity<CustomerResponseDto>> getCustomerById(
            String customerId,
            ServerWebExchange exchange) {
        log.debug("Fetching customer customerId={}", customerId);
        return Mono.from(getCustomerUseCase.execute(customerId).toFlowable())
                .map(customer -> ResponseEntity.ok(CustomerWebMapper.toResponse(customer)));
    }

    @Override
    public Mono<ResponseEntity<Flux<CustomerResponseDto>>> listCustomers(
            String adminId,
            ServerWebExchange exchange) {
        log.debug("Listing customers adminId={}", adminId);
        return Mono.just(ResponseEntity.ok(
                Flux.from(getCustomerUseCase.findAll()).map(CustomerWebMapper::toResponse)));
    }

    @Override
    public Mono<ResponseEntity<CustomerResponseDto>> updateCustomer(
            String customerId,
            Mono<CustomerUpdateRequestDto> customerUpdateRequestDto,
            String requesterCustomerId,
            String adminId,
            ServerWebExchange exchange) {
        if (requesterCustomerId == null && adminId == null) {
            return Mono.error(new ForbiddenException(
                    "La petición debe venir de un cliente o de un empleado"));
        }
        // Si la hace un empleado, puede cambiar el perfil.
        String requester = adminId != null ? null : requesterCustomerId;
        return customerUpdateRequestDto
                .flatMap(request -> Mono.from(updateCustomerUseCase
                        .execute(
                                customerId,
                                requester,
                                request.getName(),
                                CustomerWebMapper.toDomain(request.getProfile()))
                        .toFlowable()))
                .doOnNext(customer -> log.info(
                        "Customer updated customerId={} profile={} byAdmin={}",
                        customer.getId(),
                        customer.getProfile(),
                        adminId != null))
                .map(customer -> ResponseEntity.ok(CustomerWebMapper.toResponse(customer)));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCustomer(
            String customerId,
            String adminId,
            ServerWebExchange exchange) {
        return Mono.from(closeCustomerUseCase.execute(customerId).toFlowable())
                .then(Mono.fromCallable(() -> {
                    log.info("Customer closed customerId={} adminId={}", customerId, adminId);
                    return ResponseEntity.noContent().<Void>build();
                }));
    }
}
