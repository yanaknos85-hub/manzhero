package ru.sber.transport.fraud.monitoring.providers;

import java.util.List;
import java.util.UUID;

import static ru.sber.transport.fraud.monitoring.model.FraudCaseData.*;

/**
 * Провайдер для сохранения email-сообщений кейса фрода в базу данных.
 */
public interface FraudMessageItemDataBaseProvider {

    /**
     * Сохраняет список email-сообщений для указанного кейса фрода.
     *
     * @param messageItems список email-сообщений для сохранения
     * @param fraudId      идентификатор кейса фрода
     */
    void saveFraudMessageItem(List<FraudMessageItem> messageItems, UUID fraudId);
}
