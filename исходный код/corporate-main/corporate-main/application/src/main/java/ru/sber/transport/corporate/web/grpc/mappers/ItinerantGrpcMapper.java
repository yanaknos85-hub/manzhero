package ru.sber.transport.corporate.web.grpc.mappers;

import org.mapstruct.Mapper;
import ru.sber.transport.corporate.business.model.ItinerantType;
import ru.sber.transport.corporate.sync.grpc.service.State;
import ru.sber.transport.corporate.sync_import.grpc.service.Import;

/**
 * Маппер данных из ЕАСУП.
 */
@Mapper
public interface ItinerantGrpcMapper {

    /**
     * Преобразование gRPC в бизнес.
     *
     * @param source gRPC.
     * @return бизнес.
     */
    default ItinerantType toBusiness(State.ItinerantType source) {
        return ItinerantType.valueOf(source.name());
    }

    /**
     * Преобразование gRPC в бизнес.
     *
     * @param source gRPC.
     * @return бизнес.
     */
    default ItinerantType toBusiness(Import.Itinerant source) {
        return ItinerantType.valueOf(source.name());
    }

}
