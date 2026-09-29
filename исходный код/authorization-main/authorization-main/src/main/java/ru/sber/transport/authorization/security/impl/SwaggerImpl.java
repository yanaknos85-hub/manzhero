package ru.sber.transport.authorization.security.impl;

import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.AbstractOpenApiResource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import ru.sber.transport.authorization.security.NoAuthorized;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@ConditionalOnClass(AbstractOpenApiResource.class)
public class SwaggerImpl implements NoAuthorized {

    public SwaggerImpl() {
        log.debug("Swagger found and will not authorized");
    }

    @Override
    public Set<Map.Entry<HttpMethod, String>> getUrls() {
        return Set.of(new AbstractMap.SimpleEntry<>(HttpMethod.GET, "/v3/api-docs/**"));
    }

}
