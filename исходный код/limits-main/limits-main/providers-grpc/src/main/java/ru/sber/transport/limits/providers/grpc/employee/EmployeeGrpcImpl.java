package ru.sber.transport.limits.providers.grpc.employee;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.corporate.grpc.service.EmployeesGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.limits.model.Employee;
import ru.sber.transport.limits.providers.Employees;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class EmployeeGrpcImpl implements Employees {

    private final EmployeesGrpc.EmployeesBlockingStub stub;

    @Override
    public Employee get(UUID id) {
        final var employee = stub.one(OrganizationsOuterClass.Request.newBuilder().setId(id.toString()).build());
        return new Employee() {
            @Override
            public UUID id() {
                return UUID.fromString(employee.getId());
            }

            @Override
            public UUID departmentId() {
                return UUID.fromString(employee.getDepartmentId());
            }
        };
    }
}
