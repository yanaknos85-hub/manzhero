package ru.sber.transport.grpc.server.interceptors;

import io.grpc.*;
import io.qameta.allure.Feature;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.grpc.common.marshallers.AuthenticationMarshaller;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@IsolatedTest
@UnitTest
@Feature("lib_grpc_server")
@DisplayName("Внедрение контекста безопасности")
class SecurityInterceptorTest {

    @Test
    @DisplayName("Безопасный запрос")
    void test_secured() {
        var interceptor = new ServerSecurityInterceptor();

        var metadata = new Metadata();
        var auth = new UsernamePasswordAuthenticationToken("test", "test", List.of(new SimpleGrantedAuthority("test")));
        metadata.put(Metadata.Key.of("authentication", new AuthenticationMarshaller()), auth);

        interceptor.interceptCall(new TestServerCall(metadata), metadata, (serverCall, metadata1) -> null);

        assertThat(SecurityContextHolder.getContext()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("test");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities()).hasSize(1);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority()).isEqualTo("test");
    }

    @Test
    @DisplayName("Небезопасный запрос")
    void test_non_secured() {
        var interceptor = new ServerSecurityInterceptor();

        var metadata = new Metadata();

        interceptor.interceptCall(new TestServerCall(metadata), metadata, (serverCall, metadata1) -> null);

        assertThat(SecurityContextHolder.getContext()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
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