package ru.sber.transport.grpc.common.marshallers;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import static org.assertj.core.api.Assertions.assertThat;

@IsolatedTest
@UnitTest
@Feature("lib_grpc_common")
@DisplayName("Проверка маршаллера строки")
class StringMarshallerTest {

    @Test
    @DisplayName("Маршаллинг")
    void test_marshal() {
        var marshaller = new StringMarshaller();

        var marshalled = marshaller.toAsciiString("Какой-то не ASCII текст");
        var unmarshalled = marshaller.parseAsciiString(marshalled);

        assertThat(unmarshalled).isEqualTo("Какой-то не ASCII текст");
    }

}