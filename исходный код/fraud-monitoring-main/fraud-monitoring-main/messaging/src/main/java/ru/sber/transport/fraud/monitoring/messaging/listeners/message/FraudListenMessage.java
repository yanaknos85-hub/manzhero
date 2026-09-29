package ru.sber.transport.fraud.monitoring.messaging.listeners.message;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.sber.transport.messaging.Message;

import java.util.List;
import java.util.UUID;

/**
 * Сообщение из топика service.fraud-monitoring.listen.fraud
 *
 * @param requestId   Идентификатор заявки
 * @param source      Название сервиса-источника
 * @param fraudData   Массив данных о мошенничестве
 */
public record FraudListenMessage(

        @Schema(description = "Идентификатор заявки")
        UUID requestId,

        @Schema(description = "Название сервиса-источника", maxLength = 50)
        String source,

        @Schema(description = "Массив данных о мошенничестве")
        List<FraudDataItem> fraudData

) implements Message<UUID> {

    @Override
    public UUID getId() {
        return requestId;
    }

    /**
     * Элемент данных о мошенничестве
     *
     * @param comment           Комментарий о подозрении на мошенничество
     * @param relatedRequestId  Идентификатор связанной заявки (опционально)
     * @param type              Тип мошенничества
     */
    public record FraudDataItem(

            @Schema(description = "Комментарий о подозрении на мошенничество", maxLength = 500)
            String comment,

            @Schema(description = "Идентификатор связанной заявки")
            UUID relatedRequestId,

            @Schema(description = "Тип мошенничества", maxLength = 50)
            String type

    ) {}
}