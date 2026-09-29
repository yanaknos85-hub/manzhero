package ru.sber.transport.integrations.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@UtilityClass
public class StringToLocalDateTimeConverter {
    public LocalDateTime convert(String value) {
        var supportedPatterns = List.of(
                "dd.MM.yyyy H:mm:ss",
                "dd.MM.yyyy H:m:ss",
                "dd.MM.yyyy H:m:s",
                "dd.MM.yyyy HH:m:ss",
                "dd.MM.yyyy HH:m:s",
                "dd.MM.yyyy HH:mm:s",
                "dd.MM.yyyy H:mm:s",
                "d.MM.yyyy H:mm:s",
                "d.M.yyyy HH:mm:ss",
                "dd.M.yyyy HH:mm:ss"
        );
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return ZonedDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .withZoneSameInstant(ZoneOffset.UTC)
                    .toLocalDateTime();
        } catch (DateTimeParseException e) {
            log.warn("Не удалось распарсить ISO_OFFSET_DATE_TIME формат. {}", e.getMessage());
        }
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .atZone(ZoneOffset.UTC)
                    .toLocalDateTime();
        } catch (DateTimeParseException e) {
            log.warn("Не удалось распарсить формат, dateTimeFormatter:ISO_LOCAL_DATE_TIME,error:{}", e.getMessage());
        }
        for (var pattern : supportedPatterns) {
            try {
                return LocalDateTime.parse(value, DateTimeFormatter.ofPattern(pattern))
                        .atZone(ZoneOffset.UTC)
                        .toLocalDateTime();
            } catch (DateTimeParseException e) {
                log.warn("Не удалось распарсить формат, dateTimeFormatter:{},error:{}", pattern, e.getMessage());
            }
        }
        log.error("Не удалось распарсить ни в один формат, dateTimeString:{}", value);
        return null;
    }
}
