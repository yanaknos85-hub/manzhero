package ru.sberbank.ditsib.transport.limits.dto.v2;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Getter;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.dto.v2.serialization.PeriodSerializer;

import java.util.UUID;

@Getter
@Builder
public class LimitData {
    private UUID limit;
    private TransportTypeEnum transportType;
    @JsonSerialize(using = PeriodSerializer.class)
    private Period period;
}
