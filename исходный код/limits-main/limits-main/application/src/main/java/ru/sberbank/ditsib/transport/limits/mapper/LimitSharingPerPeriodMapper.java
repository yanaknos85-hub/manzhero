package ru.sberbank.ditsib.transport.limits.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.limits.dto.ChildLimitShardingPerPeriodDTO;
import ru.sberbank.ditsib.transport.limits.model.GetLimitSharingPerPeriodDTO;

@Mapper
public interface LimitSharingPerPeriodMapper {

    @Mapping(target = "author", source = "authorId")
    @Mapping(target = "limitSharing", source = "limitSharingId")
    @Mapping(target = "sumReservedForCurrentPeriod", ignore = true)
    @Mapping(target = "sumResharingsPeriod", ignore = true)
    GetLimitSharingPerPeriodDTO toDto(ChildLimitShardingPerPeriodDTO shardingByPeriod);

}
