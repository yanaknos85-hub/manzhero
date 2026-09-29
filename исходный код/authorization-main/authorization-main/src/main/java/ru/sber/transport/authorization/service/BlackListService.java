package ru.sber.transport.authorization.service;

/**
 * Сервис для работы с черным списком.
 */
public interface BlackListService {
    
    /**
     * Добавляет токен в ЧС.
     *
     * @param token токен.
     */
    void add(String token);
    
    /**
     * Проверка токена на блокировку.
     *
     * @param token токен.
     * @return <code>true</code> если токен заблокирован.
     */
    boolean check(String token);
}
