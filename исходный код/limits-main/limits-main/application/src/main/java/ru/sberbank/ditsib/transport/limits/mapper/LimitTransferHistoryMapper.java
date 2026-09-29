package ru.sberbank.ditsib.transport.limits.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitTransferHistoryV2DTO;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitTransferHistory;

@Mapper(uses = PeriodMapper.class)
public interface LimitTransferHistoryMapper {

    @Mapping(target = "source.period", source = "sourcePeriod", qualifiedByName = PeriodMapper.PERIOD_TO_DTO)
    @Mapping(target = "target.period", source = "targetPeriod", qualifiedByName = PeriodMapper.PERIOD_TO_DTO)
    @Mapping(target = "source.limit", source = "sourceLimitId")
    @Mapping(target = "target.limit", source = "targetLimitId")
    @Mapping(target = "source.transportType", source = "sourceTransportType")
    @Mapping(target = "target.transportType", source = "targetTransportType")
    GetLimitTransferHistoryV2DTO toV2Dto(LimitTransferHistory limitTransferHistory);

}
