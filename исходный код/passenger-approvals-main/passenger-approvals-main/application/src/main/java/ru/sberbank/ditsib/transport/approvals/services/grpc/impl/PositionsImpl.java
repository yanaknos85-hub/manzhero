package ru.sberbank.ditsib.transport.approvals.services.grpc.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.corporate.grpc.service.PositionsGrpc;
import ru.sberbank.ditsib.transport.approvals.database.model.Position;
import ru.sberbank.ditsib.transport.approvals.services.grpc.Positions;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PositionsImpl implements Positions {

    @GrpcClient("grpc-corporate")
    private PositionsGrpc.PositionsBlockingStub stub;

    @Override
    public Position one(UUID id) {
        final var request = OrganizationsOuterClass.Request.newBuilder().setId(id.toString()).build();
        try {
            log.info("Position {} not found. Requesting from source", id);
            final var response = stub.one(request);
            log.info("Position {} responded", id);
            return new Position(
                    UUID.fromString(response.getId()),
                    UUID.fromString(response.getOrganizationId()),
                    response.getNoApproveRequired(),
                    !response.getDeleted()
            );
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            log.info("Failed to receive position, id: {}, cause: {}", id, e.getMessage());
            return null;
        }
    }
}
