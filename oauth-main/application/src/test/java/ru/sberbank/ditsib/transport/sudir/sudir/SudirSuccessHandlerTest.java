package ru.sberbank.ditsib.transport.sudir.sudir;

import io.qameta.allure.Feature;
import jakarta.servlet.ServletException;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.transport.sudir.business.model.Token;
import ru.sberbank.ditsib.transport.sudir.business.exceptions.AccountNotFoundException;
import ru.sberbank.ditsib.transport.sudir.business.Accounts;
import ru.sberbank.ditsib.transport.sudir.messaging.sender.UserAgentSender;
import ru.sberbank.ditsib.transport.sudir.model.AccountUserDetails;

import java.io.IOException;
import java.security.spec.InvalidKeySpecException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_oauth")
@DisplayName("Проверка хэндлера ответов")
class SudirSuccessHandlerTest {

    private final Accounts cases = mock(Accounts.class);

    private final UserAgentSender sender = mock(UserAgentSender.class);

    private final AuthenticationSuccessHandler handler = new SudirSuccessHandler(cases, sender);

    @Test
    @DisplayName("Успех")
    void test_success() throws InvalidKeySpecException, AccountNotFoundException, ServletException, IOException {
        var userDetails = Instancio.create(AccountUserDetails.class);
        var token = Instancio.create(Token.class);
        var authentication = new UsernamePasswordAuthenticationToken(userDetails, "");

        when(cases.login(userDetails.getAccount())).thenReturn(token);

        var request = MockMvcRequestBuilders.get("http://uri");
        request.header("User-Agent", "UserAgent");
        request.header("x-client-type", "ClientType");
        var response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(request.buildRequest(new MockServletContext()), response, authentication);

        verify(sender).send(userDetails.getAccount(), "UserAgent", "ClientType", "http://uri");

        assertThat(response.getHeader("Location")).isNotNull().endsWith("/success#token=%s&refreshToken=%s".formatted(token.getAccessToken(), token.getRefreshToken()));
    }

    @Test
    @DisplayName("Успех без заголовков")
    void test_success_no_headers() throws InvalidKeySpecException, AccountNotFoundException, ServletException, IOException {
        var userDetails = Instancio.create(AccountUserDetails.class);
        var token = Instancio.create(Token.class);
        var authentication = new UsernamePasswordAuthenticationToken(userDetails, "");

        when(cases.login(userDetails.getAccount())).thenReturn(token);

        var request = MockMvcRequestBuilders.get("http://uri");
        var response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(request.buildRequest(new MockServletContext()), response, authentication);

        verify(sender, never()).send(any(), any(), any(), any());

        assertThat(response.getHeader("Location")).isNotNull().endsWith("/success#token=%s&refreshToken=%s".formatted(token.getAccessToken(), token.getRefreshToken()));
    }

    @Test
    @DisplayName("Провал")
    void test_failure() throws InvalidKeySpecException, AccountNotFoundException, ServletException, IOException {
        var userDetails = Instancio.create(AccountUserDetails.class);
        var authentication = new UsernamePasswordAuthenticationToken(userDetails, "");

        when(cases.login(userDetails.getAccount())).thenThrow(new AccountNotFoundException(userDetails.getUsername()));

        var request = MockMvcRequestBuilders.get("http://uri");
        var response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(request.buildRequest(new MockServletContext()), response, authentication);

        verify(sender, never()).send(any(), any(), any(), any());

        assertThat(response.getHeader("Location")).isNotNull().endsWith("/failed#personnelNumber=%s".formatted(userDetails.getAccount().getPersonnelNumber()));
    }

}