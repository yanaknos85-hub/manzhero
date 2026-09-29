package ru.sber.transport.corporate.web.grpc.server;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.sber.transport.corporate.business.model.Position;
import ru.sber.transport.corporate.business.model.PositionFilter;
import ru.sber.transport.corporate.business.providers.Provider;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.corporate.grpc.service.PositionsGrpc;
import ru.sber.transport.corporate.messaging.senders.PositionSender;
import ru.sber.transport.corporate.web.grpc.mappers.PositionGrpcMapper;

@Slf4j
@GrpcService
class PositionGrpc extends PositionsGrpc.PositionsImplBase {

    private final GrpcSender<Position, OrganizationsOuterClass.Position> grpc;

    public PositionGrpc(Provider<Position, PositionFilter> provider, PositionGrpcMapper mapper, PositionSender sender) {
        grpc = new GrpcSender<>(provider, mapper, sender, Position.class);
    }

    @Override
    public void all(Empty request, StreamObserver<OrganizationsOuterClass.Position> responseObserver) {
        grpc.all(responseObserver);
    }

    @Override
    public void one(OrganizationsOuterClass.Request request, StreamObserver<OrganizationsOuterClass.Position> responseObserver) {
        grpc.one(request, responseObserver);
    }

    @Override
    public void refresh(Empty request, StreamObserver<Empty> responseObserver) {
        grpc.refresh(responseObserver);
    }
}
