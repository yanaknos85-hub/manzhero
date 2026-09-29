package ru.sber.transport.integrations.utils;

import ch.qos.logback.classic.Level;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.sber.transport.integrations.LoggingExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

class StringToLocalDateTimeConverterTest {
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(StringToLocalDateTimeConverter.class);

    @ParameterizedTest
    @MethodSource("convertIsoOffsetDateTimeToLocalDateTimeUTCData")
    void convert(String dateTimeString, LocalDateTime result, List<Tuple> levelStringMap) {
        assertThat(StringToLocalDateTimeConverter.convert(dateTimeString)).isEqualTo(result);
        assertThat(LOGGING_EXTENSION.getEvents()).hasSize(levelStringMap.size());
        if (!LOGGING_EXTENSION.getEvents().isEmpty()) {
            assertThat(LOGGING_EXTENSION.getEvents().stream()
                    .map(iLoggingEvent -> tuple(
                            iLoggingEvent.getLevel(),
                            iLoggingEvent.getFormattedMessage()
                    ))
                    .toList()
            ).isEqualTo(levelStringMap);
        }
    }

    public static Stream<Arguments> convertIsoOffsetDateTimeToLocalDateTimeUTCData() {
        return Stream.of(
                Arguments.of("2011-12-03T10:15:30+01:00",
                        LocalDateTime.of(2011, 12, 3, 9, 15, 30),
                        Collections.emptyList()
                ),
                Arguments.of("",
                        null,
                        Collections.emptyList()
                ),
                Arguments.of(null,
                        null,
                        Collections.emptyList()
                ),
                Arguments.of(LocalDateTime.of(2026, 11, 11, 11, 11, 11).toString(),
                        LocalDateTime.of(2026, 11, 11, 11, 11, 11),
                        List.of(
                                tuple(Level.WARN, "Не удалось распарсить ISO_OFFSET_DATE_TIME формат. Text '2026-11-11T11:11:11' could not be parsed at index 19")
                        )
                ),
                Arguments.of("12.02.2026 23:30:08",
                        LocalDateTime.of(2026, 2, 12, 23, 30, 8),
                        List.of(
                                tuple(Level.WARN, "Не удалось распарсить ISO_OFFSET_DATE_TIME формат. Text '12.02.2026 23:30:08' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:ISO_LOCAL_DATE_TIME,error:Text '12.02.2026 23:30:08' could not be parsed at index 0")
                        )
                ),
                Arguments.of("12.02.2026 2:33:08",
                        LocalDateTime.of(2026, 2, 12, 2, 33, 8),
                        List.of(
                                tuple(Level.WARN, "Не удалось распарсить ISO_OFFSET_DATE_TIME формат. Text '12.02.2026 2:33:08' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:ISO_LOCAL_DATE_TIME,error:Text '12.02.2026 2:33:08' could not be parsed at index 0")
                        )
                ),
                Arguments.of("12.02.2026 2:3:08",
                        LocalDateTime.of(2026, 2, 12, 2, 3, 8),
                        List.of(
                                tuple(Level.WARN, "Не удалось распарсить ISO_OFFSET_DATE_TIME формат. Text '12.02.2026 2:3:08' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:ISO_LOCAL_DATE_TIME,error:Text '12.02.2026 2:3:08' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:mm:ss,error:Text '12.02.2026 2:3:08' could not be parsed at index 13")
                        )
                ),
                Arguments.of("12.02.2026 3:3:8",
                        LocalDateTime.of(2026, 2, 12, 3, 3, 8),
                        List.of(
                                tuple(Level.WARN, "Не удалось распарсить ISO_OFFSET_DATE_TIME формат. Text '12.02.2026 3:3:8' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:ISO_LOCAL_DATE_TIME,error:Text '12.02.2026 3:3:8' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:mm:ss,error:Text '12.02.2026 3:3:8' could not be parsed at index 13"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:m:ss,error:Text '12.02.2026 3:3:8' could not be parsed at index 15")
                        )
                ),
                Arguments.of("12.02.2026 23:3:08",
                        LocalDateTime.of(2026, 2, 12, 23, 3, 8),
                        List.of(
                                tuple(Level.WARN, "Не удалось распарсить ISO_OFFSET_DATE_TIME формат. Text '12.02.2026 23:3:08' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:ISO_LOCAL_DATE_TIME,error:Text '12.02.2026 23:3:08' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:mm:ss,error:Text '12.02.2026 23:3:08' could not be parsed at index 14")
                        )
                ),
                Arguments.of("12.02.2026 23:3:8",
                        LocalDateTime.of(2026, 2, 12, 23, 3, 8),
                        List.of(
                                tuple(Level.WARN, "Не удалось распарсить ISO_OFFSET_DATE_TIME формат. Text '12.02.2026 23:3:8' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:ISO_LOCAL_DATE_TIME,error:Text '12.02.2026 23:3:8' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:mm:ss,error:Text '12.02.2026 23:3:8' could not be parsed at index 14"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:m:ss,error:Text '12.02.2026 23:3:8' could not be parsed at index 16")
                        )
                ),
                Arguments.of("12.02.2026 23:30:8",
                        LocalDateTime.of(2026, 2, 12, 23, 30, 8),
                        List.of(
                                tuple(Level.WARN, "Не удалось распарсить ISO_OFFSET_DATE_TIME формат. Text '12.02.2026 23:30:8' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:ISO_LOCAL_DATE_TIME,error:Text '12.02.2026 23:30:8' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:mm:ss,error:Text '12.02.2026 23:30:8' could not be parsed at index 17"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:m:ss,error:Text '12.02.2026 23:30:8' could not be parsed at index 17")
                        )
                ),
                Arguments.of("12.02.2026 3:30:8",
                        LocalDateTime.of(2026, 2, 12, 3, 30, 8),
                        List.of(
                                tuple(Level.WARN, "Не удалось распарсить ISO_OFFSET_DATE_TIME формат. Text '12.02.2026 3:30:8' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:ISO_LOCAL_DATE_TIME,error:Text '12.02.2026 3:30:8' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:mm:ss,error:Text '12.02.2026 3:30:8' could not be parsed at index 16"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:m:ss,error:Text '12.02.2026 3:30:8' could not be parsed at index 16")
                        )
                ),
                Arguments.of("2.02.2026 13:30:18",
                        LocalDateTime.of(2026, 2, 2, 13, 30, 18),
                        List.of(
                                tuple(Level.WARN, "Не удалось распарсить ISO_OFFSET_DATE_TIME формат. Text '2.02.2026 13:30:18' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:ISO_LOCAL_DATE_TIME,error:Text '2.02.2026 13:30:18' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:mm:ss,error:Text '2.02.2026 13:30:18' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:m:ss,error:Text '2.02.2026 13:30:18' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:m:s,error:Text '2.02.2026 13:30:18' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy HH:m:ss,error:Text '2.02.2026 13:30:18' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy HH:m:s,error:Text '2.02.2026 13:30:18' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy HH:mm:s,error:Text '2.02.2026 13:30:18' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:mm:s,error:Text '2.02.2026 13:30:18' could not be parsed at index 0")
                        )
                ),
                Arguments.of("2.2.2026 13:30:18",
                        LocalDateTime.of(2026, 2, 2, 13, 30, 18),
                        List.of(
                                tuple(Level.WARN, "Не удалось распарсить ISO_OFFSET_DATE_TIME формат. Text '2.2.2026 13:30:18' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:ISO_LOCAL_DATE_TIME,error:Text '2.2.2026 13:30:18' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:mm:ss,error:Text '2.2.2026 13:30:18' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:m:ss,error:Text '2.2.2026 13:30:18' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:m:s,error:Text '2.2.2026 13:30:18' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy HH:m:ss,error:Text '2.2.2026 13:30:18' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy HH:m:s,error:Text '2.2.2026 13:30:18' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy HH:mm:s,error:Text '2.2.2026 13:30:18' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:mm:s,error:Text '2.2.2026 13:30:18' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:d.MM.yyyy H:mm:s,error:Text '2.2.2026 13:30:18' could not be parsed at index 2")
                        )
                ),
                Arguments.of("02.2.2026 13:30:18",
                        LocalDateTime.of(2026, 2, 2, 13, 30, 18),
                        List.of(
                                tuple(Level.WARN, "Не удалось распарсить ISO_OFFSET_DATE_TIME формат. Text '02.2.2026 13:30:18' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:ISO_LOCAL_DATE_TIME,error:Text '02.2.2026 13:30:18' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:mm:ss,error:Text '02.2.2026 13:30:18' could not be parsed at index 3"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:m:ss,error:Text '02.2.2026 13:30:18' could not be parsed at index 3"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:m:s,error:Text '02.2.2026 13:30:18' could not be parsed at index 3"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy HH:m:ss,error:Text '02.2.2026 13:30:18' could not be parsed at index 3"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy HH:m:s,error:Text '02.2.2026 13:30:18' could not be parsed at index 3"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy HH:mm:s,error:Text '02.2.2026 13:30:18' could not be parsed at index 3"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:mm:s,error:Text '02.2.2026 13:30:18' could not be parsed at index 3"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:d.MM.yyyy H:mm:s,error:Text '02.2.2026 13:30:18' could not be parsed at index 3")
                        )
                ),
                Arguments.of("02.2.26 13:30:18",
                        null,
                        List.of(
                                tuple(Level.WARN, "Не удалось распарсить ISO_OFFSET_DATE_TIME формат. Text '02.2.26 13:30:18' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:ISO_LOCAL_DATE_TIME,error:Text '02.2.26 13:30:18' could not be parsed at index 0"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:mm:ss,error:Text '02.2.26 13:30:18' could not be parsed at index 3"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:m:ss,error:Text '02.2.26 13:30:18' could not be parsed at index 3"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:m:s,error:Text '02.2.26 13:30:18' could not be parsed at index 3"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy HH:m:ss,error:Text '02.2.26 13:30:18' could not be parsed at index 3"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy HH:m:s,error:Text '02.2.26 13:30:18' could not be parsed at index 3"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy HH:mm:s,error:Text '02.2.26 13:30:18' could not be parsed at index 3"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.MM.yyyy H:mm:s,error:Text '02.2.26 13:30:18' could not be parsed at index 3"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:d.MM.yyyy H:mm:s,error:Text '02.2.26 13:30:18' could not be parsed at index 3"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:d.M.yyyy HH:mm:ss,error:Text '02.2.26 13:30:18' could not be parsed at index 5"),
                                tuple(Level.WARN, "Не удалось распарсить формат, dateTimeFormatter:dd.M.yyyy HH:mm:ss,error:Text '02.2.26 13:30:18' could not be parsed at index 5"),
                                tuple(Level.ERROR, "Не удалось распарсить ни в один формат, dateTimeString:02.2.26 13:30:18")
                        )
                )
        );
    }
}