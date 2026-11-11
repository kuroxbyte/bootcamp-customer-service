package dev.bootcamp.customerservice.infrastructure.messaging;

import java.time.Instant;

/**
 * Evento {@code CustomerCreated} del tópico {@code customer-events}, tal como se publica en JSON.
 * Su contrato está en {@code docs/events/asyncapi.yaml} del repositorio raíz.
 *
 * @param eventType  siempre {@code CustomerCreated}
 * @param customerId id del cliente creado; también es la clave del mensaje
 * @param type       PERSONAL o BUSINESS
 * @param profile    STANDARD, VIP o PYME
 * @param occurredAt momento de la creación
 */
public record CustomerCreatedEvent(
        String eventType,
        String customerId,
        String type,
        String profile,
        Instant occurredAt) {

    /**
     * Nombre del tipo de evento.
     */
    public static final String EVENT_TYPE = "CustomerCreated";
}
