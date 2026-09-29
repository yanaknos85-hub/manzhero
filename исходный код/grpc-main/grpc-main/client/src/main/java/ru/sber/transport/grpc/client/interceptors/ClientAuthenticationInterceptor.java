package ru.sber.transport.grpc.client.interceptors;

import io.grpc.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.sber.transport.grpc.common.marshallers.AuthenticationMarshaller;

/**
 * Перехватчик запросов для подмешивания данных аутентификации.
 */
@ConditionalOnClass(SecurityContextHolder.class)
@Component
public class ClientAuthenticationInterceptor implements ClientInterceptor {
    
    @Override
    public <S, T> ClientCall<S, T> interceptCall(MethodDescriptor<S, T> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<T> responseListener, Metadata headers) {
                var context = SecurityContextHolder.getContext();
                if (context != null) {
                    var authentication = context.getAuthentication();
                    if (authentication != null) {
                        headers.put(Metadata.Key.of("authentication", new AuthenticationMarshaller()), authentication);
                    }
                }
                super.start(responseListener, headers);
            }
        };
    }
}
