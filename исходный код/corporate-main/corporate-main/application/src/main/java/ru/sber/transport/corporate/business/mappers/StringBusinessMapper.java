package ru.sber.transport.corporate.business.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.Optional;

/**
 * Строковый бизнес-маппер
 */
@Mapper
public interface StringBusinessMapper {

    /**
     * Название метода удаления нулей из строки.
     */
    String LEADING_ZEROS = "leadingZeros";

    /**
     * Удаление нулей из строки.
     *
     * @param source исходная строка.
     * @return исходная строка без нулей.
     */
    @Named(LEADING_ZEROS)
    default String leadingZeros(String source) {
        return Optional.ofNullable(source).map(it -> it.replaceFirst("^0+", "")).orElse(null);
    }

}
