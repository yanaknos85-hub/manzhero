package ru.sber.transport.corporate.web.grpc.server;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.corporate.business.model.EmployeeFilter;
import ru.sber.transport.corporate.business.providers.Provider;
import ru.sber.transport.corporate.grpc.service.EmployeesGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.corporate.messaging.senders.EmployeeSender;
import ru.sber.transport.corporate.web.grpc.mappers.EmployeeGrpcMapper;

@Slf4j
@GrpcService
class EmployeeGrpc extends EmployeesGrpc.EmployeesImplBase {

    private final GrpcSender<Employee, OrganizationsOuterClass.Employee> grpc;

    public EmployeeGrpc(Provider<Employee, EmployeeFilter> provider, EmployeeGrpcMapper mapper, EmployeeSender sender) {
        grpc = new GrpcSender<>(provider, mapper, sender, Employee.class);
    }

    @Override
    public void all(Empty request, StreamObserver<OrganizationsOuterClass.Employee> responseObserver) {
        grpc.all(responseObserver);
    }

    @Override
    public void one(OrganizationsOuterClass.Request request, StreamObserver<OrganizationsOuterClass.Employee> responseObserver) {
        grpc.one(request, responseObserver);
    }

    @Override
    public void refresh(Empty request, StreamObserver<Empty> responseObserver) {
        grpc.refresh(responseObserver);
    }
}
