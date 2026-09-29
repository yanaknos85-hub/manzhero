package ru.sberbank.ditsib.transport.limits.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.dto.v2.serialization.PeriodSerializer;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;
import ru.sberbank.ditsib.transport.limits.model.limit.Period;

public record LimitData(
        Limit limit,
        TransportTypeEnum transportType,
        @JsonSerialize(using = PeriodSerializer.class)
        Period period
) {

    public LimitData(
            Limit limit,
            TransportTypeEnum transportType) {
        this(limit, transportType, null);
    }

}
