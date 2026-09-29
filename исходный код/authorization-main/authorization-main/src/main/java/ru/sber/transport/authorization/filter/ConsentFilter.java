package ru.sber.transport.authorization.filter;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import ru.sber.transport.authorization.annotations.SkipConsentCheck;
import ru.sber.transport.authorization.service.CheckConsentAccessService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Фильтр проверки доступа по факту подписания ПДн.
 */
@Slf4j
@ConditionalOnProperty(name = "authorization.consent-check", havingValue = "true")
@Order
@RequiredArgsConstructor
@Component
public class ConsentFilter extends OncePerRequestFilter {

    private final CheckConsentAccessService checkConsentAccessService;

    @Qualifier("requestMappingHandlerMapping")
    private final RequestMappingHandlerMapping handlerMappings;

    private final ApplicationContext context;

    private List<String> skipUrls;

    @PostConstruct
    private void init() {
        skipUrls = Arrays.stream(context.getBeanNamesForAnnotation(SkipConsentCheck.class))
                .map(k -> context.findAllAnnotationsOnBean(k, SkipConsentCheck.class, true))
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(SkipConsentCheck::value)
                .filter(Predicate.not(String::isBlank))
                .toList();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        if (skipUrlCheck(request.getRequestURI())
                || doesHaveAnnotation(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (checkConsentAccessService.check()) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private boolean doesHaveAnnotation(HttpServletRequest request) {
        var result = false;

        try {
            var handler = (HandlerMethod) Objects.requireNonNull(handlerMappings.getHandler(request)).getHandler();
            var skipConsentCheckAnnotation = handler.getMethodAnnotation(SkipConsentCheck.class);
            if (skipConsentCheckAnnotation != null) {
                result = true;
            }

            var clazz = handler.getClass();
            var skipAllAnnotation = clazz.getAnnotation(SkipConsentCheck.class);
            if (skipAllAnnotation != null) {
                result = true;
            }
        } catch (Exception ex) {
            logger.error("Error getting the mapping bean for the request URL " + request.getRequestURI(), ex);
        }

        return result;
    }

    private boolean skipUrlCheck(String url) {
        return skipUrls.stream().anyMatch(url::contains);
    }
}
