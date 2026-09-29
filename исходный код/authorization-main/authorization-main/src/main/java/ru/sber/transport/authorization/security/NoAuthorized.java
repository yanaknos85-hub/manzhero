package ru.sber.transport.authorization.security;

import org.springframework.http.HttpMethod;

import java.util.Map;
import java.util.Set;

/**
 * Interface for defining methods for avoiding authorization.
 */
public interface NoAuthorized {

    /**
     * Get set of urls.
     *
     * @return set of urls.
     */
    Set<Map.Entry<HttpMethod, String>> getUrls();

}
