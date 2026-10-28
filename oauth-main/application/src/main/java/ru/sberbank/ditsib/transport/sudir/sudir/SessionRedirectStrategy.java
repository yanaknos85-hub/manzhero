package ru.sberbank.ditsib.transport.sudir.sudir;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.sudir.sudir.providers.AllowUrlProvider;

import java.io.IOException;

@RequiredArgsConstructor
@Component
class SessionRedirectStrategy extends DefaultRedirectStrategy {

    private final AllowUrlProvider allowUrlProvider;

    @Override
    public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
        var referer = request.getHeader(HttpHeaders.REFERER);
        if (referer != null) {
            if (allowUrlProvider.allow(referer)) {
                var cookie = new Cookie(CookieConstants.ORIGINAL_REFERER_ALTERNATIVE, referer);
                var header = request.getHeader("x-envoy-original-path").split("/");
                var path = "/%s/%s/login/oauth2/code/sudir".formatted(header[1], header[2]);
                cookie.setPath(path);
                cookie.setSecure(true);
                cookie.setHttpOnly(true);
                response.addCookie(cookie);
            } else {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().println("""
                        {
                            "message": "Referer URL is not allowed for redirect"
                        }""");
                return;
            }
        }
        super.sendRedirect(request, response, url);
    }
}
