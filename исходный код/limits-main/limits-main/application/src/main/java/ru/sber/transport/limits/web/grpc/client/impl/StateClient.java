package ru.sber.transport.limits.web.grpc.client.impl;

import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.sber.transport.corporate.grpc.service.EmployeesGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.limits.business.model.Employee;
import ru.sber.transport.limits.web.grpc.client.StateClient;
import ru.sber.transport.limits.web.grpc.client.mappers.EmployeeGrpcMapper;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class StateClientImpl implements StateClient {

    private final EmployeeGrpcMapper mapper;

    @GrpcClient("corporate")
    private EmployeesGrpc.EmployeesBlockingStub stub;

    @Override
    public Optional<Employee> get(UUID id) {
        try {
            return Optional.of(mapper.toBusiness(stub.one(OrganizationsOuterClass.Request.newBuilder().setId(id.toString()).build())));
        } catch (StatusRuntimeException e) {
            return Optional.empty();
        }
    }

}
