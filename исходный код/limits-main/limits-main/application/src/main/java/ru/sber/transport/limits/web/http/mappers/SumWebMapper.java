package ru.sber.transport.limits.web.http.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Веб-маппер для работы с суммами
 */
@Mapper
public interface SumWebMapper {

    /**
     * Перевод суммы из бизнеса в веб.
     * @param source сумма в бизнесе
     * @return сумма в вебе
     */
    default BigDecimal toWeb(long source) {
        return BigDecimal.valueOf(source).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_DOWN);
    }

    @Named("multiply")
    default BigDecimal multiply(BigDecimal value) {
        if (value == null) {
            return null;
        } else {
            return value.multiply(BigDecimal.valueOf(100));
        }
    }
}
