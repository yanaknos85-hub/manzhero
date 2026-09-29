package ru.sberbank.ditsib.transport.limits.controller.v2.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.controller.v2.EconomyController;
import ru.sberbank.ditsib.transport.limits.dto.v2.EconomyV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitTransferHistoryV2DTO;
import ru.sberbank.ditsib.transport.limits.mapper.LimitTransferHistoryMapper;
import ru.sberbank.ditsib.transport.limits.mapper.PeriodMapper;
import ru.sberbank.ditsib.transport.limits.model.limit.*;
import ru.sberbank.ditsib.transport.limits.service.DepLimitService;
import ru.sberbank.ditsib.transport.limits.service.LimitSharingPerPeriodService;
import ru.sberbank.ditsib.transport.limits.service.LimitSharingService;
import ru.sberbank.ditsib.transport.limits.service.LimitTransferHistoryService;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Scope("request")
class EconomyControllerV2Impl implements EconomyController {

    private final DepLimitService depLimitService;

    private final LimitTransferHistoryService limitTransferHistoryService;

    private final LimitSharingService limitSharingService;

    private final Map<LimitSharingType, LimitSharingPerPeriodService<? extends Period>> limitSharingPerPeriodServices;

    private final PeriodMapper periodMapper;

    private final LimitTransferHistoryMapper limitTransferHistoryMapper;

    @Override
    public List<EconomyV2DTO> getTransfersToEconomy(Integer year, UUID limitId) {
        var depLimit = depLimitService.get(limitId).orElseThrow(() -> new EntityNotFoundException(Limit.class, limitId));
        var result = new ArrayList<EconomyV2DTO>();

        for (var period : LimitSharingType.QUARTER.equals(depLimit.getLimitSharingType()) ? Quarter.values() : Month.values()) {
            var economy = getTransfersToEconomy(depLimit.getOrganization().getId(), depLimit.getLimitServiceType(), year, period);
            if (economy != null) {
                result.add(economy);
            }
        }
        return result;
    }

    @Override
    public List<GetLimitTransferHistoryV2DTO> getTransfersFromEconomy(Integer year, UUID limitId) {
        var list = limitTransferHistoryService.getHistoryTransfersFromEconomy(limitId, year);
        return list.stream().map(this::transformEntityToDTO).toList();
    }

    private EconomyV2DTO getTransfersToEconomy(UUID organizationId, String serviceType, Integer year, Period period) {
        var list = limitTransferHistoryService.getHistoryTransfersToEconomy(organizationId, serviceType, year, period);
        if (list.isEmpty()) {
            return null;
        }
        var limitTransferHistory1 = list.getFirst();

        var depLimit = depLimitService.get(limitTransferHistory1.getTargetLimitId());
        if (depLimit.isEmpty()) {
            log.error("getTransfersToEconomy: depLimit not found for id {}", limitTransferHistory1.getTargetLimitId());
            return null;
        }

        var department = depLimit.map(DepLimit::getDepartment).orElse(null);
        if (department == null) {
            log.error("getTransfersToEconomy: source department is null!");
            return null;
        }
        final var totalEconomyPeriod = list.stream().map(LimitTransferHistory::getSum).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);

        var limitSharingType = depLimit.get().getLimitSharingType();
        var limitSharing = limitSharingService.getByLimitAndTransportType(depLimit.get(), limitTransferHistory1.getTargetTransportType());
        if (limitSharing == null) {
            log.error("getTransfersToEconomy: limit sharing not found for limit id {} and transport type {}", depLimit.get(), limitTransferHistory1.getTargetTransportType());
            return null;
        }
        var limitSharingPerPeriodService = limitSharingPerPeriodServices.get(limitSharingType);
        var limitSharingPerPeriodListFull = limitSharingPerPeriodService.getByLimitSharing(limitSharing);
        var limitSharingPerPeriod = limitSharingPerPeriodListFull.get(period);

        var percent = BigDecimal.ZERO;
        var sum = Optional.ofNullable(limitSharingPerPeriod).map(LimitSharingPerPeriod::getSum).orElse(BigDecimal.ZERO);
        if (sum.compareTo(BigDecimal.ZERO) != 0) {
            percent = totalEconomyPeriod.multiply(BigDecimal.valueOf(100)).divide(sum, 2, RoundingMode.HALF_EVEN);
        }

        return new EconomyV2DTO(
                department.getDepartmentName(),
                periodMapper.toDto(period),
                totalEconomyPeriod,
                sum,
                limitTransferHistory1.getTargetTransportType(),
                percent.longValue()
        );
    }

    private GetLimitTransferHistoryV2DTO transformEntityToDTO(LimitTransferHistory limitTransferHistory) {
        return limitTransferHistoryMapper.toV2Dto(limitTransferHistory);
    }

}
