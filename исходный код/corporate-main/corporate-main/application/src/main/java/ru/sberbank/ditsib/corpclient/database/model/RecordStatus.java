package ru.sberbank.ditsib.corpclient.database.model;

/**
 * Доступные состояния записи в БД.
 */
public enum RecordStatus {
    
    /**
     * Запись активна.
     */
    ACTIVE,
    
    /**
     * Запись не активна (удалена).
     */
    INACTIVE
}
