package ru.sber.transport.grpc.common.marshallers;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IsolatedTest
@UnitTest
@Feature("lib_grpc_common")
@DisplayName("Проверка маршаллера аутентификации")
class AuthenticationMarshallerTest {

    @Test
    @DisplayName("Маршаллинг")
    void test_marshal() {
        var marshalled = new AuthenticationMarshaller().toAsciiString(new UsernamePasswordAuthenticationToken("login", "password", List.of(new SimpleGrantedAuthority("ROLE"))));
        var unmarshalled = new AuthenticationMarshaller().parseAsciiString(marshalled);

        assertThat(unmarshalled).isInstanceOf(Authentication.class);
        assertThat(unmarshalled.getName()).isEqualTo("login");
        assertThat(unmarshalled.getAuthorities()).hasSize(1);
        assertThat(unmarshalled.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE");
    }

    @Test
    @DisplayName("Ошибка анмаршаллинга")
    void test_unmarshal_error() {
        var unmarshalled = new AuthenticationMarshaller().parseAsciiString("wrong header");

        assertThat(unmarshalled).isInstanceOf(Authentication.class);
        assertThat(unmarshalled.getName()).isEqualTo("system");
        assertThat(unmarshalled.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("Ошибка анмаршаллинга. Слишком много данных")
    void test_unmarshal_error_Too_long() {
        assertThatThrownBy(() -> new AuthenticationMarshaller().parseAsciiString("wrong|header|data"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Illegal authority header");
    }

}