package ru.sber.transport.fraud.monitoring.messaging.listeners;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import ru.sber.transport.fraud.monitoring.business.FraudCaseDataService;
import ru.sber.transport.fraud.monitoring.messaging.listeners.mapper.FraudCaseDataMapper;
import ru.sber.transport.fraud.monitoring.messaging.listeners.message.FraudCaseDataMessage;
import ru.sber.transport.fraud.monitoring.model.FraudCaseData;

/**
 * Слушатель топика service.fraud-monitoring.listen.cases_data.
 * <p>
 * Получает сообщения о кейсах фрода с email-перепиской, маппит их в модель {@link FraudCaseData} и сохраняет через
 * сервис {@link FraudCaseDataService}.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class FraudCaseDataListener implements Consumer<Message<FraudCaseDataMessage>> {

    /**
     * Сервис для сохранения данных кейса фрода
     */
    private final FraudCaseDataService fraudCaseDataService;

    /**
     * Маппер для преобразования сообщения в модель БД
     */
    private final FraudCaseDataMapper fraudCaseDataMapper;

    /**
     * Обрабатывает входящее сообщение из топика: маппит в модель и сохраняет в БД.
     *
     * @param fraudCaseDataMessage входящее сообщение из Kafka
     */
    @Override
    public void accept(Message<FraudCaseDataMessage> fraudCaseDataMessage) {
        var messagePayload = fraudCaseDataMessage.getPayload();

        var fraudId = messagePayload.getId();
        if (fraudId == null) {
            log.warn("Фрод кейс не содержит id");
            return;
        }

        var data = fraudCaseDataMapper.toFraudCaseData(messagePayload);
        log.debug("Обработка сообщения о кейсе фрода: {}", data.getId());
        fraudCaseDataService.updateFraudCaseData(data);
        log.info("Обработано сообщение о кейсе фрода: {}", data.getId());
    }

}