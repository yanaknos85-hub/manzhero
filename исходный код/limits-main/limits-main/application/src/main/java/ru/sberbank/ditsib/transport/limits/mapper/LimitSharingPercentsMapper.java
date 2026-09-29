package ru.sberbank.ditsib.transport.limits.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sberbank.ditsib.transport.limits.dto.GetLimitSharingPercentsDTO;
import ru.sberbank.ditsib.transport.limits.dto.LimitSharingPercentsDTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitSharingPercentsV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitSharingPercentsV2DTO;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharingPercents;

@Mapper
public interface LimitSharingPercentsMapper {

    void update(@MappingTarget LimitSharingPercents limitSharingPercents, LimitSharingPercentsDTO limitSharingPercentsDTO);

    void update(@MappingTarget LimitSharingPercents limitSharingPercents, LimitSharingPercentsV2DTO limitSharingPercentsDTO);

    @Mapping(target = "author", source = "author.id")
    @Mapping(target = "limitId", source = "limit.id")
    GetLimitSharingPercentsDTO toDto(LimitSharingPercents limitSharingPercents);

    @Mapping(target = "author", source = "author.id")
    @Mapping(target = "limitId", source = "limit.id")
    GetLimitSharingPercentsV2DTO toV2Dto(LimitSharingPercents limitSharingPercents);
}
