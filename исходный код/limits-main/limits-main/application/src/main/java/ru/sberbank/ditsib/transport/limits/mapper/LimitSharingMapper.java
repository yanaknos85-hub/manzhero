package ru.sberbank.ditsib.transport.limits.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.limits.dto.ChildLimitShardingPerPeriodDTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitSharingPerPeriodV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitSharingV2DTO;
import ru.sberbank.ditsib.transport.limits.model.GetLimitSharingDTO;
import ru.sberbank.ditsib.transport.limits.model.GetLimitSharingPerPeriodDTO;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharing;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharingPerPeriod;
import ru.sberbank.ditsib.transport.limits.model.limit.Period;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper(uses = { EmployeeMapper.class, PeriodMapper.class })
public interface LimitSharingMapper {
    
    @Mapping(target = "periodNumber", source = "period", qualifiedByName = PeriodMapper.PERIOD_OBJECT_TO_INTEGER)
    @Mapping(target = "sumReservedForCurrentPeriod", ignore = true)
    @Mapping(target = "sumResharingsPeriod", source = "additionalSum")
    GetLimitSharingPerPeriodDTO toDto(LimitSharingPerPeriod limitSharingPerPeriod);

    @Mapping(target = "sumReservedForCurrentPeriod", ignore = true)
    @Mapping(target = "sumResharingsPeriod", source = "additionalSum")
    @Mapping(target = "period", source = "periodData", qualifiedByName = PeriodMapper.PERIOD_TO_DTO)
    GetLimitSharingPerPeriodV2DTO toV2Dto(LimitSharingPerPeriod limitSharingPerPeriod);
    
    GetLimitSharingDTO toDto(LimitSharing limitSharing);
    
    default UUID toDtoId(LimitSharing source) {
        return Optional.ofNullable(source).map(LimitSharing::getId).orElse(null);
    }
    
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "periodNumber", source = "period", qualifiedByName = PeriodMapper.PERIOD_OBJECT_TO_INTEGER)
    @Mapping(target = "limitSharingId", source = "limitSharing.id")
    @Mapping(target = "organizationId", ignore = true)
    ChildLimitShardingPerPeriodDTO map(LimitSharingPerPeriod limitSharingPerPeriod);

    @Mapping(target = "limitId", ignore = true)
    @Mapping(target = "sumResharingsYear", ignore = true)
    @Mapping(target = "sharings", source = "sharingPerPeriods")
    GetLimitSharingV2DTO toV2Dto(LimitSharing limitSharing);

    @Mapping(target = "limitSharingPerPeriodDTO", source = "periods")
    @Mapping(target = "balance", source = "remains")
    GetLimitSharingDTO toDto(ru.sber.transport.limits.business.model.LimitSharing source);

    default GetLimitSharingPerPeriodDTO toDto(List<ru.sber.transport.limits.business.model.LimitSharingPerPeriod> source) {
        return source.parallelStream().filter(it -> LocalDate.now().getMonth().name().equals(it.getPeriod().name()))
                .findFirst()
                .map(this::toDto)
                .orElse(null);
    }

    @Mapping(target = "periodNumber", source = "period")
    GetLimitSharingPerPeriodDTO toDto(ru.sber.transport.limits.business.model.LimitSharingPerPeriod source);

    default Integer toDto(Period source) {
        return source.ordinal();
    }
}
