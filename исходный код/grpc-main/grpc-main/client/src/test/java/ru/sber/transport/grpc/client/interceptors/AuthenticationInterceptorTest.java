package ru.sber.transport.grpc.client.interceptors;

import io.grpc.*;
import io.grpc.testing.TestMethodDescriptors;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.grpc.common.marshallers.AuthenticationMarshaller;

import javax.annotation.Nullable;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@IsolatedTest
@UnitTest
@Feature("lib_grpc_client")
@DisplayName("Проверка интерцептора авторизации")
class AuthenticationInterceptorTest {

    @Test
    @DisplayName("Перехват")
    void test_interceptCall() {
        var interceptor = new ClientAuthenticationInterceptor();

        var channel = mock(Channel.class);
        var method = TestMethodDescriptors.voidMethod();

        var callOptions = CallOptions.DEFAULT;

        interceptor.interceptCall(method, callOptions, channel);

        var optionsCaptor = ArgumentCaptor.forClass(CallOptions.class);

        //noinspection unchecked
        verify(channel).newCall(any(MethodDescriptor.class), optionsCaptor.capture());

        assertThat(optionsCaptor.getValue()).isEqualTo(callOptions);
    }

    @Test
    @DisplayName("Перехват с авторизацией")
    void test_interceptCall_auth() {
        var authentication = new UsernamePasswordAuthenticationToken("name", "", List.of(new SimpleGrantedAuthority("Role")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var interceptor = new ClientAuthenticationInterceptor();

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

        var actualAuth = actualMetadata.get(Metadata.Key.of("authentication", new AuthenticationMarshaller()));
        assertThat(actualAuth).isNotNull();
        assertThat(actualAuth.getName()).isEqualTo("name");
        assertThat(actualAuth.getAuthorities()).hasSize(1);
        assertThat(actualAuth.getAuthorities().iterator().next().getAuthority()).isEqualTo("Role");
    }

}