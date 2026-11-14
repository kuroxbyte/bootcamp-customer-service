package dev.bootcamp.customerservice.domain.exception;

/**
 * Quien hace la petición no puede realizar la operación (por ejemplo, un cliente que intenta
 * modificar a otro cliente o cambiar su propio perfil). Se traduce a una respuesta 403.
 */
public class ForbiddenException extends RuntimeException {

    /**
     * Crea la excepción con el mensaje para el usuario.
     */
    public ForbiddenException(String message) {
        super(message);
    }
}
