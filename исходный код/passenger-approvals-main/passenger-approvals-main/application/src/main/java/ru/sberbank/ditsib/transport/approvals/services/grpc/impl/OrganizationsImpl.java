package ru.sberbank.ditsib.transport.approvals.services.grpc.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.sber.transport.corporate.grpc.service.OrganizationsGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sberbank.ditsib.transport.approvals.database.model.Organization;
import ru.sberbank.ditsib.transport.approvals.services.grpc.Organizations;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrganizationsImpl implements Organizations {

    @GrpcClient("grpc-corporate")
    private OrganizationsGrpc.OrganizationsBlockingStub stub;

    @Override
    public Organization one(UUID id) {
        final var request = OrganizationsOuterClass.Request.newBuilder().setId(id.toString()).build();
        try {
            log.info("Organization {} not found. Requesting from source", id);
            final var response = stub.one(request);
            log.info("Organization {} responded", id);
            return new Organization(
                    UUID.fromString(response.getId()),
                    (long) response.getDigitId(),
                    !response.getDeleted()
            );
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            log.info("Failed to receive organization, id: {}, cause: {}", id, e.getMessage());
            return null;
        }
    }
}
