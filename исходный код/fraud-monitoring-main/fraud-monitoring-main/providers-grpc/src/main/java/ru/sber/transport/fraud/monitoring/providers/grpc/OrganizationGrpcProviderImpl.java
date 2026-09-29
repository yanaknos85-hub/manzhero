package ru.sber.transport.fraud.monitoring.providers.grpc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.corporate.grpc.service.OrganizationsGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.fraud.monitoring.model.Organization;
import ru.sber.transport.fraud.monitoring.providers.OrganizationsGrpcProvider;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class OrganizationGrpcProviderImpl implements OrganizationsGrpcProvider {

    private final OrganizationsGrpc.OrganizationsBlockingStub stub;

    @Override
    public Organization get(UUID id) {
        try {
            log.info("Organization {} not found. Requesting from source", id);
            final var request = OrganizationsOuterClass.Request.newBuilder().setId(id.toString()).build();
            final var response = stub.one(request);
            log.info("Organization {} responded", id);
            return new Organization() {

                @Override
                public UUID getId() {
                    return UUID.fromString(response.getId());
                }

                @Override
                public long getDigitId() {
                    return response.getDigitId();
                }

            };
        } catch (Exception e) {
            log.warn("Failed to receive organization, id: {}, cause: {}", id, e.getMessage());
            return null;
        }
    }
}