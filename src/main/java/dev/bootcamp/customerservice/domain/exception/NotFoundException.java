package dev.bootcamp.customerservice.domain.exception;

/**
 * El recurso no existe.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
