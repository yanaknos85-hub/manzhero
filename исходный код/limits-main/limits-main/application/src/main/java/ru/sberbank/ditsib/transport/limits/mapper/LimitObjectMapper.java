package ru.sberbank.ditsib.transport.limits.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.limits.dto.GetLimitHistoryDTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitHistoryV2DTO;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitHistory;

/**
 * Mapper лимита департамента
 */
@Mapper(uses = PeriodMapper.class)
public interface LimitObjectMapper {

    @Mapping(target = "period", source = "period", qualifiedByName = PeriodMapper.PERIOD_TO_INTEGER)
    @Mapping(target = "counterpartPeriod", source = "counterpartPeriod", qualifiedByName = PeriodMapper.PERIOD_TO_INTEGER)
    GetLimitHistoryDTO convertToDto(LimitHistory limitHistory);

    @Mapping(target = "period", source = "period", qualifiedByName = PeriodMapper.PERIOD_TO_DTO)
    @Mapping(target = "counterpartPeriod", source = "counterpartPeriod", qualifiedByName = PeriodMapper.PERIOD_TO_DTO)
    GetLimitHistoryV2DTO convertToV2Dto(LimitHistory limitHistory);
    
}
