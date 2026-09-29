package ru.sber.transport.integrations.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import ru.sber.transport.integrations.config.ContractorBlockProperties;
import ru.sber.transport.integrations.dto.*;
import ru.sber.transport.integrations.mapper.InContractorTaxiTripInProgressMessageMapper;
import ru.sber.transport.integrations.mapper.OrderRequestMapper;
import ru.sber.transport.integrations.messaging.InContractorTaxiTripInProgressMessage;
import ru.sber.transport.integrations.messaging.sender.InContractorTaxiTripInProgressSender;
import ru.sber.transport.integrations.service.ContractorCacheService;
import ru.sber.transport.integrations.service.OrderService;
import ru.sber.transport.request.messaging.OutContractorTaxiTripMessage;
import ru.sberbank.ditsib.transport.constants.external.taxi.OutboundRequestStatus;

import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommonApiProcessorImplTest {
    @Mock
    private OrderRequestMapper orderRequestMapper;
    @Mock
    private OrderService orderService;
    @Mock
    private InContractorTaxiTripInProgressSender inProgressSender;
    @Mock
    private InContractorTaxiTripInProgressMessageMapper inContractorTaxiTripInProgressMessageMapper;
    @Mock
    private Queue<OutContractorTaxiTripMessage> commonApiQueue;
    @Mock
    @SuppressWarnings("unused")
    private AtomicBoolean running;
    @Mock
    private ContractorBlockProperties contractorBlockProperties;
    @Mock
    private ContractorCacheService contractorCacheService;
    @InjectMocks
    private CommonApiProcessorImpl commonApiProcessor;

    @Test
    void processOutboundCommonApi() {
        var payloadNew = Instancio.of(OutContractorTaxiTripMessage.class)
                .set(field(OutContractorTaxiTripMessage::status), OutboundRequestStatus.NEW)
                .create();
        var payloadInProgress = Instancio.of(OutContractorTaxiTripMessage.class)
                .set(field(OutContractorTaxiTripMessage::status), OutboundRequestStatus.IN_PROGRESS)
                .create();
        var payloadReject = Instancio.of(OutContractorTaxiTripMessage.class)
                .set(field(OutContractorTaxiTripMessage::status), OutboundRequestStatus.REJECT)
                .create();
        var credentialClient = Instancio.create(CredentialClient.class);
        var orderRequest = Instancio.create(OrderRequest.class);
        var orderResponse = Instancio.create(OrderResponse.class);
        var orderInfoResponse1 = Instancio.create(OrderInfoResponse.class);
        var orderInfoResponse2 = Instancio.create(OrderInfoResponse.class);
        var cancelOrderResponse = Instancio.of(CancelOrderResponse.class)
                .set(field(CancelOrderResponse::getIsSuccess), Boolean.TRUE)
                .create();
        var responseMessage1 = Instancio.create(InContractorTaxiTripInProgressMessage.class);
        var responseMessage2 = Instancio.create(InContractorTaxiTripInProgressMessage.class);
        var responseMessage3 = Instancio.create(InContractorTaxiTripInProgressMessage.class);
        doReturn(false, false, false, false, true).when(commonApiQueue).isEmpty();
        doReturn(MessageBuilder.createMessage(payloadNew, new MessageHeaders(Collections.emptyMap())),
                MessageBuilder.createMessage(payloadInProgress, new MessageHeaders(Collections.emptyMap())),
                MessageBuilder.createMessage(payloadReject, new MessageHeaders(Collections.emptyMap()))).when(commonApiQueue).poll();
        doReturn(credentialClient).when(orderRequestMapper).toCredentialClient(payloadNew);
        doReturn(credentialClient).when(orderRequestMapper).toCredentialClient(payloadInProgress);
        doReturn(credentialClient).when(orderRequestMapper).toCredentialClient(payloadReject);
        doReturn(orderRequest).when(orderRequestMapper).toOrderRequestDTO(payloadNew);
        doReturn(orderResponse).when(orderService).create(credentialClient, orderRequest);
        doReturn(orderInfoResponse1).when(orderService).info(credentialClient, payloadInProgress.taxiId());
        doReturn(orderInfoResponse2).when(orderService).info(credentialClient, payloadReject.taxiId());
        doReturn(cancelOrderResponse).when(orderService).cancel(credentialClient, payloadReject.taxiId());
        doReturn(responseMessage1).when(inContractorTaxiTripInProgressMessageMapper)
                .outContractorTaxiTripMessageToInContractorTaxiTripInProgressMessage(payloadNew, orderResponse.orderPartnerId());
        doReturn(responseMessage2).when(inContractorTaxiTripInProgressMessageMapper)
                .toInProgressMessage(orderInfoResponse1.getOrder(),
                        payloadInProgress.tripId(),
                        payloadInProgress.humanId(),
                        payloadInProgress.transportType());
        doReturn(responseMessage3).when(inContractorTaxiTripInProgressMessageMapper)
                .toInProgressMessage(orderInfoResponse2.getOrder(),
                        payloadReject.tripId(),
                        payloadReject.humanId(),
                        payloadReject.transportType());
        doNothing().when(inProgressSender).send(responseMessage1);
        doNothing().when(inProgressSender).send(responseMessage2);
        doNothing().when(inProgressSender).send(responseMessage3);
        doReturn(true).when(contractorBlockProperties).isEnabled();
        doReturn(true).when(contractorCacheService).isAllowed(any());
        commonApiProcessor.processOutboundCommonApi();
        verify(inProgressSender).send(responseMessage1);
        verify(inProgressSender).send(responseMessage2);
        verify(inProgressSender).send(responseMessage3);
    }

    @Test
    void processOutboundCommonApiAlreadyRunning() {
        doReturn(true, false, false, false, true).when(commonApiQueue).isEmpty();
        commonApiProcessor.processOutboundCommonApi();
        verify(inProgressSender, never()).send(any());
    }

    @Test
    void processOutboundCommonApiIsEmpty() {
        doReturn(true).when(commonApiQueue).isEmpty();
        commonApiProcessor.processOutboundCommonApi();
        verify(inProgressSender, never()).send(any());
    }

    @Test
    void processOutboundCommonApiErrors() {
        var payloadNew = Instancio.of(OutContractorTaxiTripMessage.class)
                .set(field(OutContractorTaxiTripMessage::status), OutboundRequestStatus.NEW)
                .create();
        var payloadInProgress = Instancio.of(OutContractorTaxiTripMessage.class)
                .set(field(OutContractorTaxiTripMessage::status), OutboundRequestStatus.IN_PROGRESS)
                .create();
        var payloadReject = Instancio.of(OutContractorTaxiTripMessage.class)
                .set(field(OutContractorTaxiTripMessage::status), OutboundRequestStatus.REJECT)
                .create();
        doReturn(false, false, false, false, true).when(commonApiQueue).isEmpty();
        doReturn(MessageBuilder.createMessage(payloadNew, new MessageHeaders(Collections.emptyMap())),
                MessageBuilder.createMessage(payloadInProgress, new MessageHeaders(Collections.emptyMap())),
                MessageBuilder.createMessage(payloadReject, new MessageHeaders(Collections.emptyMap()))).when(commonApiQueue).poll();
        doReturn(true).when(contractorBlockProperties).isEnabled();
        commonApiProcessor.processOutboundCommonApi();
        verify(inProgressSender, never()).send(any());
    }

    @Test
    void processOutboundCommonApiContractorBlockDisabled() {
        doReturn(false).when(contractorBlockProperties).isEnabled();
        var payloadNew = Instancio.of(OutContractorTaxiTripMessage.class)
                .set(field(OutContractorTaxiTripMessage::status), OutboundRequestStatus.NEW)
                .set(field(OutContractorTaxiTripMessage::contractorId), "12345")
                .create();
        var credentialClient = Instancio.create(CredentialClient.class);
        var orderRequest = Instancio.create(OrderRequest.class);
        var orderResponse = Instancio.of(OrderResponse.class)
                .set(field(OrderResponse::orderPartnerId), "partner-123")
                .create();
        var responseMessage = Instancio.create(InContractorTaxiTripInProgressMessage.class);
        doReturn(false, false, true).when(commonApiQueue).isEmpty();
        doReturn(MessageBuilder.createMessage(payloadNew, new MessageHeaders(Collections.emptyMap()))).when(commonApiQueue).poll();
        doReturn(credentialClient).when(orderRequestMapper).toCredentialClient(payloadNew);
        doReturn(orderRequest).when(orderRequestMapper).toOrderRequestDTO(payloadNew);
        doReturn(orderResponse).when(orderService).create(credentialClient, orderRequest);
        doReturn(responseMessage).when(inContractorTaxiTripInProgressMessageMapper)
                .outContractorTaxiTripMessageToInContractorTaxiTripInProgressMessage(payloadNew, orderResponse.orderPartnerId());
        doNothing().when(inProgressSender).send(responseMessage);
        commonApiProcessor.processOutboundCommonApi();
        verify(inProgressSender).send(responseMessage);
        verify(contractorCacheService, never()).isAllowed(any());
    }

    @Test
    void processOutboundCommonApiBlockedContractorId() {
        var blockedContractorId = "blocked-contractor";
        var payloadNew = Instancio.of(OutContractorTaxiTripMessage.class)
                .set(field(OutContractorTaxiTripMessage::status), OutboundRequestStatus.NEW)
                .set(field(OutContractorTaxiTripMessage::contractorId), blockedContractorId)
                .create();
        doReturn(false, false, true).when(commonApiQueue).isEmpty();
        doReturn(MessageBuilder.createMessage(payloadNew, new MessageHeaders(Collections.emptyMap()))).when(commonApiQueue).poll();
        doReturn(true).when(contractorBlockProperties).isEnabled();
        doReturn(false).when(contractorCacheService).isAllowed(blockedContractorId);
        commonApiProcessor.processOutboundCommonApi();
        verify(inProgressSender, never()).send(any());
        verify(contractorCacheService).isAllowed(blockedContractorId);
    }

    @Test
    void processOutboundCommonApiNullContractorId() {
        var payloadNew = Instancio.of(OutContractorTaxiTripMessage.class)
                .set(field(OutContractorTaxiTripMessage::status), OutboundRequestStatus.NEW)
                .set(field(OutContractorTaxiTripMessage::contractorId), null)
                .create();
        doReturn(false, false, true).when(commonApiQueue).isEmpty();
        doReturn(MessageBuilder.createMessage(payloadNew, new MessageHeaders(Collections.emptyMap()))).when(commonApiQueue).poll();
        commonApiProcessor.processOutboundCommonApi();
        verify(inProgressSender, never()).send(any());
        verify(contractorCacheService, never()).isAllowed(any());
    }

    @Test
    void processOutboundCommonApiCreateOrderException() {
        var contractorId = "12345";
        var payloadNew = Instancio.of(OutContractorTaxiTripMessage.class)
                .set(field(OutContractorTaxiTripMessage::status), OutboundRequestStatus.NEW)
                .set(field(OutContractorTaxiTripMessage::contractorId), contractorId)
                .create();
        var credentialClient = Instancio.create(CredentialClient.class);
        var orderRequest = Instancio.create(OrderRequest.class);
        doReturn(false, false, true).when(commonApiQueue).isEmpty();
        doReturn(MessageBuilder.createMessage(payloadNew, new MessageHeaders(Collections.emptyMap()))).when(commonApiQueue).poll();
        doReturn(credentialClient).when(orderRequestMapper).toCredentialClient(payloadNew);
        doReturn(orderRequest).when(orderRequestMapper).toOrderRequestDTO(payloadNew);
        doThrow(new RuntimeException("External service error")).when(orderService).create(credentialClient, orderRequest);
        doReturn(true).when(contractorBlockProperties).isEnabled();
        doReturn(true).when(contractorCacheService).isAllowed(contractorId);
        commonApiProcessor.processOutboundCommonApi();
        verify(inProgressSender, never()).send(any());
        verify(contractorCacheService).markBlocked(contractorId);
    }

    @Test
    void processOutboundCommonApiOrderInfoException() {
        var contractorId = "12345";
        var payloadInProgress = Instancio.of(OutContractorTaxiTripMessage.class)
                .set(field(OutContractorTaxiTripMessage::status), OutboundRequestStatus.IN_PROGRESS)
                .set(field(OutContractorTaxiTripMessage::contractorId), contractorId)
                .create();
        var credentialClient = Instancio.create(CredentialClient.class);
        doReturn(false, false, true).when(commonApiQueue).isEmpty();
        doReturn(MessageBuilder.createMessage(payloadInProgress, new MessageHeaders(Collections.emptyMap()))).when(commonApiQueue).poll();
        doReturn(credentialClient).when(orderRequestMapper).toCredentialClient(payloadInProgress);
        doThrow(new RuntimeException("Info service error")).when(orderService).info(credentialClient, payloadInProgress.taxiId());
        doReturn(true).when(contractorBlockProperties).isEnabled();
        doReturn(true).when(contractorCacheService).isAllowed(contractorId);
        commonApiProcessor.processOutboundCommonApi();
        verify(inProgressSender, never()).send(any());
        verify(contractorCacheService).markBlocked(contractorId);
    }

    @Test
    void processOutboundCommonApiCancelOrderException() {
        var contractorId = "12345";
        var payloadReject = Instancio.of(OutContractorTaxiTripMessage.class)
                .set(field(OutContractorTaxiTripMessage::status), OutboundRequestStatus.REJECT)
                .set(field(OutContractorTaxiTripMessage::contractorId), contractorId)
                .create();
        var credentialClient = Instancio.create(CredentialClient.class);
        doReturn(false, false, true).when(commonApiQueue).isEmpty();
        doReturn(MessageBuilder.createMessage(payloadReject, new MessageHeaders(Collections.emptyMap()))).when(commonApiQueue).poll();
        doReturn(credentialClient).when(orderRequestMapper).toCredentialClient(payloadReject);
        doThrow(new RuntimeException("Cancel service error")).when(orderService).cancel(credentialClient, payloadReject.taxiId());
        doReturn(true).when(contractorBlockProperties).isEnabled();
        doReturn(true).when(contractorCacheService).isAllowed(contractorId);
        commonApiProcessor.processOutboundCommonApi();
        verify(inProgressSender, never()).send(any());
        verify(contractorCacheService).markBlocked(contractorId);
    }

    @Test
    void processOutboundCommonApiMessageWithoutContractorId() {
        var payloadNew = Instancio.of(OutContractorTaxiTripMessage.class)
                .set(field(OutContractorTaxiTripMessage::status), OutboundRequestStatus.NEW)
                .set(field(OutContractorTaxiTripMessage::contractorId), null)
                .create();
        doReturn(false, false, true).when(commonApiQueue).isEmpty();
        doReturn(MessageBuilder.createMessage(payloadNew, new MessageHeaders(Collections.emptyMap()))).when(commonApiQueue).poll();
        commonApiProcessor.processOutboundCommonApi();
        verify(inProgressSender, never()).send(any());
        verify(orderRequestMapper, never()).toCredentialClient(any());
        verify(contractorCacheService, never()).isAllowed(any());
    }
}
