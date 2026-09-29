package ru.sber.transport.corporate.messaging.senders;

import lombok.SneakyThrows;
import ru.sber.transport.corporate.business.model.Organization;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Отправка организаций
 */
public interface OrganizationSender {

    /**
     * Отправка сообщения с организацией
     *
     * @param source данные организации
     * @return выполняемая асинхронная задача.
     */
    Future<Void> send(Organization source);

    /**
     * Отправка сообщения с организацией
     *
     * @param source данные организации
     */
    default void send(List<Organization> source) {
        source.forEach(it -> await(send(it)));
    }

    @SneakyThrows({InterruptedException.class, ExecutionException.class})
    private void await(Future<Void> future) {
        future.get();
    }

}
