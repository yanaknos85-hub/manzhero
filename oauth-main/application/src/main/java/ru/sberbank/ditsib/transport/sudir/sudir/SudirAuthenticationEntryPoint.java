package ru.sberbank.ditsib.transport.sudir.sudir;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Точка входа авторизации. Выполняется в случае если запрос требует авторизации.
 */
@Slf4j
@Component
public class SudirAuthenticationEntryPoint implements AuthenticationEntryPoint, RequestResultHandler {
    
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        log.error("Authentication failed", authException);
        var referer = getReferer(request, response).map(ref -> ref + "/oauth/failure");
        var callback = referer.map(ref -> String.format("%s#message=Unauthorized", ref)).orElse("/unauthorized");
        response.sendRedirect(callback);
    }
}
