package ru.sber.transport.corporate.web.grpc.mappers;

import ru.sber.transport.corporate.business.model.HasOrganizationStructure;

/**
 * Маппер gRPC
 *
 * @param <T> тип сущности
 * @param <R> тип сообщения
 */
public interface GrpcMapper<T extends HasOrganizationStructure, R> {

    /**
     * Конвертировать бизнес в gRPC
     *
     * @param source бизнес
     * @return gRPC
     */
    R toGrpc(T source);
}
