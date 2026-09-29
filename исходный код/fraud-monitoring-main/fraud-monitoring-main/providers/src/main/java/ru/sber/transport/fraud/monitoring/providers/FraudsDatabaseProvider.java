package ru.sber.transport.fraud.monitoring.providers;


import lombok.NonNull;
import ru.sber.transport.fraud.monitoring.model.Fraud;

import java.util.List;
import java.util.UUID;

/**
 * Провайдер данных о фроде.
 */
public interface FraudsDatabaseProvider {

    /**
     * Сохраняет данные о фроде
     *
     * @param source данные о фроде
     */
    void save(Fraud source);

    /**
     * Получает список фрод-записей по идентификатору заявки.
     *
     * @param requestId идентификатор заявки на поездку
     * @return список фрод-записей
     */
    List<Fraud> findByRequestId(@NonNull UUID requestId);
}
