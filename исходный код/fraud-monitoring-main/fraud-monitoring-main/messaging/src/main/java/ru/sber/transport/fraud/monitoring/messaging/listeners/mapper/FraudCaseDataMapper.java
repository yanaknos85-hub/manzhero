package ru.sber.transport.fraud.monitoring.messaging.listeners.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.fraud.monitoring.messaging.listeners.message.FraudCaseDataMessage;
import ru.sber.transport.fraud.monitoring.messaging.listeners.model.FraudCaseDataImpl;
import ru.sber.transport.fraud.monitoring.messaging.listeners.model.FraudMessageItemImpl;

import java.util.List;

/**
 * Маппер для преобразования сообщения из топика service.fraud-monitoring.listen.cases_data
 * в модель данных {@link FraudCaseDataImpl} для сохранения в БД.
 * <p>
 * Маппит {@link FraudCaseDataMessage} в {@link FraudCaseDataImpl},
 * а также каждый {@link FraudCaseDataMessage.MessageItem} в {@link FraudMessageItemImpl}.</p>
 */
@Mapper
public interface FraudCaseDataMapper {

    /**
     * Маппит сообщение из топика в модель для сохранения в БД.
     *
     * @param message сообщение из топика service.fraud-monitoring.listen.cases_data
     * @return модель данных {@link FraudCaseDataImpl}
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "aiComment", source = "aiComment")
    @Mapping(target = "aiVerdict", source = "aiVerdict")
    @Mapping(target = "needValidation", source = "needValidation")
    @Mapping(target = "messaging", source = "messaging")
    FraudCaseDataImpl toFraudCaseData(FraudCaseDataMessage message);

    /**
     * Преобразует список {@link FraudCaseDataMessage.MessageItem} из сообщения
     * в список {@link FraudMessageItemImpl} для сохранения в БД.
     * <p>
     * Если сообщение или список сообщений null, возвращает пустой список.</p>
     *
     * @param message сообщение из топика
     * @return список моделей email-сообщений
     */
    default List<FraudMessageItemImpl> toMessageItemList(FraudCaseDataMessage message) {
        if (message == null || message.messaging() == null) {
            return List.of();
        }
        return message.messaging().stream()
                .map(this::toMessageItem)
                .toList();
    }

    /**
     * Маппит отдельный элемент сообщения из топика в модель email-сообщения.
     *
     * @param item элемент списка сообщений из топика
     * @return модель email-сообщения для сохранения в таблицу {@code messaging}
     */
    @Mapping(target = "messageDate", source = "messageDate")
    @Mapping(target = "fromEmail", source = "fromEmail")
    @Mapping(target = "toEmail", source = "toEmail")
    @Mapping(target = "body", source = "body")
    FraudMessageItemImpl toMessageItem(FraudCaseDataMessage.MessageItem item);
}