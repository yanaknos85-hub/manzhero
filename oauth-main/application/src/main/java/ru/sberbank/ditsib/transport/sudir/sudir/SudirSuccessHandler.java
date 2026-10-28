package ru.sberbank.ditsib.transport.sudir.sudir;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.sudir.business.exceptions.AccountNotFoundException;
import ru.sberbank.ditsib.transport.sudir.business.Accounts;
import ru.sberbank.ditsib.transport.sudir.messaging.sender.UserAgentSender;
import ru.sberbank.ditsib.transport.sudir.model.AccountUserDetails;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.spec.InvalidKeySpecException;

/**
 * Перехватчик успешных авторизаций.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class SudirSuccessHandler implements AuthenticationSuccessHandler, RequestResultHandler {
    
    private final Accounts accounts;

    private final UserAgentSender userAgentSender;

    @SneakyThrows(InvalidKeySpecException.class)
    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest,
                                        HttpServletResponse httpServletResponse,
                                        Authentication authentication) throws IOException {
        log.debug("DEBUGSUDIR point 4");
        var principal = (AccountUserDetails) authentication.getPrincipal();
        var account = principal.getAccount();
        String result;
        var referer = getReferer(httpServletRequest, httpServletResponse);
        try {
            var token = accounts.login(account);
            log.debug("Authorized");
            var accessToken = token.getAccessToken();
            var refreshToken = token.getRefreshToken();
            result = referer.map(ref -> "%s/oauth/success#token=%s&refreshToken=%s".formatted(ref, accessToken, refreshToken))
                            .orElse("/success#token=%s&refreshToken=%s".formatted(accessToken, refreshToken));
        } catch (AccountNotFoundException e) {
            log.debug("Not authorized");
            result = referer.map(ref -> "%s/oauth/failure#personnelNumber=%s".formatted(ref, account.getPersonnelNumber()))
                            .orElse(("/failed" + "#personnelNumber=%s".formatted(account.getPersonnelNumber())));
        }
        if(httpServletRequest.getHeader("User-Agent")!=null && httpServletRequest.getHeader("x-client-type")!=null) {
            userAgentSender.send(account, httpServletRequest.getHeader("User-Agent"),
                    httpServletRequest.getHeader("x-client-type"), httpServletRequest.getRequestURL().toString());
        }
        httpServletResponse.sendRedirect(result);
    }
}