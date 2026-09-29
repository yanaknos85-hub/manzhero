package ru.sberbank.ditsib.transport.approvals.configuration;


import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import ru.sberbank.ditsib.transport.approvals.dto.params.SortField;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/**
 * Базовый конвертер перечисления полей сортировки.
 *
 * @param <T> Тип перечисления полей сортировки.
 */
@RequiredArgsConstructor
public abstract class SortEnumConverter<T extends SortField> implements Converter<String, T> {
    
    private final Class<T> enumeration;
    
    @Override
    public T convert(@NonNull String source) {
        for (var constant : Optional.ofNullable(enumeration.getEnumConstants()).map(Arrays::asList).orElse(Collections.emptyList())) {
            if (source.equalsIgnoreCase(constant.getName())) {
                return constant;
            }
        }
        return null;
    }
}
