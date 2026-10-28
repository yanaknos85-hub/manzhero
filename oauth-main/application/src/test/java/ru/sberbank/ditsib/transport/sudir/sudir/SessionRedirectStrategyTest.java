package ru.sberbank.ditsib.transport.sudir.sudir;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.DefaultRedirectStrategy;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.transport.sudir.sudir.providers.AllowUrlProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_oauth")
@DisplayName("Проверка стратегии редиректа")
class SessionRedirectStrategyTest {

    private final AllowUrlProvider allowUrlProvider = mock(AllowUrlProvider.class);

    private final DefaultRedirectStrategy redirectStrategy = new SessionRedirectStrategy(allowUrlProvider);

    @Test
    @DisplayName("Нет реферера")
    void test_noReferer() throws IOException {
        var request = new MockHttpServletRequest(HttpMethod.HEAD.name(), "/");
        var response = mock(MockHttpServletResponse.class);
        var url = "/";

        when(response.encodeRedirectURL(any())).thenAnswer(inv -> inv.getArgument(0));

        redirectStrategy.sendRedirect(request, response, url);

        verify(response).sendRedirect(url);
    }

    @Test
    @DisplayName("Реферер разрешен")
    void test_referer_allowed() throws IOException {
        var request = new MockHttpServletRequest(HttpMethod.HEAD.name(), "/");
        request.addHeader(HttpHeaders.REFERER, "referer");
        request.addHeader("x-envoy-original-path", "/1/2/3");
        var response = mock(MockHttpServletResponse.class);
        var url = "/";

        when(allowUrlProvider.allow("referer")).thenReturn(true);
        when(response.encodeRedirectURL(any())).thenAnswer(inv -> inv.getArgument(0));

        redirectStrategy.sendRedirect(request, response, url);

        verify(response).sendRedirect(url);
    }

    @Test
    @DisplayName("Реферер запрещен")
    void test_referer_forbidden() throws IOException {
        var request = new MockHttpServletRequest(HttpMethod.HEAD.name(), "/");
        request.addHeader(HttpHeaders.REFERER, "referer");
        var response = mock(MockHttpServletResponse.class);
        var url = "/";
        try (var baos = new ByteArrayOutputStream();
             var printWriter = new PrintWriter(baos)) {

            when(allowUrlProvider.allow("referer")).thenReturn(false);
            when(response.getWriter()).thenReturn(printWriter);
            when(response.encodeRedirectURL(any())).thenAnswer(inv -> inv.getArgument(0));

            redirectStrategy.sendRedirect(request, response, url);

            verify(response, never()).sendRedirect(url);
            printWriter.flush();
            var content = baos.toString(StandardCharsets.UTF_8);
            assertThat(content).isEqualTo("""
                {
                    "message": "Referer URL is not allowed for redirect"
                }
                """);
        }
    }

}