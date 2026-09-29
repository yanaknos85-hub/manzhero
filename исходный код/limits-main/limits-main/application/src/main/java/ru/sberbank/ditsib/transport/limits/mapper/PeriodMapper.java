package ru.sberbank.ditsib.transport.limits.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import ru.sberbank.ditsib.transport.limits.dto.v2.Month;
import ru.sberbank.ditsib.transport.limits.model.limit.Period;
import ru.sberbank.ditsib.transport.limits.model.limit.PeriodData;
import ru.sberbank.ditsib.transport.limits.model.limit.Quarter;

import java.util.Optional;

import static ru.sberbank.ditsib.transport.limits.constants.LimitSharingType.QUARTER;

@Mapper
public interface PeriodMapper {

    String PERIOD_TO_DTO = "periodToDto";

    String PERIOD_OBJECT_TO_INTEGER = "periodObjectToInteger";

    String PERIOD_TO_INTEGER = "periodStringToInteger";

    String PERIOD_TO_MODEL_STRING = "periodToModelString";

    String INT_TO_MONTH = "intToMonth";

    @Named(PERIOD_TO_INTEGER)
    default Integer periodToInteger(PeriodData period) {
        if (QUARTER.equals(period.getType())) {
            return Optional.of(period).map(PeriodData::name).map(Quarter::valueOf).map(Quarter::ordinal).orElse(null);
        }
        return Optional.of(period).map(PeriodData::name).map(Month::valueOf).map(Month::ordinal).orElse(null);
    }

    @Named(INT_TO_MONTH)
    default Month intToMonth(Integer source) {
        if (source == null) {
            return null;
        }
        return Month.values()[source];
    }

    @Named(PERIOD_OBJECT_TO_INTEGER)
    default <T extends Period> Integer periodObjectToInteger(T period) {
        return Optional.ofNullable(period).map(Period::ordinal).orElse(null);
    }

    @Named(PERIOD_TO_DTO)
    default ru.sberbank.ditsib.transport.limits.dto.v2.Period toDto(PeriodData source) {
        if (source == null) {
            return null;
        }
        if (QUARTER.equals(source.getType())) {
            return ru.sberbank.ditsib.transport.limits.dto.v2.Quarter.valueOf(source.name());
        } else {
            return ru.sberbank.ditsib.transport.limits.dto.v2.Month.valueOf(source.name());
        }
    }

    @Named(PERIOD_TO_DTO)
    default ru.sberbank.ditsib.transport.limits.dto.v2.Period toDto(Period source) {
        if (source == null) {
            return null;
        }
        if (source instanceof Quarter quarter) {
            return ru.sberbank.ditsib.transport.limits.dto.v2.Quarter.valueOf(quarter.name());
        }
        return ru.sberbank.ditsib.transport.limits.dto.v2.Month.valueOf(source.name());
    }

    default <T extends ru.sberbank.ditsib.transport.limits.dto.v2.Period> PeriodData toModelString(T source) {
        return Optional.ofNullable(source).map(ru.sberbank.ditsib.transport.limits.dto.v2.Period::name)
                .map(PeriodData::valueOf)
                .orElse(null);
    }

    default String toMessage(Period source) {
        return Optional.ofNullable(source).map(Period::name).orElse(null);
    }
}
