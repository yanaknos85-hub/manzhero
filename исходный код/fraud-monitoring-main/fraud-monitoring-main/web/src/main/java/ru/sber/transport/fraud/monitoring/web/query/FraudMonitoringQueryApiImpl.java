package ru.sber.transport.fraud.monitoring.web.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.fraud.monitoring.business.TripRequestsService;
import ru.sber.transport.fraud.monitoring.model.*;
import ru.sber.transport.web.api.FraudMonitoringQueryApi;
import ru.sber.transport.web.model.*;
import ru.sber.transport.web.model.Page;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Реализация сервиса для работы с заявками на поездку
 */
@Slf4j
@RequiredArgsConstructor
public class FraudMonitoringQueryApiImpl implements FraudMonitoringQueryApi {

    private final TripRequestsService tripRequestsService;

    /**
     * Получает детальную информацию о заявке на поездку по идентификатору
     *
     * @param id идентификатор заявки на поездку
     * @return заявка на поездку с детальной информацией
     */
    @Override
    public CompletableFuture<ResponseEntity<FraudRequestDetailDto>> getFraudRequestById(UUID id) {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        return CompletableFuture.supplyAsync(() -> {
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("Get fraud request by id {}", id);

            final var tripRequest = tripRequestsService.get(id).orElseThrow(() -> {
                log.warn("Trip request with id {} not found", id);
                return new EntityNotFoundException(TripRequest.class, id);
            });

            return ResponseEntity.ok()
                    .body(new WebResponseFraudRequestDetailDto(tripRequest));
        });
    }

    @Override
    public CompletableFuture<ResponseEntity<PageFraudRequestDto>> getAllFraudRequests(GetAllFraudRequestsRequest getAllFraudRequestsRequest) {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();

        final var rqFilter = getAllFraudRequestsRequest;
        final var registryFilter = rqFilter.getFilter();
        return CompletableFuture.supplyAsync(() -> {
            SecurityContextHolder.getContext().setAuthentication(authentication);

            final var sortField = Optional.of(rqFilter.getSort()).orElse("humanReadableId");
            final var sortDirection = Optional.of(rqFilter.getDirection()).orElse(SortDirection.ASC);
            final var filter = WebRequestFilter.builder()
                    .transportType(registryFilter.getTransportType())
                    .approverName(registryFilter.getApproverName())
                    .passengerName(registryFilter.getPassengerName())
                    .approver(registryFilter.getApprover())
                    .passenger(registryFilter.getPassenger())
                    .purpose(registryFilter.getPurpose())
                    .tripDateStart(registryFilter.getTripDateStart())
                    .tripDateEnd(registryFilter.getTripDateEnd())
                    .approveDateStart(registryFilter.getApproveDateStart())
                    .approveDateEnd(registryFilter.getApproveDateEnd())
                    .humanReadableId(registryFilter.getHumanReadableId())
                    .build();


            final var contentPage = tripRequestsService.get(filter,
                    Optional.ofNullable(getAllFraudRequestsRequest.getPage()).orElse(0),
                    Optional.ofNullable(getAllFraudRequestsRequest.getSize()).orElse(20),
                    sortField, sortDirection.equals(SortDirection.ASC));


            final var content = contentPage.content().stream()
                    .map(WebFraudRequestDto::new)
                    .map(FraudRequestDto.class::cast)
                    .toList();
            final var sortData = contentPage.sort();
            final var pageData = contentPage.page();
            final var result = new PageFraudRequestDto(content, new Sort(sortData.field(), sortData.asc() ? Sort.DirectionEnum.ASC : Sort.DirectionEnum.DESC), new Page(pageData.number(), pageData.size(), pageData.last(), pageData.first(), pageData.total(), pageData.count()));
            return ResponseEntity.ok(result);
        });
    }
}
