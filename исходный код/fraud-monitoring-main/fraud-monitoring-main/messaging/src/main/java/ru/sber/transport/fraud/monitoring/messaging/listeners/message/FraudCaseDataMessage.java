package ru.sber.transport.fraud.monitoring.messaging.listeners.message;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.sber.transport.messaging.Message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Сообщение из топика service.fraud-monitoring.listen.cases_data
 *
 * @param id             Идентификатор кейса фрода
 * @param messaging      Список email-сообщений
 * @param aiVerdict      Вердикт ИИ
 * @param aiComment      Комментарий ИИ
 * @param needValidation  Требуется ли валидация
 */
public record FraudCaseDataMessage(

        @Schema(description = "Идентификатор кейса фрода")
        UUID id,

        @Schema(description = "Список email-сообщений")
        List<MessageItem> messaging,

        @Schema(description = "Вердикт ИИ", maxLength = 255)
        String aiVerdict,

        @Schema(description = "Комментарий ИИ", maxLength = 255)
        String aiComment,

        @Schema(description = "Требуется ли валидация")
        boolean needValidation

) implements Message<UUID> {

    @Override
    public UUID getId() {
        return id;
    }

    /**
     * Email-сообщение
     *
     * @param messageDate  Дата/время отправки письма
     * @param fromEmail    Отправитель
     * @param toEmail      Получатель
     * @param body         Тело письма
     */
    public record MessageItem(

            @Schema(description = "Дата/время отправки письма")
            LocalDateTime messageDate,

            @Schema(description = "Отправитель", maxLength = 255)
            String fromEmail,

            @Schema(description = "Получатель", maxLength = 255)
            String toEmail,

            @Schema(description = "Тело письма", maxLength = 10000)
            String body
    ) {}
}