package ru.sberbank.ditsib.transport.sudir.sudir.providers;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/**
 * Реализация {@link AuthorizationRequestRepository} которая хранит
 * {@link OAuth2AuthorizationRequest} в базе данных.
 */
public interface DatabaseOAuth2AuthorizationRequestProvider extends AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
}
