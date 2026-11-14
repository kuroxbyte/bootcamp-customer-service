package dev.bootcamp.customerservice.infrastructure.messaging;

import java.time.Instant;

/**
 * Evento del tópico {@code customer-events}, tal como se publica en JSON. Su contrato está en
 * {@code docs/events/asyncapi.yaml} del repositorio raíz.
 *
 * @param eventType  {@code CustomerCreated}, {@code CustomerUpdated} o {@code CustomerClosed}
 * @param customerId id del cliente; también es la clave del mensaje
 * @param type       PERSONAL o BUSINESS
 * @param profile    STANDARD, VIP o PYME
 * @param occurredAt momento del cambio
 */
public record CustomerEvent(
        String eventType,
        String customerId,
        String type,
        String profile,
        Instant occurredAt) {

    /**
     * Se creó el cliente.
     */
    public static final String CUSTOMER_CREATED = "CustomerCreated";

    /**
     * Se modificó el nombre o el perfil del cliente.
     */
    public static final String CUSTOMER_UPDATED = "CustomerUpdated";

    /**
     * Se dio de baja al cliente.
     */
    public static final String CUSTOMER_CLOSED = "CustomerClosed";
}
