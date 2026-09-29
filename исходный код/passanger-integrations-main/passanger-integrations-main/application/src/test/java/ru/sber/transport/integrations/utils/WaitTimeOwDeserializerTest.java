package ru.sber.transport.integrations.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.integrations.exception.WaitTimeOwParseException;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class WaitTimeOwDeserializerTest {
    @InjectMocks
    private WaitTimeOwDeserializer waitTimeOwDeserializer;
    @Mock
    private JsonParser jsonParser;
    @Mock
    private DeserializationContext deserializationContext;

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("deserializeData")
    void deserialize(String value, Integer result/*, List<Tuple> levelStringMap*/) {
        doReturn(value).when(jsonParser).getText();
        assertThat(waitTimeOwDeserializer.deserialize(jsonParser, deserializationContext)).isEqualTo(result);
    }

    @SneakyThrows
    @Test
    void deserializeException() {
        var value = "12:12:12:12";
        doReturn(value).when(jsonParser).getText();
        assertThatThrownBy(() -> waitTimeOwDeserializer.deserialize(jsonParser, deserializationContext))
                .isInstanceOf(WaitTimeOwParseException.class)
                .hasMessage("Cant parse waitTimeOw, value:" + value);
    }

    public static Stream<Arguments> deserializeData() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("", null),
                Arguments.of(" ", null),
                Arguments.of("00:00:00", 0),
                Arguments.of("23:59:59", 86399),
                Arguments.of("00:01:00", 60),
                Arguments.of("60", 60),
                Arguments.of("01:02:03", 3723),
                Arguments.of("01:02", 3720),
                Arguments.of("00:00", 0)
        );
    }
}