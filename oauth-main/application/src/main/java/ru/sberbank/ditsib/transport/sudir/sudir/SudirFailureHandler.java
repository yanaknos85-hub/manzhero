package ru.sberbank.ditsib.transport.sudir.sudir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Перехватчик ошибок авторизации от провайдера.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class SudirFailureHandler implements AuthenticationFailureHandler, RequestResultHandler {
    
    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e)
            throws IOException {
        log.error("Authentication failed", e);
        var referer = getReferer(httpServletRequest, httpServletResponse).map(ref -> ref + "/oauth/failure");
        var callback = referer.map(ref -> String.format("%s#message=Authentication unsuccessful", ref))
                                      .orElse("/failed");
        httpServletResponse.sendRedirect(callback);
    }
}