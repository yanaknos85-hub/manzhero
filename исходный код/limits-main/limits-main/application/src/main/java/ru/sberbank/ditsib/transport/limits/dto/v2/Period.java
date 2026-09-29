package ru.sberbank.ditsib.transport.limits.dto.v2;

import lombok.NonNull;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.time.LocalDate;

public interface Period {

    static <T extends Period> T create(@NonNull LocalDate now, @NonNull Class<? super T> periodClass) {
        var month = now.getMonth();
        Period result;
        if (Month.class.isAssignableFrom(periodClass)) {
            result = Month.valueOf(month.name());
        } else if (Quarter.class.isAssignableFrom(periodClass)) {
            result = switch (month) {
                case JANUARY, FEBRUARY, MARCH -> Quarter.Q1;
                case APRIL, MAY, JUNE -> Quarter.Q2;
                case JULY, AUGUST, SEPTEMBER -> Quarter.Q3;
                case OCTOBER, NOVEMBER, DECEMBER -> Quarter.Q4;
            };
        } else {
            throw new UnsupportedOperationException("Period class %s is not supported".formatted(periodClass.getSimpleName()));
        }
        return ReflectionUtils.cast(result);
    }

    static Period fromString(String string){
        return switch (string) {
            case "JANUARY","FEBRUARY","MARCH" -> Quarter.Q1;
            case "APRIL", "MAY", "JUNE" -> Quarter.Q2;
            case "JULY", "AUGUST", "SEPTEMBER" -> Quarter.Q3;
            case "OCTOBER", "NOVEMBER", "DECEMBER" -> Quarter.Q4;
            default -> throw new UnsupportedOperationException("Period type %s is not supported".formatted(string));
        };
    }

    String name();

    ru.sberbank.ditsib.transport.limits.model.limit.Period toModel();

    int ordinal();

}
