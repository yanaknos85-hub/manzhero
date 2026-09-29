package ru.sber.transport.grpc.server.interceptors;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.Status;
import io.micrometer.tracing.CurrentTraceContext;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import io.qameta.allure.Feature;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.grpc.common.marshallers.TracerMarshaller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@IsolatedTest
@UnitTest
@Feature("lib_grpc_server")
@DisplayName("Внедрение трейсера")
class TracerInterceptorTest {

    @Test
    @DisplayName("Трейсер")
    void test() {
        var mockContext = mock(ApplicationContext.class);
        var tracer = mock(Tracer.class);
        var context = mock(CurrentTraceContext.class);

        var interceptor = new ServerTracerInterceptor();
        interceptor.setApplicationContext(mockContext);

        var metadata = new Metadata();
        var auth = new TraceContext() {

            @Override
            public String traceId() {
                return "trace";
            }

            @Override
            public String parentId() {
                return "parent";
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

        metadata.put(Metadata.Key.of("tracer", new TracerMarshaller()), auth);

        when(mockContext.getBeanProvider(Tracer.class)).thenReturn(new ObjectProvider<>() {
            @Override
            public @NonNull Tracer getObject(@NonNull Object... args) throws BeansException {
                return tracer;
            }

            @Override
            public Tracer getIfAvailable() throws BeansException {
                return tracer;
            }

            @Override
            public Tracer getIfUnique() throws BeansException {
                return tracer;
            }

            @Override
            public @NonNull Tracer getObject() throws BeansException {
                return tracer;
            }
        });
        when(tracer.currentTraceContext()).thenReturn(context);

        interceptor.interceptCall(new TestServerCall(metadata), metadata, (serverCall, metadata1) -> null);

        var traceCaptor = ArgumentCaptor.forClass(TraceContext.class);
        verify(context).newScope(traceCaptor.capture());

        assertThat(traceCaptor.getValue().traceId()).isEqualTo("trace");
        assertThat(traceCaptor.getValue().parentId()).isEqualTo("parent");
        assertThat(traceCaptor.getValue().spanId()).isEqualTo("span");
    }


    @RequiredArgsConstructor
    private static class TestServerCall extends ServerCall<Object, Object> {

        private final Metadata metadata;

        @Override
        public void request(int i) {
        }

        @Override
        public void sendHeaders(Metadata metadata) {
            metadata.merge(this.metadata);
        }

        @Override
        public void sendMessage(Object o) {
        }

        @Override
        public void close(Status status, Metadata metadata) {
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public MethodDescriptor<Object, Object> getMethodDescriptor() {
            return null;
        }
    }

}