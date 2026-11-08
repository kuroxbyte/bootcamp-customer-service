package dev.bootcamp.customerservice.domain.exception;

/**
 * El recurso no existe.
 */
public class NotFoundException extends RuntimeException {
    /**
     * Crea la excepción con el mensaje indicado.
     */
    public NotFoundException(String message) {
        super(message);
    }
}
