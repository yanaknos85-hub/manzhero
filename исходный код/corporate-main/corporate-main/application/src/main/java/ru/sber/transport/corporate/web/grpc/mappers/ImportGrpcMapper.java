package ru.sber.transport.corporate.web.grpc.mappers;

import ru.sber.transport.corporate.business.model.HasOrganizationStructure;
import ru.sber.transport.corporate.sync_import.grpc.service.Import;

/**
 * Маппер данных при импорте
 *
 * @param <T> тип входящих данных
 * @param <M> тип данных в системе
 */
public interface ImportGrpcMapper<T, M extends HasOrganizationStructure> {

    /**
     * Конвертация данных из gRPC в бизнес
     *
     * @param value входящий объект
     * @return бизнес объект
     */
    M toBusiness(T value);

    /**
     * Конвертация данных из бизнес в gRPC
     *
     * @param source бизнес объект
     * @return gRPC объект
     */
    Import.SyncResponse toGrpc(M source);
}
