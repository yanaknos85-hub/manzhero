package ru.sber.transport.limits.providers.grpc.department;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.corporate.grpc.service.DepartmentsGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.limits.model.Department;
import ru.sber.transport.limits.providers.Departments;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class DepartmentGrpcImpl implements Departments {

    private final DepartmentsGrpc.DepartmentsBlockingStub stub;

    @Override
    public Department get(UUID id) {
        final var department = stub.one(OrganizationsOuterClass.Request.newBuilder().setId(id.toString()).build());
        return new Department() {
            @Override
            public UUID id() {
                return UUID.fromString(department.getId());
            }

            @Override
            public UUID parentId() {
                final var parentId = department.getParentId();
                return parentId.hasValue() ? UUID.fromString(parentId.getValue()) : null;
            }
        };
    }
}
