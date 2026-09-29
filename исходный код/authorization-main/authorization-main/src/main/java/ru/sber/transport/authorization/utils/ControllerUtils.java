package ru.sber.transport.authorization.utils;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utils of controller data.
 */
@UtilityClass
public class ControllerUtils {

    /**
     * Получение данных текущего пользователя.
     *
     * @return uuid текущего пользователя.
     */
    public UUID currentUser() {
        if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken token) {
            return UUID.fromString(token.getToken().getId());
        }
        return null;
    }

    /**
     * Проверка, является ли текущий пользователь СМД.
     *
     * @return true если пользователь является СМД.
     */
    public boolean isDataMaster() {
        if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken token) {
            return Optional.ofNullable(token.getToken().getClaimAsBoolean("data_master")).orElse(false);
        }
        return false;
    }

    /**
     * Get annotation from method.
     *
     * @param method method.
     * @param annotationClass class of annotation.
     * @return annotation.
     * @param <A> type of annotation.
     */
    public <A extends Annotation> Optional<A> getAnnotation(Method method, Class<A> annotationClass) {
        if (method.isAnnotationPresent(annotationClass)) {
            return Optional.of(method.getAnnotation(annotationClass));
        }
        var controller = method.getDeclaringClass();
        for (var iFace : controller.getInterfaces()) {
            Method superMethod = null;
            try {
                superMethod = iFace.getDeclaredMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                // Игнорируем ошибку. Нет и нет.
            }
            if (superMethod != null) {
                return getAnnotation(superMethod, annotationClass);
            }
        }
        return Optional.empty();
    }

    /**
     * Get URL of method.
     *
     * @param method method to get URLs.
     * @return data of URLs.
     */
    public Set<Map.Entry<HttpMethod, String>> getUrl(Method method) {
        if (ControllerUtils.getAnnotation(method, GetMapping.class).isPresent()) {
            return getUrl(method, GetMapping.class, GetMapping::value, HttpMethod.GET);
        }
        if (ControllerUtils.getAnnotation(method, PostMapping.class).isPresent()) {
            return getUrl(method, PostMapping.class, PostMapping::value, HttpMethod.POST);
        }
        if (ControllerUtils.getAnnotation(method, DeleteMapping.class).isPresent()) {
            return getUrl(method, DeleteMapping.class, DeleteMapping::value, HttpMethod.DELETE);
        }
        if (ControllerUtils.getAnnotation(method, PutMapping.class).isPresent()) {
            return getUrl(method, PutMapping.class, PutMapping::value, HttpMethod.PUT);
        }
        if (ControllerUtils.getAnnotation(method, PatchMapping.class).isPresent()) {
            return getUrl(method, PatchMapping.class, PatchMapping::value, HttpMethod.PATCH);
        }
        return ControllerUtils.getAnnotation(method, RequestMapping.class)
            .map(requestMapping -> getUrl(method, RequestMapping.class, RequestMapping::value, Arrays.stream(requestMapping.method()).map(RequestMethod::asHttpMethod).toArray(HttpMethod[]::new)))
            .orElseGet(() -> Set.of(new AbstractMap.SimpleImmutableEntry<>(HttpMethod.GET, "/")));
    }

    /**
     * Get URL.
     *
     * @param method          method to get URL.
     * @param annotationClass class of annotation.
     * @param getUrlFunction  function to get URLs.
     * @param <A>             type of annotation.
     * @return URL.
     */
    private <A extends Annotation> Set<Map.Entry<HttpMethod, String>> getUrl(
        Method method, Class<A> annotationClass,
        Function<A, String[]> getUrlFunction,
        HttpMethod... httpMethods
    ) {
        var annotation = ControllerUtils.getAnnotation(method, annotationClass).orElse(null);
        var methodSet = Set.of(httpMethods);
        if (annotation != null && getUrlFunction.apply(annotation).length == 1) {
            var value = getUrlFunction.apply(annotation)[0];
            if (value == null) {
                value = "";
            }
            if (!value.startsWith("/")) {
                value = "/" + value;
            }
            var finalValue = value;
            return methodSet.parallelStream().map(httpMethod -> new AbstractMap.SimpleImmutableEntry<>(httpMethod, finalValue))
                .collect(Collectors.toUnmodifiableSet());
        } else {
            return methodSet.parallelStream().map(httpMethod -> new AbstractMap.SimpleImmutableEntry<>(httpMethod, "/")).collect(Collectors.toUnmodifiableSet());
        }
    }

}
