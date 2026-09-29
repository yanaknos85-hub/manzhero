package ru.sberbank.ditsib.corpclient.messaging.sender;

import java.util.UUID;

/**
 * Сервис для отправки сообщений на подтверждение контактных данных
 */
public interface ConfirmationSender {

    /**
     * Отправка сообщения для подтверждения контактных данных сотрудника
     * @param id идентификатор сотрудника
     * @param phone мобильный телефон сотрудника
     */
    void send(UUID id, String phone);

}
