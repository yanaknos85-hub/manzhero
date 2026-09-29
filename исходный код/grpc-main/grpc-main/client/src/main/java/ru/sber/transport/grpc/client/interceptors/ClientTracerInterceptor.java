package ru.sber.transport.grpc.client.interceptors;

import io.grpc.*;
import io.micrometer.tracing.Tracer;
import lombok.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import ru.sber.transport.grpc.common.marshallers.TracerMarshaller;

import java.util.Objects;

/**
 * Перехватчик запросов для подмешивания данных аутентификации.
 */
@Component
public class ClientTracerInterceptor implements ClientInterceptor, ApplicationContextAware {

    private ApplicationContext context;

    @Override
    public <S, T> ClientCall<S, T> interceptCall(MethodDescriptor<S, T> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<T> responseListener, Metadata headers) {
                var tracer = context.getBeanProvider(Tracer.class).getIfAvailable();
                if (tracer != null) {
                    var traceContext = tracer.currentTraceContext();
                    if (traceContext != null && traceContext.context() != null) {
                        headers.put(Metadata.Key.of("tracer", new TracerMarshaller()), Objects.requireNonNull(traceContext.context()));
                    }
                }
                super.start(responseListener, headers);
            }
        };
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
