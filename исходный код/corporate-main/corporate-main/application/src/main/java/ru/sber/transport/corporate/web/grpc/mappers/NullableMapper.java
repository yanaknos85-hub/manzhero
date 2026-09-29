package ru.sber.transport.corporate.web.grpc.mappers;

import com.google.protobuf.NullValue;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.corporate.sync.grpc.service.State;
import ru.sber.transport.corporate.sync_import.grpc.service.Import;

import java.time.LocalDate;

/**
 * Маппер обнуляемых значений.
 */
@Mapper(uses = DateGrpcMapper.class)
public interface NullableMapper {

    /**
     * Преобразование gRPC в бизнес.
     *
     * @param source gRPC.
     * @return бизнес.
     */
    default String toBusiness(OrganizationsOuterClass.NullableString source) {
        return source.hasValue() ? source.getValue() : null;
    }

    /**
     * Преобразование gRPC в бизнес.
     *
     * @param source gRPC.
     * @return бизнес.
     */
    default LocalDate toBusiness(State.NullableDate source) {
        return source.hasValue() ? Mappers.getMapper(DateGrpcMapper.class).toBusiness(source.getValue()) : null;
    }

    /**
     * Преобразование gRPC в бизнес.
     *
     * @param source gRPC.
     * @return бизнес.
     */
    default String toBusiness(State.NullableString source) {
        return source.hasValue() ? source.getValue() : null;
    }

    /**
     * Преобразование gRPC в бизнес.
     *
     * @param source gRPC.
     * @return бизнес.
     */
    default String toBusiness(Import.NullableString source) {
        return source.hasValue() ? source.getValue() : null;
    }

    /**
     * Преобразование gRPC в бизнес.
     *
     * @param source gRPC.
     * @return бизнес.
     */
    default Integer toBusiness(OrganizationsOuterClass.NullableInt source) {
        return source.hasValue() ? source.getValue() : null;
    }

    /**
     * Преобразование gRPC в бизнес.
     *
     * @param source gRPC.
     * @return бизнес.
     */
    default Integer toBusiness(Import.NullableInt source) {
        return source.hasValue() ? source.getValue() : null;
    }

    /**
     * Преобразование бизнес в gRPC.
     *
     * @param source бизнес.
     * @return gRPC.
     */
    default OrganizationsOuterClass.NullableString toGrpc(String source) {
        var valueBuilder = OrganizationsOuterClass.NullableString.newBuilder();
        if (source == null) {
            valueBuilder.setNull(NullValue.NULL_VALUE);
        } else {
            valueBuilder.setValue(source);
        }
        return valueBuilder.build();
    }

    /**
     * Преобразование бизнес в gRPC.
     *
     * @param source бизнес.
     * @return gRPC.
     */
    default OrganizationsOuterClass.NullableInt toGrpc(Integer source) {
        var valueBuilder = OrganizationsOuterClass.NullableInt.newBuilder();
        if (source == null) {
            valueBuilder.setNull(NullValue.NULL_VALUE);
        } else {
            valueBuilder.setValue(source);
        }
        return valueBuilder.build();
    }

}
