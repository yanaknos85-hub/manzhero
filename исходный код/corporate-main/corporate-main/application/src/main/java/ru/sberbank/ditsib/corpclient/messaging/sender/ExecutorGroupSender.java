package ru.sberbank.ditsib.corpclient.messaging.sender;

import ru.sberbank.ditsib.corpclient.dto.ExecutorGroupDTO;

/**
 * Sending data about executor group.
 */
public interface ExecutorGroupSender {

    /**
     * Отправить данные группы исполнителей.
     *
     * @param executorGroup группа исполнителей для отправки.
     */
    void send(ExecutorGroupDTO executorGroup);
}
