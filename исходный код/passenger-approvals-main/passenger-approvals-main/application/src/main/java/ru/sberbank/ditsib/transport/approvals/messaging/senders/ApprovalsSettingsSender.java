package ru.sberbank.ditsib.transport.approvals.messaging.senders;

import ru.sberbank.ditsib.transport.approvals.database.model.ApprovalsSettings;

/**
 * Сендер сообщения с настройками согласовний такси
 */
public interface ApprovalsSettingsSender {

    /**
     * Отправить сообщение на создание / изменение
     * @param settings ApprovalsSettings
     */
    void send(ApprovalsSettings settings);
    
    /**
     * Отправить сообщение на удаление
     * @param settings ApprovalsSettings
     */
    void sendDeleted(ApprovalsSettings settings);
}
