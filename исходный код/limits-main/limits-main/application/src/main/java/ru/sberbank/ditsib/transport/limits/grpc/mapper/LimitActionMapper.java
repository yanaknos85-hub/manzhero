package ru.sberbank.ditsib.transport.limits.grpc.mapper;

import com.google.protobuf.NullValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.limits.grpc.OldReserve;
import ru.sber.transport.limits.grpc.dto.LimitReservationModel;
import ru.sberbank.ditsib.transport.limits.dto.LimitActionMessage;
import ru.sberbank.ditsib.transport.limits.dto.LimitActionResultMessage;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface LimitActionMapper {

    @Mapping(target = "action", ignore = true)
    @Mapping(target = "bonusSum", ignore = true)
    @Mapping(target = "moneySaved", ignore = true)
    @Mapping(target = "humanReadableId", ignore = true)
    @Mapping(target = "coop", ignore = true)
    @Mapping(target = "driver", ignore = true)
    LimitActionMessage map(LimitReservationModel.LimitReservationRequest source);

    @Mapping(target = "bonusSum", ignore = true)
    @Mapping(target = "moneySaved", ignore = true)
    @Mapping(target = "humanReadableId", ignore = true)
    @Mapping(target = "coop", ignore = true)
    @Mapping(target = "driver", ignore = true)
    LimitActionMessage map(OldReserve.LimitReservationRequest source);

    @Mapping(target = "mergeFrom", ignore = true)
    @Mapping(target = "clearField", ignore = true)
    @Mapping(target = "clearOneof", ignore = true)
    @Mapping(target = "unknownFields", ignore = true)
    @Mapping(target = "mergeUnknownFields", ignore = true)
    @Mapping(target = "tripRequestIdBytes", ignore = true)
    @Mapping(target = "mergeLimitId", ignore = true)
    @Mapping(target = "limitReservationStatusBytes", ignore = true)
    @Mapping(target = "mergeMessage", ignore = true)
    @Mapping(target = "allFields", ignore = true)
    LimitReservationModel.LimitReservationResponse map(LimitActionResultMessage source);

    @Mapping(target = "mergeFrom", ignore = true)
    @Mapping(target = "clearField", ignore = true)
    @Mapping(target = "clearOneof", ignore = true)
    @Mapping(target = "unknownFields", ignore = true)
    @Mapping(target = "mergeUnknownFields", ignore = true)
    @Mapping(target = "tripRequestIdBytes", ignore = true)
    @Mapping(target = "mergeLimitId", ignore = true)
    @Mapping(target = "limitReservationStatusBytes", ignore = true)
    @Mapping(target = "mergeMessage", ignore = true)
    @Mapping(target = "allFields", ignore = true)
    OldReserve.LimitReservationResponse mapNext(LimitActionResultMessage source);
    
    default UUID map(String source) {
        return Optional.ofNullable(source).map(UUID::fromString).orElse(null);
    }
    
    default String map(UUID source) {
        return Optional.ofNullable(source).map(UUID::toString).orElse(null);
    }
    
    default LimitReservationModel.NullableString getNullableUUID(UUID source) {
        if (source == null) {
            return LimitReservationModel.NullableString.newBuilder().setNull(NullValue.NULL_VALUE).build();
        }
        return LimitReservationModel.NullableString.newBuilder().setData(source.toString()).build();
    }

    default OldReserve.NullableString getNullableUUIDТуче(UUID source) {
        if (source == null) {
            return OldReserve.NullableString.newBuilder().setNull(NullValue.NULL_VALUE).build();
        }
        return OldReserve.NullableString.newBuilder().setData(source.toString()).build();
    }
    
    default LimitReservationModel.NullableString getNullableString(String source) {
        if (source == null) {
            return LimitReservationModel.NullableString.newBuilder().setNull(NullValue.NULL_VALUE).build();
        }
        return LimitReservationModel.NullableString.newBuilder().setData(source).build();
    }

    default OldReserve.NullableString getNullableStringNext(String source) {
        if (source == null) {
            return OldReserve.NullableString.newBuilder().setNull(NullValue.NULL_VALUE).build();
        }
        return OldReserve.NullableString.newBuilder().setData(source).build();
    }
    
    default LocalDateTime convertGoogleTimestampToLocalDateTime(com.google.protobuf.Timestamp timestamp) {
        return Instant
                .ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos())
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();
    }
}
