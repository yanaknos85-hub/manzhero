package ru.sber.transport.grpc.common.marshallers;

import io.micrometer.tracing.TraceContext;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import static org.assertj.core.api.Assertions.assertThat;

@IsolatedTest
@UnitTest
@Feature("lib_grpc_common")
@DisplayName("Проверка маршаллера трейсера")
class TracerMarshallerTest {

    @Test
    @DisplayName("Маршаллинг")
    void test_marshal() {
        var marshaller = new TracerMarshaller();

        var marshalled = marshaller.toAsciiString(createTracer());
        var unmarshalled = marshaller.parseAsciiString(marshalled);

        assertThat(unmarshalled.traceId()).isEqualTo("trace");
        assertThat(unmarshalled.parentId()).isEqualTo("parentSpan");
        assertThat(unmarshalled.spanId()).isEqualTo("span");
    }

    private TraceContext createTracer() {
        return new TraceContext() {
            @Override
            public String traceId() {
                return "trace";
            }

            @Override
            public String parentId() {
                return "parentSpan";
            }

            @Override
            public String spanId() {
                return "span";
            }

            @Override
            public Boolean sampled() {
                return false;
            }
        };
    }
}