package ru.sber.transport.corporate.web.grpc.server;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.corporate.business.model.Filter;
import ru.sber.transport.corporate.business.model.HasOrganizationStructure;
import ru.sber.transport.corporate.business.providers.Provider;
import ru.sber.transport.corporate.business.senders.Sender;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.corporate.web.grpc.mappers.GrpcMapper;
import ru.sber.transport.exceptions.EntityNotFoundException;

import java.util.UUID;

/**
 * Базовый класс для gRPC-запросов
 *
 * @param <T> тип сущности
 * @param <R> тип gRPC объекта
 */
@Slf4j
@RequiredArgsConstructor
class GrpcSender<T extends HasOrganizationStructure, R> {

    private final Provider<T, ? extends Filter> provider;

    private final GrpcMapper<T, R> mapper;

    private final Sender<T> sender;

    private final Class<T> clazz;

    /**
     * Отправить все.
     *
     * @param responseObserver поток для записи ответа
     */
    void all(StreamObserver<R> responseObserver) {
        log.info("Requested all {}", clazz.getSimpleName());
        try {
            provider.get()
                    .stream()
                    .map(mapper::toGrpc)
                    .forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Responding failed", e);
            responseObserver.onError(e);
        }
    }

    void one(OrganizationsOuterClass.Request request, StreamObserver<R> responseObserver) {
        var rawId = request.getId();
        log.info("Requested {} {}", clazz.getSimpleName(), rawId);
        try {
            var id = UUID.fromString(rawId);
            var organization = provider.get(id)
                    .map(mapper::toGrpc)
                    .orElseThrow(() -> new EntityNotFoundException(clazz, id));
            responseObserver.onNext(organization);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Response to request department {} failed", rawId, e);
            responseObserver.onError(e);
        }
    }

    void refresh(StreamObserver<Empty> responseObserver) {
        sender.sendAll(provider.get());
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

}
