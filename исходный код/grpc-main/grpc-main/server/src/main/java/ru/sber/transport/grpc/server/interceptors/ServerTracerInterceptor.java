package ru.sber.transport.grpc.server.interceptors;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.micrometer.tracing.Tracer;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import ru.sber.transport.grpc.common.marshallers.TracerMarshaller;

/**
 * Перехватчик трейсинга
 */
@Slf4j
@GrpcGlobalServerInterceptor
public class ServerTracerInterceptor implements ServerInterceptor, ApplicationContextAware {

    private ApplicationContext context;

    @Override
    public <R, S> ServerCall.Listener<R> interceptCall(ServerCall<R, S> call, Metadata headers, ServerCallHandler<R, S> next) {
        context.getBeanProvider(Tracer.class)
            .ifAvailable(tracer -> {
                var trace = headers.get(Metadata.Key.of("tracer", new TracerMarshaller()));
                tracer.currentTraceContext().newScope(trace);
            });
        return next.startCall(call, headers);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
