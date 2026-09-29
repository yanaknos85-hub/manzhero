package ru.sberbank.ditsib.transport.approvals.services.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.approvals.database.model.TripPurpose;
import ru.sberbank.ditsib.transport.approvals.dto.TripApproveDTO;
import ru.sberbank.ditsib.transport.approvals.messaging.resolvers.LimitsDataResolver;
import ru.sberbank.ditsib.transport.approvals.services.ApproveControllerService;
import ru.sberbank.ditsib.transport.approvals.services.TripPurposeService;
import ru.sberbank.ditsib.transport.constants.limits.LimitServiceType;
import ru.sberbank.ditsib.transport.dto.limits.GetLimitDTO;
import ru.sberbank.ditsib.transport.dto.limits.GetLimitSharingDTO;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of controller service.
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class ApproveControllerServiceImpl implements ApproveControllerService {

    private final TripPurposeService tripPurposeService;
    private final LimitsDataResolver limitsDataResolver;

    @Override
    public <A extends TripApproveDTO> Collection<A> fillTripPurpose(Collection<A> dtoList) {
        var idList = dtoList.stream()
                .map(TripApproveDTO::getPurposeId)
                .collect(Collectors.toSet());
        List<TripPurpose> purposes = tripPurposeService.getByIds(idList);
        final Map<UUID, TripPurpose> map = purposes.stream()
                .collect(Collectors.toMap(TripPurpose::getId, Function.identity()));
        dtoList.forEach(dto -> dto.setPurposeLabel(getTripPurposeLabel(map, dto)));
        return dtoList;
    }

    @Override
    public <E extends TripApproveDTO> Collection<E> fillLimits(
            Collection<E> dtoList, UUID departmentId, String token,
            boolean skipApprovalsWithNoLimitSharing
    ) {
        //Группируем dto по годам
        Map<Integer, List<TripApproveDTO>> dtoGroupedByYears = dtoList.stream()
                .collect(Collectors.groupingBy(dto -> dto.getDesiredDate().getYear()));
        Map<Integer, List<String>> dtoTransportTypesGroupedByYear = dtoGroupedByYears
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream().map(TripApproveDTO::getTransportType).distinct().toList()));

        //Ищем лимит для каждого года, в котором есть согласования
        Map<Integer, LimitDto> limitSharings =
                dtoGroupedByYears
                        .keySet().stream()
                        .map(year -> limitsDataResolver.getByDepartmentAndYearAndLimitServiceTypeFull(
                                departmentId,
                                year,
                                LimitServiceType.PASSENGER,
                                token))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(
                                GetLimitDTO::getYear,
                                limit -> new LimitDto(limit.getId(),
                                        limitsDataResolver.getByLimitFull(limit.getId(),
                                                token)))
                        );

        //для выделенных типов транспорта рассчитать restOfLimit и sumLimit
        //Группируем по году/типу транспорта
        Map<Pair<Integer, String>, Long> restOfLimitForTransportType = new HashMap<>();
        Map<Pair<Integer, String>, Long> sumLimitForTransportType = new HashMap<>();
        for (Integer year : limitSharings.keySet()) {
            for (final var transportType : dtoTransportTypesGroupedByYear.get(year)) {
                LimitDto limitDto = limitSharings.get(year);
                if (limitDto == null || limitDto.getId() == null) {
                    continue;
                }

                Optional<GetLimitSharingDTO> limitSharingOptional = limitDto.getLimitSharingDTO()
                        .stream()
                        .filter(dto -> dto.getTransportType().equals(transportType))
                        .findFirst();

                if (limitSharingOptional.isEmpty() && skipApprovalsWithNoLimitSharing) {
                    continue;
                }

                GetLimitSharingDTO limitSharing = limitSharingOptional
                        .orElseThrow(() -> new EntityNotFoundException(GetLimitSharingDTO.class,
                                Map.of("limit.id", limitDto.getId(), "transportType", transportType)));

                Long balance = 0L;
                if (limitSharing != null && limitSharing.getLimitSharingPerPeriodDTO() != null &&
                        limitSharing.getLimitSharingPerPeriodDTO().getBalance() != null) {
                    balance = limitSharing.getLimitSharingPerPeriodDTO().getBalance();
                }

                Long reserved = 0L;
                if (limitSharing != null && limitSharing.getLimitSharingPerPeriodDTO() != null &&
                        limitSharing.getLimitSharingPerPeriodDTO().getSumReservedForCurrentPeriod() != null) {
                    reserved = limitSharing.getLimitSharingPerPeriodDTO().getSumReservedForCurrentPeriod();
                }

                Long sum = 0L;
                if (limitSharing != null && limitSharing.getLimitSharingPerPeriodDTO() != null &&
                        limitSharing.getLimitSharingPerPeriodDTO().getSum() != null) {
                    sum = limitSharing.getLimitSharingPerPeriodDTO().getSum();
                }

                restOfLimitForTransportType.put(Pair.of(year, transportType), balance + reserved);
                sumLimitForTransportType.put(Pair.of(year, transportType), sum);
            }
        }

        //в каждую dto записать restOfLimit и sumLimit
        for (TripApproveDTO approve : dtoList) {
            final var transportType = approve.getTransportType();
            int year = approve.getDesiredDate().getYear();
            approve.setRestOfLimit(restOfLimitForTransportType.get(Pair.of(year, transportType)));
            approve.setSumLimit(sumLimitForTransportType.get(Pair.of(year, transportType)));
        }
        return dtoList;
    }

    private String getTripPurposeLabel(Map<UUID, TripPurpose> map, TripApproveDTO dto) {
        if (null == dto.getPurposeId()) {
            return null;
        }
        final TripPurpose tripPurpose = map.get(dto.getPurposeId());
        if (null == tripPurpose) {
            log.warn("Not found trip purpose for id '{}'", dto.getPurposeId());
            return null;
        }
        return tripPurpose.getLabel();
    }

    @Data
    @AllArgsConstructor
    private static class LimitDto {
        private UUID id;
        private List<GetLimitSharingDTO> limitSharingDTO;
    }
}
