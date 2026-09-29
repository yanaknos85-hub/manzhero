package ru.sber.transport.integrations.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.JUnitException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.sber.transport.integrations.config.JsonApiProperties;
import ru.sber.transport.integrations.dto.*;
import ru.sber.transport.integrations.exception.RequestException;
import ru.sber.transport.integrations.feign.OrderClient;
import ru.sber.transport.integrations.service.AuthorizationService;
import ru.sber.transport.integrations.service.CredentialService;
import ru.sber.transport.integrations.service.ValidationResponseService;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    
    @Mock
    private CredentialService credentialService;
    @Mock
    private OrderClient orderClient;
    @Mock
    private JsonApiProperties jsonApiProperties;
    @Mock
    private ValidationResponseService validationResponseService;
    @Mock
    private AuthorizationService authorizationService;
    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void create() {
        var credentialClient = Instancio.create(CredentialClient.class);
        var credentialClientUnauthorized = Instancio.create(CredentialClient.class);
        var orderRequest = Instancio.create(OrderRequest.class);
        var orderResponse = Instancio.create(OrderResponse.class);
        var responseUnauthorized = ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(orderResponse);
        var responseOk = ResponseEntity
                .status(HttpStatus.OK)
                .body(orderResponse);
        var responseNullBody = ResponseEntity
                .status(HttpStatus.OK)
                .body(null);
        var responseNotValidated = ResponseEntity
                .status(HttpStatus.OK)
                .body(Instancio.create(OrderResponse.class));
        var exceptionMessage = responseNotValidated.getStatusCode() + " Error. Response: " + responseNotValidated.getBody();
        doNothing().when(authorizationService).auth(credentialClient);
        doThrow(JUnitException.class).when(authorizationService).auth(credentialClientUnauthorized);
        doReturn(null, Instancio.create(String.class)).when(jsonApiProperties).getRequestUrl();
        doNothing().when(validationResponseService).validate(responseOk);
        doNothing().when(validationResponseService).validate(responseNullBody);
        doThrow(new RequestException(exceptionMessage)).when(validationResponseService).validate(responseNotValidated);
        doReturn(Instancio.create(Token.class)).when(credentialService).get(credentialClient.getUri(), credentialClient.getLogin());
        doReturn(Instancio.create(Token.class)).when(credentialService).get(credentialClientUnauthorized.getUri(), credentialClientUnauthorized.getLogin());
        doReturn(responseUnauthorized,
                responseOk,
                responseNullBody,
                responseNotValidated,
                responseUnauthorized).when(orderClient).createOrder(
                any(URI.class),
                anyString(),
                anyString(),
                eq("PASSENGER"),
                eq(orderRequest)
                );
        assertThat(orderService.create(credentialClient, orderRequest))
                .usingRecursiveComparison()
                .isEqualTo(responseOk.getBody());
        assertThat(orderService.create(credentialClient, orderRequest))
                .usingRecursiveComparison()
                .isEqualTo(responseNullBody.getBody());
        assertThatExceptionOfType(Exception.class)
                .isThrownBy(() -> orderService.create(credentialClient, orderRequest))
                .withMessage(exceptionMessage);
        assertThat(orderService.create(credentialClientUnauthorized, orderRequest))
                .usingRecursiveComparison()
                .isEqualTo(responseUnauthorized.getBody());
    }

    @Test
    void info() {
        var credentialClient = Instancio.create(CredentialClient.class);
        var credentialClientUnauthorized = Instancio.create(CredentialClient.class);
        var orderPartnerId = Instancio.create(String.class);
        var orderResponse = Instancio.create(OrderInfoResponse.class);
        var responseUnauthorized = ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(orderResponse);
        var responseOk = ResponseEntity
                .status(HttpStatus.OK)
                .body(orderResponse);
        var responseNullBody = ResponseEntity
                .status(HttpStatus.OK)
                .body(null);
        var responseNotValidated = ResponseEntity
                .status(HttpStatus.OK)
                .body(Instancio.create(OrderInfoResponse.class));
        var exceptionMessage = responseNotValidated.getStatusCode() + " Error. Response: " + responseNotValidated.getBody();
        doNothing().when(authorizationService).auth(credentialClient);
        doThrow(JUnitException.class).when(authorizationService).auth(credentialClientUnauthorized);
        doReturn(null,
                Instancio.create(String.class),
                Instancio.create(String.class)).when(jsonApiProperties).getRequestUrl();
        doNothing().when(validationResponseService).validate(responseOk);
        doNothing().when(validationResponseService).validate(responseNullBody);
        doThrow(new RequestException(exceptionMessage)).when(validationResponseService).validate(responseNotValidated);
        doReturn(Instancio.create(Token.class)).when(credentialService).get(credentialClient.getUri(), credentialClient.getLogin());
        doReturn(Instancio.create(Token.class)).when(credentialService).get(credentialClientUnauthorized.getUri(), credentialClientUnauthorized.getLogin());
        doReturn(responseUnauthorized,
                responseOk,
                responseNullBody,
                responseNotValidated,
                responseUnauthorized).when(orderClient).orderInfo(
                any(URI.class),
                anyString(),
                anyString(),
                eq("PASSENGER"),
                eq(orderPartnerId)
        );
        assertThat(orderService.info(credentialClient, orderPartnerId))
                .usingRecursiveComparison()
                .isEqualTo(responseOk.getBody());
        assertThat(orderService.info(credentialClient, orderPartnerId))
                .usingRecursiveComparison()
                .isEqualTo(responseNullBody.getBody());
        assertThatExceptionOfType(Exception.class)
                .isThrownBy(() -> orderService.info(credentialClient, orderPartnerId))
                .withMessage(exceptionMessage);
        assertThat(orderService.info(credentialClientUnauthorized, orderPartnerId))
                .usingRecursiveComparison()
                .isEqualTo(responseUnauthorized.getBody());
    }

    @Test
    void cancel() {
        var credentialClient = Instancio.create(CredentialClient.class);
        var credentialClientUnauthorized = Instancio.create(CredentialClient.class);
        var orderPartnerId = Instancio.create(String.class);
        var orderResponse = Instancio.create(CancelOrderResponse.class);
        var responseUnauthorized = ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(orderResponse);
        var responseOk = ResponseEntity
                .status(HttpStatus.OK)
                .body(orderResponse);
        var responseNullBody = ResponseEntity
                .status(HttpStatus.OK)
                .body(null);
        var responseNotValidated = ResponseEntity
                .status(HttpStatus.OK)
                .body(Instancio.create(CancelOrderResponse.class));
        var exceptionMessage = responseNotValidated.getStatusCode() + " Error. Response: " + responseNotValidated.getBody();
        doNothing().when(authorizationService).auth(credentialClient);
        doThrow(JUnitException.class).when(authorizationService).auth(credentialClientUnauthorized);
        doReturn(null, Instancio.create(String.class)).when(jsonApiProperties).getRequestUrl();
        doNothing().when(validationResponseService).validate(responseOk);
        doNothing().when(validationResponseService).validate(responseNullBody);
        doThrow(new RequestException(exceptionMessage)).when(validationResponseService).validate(responseNotValidated);
        doReturn(Instancio.create(Token.class)).when(credentialService).get(credentialClient.getUri(), credentialClient.getLogin());
        doReturn(Instancio.create(Token.class)).when(credentialService).get(credentialClientUnauthorized.getUri(), credentialClientUnauthorized.getLogin());
        doReturn(responseUnauthorized,
                responseOk,
                responseNullBody,
                responseNotValidated,
                responseUnauthorized).when(orderClient).cancel(
                any(URI.class),
                anyString(),
                anyString(),
                eq("PASSENGER"),
                eq(orderPartnerId),
                eq(orderPartnerId)
        );
        assertThat(orderService.cancel(credentialClient, orderPartnerId))
                .usingRecursiveComparison()
                .isEqualTo(responseOk.getBody());
        assertThat(orderService.cancel(credentialClient, orderPartnerId))
                .usingRecursiveComparison()
                .isEqualTo(responseNullBody.getBody());
        assertThatExceptionOfType(Exception.class)
                .isThrownBy(() -> orderService.cancel(credentialClient, orderPartnerId))
                .withMessage(exceptionMessage);
        assertThat(orderService.cancel(credentialClientUnauthorized, orderPartnerId))
                .usingRecursiveComparison()
                .isEqualTo(responseUnauthorized.getBody());
    }

    @Test
    void getOrdersLocation() {
        var auth = new CredentialClient(URI.create("http://test-api-contractor.ru"),
                                        "login",
                                        "password");
        var orderPartnerIds = List.of("firstOrderPartnerId", "secondOrderPartnerId");
        var token = new Token("token", null, null, null, null);
        var response = new OrdersLocationResponse(
                List.of(
                        new OrdersLocationResponse.OrderLocation(
                                "firstOrderPartnerId",
                                new OrdersLocationResponse.OrderCoordinates(
                                        10.0003,
                                        10.0004
                                ),
                                3600
                        ),
                        new OrdersLocationResponse.OrderLocation(
                                "secondOrderPartnerId",
                                new OrdersLocationResponse.OrderCoordinates(
                                        10.0005,
                                        10.0006
                                ),
                                1200
                        )
                       )
        );
        doReturn(token).when(credentialService).get(auth.getUri(), auth.getLogin());
        doReturn(null).when(jsonApiProperties).getRequestUrl();
        
        doReturn(ResponseEntity.of(Optional.of(response)))
                .when(orderClient).getOrdersLocation(auth.getUri(), "Bearer " + token.token(), auth.getUri().toString(),
                                                     new OrderPartnerIdsRequest(orderPartnerIds));
        
        var result = orderService.getOrdersLocation(auth, orderPartnerIds);
        assertThat(result).isNotNull();
        var orderLocationsResult = result.orderLocations();
        assertThat(orderLocationsResult).hasSize(2)
                                        .usingRecursiveComparison()
                                        .isEqualTo(response.orderLocations()
                                                  );
    }
}
