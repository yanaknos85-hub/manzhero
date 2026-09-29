package ru.sberbank.ditsib.transport.limits.grpc.mapper;

import com.google.protobuf.NullValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.limits.grpc.dto.cargo.MassLimitReservationModel;
import ru.sberbank.ditsib.transport.limits.dto.LimitActionMessage;
import ru.sberbank.ditsib.transport.limits.dto.LimitActionResultMessage;
import ru.sberbank.ditsib.transport.limits.dto.MassLimitActionMessage;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Mapper
public interface MassLimitActionMapper {

     MassLimitActionMessage map(MassLimitReservationModel.MassLimitReservationRequestList source);

     MassLimitReservationModel.LimitReservationResponse map(LimitActionResultMessage source);

     LimitActionMessage map(MassLimitReservationModel.LimitReservationRequest source);

     @Mapping(target = "organizationId", ignore = true)
     @Mapping(target = "departmentId", ignore = true)
     @Mapping(target = "employeeId", ignore = true)
     LimitActionMessage toDoCancel(MassLimitReservationModel.LimitReservationRequest source);

     default MassLimitReservationModel.NullableString getNullableString(String source) {
          if (source == null) {
               return MassLimitReservationModel.NullableString.newBuilder().setNull(NullValue.NULL_VALUE).build();
          }
          return MassLimitReservationModel.NullableString.newBuilder().setData(source).build();
     }

     default LocalDateTime convertGoogleTimestampToLocalDateTime(com.google.protobuf.Timestamp timestamp) {
          return Instant
                  .ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos())
                  .atZone(ZoneOffset.UTC)
                  .toLocalDateTime();
     }

}
