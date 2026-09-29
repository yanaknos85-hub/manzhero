package ru.sber.transport.corporate.web.grpc.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import ru.sber.transport.corporate.business.model.Active;
import ru.sber.transport.corporate.sync_import.grpc.service.Import;

/**
 * Маппер статусов активности
 */
@Mapper
public interface ActiveStatusGrpcMapper {

    String DELETED = "deleted";

    /**
     * Преобразование бизнес в gRPC.
     *
     * @param source бизнес.
     * @return gRPC.
     */
    @Named(DELETED)
    default boolean toMessage(Active source) {
        return Active.INACTIVE.equals(source);
    }

    /**
     * Преобразование бизнес в gRPC.
     *
     * @param source бизнес.
     * @return gRPC.
     */
    default Active toMessage(Import.Active source) {
        return Active.valueOf(source.name());
    }

    /**
     * Преобразование gRPC в бизнес.
     *
     * @param active gRPC.
     * @return бизнес.
     */
    default Active toBusiness(boolean active) {
        return active ? Active.ACTIVE : Active.INACTIVE;
    }

}
