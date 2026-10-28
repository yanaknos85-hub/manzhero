package ru.sber.transport.trip.registries.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.sber.transport.trip.registries.api.dto.ActNumberResponse;
import ru.sber.transport.trip.registries.api.dto.ActStatusResponse;
import ru.sber.transport.trip.registries.api.service.ApiService;
import ru.sber.transport.trip.registries.database.dao.RegistryRepository;
import ru.sber.transport.trip.registries.database.model.RegistryState;
import ru.sber.transport.trip.registries.messaging.senders.RegistrySender;
import ru.sber.transport.trip.registries.service.RegistryProcessor;

import java.util.List;
import java.util.Optional;

import static ru.sber.transport.trip.registries.database.model.RegistryState.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistryProcessorImpl implements RegistryProcessor {

    private final RegistryRepository registryRepository;
    private final ApiService apiService;
    private final RegistrySender registrySender;

    private static final List<RegistryState> PROCESS_SENT_SUCCESS_REGISTRIES_STATES = List.of(REGISTRY_SENT_SUCCESS);
    private static final List<RegistryState> PROCESS_ACT_NUMBER_RECEIVED_REGISTRIES_STATES = List.of(ACT_NUMBER_RECIEVED_SUCCESS);

    @Override
    @Scheduled(fixedDelayString = "${config.schedule.delay}", initialDelayString = "${config.schedule.delay}")
    public void processRegistries() {
        log.info("processRegistries: start");

        processActNumberReceivedRegistries();
        processSentSuccessRegistries();

        log.info("processRegistries: end");
    }

    private void processSentSuccessRegistries() {
        var registries = registryRepository.findByRegistryStateIn(PROCESS_SENT_SUCCESS_REGISTRIES_STATES);

        for (var registry : registries) {
            ActNumberResponse response = new ActNumberResponse();
            try {
                response = Optional.ofNullable(apiService.getActNumber(registry.getRegistryId()))
                        .orElse(response);
            } catch (Exception e) {
                registry.setLastError(e.toString());
            }

            if (StringUtils.isEmpty(response.getErrorType()) && response.getActId() != null) {
                registry.setActNumberError(null);
                registry.setActId(response.getActId());
                registry.setRegistryState(ACT_NUMBER_RECIEVED_SUCCESS.name());
            } else {
                registry.setActNumberError(response.getErrorType() + ";" + response.getErrorText());
                registry.setRegistryState(ACT_NUMBER_RECIEVING_FAILED.name());
            }
            registryRepository.save(registry);
            registrySender.send(registry);
        }

        log.info("processRegistries: sent success count: {}", registries.size());
    }

    private void processActNumberReceivedRegistries() {
        var registries = registryRepository.findByRegistryStateIn(PROCESS_ACT_NUMBER_RECEIVED_REGISTRIES_STATES);

        for (var registry : registries) {
            ActStatusResponse response = new ActStatusResponse();
            try {
                response = Optional.ofNullable(apiService.getActStatus(registry.getActId()))
                        .orElse(response);
            } catch (Exception e) {
                registry.setLastError(e.toString());
            }

            var updStatus = 0;
            if (StringUtils.isNotEmpty(response.getUpdStatus())) {
                updStatus = Integer.parseInt(response.getUpdStatus());
                registry.setUpdStatus(updStatus);
            }

            var lockingIndicator = 0;
            if (StringUtils.isNotEmpty(response.getLockingIndicator())) {
                lockingIndicator = Integer.parseInt(response.getLockingIndicator());
                registry.setLockingIndicator(lockingIndicator);
            }
            if (response.getUpdId() != null) {
                registry.setUpdId(response.getUpdId());
            }
            if (lockingIndicator == 3 && updStatus == 60) {
                registry.setRegistryState(REGISTRY_PROCESSED_FINISHED.name());
            }

            registrySender.send(registry);
            registryRepository.save(registry);
        }

        log.info("processRegistries: act number count: {}", registries.size());
    }
}
