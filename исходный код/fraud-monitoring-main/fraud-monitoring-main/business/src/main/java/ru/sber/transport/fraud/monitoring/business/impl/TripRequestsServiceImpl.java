package ru.sber.transport.fraud.monitoring.business.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.fraud.monitoring.business.DepartmentsService;
import ru.sber.transport.fraud.monitoring.business.EmployeesService;
import ru.sber.transport.fraud.monitoring.business.TripPurposesService;
import ru.sber.transport.fraud.monitoring.business.TripRequestsService;
import ru.sber.transport.fraud.monitoring.model.*;
import ru.sber.transport.fraud.monitoring.providers.TripRequestsDatabaseProvider;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class TripRequestsServiceImpl implements TripRequestsService {

    private final TripRequestsDatabaseProvider tripRequestsDatabaseProvider;
    private final TripPurposesService tripPurposesService;
    private final EmployeesService employeesService;
    private final DepartmentsService departmentsService;


    @Override
    public TripRequest createOrUpdate(TripRequest source) {
        final var tripRequest = tripRequestsDatabaseProvider.createOrUpdate(source);

        if (tripRequest.getPurposeId() != null) {
            CompletableFuture.runAsync(() -> tripPurposesService.getExistedOrCreate(tripRequest.getPurposeId()));
        }

        if (tripRequest.getPassengerId() != null) {
            CompletableFuture.runAsync(() -> employeesService.getExistedOrCreate(tripRequest.getPassengerId()));
        }

        if (tripRequest.getApproverId() != null) {
            CompletableFuture.runAsync(() -> employeesService.getExistedOrCreate(tripRequest.getApproverId()));
        }

        if (tripRequest.getDepartmentId() != null) {
            CompletableFuture.runAsync(() -> departmentsService.getExistedOrCreate(tripRequest.getDepartmentId()));
        }

        return tripRequest;
    }

    @Override
    public Optional<TripRequestDataWithMessages> get(UUID id) {
        return tripRequestsDatabaseProvider.getWithMessages(id);
    }

    @Override
    public Page<TripRequestData> get(RequestFilter filter, int page, int size, String sort, boolean asc) {
        return tripRequestsDatabaseProvider.get(filter, page, size, sort, asc);
    }
}
