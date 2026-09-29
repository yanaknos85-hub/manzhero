package ru.sberbank.ditsib.corpclient.messaging.sender;

import ru.sberbank.ditsib.corpclient.database.model.TransportOrg;

/**
 * Sending data about transport for organization.
 */
public interface TransportOrgSender {

    /**
     * Отправить данные типов транспорта по организациям.
     *
     * @param transportOrg связка типа транспорта и организации для отправки.
     * @param deleted      признак удаления.
     */
    void send(TransportOrg transportOrg, boolean deleted);
}
