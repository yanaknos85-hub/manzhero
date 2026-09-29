package ru.sber.transport.fraud.monitoring.providers.grpc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.corporate.grpc.service.PositionsGrpc;
import ru.sber.transport.fraud.monitoring.model.Position;
import ru.sber.transport.fraud.monitoring.providers.PositionsGrpcProvider;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class PositionsGrpcProviderImpl implements PositionsGrpcProvider {

    private final PositionsGrpc.PositionsBlockingStub stub;

    @Override
    public Position get(UUID id) {
        try {
            log.info("Position {} not found. Requesting from source", id);
            final var request = OrganizationsOuterClass.Request.newBuilder().setId(id.toString()).build();
            final var response = stub.one(request);
            log.info("Position {} responded", id);
            return new Position() {

                @Override
                public UUID getId() {
                    return UUID.fromString(response.getId());
                }

                @Override
                public String getName() {
                    return response.getName();
                }


            };
        } catch (Exception e) {
            log.warn("Failed to receive position, id: {}, cause: {}", id, e.getMessage());
            return null;
        }
    }
}