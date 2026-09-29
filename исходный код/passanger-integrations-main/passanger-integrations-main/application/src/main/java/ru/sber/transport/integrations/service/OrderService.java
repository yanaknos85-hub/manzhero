package ru.sber.transport.integrations.service;

import ru.sber.transport.integrations.dto.*;

import java.util.List;

public interface OrderService {
    
    /**
     * Отправка заявки контрагенту на создание заказа
     *
     * @param request
     * @param auth
     *
     * @return
     */
    OrderResponse create(CredentialClient auth, OrderRequest request);
    
    /**
     * Получение информации о заказе от Контрагента
     *
     * @param auth
     * @param orderPartnerID Идентификатор созданного заказа
     *
     * @return
     */
    OrderInfoResponse info(CredentialClient auth, String orderPartnerID);
    
    /**
     * Отмена заказа у Контрагента
     *
     * @param auth
     * @param orderPartnerID Идентификатор созданного заказа
     *
     * @return
     */
    CancelOrderResponse cancel(CredentialClient auth, String orderPartnerID);
    
    OrdersLocationResponse getOrdersLocation(CredentialClient auth, List<String> orderPartnerIds);
}
