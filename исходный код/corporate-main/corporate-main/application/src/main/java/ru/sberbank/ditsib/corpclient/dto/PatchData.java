package ru.sberbank.ditsib.corpclient.dto;

import java.io.*;

/**
 * Данные для частичного обновления сущностей.
 *
 * @param field поле для обновления.
 * @param value новое значение.
 * @param <T> тип перечисления поддерживаемых для обновления полей.
 */
public record PatchData<T extends Enum<?>>(
        T field,
        Serializable value
) {
}
