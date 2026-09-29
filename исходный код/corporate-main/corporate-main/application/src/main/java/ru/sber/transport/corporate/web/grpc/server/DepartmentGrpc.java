package ru.sber.transport.corporate.web.grpc.server;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.sber.transport.corporate.business.model.Department;
import ru.sber.transport.corporate.business.providers.DepartmentProvider;
import ru.sber.transport.corporate.grpc.service.DepartmentsGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.corporate.messaging.senders.DepartmentSender;
import ru.sber.transport.corporate.web.grpc.mappers.DepartmentGrpcMapper;

@Slf4j
@GrpcService
class DepartmentGrpc extends DepartmentsGrpc.DepartmentsImplBase {

    private final GrpcSender<Department, OrganizationsOuterClass.Department> grpc;

    public DepartmentGrpc(DepartmentProvider provider, DepartmentGrpcMapper mapper, DepartmentSender sender) {
        grpc = new GrpcSender<>(provider, mapper, sender, Department.class);
    }

    @Override
    public void all(Empty request, StreamObserver<OrganizationsOuterClass.Department> responseObserver) {
        grpc.all(responseObserver);
    }

    @Override
    public void one(OrganizationsOuterClass.Request request, StreamObserver<OrganizationsOuterClass.Department> responseObserver) {
        grpc.one(request, responseObserver);
    }

    @Override
    public void refresh(Empty request, StreamObserver<Empty> responseObserver) {
        grpc.refresh(responseObserver);
    }
}
