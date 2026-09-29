package ru.sberbank.ditsib.transport.limits.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.limits.controller.LimitTransferHistoryController;
import ru.sberbank.ditsib.transport.limits.controller.v2.EconomyController;
import ru.sberbank.ditsib.transport.limits.dto.EconomyDTO;
import ru.sberbank.ditsib.transport.limits.dto.GetLimitTransferHistoryDTO;
import ru.sberbank.ditsib.transport.limits.dto.LimitTransferHistoryDTO;
import ru.sberbank.ditsib.transport.limits.mapper.VersionConverter;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of limit controller service.
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@Scope("request")
class LimitTransferHistoryControllerImpl implements LimitTransferHistoryController {

    private final ru.sberbank.ditsib.transport.limits.controller.v2.LimitTransferHistoryController controller;

    private final EconomyController economyController;

    private final VersionConverter versionConverter;

    @Override
    public GetLimitTransferHistoryDTO add(
            LimitTransferHistoryDTO limitTransferHistoryDTO, JwtAuthenticationToken authentication
    ) {
        return versionConverter.toV1(controller.add(versionConverter.toV2(limitTransferHistoryDTO), authentication));
    }

    @Override
    public void delete(UUID limitTransferHistoryId, JwtAuthenticationToken authentication) {
        controller.delete(limitTransferHistoryId, authentication);
    }

    @Override
    public GetLimitTransferHistoryDTO get(UUID id) {
        return versionConverter.toV1(controller.get(id));
    }

    @Override
    public List<GetLimitTransferHistoryDTO> getAll() {
        return controller.getAll(0, Integer.MAX_VALUE, Sort.Direction.ASC, null, null)
                .stream().map(versionConverter::toV1).toList();
    }

    @Override
    public List<EconomyDTO> getTransfersToEconomy(Integer year, UUID limitId) {
        return economyController.getTransfersToEconomy(year, limitId).stream().map(versionConverter::toV1).toList();
    }

    @Override
    public List<GetLimitTransferHistoryDTO> getTransfersFromEconomy(Integer year, UUID limitId) {
        return economyController.getTransfersFromEconomy(year, limitId).stream().map(versionConverter::toV1).toList();
    }

    @Override
    public List<GetLimitTransferHistoryDTO> getTransfersByLimit(UUID limitId,
                                                                Integer year,
                                                                Integer maxRecords) {
        return controller.getAll(0 ,maxRecords, Sort.Direction.ASC, limitId, year).map(versionConverter::toV1)
                .getContent();
    }
}