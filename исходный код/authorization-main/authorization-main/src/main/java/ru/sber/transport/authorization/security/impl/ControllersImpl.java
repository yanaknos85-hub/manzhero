package ru.sber.transport.authorization.security.impl;

import lombok.SneakyThrows;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.authorization.security.NoAuthorized;
import ru.sber.transport.authorization.utils.ControllerUtils;
import ru.sber.transport.http.request.check.annotations.NoAuthorize;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ControllersImpl implements NoAuthorized {

    private final Future<Set<Class<?>>> controllers;

    public ControllersImpl() {
        var types = new Reflections("ru", Scanners.TypesAnnotated);
        controllers = Executors.newSingleThreadExecutor().submit(() -> Stream.concat(
            types.getTypesAnnotatedWith(Controller.class).parallelStream(),
            types.getTypesAnnotatedWith(RestController.class).parallelStream()
        ).collect(Collectors.toUnmodifiableSet()));
    }

    @SneakyThrows({InterruptedException.class, ExecutionException.class})
    @Override
    public Set<Map.Entry<HttpMethod, String>> getUrls() {
        return controllers.get().parallelStream().map(this::getUrls)
            .flatMap(Collection::parallelStream)
            .collect(Collectors.toSet());
    }

    private Set<Map.Entry<HttpMethod, String>> getUrls(Class<?> controller) {
        var requestMapping = getAnnotation(controller, RequestMapping.class);
        var roots = new HashSet<>(Set.of(requestMapping.map(RequestMapping::value)
            .orElseGet(() -> requestMapping.map(RequestMapping::path).orElseGet(() -> new String[]{"/"}))));
        var noAuthorizedAllOpt = getAnnotation(controller, NoAuthorize.class);
        var customUrl = noAuthorizedAllOpt.map(NoAuthorize::value).orElse("");
        return findNoAuthorized(controller, roots, noAuthorizedAllOpt.isPresent() && customUrl.isBlank());
    }

    private Set<Map.Entry<HttpMethod, String>> findNoAuthorized(Class<?> controller, HashSet<String> roots, boolean noAuthorizedAll) {
        return Arrays.asList(controller.getDeclaredMethods())
            .parallelStream()
            .filter(m -> ControllerUtils.getAnnotation(m, NoAuthorize.class).isPresent() || noAuthorizedAll)
            .map(ControllerUtils::getUrl)
            .map(url -> getUrls(roots, url))
            .flatMap(Collection::parallelStream)
            .collect(Collectors.toSet());
    }

    private Set<Map.Entry<HttpMethod, String>> getUrls(Set<String> roots, Set<Map.Entry<HttpMethod, String>> source) {
        return source.parallelStream()
            .map(url -> {
                if (roots.isEmpty()) {
                    return appendUrl(url);
                } else {
                    return roots.parallelStream().map(root -> appendUrl(root.startsWith("/") ? root : "/" + root, url))
                        .flatMap(Collection::parallelStream)
                        .collect(Collectors.toUnmodifiableSet());
                }
            })
            .flatMap(Collection::parallelStream)
            .collect(Collectors.toUnmodifiableSet());
    }

    private Set<Map.Entry<HttpMethod, String>> appendUrl(Map.Entry<HttpMethod, String> url) {
        return appendUrl("", url);
    }

    private Set<Map.Entry<HttpMethod, String>> appendUrl(String root, Map.Entry<HttpMethod, String> url) {
        var result = new HashSet<Map.Entry<HttpMethod, String>>();
        var subUrl = url.getValue();
        var effectiveUrl = (root + subUrl).replace("//", "/");
        result.add(new AbstractMap.SimpleImmutableEntry<>(url.getKey(), effectiveUrl));
        if (!effectiveUrl.endsWith("**")) {
            result.add(new AbstractMap.SimpleImmutableEntry<>(url.getKey(), effectiveUrl.endsWith("/") ? effectiveUrl.substring(0, effectiveUrl.length() - 1) : effectiveUrl + "/"));
        }
        return result;
    }

    private <A extends Annotation> Optional<A> getAnnotation(Class<?> controller, Class<A> requestMappingClass) {
        if (controller.isAnnotationPresent(requestMappingClass)) {
            return Optional.of(controller.getAnnotation(requestMappingClass));
        }
        for (var iFace : controller.getInterfaces()) {
            var annotation = getAnnotation(iFace, requestMappingClass);
            if (annotation.isPresent()) {
                return annotation;
            }
        }
        return Optional.empty();
    }
}
