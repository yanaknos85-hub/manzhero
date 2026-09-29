package ru.sber.transport.fraud.monitoring.business.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.fraud.monitoring.business.FraudCaseDataService;
import ru.sber.transport.fraud.monitoring.model.FraudCaseData;
import ru.sber.transport.fraud.monitoring.providers.FraudCasesDataBaseProvider;
import ru.sber.transport.fraud.monitoring.providers.FraudMessageItemDataBaseProvider;

/**
 * Реализация сервиса {@link FraudCaseDataService}.
 * <p>
 * Получает данные кейса фрода с email-перепиской из топика {@code service.fraud-monitoring.listen.cases_data}.
 * Обновляет кейс в таблице {@code FRAUD} и сохраняет email-сообщения в таблицу {@code MESSAGING} через соответствующие
 * провайдеры.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class FraudCaseDataServiceImpl implements FraudCaseDataService {

    /**
     * Провайдер для работы с данными кейса фрода (таблица {@code FRAUD})
     */
    private final FraudCasesDataBaseProvider fraudCasesDataBaseProvider;

    /**
     * Провайдер для сохранения email-сообщений (таблица {@code MESSAGING})
     */
    private final FraudMessageItemDataBaseProvider fraudMessageItemDataBaseProvider;

    /**
     * Обновляет кейс фрода и сохраняет email-сообщения.
     * <p>
     * Метод проверяет существование кейса в таблице {@code FRAUD}. Если кейс не найден — логирует предупреждение и
     * завершается. Если найден — обновляет кейс и сохраняет все сообщения из {@link FraudCaseData#getMessaging()} в
     * таблицу {@code MESSAGING}.</p>
     *
     * @param fraudCaseData данные кейса фрода (кейс + список email-сообщений)
     */
    @Override
    @Transactional
    public void updateFraudCaseData(FraudCaseData fraudCaseData) {
        var fraudId = fraudCaseData.getId();

        var existingMarker = fraudCasesDataBaseProvider.getFraudCaseByFraudCaseId(fraudId);
        if (existingMarker.isEmpty()) {
            log.warn("Нет данных кейса фрода с id {}", fraudId);
            return;
        }

        log.debug("Обновление кейса фрода: {}", fraudId);
        fraudCasesDataBaseProvider.updateFraudCase(existingMarker.get(), fraudCaseData);
        log.info("Кейс фрода обновлен: {}", fraudId);

        var messagingItems = fraudCaseData.getMessaging();
        if (messagingItems != null && !messagingItems.isEmpty()) {
            log.debug("Сохранение {} сообщений для кейса фрода: {}", messagingItems.size(), fraudId);
            fraudMessageItemDataBaseProvider.saveFraudMessageItem((List<FraudCaseData.FraudMessageItem>) messagingItems,
                fraudId);
            log.info("Сообщения для кейса фрода сохранены: {}", fraudId);
        }
    }

}