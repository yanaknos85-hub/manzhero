package ru.sberbank.ditsib.corpclient.service;

import ru.sber.transport.user_data_confirmation.message.UserDataConfirmationMessage;

/**
 * Сервис для подтверждения контактых данных клиента
 */
public interface ContactConfirmationService {

    /**
     * Подтверждение контактных данных клиента
     * @param message - сообщение с данными для подтверждения
     */
    void confirm(UserDataConfirmationMessage message);

}
