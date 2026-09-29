package ru.sberbank.ditsib.corpclient.service;

/**
 * Инициализатор топиков.
 */
public interface TopicInitializer {
    
    /**
     * Инициализация.
     *
     * @param key ключ.
     */
    void initialize(String key);
}
