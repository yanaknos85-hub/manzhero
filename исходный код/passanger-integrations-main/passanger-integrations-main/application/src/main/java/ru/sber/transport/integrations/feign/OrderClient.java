package ru.sber.transport.integrations.feign;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.integrations.dto.*;

import java.net.URI;

@FeignClient(value = "order-client")
public interface OrderClient {
    
    /**
     * POST /orders/{orderParthnerID}/cancel : Отмена заказа у Контрагента
     *
     * @param contractorUrl (required)
     * @param authorization (required)
     * @param orderParthnerID (required)
     * @param body (optional)
     *
     * @return OK (status code 200)
     */
    @PostMapping(
            value = "/orders/{orderParthnerID}/cancel",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<CancelOrderResponse> cancel(
            URI contractorUrl,
            @RequestHeader(value = "Authorization") String authorization,
            @RequestHeader(value = "X-customer-url") String customerUrl,
            @RequestHeader(value = "x-transportation-type") String transportationType,
            @PathVariable("orderParthnerID") String orderParthnerID,
            @Valid @RequestBody(required = false) String body
                                              );
    
    
    /**
     * POST /orders : Создание заказа
     *
     * @param contractorUrl (required)
     * @param authorization (required)
     * @param orderRequest (required)
     *
     * @return OK (status code 200)
     */
    @PostMapping(
            value = "/orders",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<OrderResponse> createOrder(
            @RequestHeader(value = "contractorUrl") URI contractorUrl,
            @RequestHeader(value = "Authorization") String authorization,
            @RequestHeader(value = "X-customer-url") String customerUrl,
            @RequestHeader(value = "x-transportation-type") String transportationType,
            @Valid @RequestBody OrderRequest orderRequest
                                             );
    
    
    /**
     * GET /orders/{orderParthnerID} : Получение информации о заказе от Контрагента
     *
     * @param contractorUrl (required)
     * @param authorization (required)
     * @param orderParthnerID (required)
     *
     * @return OK (status code 200)
     */
    @GetMapping(
            value = "/orders/{orderParthnerID}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<OrderInfoResponse> orderInfo(
            @RequestHeader(value = "contractorUrl") URI contractorUrl,
            @RequestHeader(value = "Authorization") String authorization,
            @RequestHeader(value = "X-customer-url") String customerUrl,
            @RequestHeader(value = "x-transportation-type") String transportationType,
            @PathVariable("orderParthnerID") String orderParthnerID
                                               );
    
    
    /**
     * POST /orders/location/v1 : получение местоположение авто
     * @param contractorUrl (required)
     * @param authorization (required)
     * @param customerUrl (required)
     * @param request (required)
     * @return {@link OrdersLocationResponse}
     */
    @PostMapping(
            value = "/orders/location/v1",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<OrdersLocationResponse> getOrdersLocation(
            @RequestHeader(value = "contractorUrl") URI contractorUrl,
            @RequestHeader(value = "Authorization") String authorization,
            @RequestHeader(value = "X-customer-url") String customerUrl,
            @RequestBody OrderPartnerIdsRequest request
                                                            );
}
