package ru.sberbank.ditsib.transport.approvals.services.grpc.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.sber.transport.corporate.grpc.service.DepartmentsGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sberbank.ditsib.transport.approvals.database.model.Department;
import ru.sberbank.ditsib.transport.approvals.services.grpc.Departments;

import java.util.Collections;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepartmentsImpl implements Departments {

    @GrpcClient("grpc-corporate")
    private DepartmentsGrpc.DepartmentsBlockingStub stub;

    @Override
    public Department one(UUID id) {
        final var request = OrganizationsOuterClass.Request.newBuilder().setId(id.toString()).build();
        try {
            log.info("Department {} not found. Requesting from source", id);
            final var response = stub.one(request);
            log.info("Department {} responded", id);
            return new Department(
                    UUID.fromString(response.getId()),
                    UUID.fromString(response.getOrganizationId()),
                    response.getParentId().hasValue() ? UUID.fromString(response.getParentId().getValue()) : null,
                    response.getHeadId().hasValue() ? UUID.fromString(response.getHeadId().getValue()) : null,
                    Collections.emptyList(),
                    response.getName(),
                    !response.getDeleted()
            );
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            log.info("Failed to receive department, id: {}, cause: {}", id, e.getMessage());
            return null;
        }
    }
}
