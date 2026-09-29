package ru.sber.transport.fraud.monitoring.providers.grpc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.corporate.grpc.service.TripPurposesGrpc;
import ru.sber.transport.fraud.monitoring.model.TripPurpose;
import ru.sber.transport.fraud.monitoring.providers.TripPurposesGrpcProvider;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class TripPurposesGrpcProviderImpl implements TripPurposesGrpcProvider {

    private final TripPurposesGrpc.TripPurposesBlockingStub stub;

    @Override
    public TripPurpose get(UUID id) {
        final var request = OrganizationsOuterClass.Request.newBuilder().setId(id.toString()).build();
        try {
            log.info("Trip purpose {} not found. Requesting from source", id);
            final var response = stub.one(request);
            log.info("Trip purpose {} responded", id);
            return new TripPurpose() {
                @Override
                public UUID getId() {
                    return UUID.fromString(response.getId());
                }

                @Override
                public String getLabel() {
                    return response.getLabel();
                }
            };
        } catch (Exception e) {
            log.warn("Failed to receive trip purpose, id: {}, cause: {}", id, e.getMessage());
            return null;
        }
    }
}
