package ru.sber.transport.grpc.server.interceptors;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.sber.transport.grpc.common.marshallers.AuthenticationMarshaller;

/**
 * Перехватчик, добавляющий принципал в запрос.
 */
@Slf4j
@GrpcGlobalServerInterceptor
public class ServerSecurityInterceptor implements ServerInterceptor {
    
    @Override
    public <R, S> ServerCall.Listener<R> interceptCall(
            ServerCall<R, S> call, Metadata headers, ServerCallHandler<R, S> next
                                                      ) {
        var header = headers.get(Metadata.Key.of("authentication", new AuthenticationMarshaller()));
        if (header != null) {
            log.debug("Handled authenticated request. User is {}", header);
            SecurityContextHolder.getContext().setAuthentication(header);
        } else {
            log.debug("Handled unauthenticated request");
        }
        return next.startCall(call, headers);
    }
    
}
