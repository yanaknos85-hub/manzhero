package ru.sberbank.ditsib.transport.sudir.sudir;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Интерфейс обработчика результатов запроса.
 */
interface RequestResultHandler {

    /**
     * Получение источника запроса. Источник определяется наличием заголовков <code>Referer</code> и <code>Forwarded</code>. Наличие заголовка
     * <code>Referer</code> говорит о том, что произошел редирект с оригинальной страницы и содержит в себе ее адрес. Заголовок
     * <code>Forwarded</code> должен присутствовать всегда из-за схемы работы OAuth и содержит в себе данные об источнике запроса. В приоритете
     * <code>Referer</code> так как он является самой дальней точкой запроса и часто указывает на истинный источник.
     *
     * @param httpServletRequest запрос.
     * @return источник запроса.
     */
    default Optional<String> getReferer(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        var referrers = httpServletRequest.getHeaders(HttpHeaders.REFERER);
        var referer = getValue(referrers);
        if (referer.isEmpty()) {
            referer = Optional.ofNullable(httpServletRequest.getCookies())
                    .stream()
                    .flatMap(Arrays::stream)
                    .parallel()
                    .filter(cookie -> cookie.getName().equals(CookieConstants.ORIGINAL_REFERER_ALTERNATIVE))
                    .map(Cookie::getValue)
                    .findAny()
                    .map(String::valueOf);

            var deletingCookie = new Cookie(CookieConstants.ORIGINAL_REFERER_ALTERNATIVE, null);
            deletingCookie.setHttpOnly(true);
            deletingCookie.setMaxAge(0);
            httpServletResponse.addCookie(deletingCookie);
        }
        if (referer.isEmpty()) {
            var forwarded = httpServletRequest.getHeaders("Forwarded");
            referer = getValue(forwarded).map(this::extractHost);
        }
        return referer.map(URI::create).map(uri -> "%s://%s:%s".formatted(uri.getScheme(), uri.getHost(), getPort(uri)));
    }

    private int getPort(URI uri) {
        var defaultPort = "https".equals(uri.getScheme()) ? 443 : 80;
        return uri.getPort() == -1 ? defaultPort : uri.getPort();
    }

    /**
     * Вытаскивает информацию о хосте из данных. За формат данных взята схема заголовка <code>Forwarded</code>, которая выглядит следующим образом:
     * <p>
     * <code>for={host};host={dns_name};proto={schema}</code><br />
     * </p>
     * <p>
     * где<br/>
     * for - IP-адрес источника запроса<br />
     * host - DNS-имя источника запроса<br />
     * proto - схема запроса (https, https или другая)<br />
     * </p>
     *
     * @param source исходные данные.
     * @return строка формата proto://host.
     */
    private String extractHost(String source) {
        var sourceMap = Arrays.stream(source.split(";"))
            .collect(Collectors.toMap(value -> value.split("=")[0], value -> value.split("=")[1]));
        var schema = sourceMap.getOrDefault("proto", "http");
        var host = sourceMap.get("host");
        if (host == null) {
            return null;
        }
        return String.format("%s://%s", schema.replace("/", ""), host);
    }

    /**
     * Получение значения. Берется первое непустое, так как в контексте запроса заголовок, как правило, имеет одно значение.
     *
     * @param requestData данные из запроса.
     * @return значение.
     */
    private Optional<String> getValue(Enumeration<String> requestData) {
        if (requestData.hasMoreElements()) {
            var referer = requestData.nextElement();
            if (StringUtils.hasText(referer)) {
                if (!referer.endsWith("/")) {
                    referer = referer + "/";
                }
                return Optional.of(referer);
            }
        }
        return Optional.empty();
    }
}
