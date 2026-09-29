package ru.sberbank.ditsib.corpclient.service;

import org.apache.kafka.clients.consumer.Consumer;

/**
 * Интерфейс управления биндерами. Остановленные биндеры не читают кафку.
 */
public interface BinderControlService {

    /**
     * Имя биндера для интеграции по ЕАСУП.
     */
    String BINDING_EASUP = "integrationEasupInput-in-0";

    /**
     * Запустить биндер.
     *
     * @param binder название биндера.
     */
    default void start(String binder) {
        start(binder, null);
    }

    /**
     * Запустить биндер.
     *
     * @param binder название биндера.
     * @param consumer consumer for setup.
     */
    void start(String binder, Consumer<?, ?> consumer);

    /**
     * Остановить биндер.
     *
     * @param binder   название биндера.
     */
    default void stop(String binder) {
        stop(binder, null);
    }

    /**
     * Остановить биндер.
     *
     * @param binder   название биндера.
     * @param consumer consumer for setup.
     */
    void stop(String binder, Consumer<?, ?> consumer);

}
