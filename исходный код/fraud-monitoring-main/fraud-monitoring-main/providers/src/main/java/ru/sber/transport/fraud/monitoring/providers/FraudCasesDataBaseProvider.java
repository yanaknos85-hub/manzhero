package ru.sber.transport.fraud.monitoring.providers;

import ru.sber.transport.fraud.monitoring.model.FraudCaseData;
import ru.sber.transport.fraud.monitoring.model.FraudCaseDataMarker;

import java.util.Optional;
import java.util.UUID;

/**
 * Провайдер для работы с данными кейса фрода в БД.
 * <p>
 * Обновляет кейс в таблице {@code fraud} и предоставляет доступ к данным кейса.
 * Сохранение email-сообщений делегируется в FraudMessageItemDataBaseProvider}
 */
public interface FraudCasesDataBaseProvider {

    /**
     * Обновляет кейс фрода в таблице {@code fraud}.
     * <p>
     * Сохраняет FraudCaseDataMarker (id, requestId, comment, fraudType, source,
     * aiVerdict, needValidation) в таблицу {@code fraud}.</p>
     *
     * @param message маркер кейса фрода с актуальными данными
     */
    void updateFraudCase(FraudCaseDataMarker message, FraudCaseData newData);

    /**
     * Получить маркер кейса фрода по идентификатору.
     * <p>
     * Выполняет поиск в таблице {@code fraud} по id кейса.
     * Возвращает Optional#empty() если кейс не найден,
     * или Optional с @link FraudCaseDataMarker если найден.</p>
     *
     * @param fraudId идентификатор кейса фрода
     * @return Optional с маркером кейса, или пустой link Optional
     */
    Optional<FraudCaseDataMarker> getFraudCaseByFraudCaseId(UUID fraudId);

    /**
     * Сохраняет решение сотрудника УТО по разбирательству фрода.
     * <p>
     * Обновляет поля {@code VERDICT} и {@code REASON} в таблице {@code fraud},
     * а также устанавливает {@code NEED_VALIDATION = false}.</p>
     *
     * @param fraudCase маркер кейса фрода
     * @param verdict   решение сотрудника
     * @param reason    комментарий к решению
     */
    void solveFraudCase(FraudCaseDataMarker fraudCase, String verdict, String reason);
}