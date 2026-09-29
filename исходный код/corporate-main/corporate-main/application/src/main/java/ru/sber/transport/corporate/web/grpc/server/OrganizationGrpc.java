package ru.sber.transport.corporate.web.grpc.server;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.sber.transport.corporate.business.model.Organization;
import ru.sber.transport.corporate.business.providers.OrganizationProvider;
import ru.sber.transport.corporate.grpc.service.OrganizationsGrpc;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.corporate.messaging.senders.OrganizationSender;
import ru.sber.transport.corporate.web.grpc.mappers.ContactsGrpcMapper;
import ru.sber.transport.corporate.web.grpc.mappers.OrganizationGrpcMapper;
import ru.sber.transport.exceptions.EntityNotFoundException;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@GrpcService
class OrganizationGrpc extends OrganizationsGrpc.OrganizationsImplBase {

    private final OrganizationProvider provider;

    private final OrganizationGrpcMapper mapper;

    private final ContactsGrpcMapper contactsMapper;

    private final OrganizationSender sender;

    @Override
    public void all(Empty request, StreamObserver<OrganizationsOuterClass.Organization> responseObserver) {
        log.info("Requested all organizations");
        try {
            provider.get()
                    .stream()
                    .map(it -> mapper.toGrpc(it).toBuilder().addAllContacts(contactsMapper.toGrpc(it.getContacts())).build())
                    .forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Responding failed", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void one(OrganizationsOuterClass.Request request, StreamObserver<OrganizationsOuterClass.Organization> responseObserver) {
        var rawId = request.getId();
        log.info("Requested organization {}", rawId);
        try {
            var id = UUID.fromString(rawId);
            var organization = provider.get(id)
                    .map(it -> mapper.toGrpc(it).toBuilder()
                            .addAllClasses(it.getAvailableClasses())
                            .addAllContacts(contactsMapper.toGrpc(it.getContacts())).build())
                    .orElseThrow(() -> new EntityNotFoundException(Organization.class, id));
            responseObserver.onNext(organization);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Response to request organization {} failed", rawId, e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void refresh(Empty request, StreamObserver<Empty> responseObserver) {
        sender.send(provider.get());
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
