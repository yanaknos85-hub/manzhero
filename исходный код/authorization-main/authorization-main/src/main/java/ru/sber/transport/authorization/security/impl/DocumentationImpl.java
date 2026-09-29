package ru.sber.transport.authorization.security.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import ru.sber.transport.authorization.security.NoAuthorized;
import ru.sber.transport.docstarter.controllers.DocumentationController;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@ConditionalOnClass(DocumentationController.class)
public class DocumentationImpl implements NoAuthorized {

    public DocumentationImpl() {
        log.debug("Documentation found and will not authorized");
    }

    @Override
    public Set<Map.Entry<HttpMethod, String>> getUrls() {
        return Set.of(new AbstractMap.SimpleEntry<>(HttpMethod.GET, "/documentation/**"));
    }

}
