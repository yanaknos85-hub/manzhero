package ru.sber.transport.authorization.security.impl;

import lombok.SneakyThrows;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import ru.sber.transport.authorization.security.NoAuthorized;
import ru.sber.transport.http.request.check.annotations.NoAuthorize;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Component
public class AnnotationsImpl implements NoAuthorized {

    private final Future<Set<Class<?>>> noAuthorizes;

    private final Environment environment;

    public AnnotationsImpl(Environment environment) {
        this.environment = environment;
        var types = new Reflections("ru", Scanners.TypesAnnotated);
        noAuthorizes = Executors.newSingleThreadExecutor().submit(() -> types.getTypesAnnotatedWith(NoAuthorize.class));
    }

    @SneakyThrows({ExecutionException.class, InterruptedException.class})
    @Override
    public Set<Map.Entry<HttpMethod, String>> getUrls() {
        return noAuthorizes.get().parallelStream()
            .map(c -> c.getAnnotation(NoAuthorize.class))
            .filter(a -> !a.value().isBlank())
            .flatMap(v -> appendUrl(getUrl(environment, v.value())).parallelStream())
            .collect(Collectors.toSet());
    }

    private Map.Entry<HttpMethod, String> getUrl(Environment environment, String value) {
        value = extractProperty(environment, value);
        assert value != null;
        var httpMethod = HttpMethod.GET;
        if (value.contains(" ")) {
            httpMethod = HttpMethod.valueOf(value.split(" ")[0]);
            value = value.replace(httpMethod.name(), "").trim();
        }
        return new AbstractMap.SimpleImmutableEntry<>(httpMethod, value);
    }

    private String extractProperty(Environment environment, String value) {
        if (value.startsWith("${") && value.endsWith("}")) {
            var property = value.replace("${", "").replace("}", "");
            var properties = property.split(":");
            value = environment.getProperty(properties[0]);
            if (value == null && property.contains(":")) {
                value = properties[1];
            }
        }
        return value;
    }

    private Set<Map.Entry<HttpMethod, String>> appendUrl(Map.Entry<HttpMethod, String> url) {
        var result = new HashSet<Map.Entry<HttpMethod, String>>();
        var subUrl = url.getValue();
        var effectiveUrl = (subUrl).replace("//", "/");
        result.add(new AbstractMap.SimpleImmutableEntry<>(url.getKey(), effectiveUrl));
        if (!effectiveUrl.endsWith("**")) {
            result.add(new AbstractMap.SimpleImmutableEntry<>(url.getKey(), effectiveUrl.endsWith("/") ? effectiveUrl.substring(0, effectiveUrl.length() - 1) : effectiveUrl + "/"));
        }
        return result;
    }
}
