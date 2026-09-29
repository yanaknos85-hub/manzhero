package ru.sber.transport.corporate.web.grpc.mappers;

import com.google.protobuf.NullValue;
import org.mapstruct.Mapper;
import ru.sber.transport.corporate.business.model.Gender;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;
import ru.sber.transport.corporate.sync.grpc.service.State;
import ru.sber.transport.corporate.sync_import.grpc.service.Import;

/**
 * Маппер данных бизнес - gRPC.
 */
@Mapper
public interface GenderGrpcMapper {

    /**
     * Преобразование бизнес в gRPC.
     *
     * @param source бизнес.
     * @return gRPC.
     */
    OrganizationsOuterClass.Gender toGrpc(Gender source);

    /**
     * Преобразование бизнес в gRPC.
     *
     * @param source бизнес.
     * @return gRPC.
     */
    default OrganizationsOuterClass.NullableGender toNullableGrpc(Gender source) {
        var builder = OrganizationsOuterClass.NullableGender.newBuilder();
        if (source == null) {
            builder.setNull(NullValue.NULL_VALUE);
        } else {
            builder.setValue(toGrpc(source));
        }
        return builder.build();
    }

    /**
     * Преобразование бизнес в gRPC.
     *
     * @param source бизнес.
     * @return gRPC.
     */
    default Gender toGrpc(OrganizationsOuterClass.Gender source) {
        return Gender.valueOf(source.name());
    }

    /**
     * Преобразование бизнес в gRPC.
     *
     * @param source бизнес.
     * @return gRPC.
     */
    default Gender toGrpc(State.Gender source) {
        return Gender.valueOf(source.name());
    }

    /**
     * Преобразование бизнес в gRPC.
     *
     * @param source бизнес.
     * @return gRPC.
     */
    default Gender toGrpc(Import.Gender source) {
        return Gender.valueOf(source.name());
    }

    /**
     * Преобразование бизнес в gRPC.
     *
     * @param source бизнес.
     * @return gRPC.
     */
    default Gender toBusiness(OrganizationsOuterClass.NullableGender source) {
        return source.hasValue() ? toGrpc(source.getValue()) : null;
    }

}
