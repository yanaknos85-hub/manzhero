package ru.sber.transport.limits.messaging.senders;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Отправка данных по электронной почте
 */
public interface EmailSender {

    /**
     * Отправить сообщение
     *
     * @param emails  список получателей
     * @param percent процент оставшихся средств
     * @return последовательность отправки сообщений
     */
    List<Future<Void>> send(Collection<String> emails, String departmentId, String limitId, int percent);

}
