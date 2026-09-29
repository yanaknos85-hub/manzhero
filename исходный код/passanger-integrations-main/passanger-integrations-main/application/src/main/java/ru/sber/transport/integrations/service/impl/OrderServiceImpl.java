package ru.sber.transport.integrations.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.sber.transport.integrations.aop.AutoAuthorization;
import ru.sber.transport.integrations.config.JsonApiProperties;
import ru.sber.transport.integrations.dto.*;
import ru.sber.transport.integrations.exception.ThreadInterruptedException;
import ru.sber.transport.integrations.feign.OrderClient;
import ru.sber.transport.integrations.service.AuthorizationService;
import ru.sber.transport.integrations.service.CredentialService;
import ru.sber.transport.integrations.service.OrderService;
import ru.sber.transport.integrations.service.ValidationResponseService;

import java.net.URI;
import java.util.List;

import static java.lang.Thread.sleep;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    
    private final ValidationResponseService validationResponseService;
    private final OrderClient orderClient;
    private final CredentialService credentialService;
    private final JsonApiProperties jsonApiProperties;
    private final AuthorizationService authService;
    
    // Количество попыток получения нового токена при ошибке 401
    private static final int MAX_NUMBER_OF_RETRY_ATTEMPTS = 10;
    
    // Пауза (мс) между попытками получения нового токена при ошибке 401
    private static final long PAUSE_BEFORE_RETRY_REQUEST = 1000L;
    
    //Значение заголовка x-transportation-type
    private static final String X_TRANSPORTATION_TYPE = "PASSENGER";
    
    @Override
    @AutoAuthorization
    public OrderResponse create(CredentialClient auth, OrderRequest request) {
        ResponseEntity<OrderResponse> response;
        boolean badToken;
        int numberOfRetryAttempts = 0;
        do {
            var token = credentialService.get(auth.getUri(), auth.getLogin());
            response = orderClient.createOrder(getRequestUri(auth),
                                            getTokenWithPrefix(token.token()),
                                            auth.getUri().toString(),
                                            X_TRANSPORTATION_TYPE,
                                            request);
            log.info("Received create order response, taxiId:{}, response:{}",
                     response.getBody() == null ? null : response.getBody().orderPartnerId(),
                     response);
            badToken = HttpStatus.UNAUTHORIZED.equals(response.getStatusCode());
            if (badToken) {
                forceAuthorization(auth);
                pause();
                numberOfRetryAttempts++;
            }
        } while (badToken && numberOfRetryAttempts <= MAX_NUMBER_OF_RETRY_ATTEMPTS);
        validateResponse(response,
                response.getBody() == null ? null : response.getBody().orderPartnerId(),
                auth.getLogin(),
                auth.getUri());
        return response.getBody();
    }
    
    @Override
    @AutoAuthorization
    public OrderInfoResponse info(CredentialClient auth, String orderPartnerID) {
        Token token;
        ResponseEntity<OrderInfoResponse> response;
        boolean badToken;
        int numberOfRetryAttempts = 0;
        do {
            token = credentialService.get(auth.getUri(), auth.getLogin());
            response = orderClient.orderInfo(getRequestUri(auth),
                    getTokenWithPrefix(token.token()),
                    auth.getUri().toString(),
                    X_TRANSPORTATION_TYPE,
                    orderPartnerID);
            log.info("Received info order response, taxiId:{}, response:{}",
                     response.getBody() == null ? null : response.getBody().getOrder().getOrderPartnerId(),
                     response);
            badToken = HttpStatus.UNAUTHORIZED.equals(response.getStatusCode());
            if (badToken) {
                forceAuthorization(auth);
                pause();
                numberOfRetryAttempts++;
            }
        } while (badToken && numberOfRetryAttempts <= MAX_NUMBER_OF_RETRY_ATTEMPTS);
        validateResponse(response, response.getBody() == null ? null : response.getBody().getOrder().getOrderPartnerId(), auth.getLogin(), auth.getUri());
        return response.getBody();
    }
    
    @Override
    @AutoAuthorization
    public CancelOrderResponse cancel(CredentialClient auth, String orderPartnerID) {
        ResponseEntity<CancelOrderResponse> response;
        boolean badToken;
        int numberOfRetryAttempts = 0;
        do {
            var token = credentialService.get(auth.getUri(), auth.getLogin());
            response = orderClient.cancel(getRequestUri(auth),
                                       getTokenWithPrefix(token.token()),
                                       auth.getUri().toString(),
                                       X_TRANSPORTATION_TYPE,
                                       orderPartnerID,
                                       orderPartnerID);
            log.info("Received cancel order response, taxiId:{}, response:{}",
                     response.getBody() == null ? null : response.getBody().getOrderPartnerId(),
                     response);
            badToken = HttpStatus.UNAUTHORIZED.equals(response.getStatusCode());
            if (badToken) {
                forceAuthorization(auth);
                pause();
                numberOfRetryAttempts++;
            }
        } while (badToken && numberOfRetryAttempts <= MAX_NUMBER_OF_RETRY_ATTEMPTS);
        validateResponse(response, response.getBody() == null ? null : response.getBody().getOrderPartnerId(), auth.getLogin(), auth.getUri());
        return response.getBody();
    }
    
    @Override
    @AutoAuthorization
    public OrdersLocationResponse getOrdersLocation(CredentialClient auth, List<String> orderPartnerIds) {
        var contractor = credentialService.get(auth.getUri(), auth.getLogin());
        log.info("Send car-location batch with request ids: {}, to contractor: {}", orderPartnerIds, auth.getUri());
        var response = orderClient.getOrdersLocation(getRequestUri(auth), getTokenWithPrefix(contractor.token()), auth.getUri().toString(),
                                                  new OrderPartnerIdsRequest(orderPartnerIds));
        log.debug("Response getOrdersLocation: {}", response);
        return response.getBody();
    }
    
    private static void pause() {
        try {
            sleep(PAUSE_BEFORE_RETRY_REQUEST);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new ThreadInterruptedException();
        }
    }
    
    private void forceAuthorization(CredentialClient auth) {
        try {
            authService.auth(auth);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
    
    private URI getRequestUri(CredentialClient credential) {
        if (jsonApiProperties.getRequestUrl() == null) {
            return credential.getUri();
        }
        return URI.create(jsonApiProperties.getRequestUrl());
    }
    
    private void validateResponse(ResponseEntity<?> response, String orderPartnerId, String login, URI uri) {
        try {
            log.info("Start validate response, taxiId:{}", orderPartnerId);
            validationResponseService.validate(response);
        } catch (Exception e) {
            log.error("Errors when validating response, uri:{}, login:{}", uri, login);
            throw e;
        }
    }
    
    private String getTokenWithPrefix(String token) {
        return "Bearer " + token;
    }
}
