package ru.sberbank.ditsib.transport.limits.mapper;

import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Mapper суммы.
 */
@Mapper
public interface SumMapper {

    /**
     * Преобразовать в сообщение.
     *
     * @param source source.
     * @return message.
     */
    default Long toMessage(BigDecimal source) {
        return Optional.ofNullable(source)
                .map(it -> it.multiply(BigDecimal.valueOf(100)))
                .map(BigDecimal::longValue)
                .orElse(null);
    }

    /**
     * Преобразовать в модель.
     *
     * @param source source.
     * @return model.
     */
    default BigDecimal toModel(Long source) {
        return Optional.ofNullable(source)
                .map(BigDecimal::valueOf)
                .map(it -> it.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN))
                .orElse(BigDecimal.ZERO);
    }

}
