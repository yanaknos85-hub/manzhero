package ru.sber.transport.integrations.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.sber.transport.integrations.config.ContractorBlockProperties;
import ru.sber.transport.integrations.dto.CredentialClient;
import ru.sber.transport.integrations.exception.CancelOrderException;
import ru.sber.transport.integrations.exception.CreateOrderException;
import ru.sber.transport.integrations.exception.OrderInfoException;
import ru.sber.transport.integrations.mapper.InContractorTaxiTripInProgressMessageMapper;
import ru.sber.transport.integrations.mapper.OrderRequestMapper;
import ru.sber.transport.integrations.messaging.sender.InContractorTaxiTripInProgressSender;
import ru.sber.transport.integrations.service.CommonApiProcessor;
import ru.sber.transport.integrations.service.ContractorCacheService;
import ru.sber.transport.integrations.service.OrderService;
import ru.sber.transport.request.messaging.OutContractorTaxiTripMessage;
import ru.sberbank.ditsib.transport.constants.external.taxi.OutboundRequestStatus;
import ru.sberbank.ditsib.transport.logging.utils.MDCUtil;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommonApiProcessorImpl implements CommonApiProcessor {
    private final OrderRequestMapper orderRequestMapper;
    private final OrderService orderService;
    private final InContractorTaxiTripInProgressSender inProgressSender;
    private final InContractorTaxiTripInProgressMessageMapper inContractorTaxiTripInProgressMessageMapper;
    @Qualifier(value = "commonApiQueue")
    private final Queue<Message<OutContractorTaxiTripMessage>> commonApiQueue;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ContractorBlockProperties contractorBlockProperties;
    private final ContractorCacheService contractorCacheService;

    @Override
    @Scheduled(cron = "${SCHEDULED_API:0/30 * * * * *}")
    public void processOutboundCommonApi() {
        if (!running.compareAndSet(false, true)) {
            log.info("processOutboundCommonApi: is already running");
            return;
        }
        log.info("processOutboundCommonApi: start");
        if (commonApiQueue.isEmpty()) {
            log.info("processOutboundCommonApi: commonApiQueue is empty");
            running.set(false);
            return;
        }
        while (!commonApiQueue.isEmpty()) {
            var message = commonApiQueue.poll();
            var payload = message.getPayload();
            var headers = message.getHeaders();
            if (payload.contractorId() != null) {
                if (contractorBlockProperties.isEnabled()) {
                    if (contractorCacheService.isAllowed(payload.contractorId())) {
                        submit(payload, headers);
                    } else {
                        log.info("processOutboundCommonApi: contractor ID {} is blocked, skipping message, taxiId:{}, tripId:{}",
                                payload.contractorId(), payload.taxiId(), payload.tripId());
                    }
                } else {
                    submit(payload, headers);
                }
            }
        }
        log.info("processOutboundCommonApi: end");
        running.set(false);
    }

    private void submit(OutContractorTaxiTripMessage payload, MessageHeaders headers) {
        try (var executorService = Executors.newFixedThreadPool(10)) {
            submit(executorService, payload, headers);
        } catch (Exception e) {
            log.error("processOutboundCommonApi: submit exception", e);
            Thread.currentThread().interrupt();
        }
    }

    private void submit(@NotNull ExecutorService executorService, OutContractorTaxiTripMessage payload, MessageHeaders headers) {
        var credentialClient = orderRequestMapper.toCredentialClient(payload);
        executorService.submit(() -> handleMessage(payload, credentialClient, headers));
    }

    private void handleMessage(OutContractorTaxiTripMessage message, CredentialClient credentialClient, MessageHeaders headers) {
        MDCUtil.putE2EHeadersToMDC(headers);
        try {
            switch (message.status()) {
                case OutboundRequestStatus.IN_PROGRESS -> orderInfo(message, credentialClient);
                case OutboundRequestStatus.REJECT -> cancelOrder(message, credentialClient);
                default -> createOrder(message, credentialClient);
            }
        } catch (Exception e) {
            log.error("handleMessage: error processing message for contractor ID {}, taxiId:{}, tripId:{}", message.contractorId(),
                    message.taxiId(), message.tripId(), e);
            if (contractorBlockProperties.isEnabled()) {
                contractorCacheService.markBlocked(message.contractorId());
            }
        }
    }

    private void createOrder(@NotNull OutContractorTaxiTripMessage outContractorTaxiTripMessage, CredentialClient credentialClient) {
        log.info("Start create order, taxiId:{}, tripId:{}",
                outContractorTaxiTripMessage.taxiId(),
                outContractorTaxiTripMessage.tripId());
        try {
            var orderRequest = orderRequestMapper.toOrderRequestDTO(outContractorTaxiTripMessage);
            var response = orderService.create(credentialClient, orderRequest);
            if (response.orderPartnerId() != null) {
                var responseMessage = inContractorTaxiTripInProgressMessageMapper
                        .outContractorTaxiTripMessageToInContractorTaxiTripInProgressMessage(outContractorTaxiTripMessage, response.orderPartnerId());
                inProgressSender.send(responseMessage);
                log.info("Finish create order, taxiId:{}, tripId:{}",
                        outContractorTaxiTripMessage.taxiId(),
                        outContractorTaxiTripMessage.tripId());
            } else {
                log.error("Create order, orderPartnerId is null, taxiId:{}", orderRequest.requestId());
            }
        } catch (Exception e) {
            var message = "Error while create order, taxiId:%s, tripId:%s".formatted(
                    outContractorTaxiTripMessage.taxiId(), outContractorTaxiTripMessage.tripId());
            log.error(message, e);
            throw new CreateOrderException(message);
        }
    }

    private void orderInfo(@NotNull OutContractorTaxiTripMessage outContractorTaxiTripMessage, CredentialClient credentialClient) {
        log.info("Start info order, taxiId:{}, tripId:{}",
                outContractorTaxiTripMessage.taxiId(),
                outContractorTaxiTripMessage.tripId());
        try {
            var response = orderService.info(credentialClient, outContractorTaxiTripMessage.taxiId());
            if (response.getOrder().getStatusCode() != null) {
                inProgressSender.send(inContractorTaxiTripInProgressMessageMapper.toInProgressMessage(response.getOrder(),
                        outContractorTaxiTripMessage.tripId(),
                        outContractorTaxiTripMessage.humanId(),
                        outContractorTaxiTripMessage.transportType()));
                log.info("Finish info order, taxiId:{}, tripId:{}",
                        outContractorTaxiTripMessage.taxiId(),
                        outContractorTaxiTripMessage.tripId());
            } else {
                log.error("Order info, statusCode is null, taxiId:{}, tripId:{}",
                        outContractorTaxiTripMessage.taxiId(),
                        outContractorTaxiTripMessage.tripId());
            }
        } catch (Exception e) {
            var message = "Error while info order, taxiId:%s, tripId:%s".formatted(
                    outContractorTaxiTripMessage.taxiId(), outContractorTaxiTripMessage.tripId());
            log.error(message, e);
            throw new OrderInfoException(message);
        }
    }

    private void cancelOrder(@NotNull OutContractorTaxiTripMessage outContractorTaxiTripMessage, CredentialClient credentialClient) {
        log.info("Start cancel order, taxiId:{}, tripId:{}",
                outContractorTaxiTripMessage.taxiId(),
                outContractorTaxiTripMessage.tripId());
        try {
            var response = orderService.cancel(credentialClient, outContractorTaxiTripMessage.taxiId());
            if (Boolean.TRUE.equals(response.getIsSuccess())) {
                orderInfo(outContractorTaxiTripMessage, credentialClient);
                log.info("Finish cancel order, taxiId:{}, tripId:{}",
                        outContractorTaxiTripMessage.taxiId(),
                        outContractorTaxiTripMessage.tripId());
            } else {
                log.error("Order cancel, response not success, taxiId {}, tripId:{}",
                        outContractorTaxiTripMessage.taxiId(),
                        outContractorTaxiTripMessage.tripId());
            }
        } catch (Exception e) {
            var message = "Error while cancel order, taxiId:%s, tripId:%s".formatted(
                    outContractorTaxiTripMessage.taxiId(), outContractorTaxiTripMessage.tripId());
            log.error(message, e);
            throw new CancelOrderException(message);
        }
    }
}
