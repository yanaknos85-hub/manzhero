package ru.sberbank.ditsib.corpclient.dto;

/**
 * Объект данных файла.
 *
 * @param contentType тип данных.
 * @param stream данные.
 */
@SuppressWarnings("java:S6218")
public record FileData(

    String contentType,
    byte[] stream

) {
}
