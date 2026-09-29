package ru.sberbank.ditsib.corpclient.service.import_easup;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис для обработки данных из интеграции.
 */
public interface IntegrationService {

    /**
     * Импорт данных.
     *
     * @param msg коллекция объектов интеграции.
     */
    CompletableFuture<Void> importData(Map<String, Queue<Object>> msg);
}
