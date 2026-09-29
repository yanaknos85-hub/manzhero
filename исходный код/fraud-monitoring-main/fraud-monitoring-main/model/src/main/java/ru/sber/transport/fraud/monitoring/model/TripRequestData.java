package ru.sber.transport.fraud.monitoring.model;

import java.util.List;

/**
 * Заявка на поездку
 */
public interface TripRequestData extends TripRequestDataBase {

    /**
     * Нарушения (фрод) поездки
     *
     * @return нарушения (фрод) поездки
     */
    List<Fraud> getFrauds();
}