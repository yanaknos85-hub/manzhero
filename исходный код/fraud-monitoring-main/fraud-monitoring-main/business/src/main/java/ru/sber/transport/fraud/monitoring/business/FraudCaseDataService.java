package ru.sber.transport.fraud.monitoring.business;

import ru.sber.transport.fraud.monitoring.model.FraudCaseData;

/**
 * Сервис для обработки и сохранения данных кейса фрода с email-перепиской.
 * <p>
 * Получает данные из топика service.fraud-monitoring.listen.cases_data
 * и делегирует сохранение в провайдер БД.</p>
 */
public interface FraudCaseDataService {

    /**
     * Обновляет данные кейса с email-перепиской.
     *
     * @param fraudCaseData данные кейса фрода (кейс + список email-сообщений)
     */
    void updateFraudCaseData(FraudCaseData fraudCaseData);
}