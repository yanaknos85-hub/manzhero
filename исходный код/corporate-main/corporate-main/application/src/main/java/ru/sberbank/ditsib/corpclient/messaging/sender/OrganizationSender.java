package ru.sberbank.ditsib.corpclient.messaging.sender;

import ru.sberbank.ditsib.corpclient.database.model.Organization;

/**
 * Sending data about organization.
 */
public interface OrganizationSender {
    
    /**
     * Отправить данные организации.
     *
     * @param organization огранизация для отправки.
     */
    void send(Organization organization);
}
