package ru.sber.transport.grpc.client.interceptors;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.testing.TestMethodDescriptors;
import io.micrometer.tracing.CurrentTraceContext;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import io.qameta.allure.Feature;
import lombok.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.grpc.common.marshallers.TracerMarshaller;

import javax.annotation.Nullable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@IsolatedTest
@UnitTest
@Feature("lib_grpc_client")
@DisplayName("Проверка интерцептора трассировки")
class TracerInterceptorTest {

    @Test
    @DisplayName("Перехват")
    void test_interceptCall() {
        var context = mock(ApplicationContext.class);
        var tracer = mock(Tracer.class);
        var currentContext = mock(CurrentTraceContext.class);
        var traceContext = new TraceContext() {

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
                return true;
            }
        };

        //noinspection NullableProblems
        when(context.getBeanProvider(Tracer.class)).thenReturn(new ObjectProvider<>() {
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
        when(tracer.currentTraceContext()).thenReturn(currentContext);
        when(currentContext.context()).thenReturn(traceContext);

        var interceptor = new ClientTracerInterceptor();
        interceptor.setApplicationContext(context);

        var channel = mock(Channel.class);
        var method = TestMethodDescriptors.voidMethod();

        var callOptions = CallOptions.DEFAULT;

        when(channel.newCall(any(), any())).thenReturn(new ClientCall<>() {
            @Override
            public void start(Listener<Object> responseListener, Metadata headers) {
            }

            @Override
            public void request(int numMessages) {
            }

            @Override
            public void cancel(@Nullable String message, @Nullable Throwable cause) {
            }

            @Override
            public void halfClose() {
            }

            @Override
            public void sendMessage(Object message) {
            }
        });

        var clientCall = interceptor.interceptCall(method, callOptions, channel);
        var actualMetadata = new Metadata();
        clientCall.start(new ClientCall.Listener<>() {
            @Override
            public void onHeaders(Metadata headers) {
                super.onHeaders(headers);
            }
        }, actualMetadata);

        var actualTrace = actualMetadata.get(Metadata.Key.of("tracer", new TracerMarshaller()));
        assertThat(actualTrace).isNotNull();
        assertThat(actualTrace.traceId()).isEqualTo("trace");
        assertThat(actualTrace.parentId()).isEqualTo("parent");
        assertThat(actualTrace.spanId()).isEqualTo("span");
    }

}