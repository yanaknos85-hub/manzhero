package ru.sber.transport.fraud.monitoring.providers.grpc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.corporate.grpc.service.DepartmentsGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.fraud.monitoring.model.Department;
import ru.sber.transport.fraud.monitoring.providers.DepartmentsGrpcProvider;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class DepartmentsGrpcProviderImpl implements DepartmentsGrpcProvider {

    private final DepartmentsGrpc.DepartmentsBlockingStub stub;

    @Override
    public Department get(UUID id) {
        final var request = OrganizationsOuterClass.Request.newBuilder().setId(id.toString()).build();
        try {
            log.info("Department {} not found. Requesting from source", id);
            final var response = stub.one(request);
            log.info("Department {} responded", id);
            return new Department() {
                @Override
                public UUID getId() {
                    return UUID.fromString(response.getId());
                }

                @Override
                public UUID getHeadId() {
                    final var headId = response.getHeadId();
                    if (headId.hasValue()) {
                        return UUID.fromString(headId.getValue());
                    }
                    return null;
                }

                @Override
                public String getName() {
                    return response.getName();
                }

                @Override
                public String getCode() {
                    return response.getCode();
                }

                @Override
                public UUID getParentId() {
                    final var parentId = response.getParentId();
                    if (parentId.hasValue()) {
                        return UUID.fromString(parentId.getValue());
                    }
                    return null;
                }
            };
        } catch (Exception e) {
            log.warn("Failed to receive department, id: {}, cause: {}", id, e.getMessage());
            return null;
        }
    }
}
