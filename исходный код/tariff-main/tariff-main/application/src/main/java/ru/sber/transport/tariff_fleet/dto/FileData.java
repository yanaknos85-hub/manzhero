package ru.sber.transport.tariff_fleet.dto;

/**
 * Данные файла.
 *
 * @param contentType тип файла.
 * @param stream массив байт файла.
 */
public record FileData(
        String contentType,
        byte[] stream
) {
}
