package ru.sber.transport.fraud.monitoring.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ru.sber.transport.fraud.monitoring.model.FraudCaseData.FraudMessageItem;

/**
 * Заявка на поездку с сообщениями по фроду
 */
public interface TripRequestDataWithMessages extends TripRequestDataBase {

    /**
     * Нарушения (фрод) поездки
     *
     * @return нарушения (фрод) поездки
     */
    List<FraudCaseData> getFrauds();

    /**
     * Сообщения по фроду, сгруппированные по идентификатору кейса
     *
     * @return сообщения по фроду
     */
    Map<UUID, List<FraudMessageItem>> getFraudMessages();
}