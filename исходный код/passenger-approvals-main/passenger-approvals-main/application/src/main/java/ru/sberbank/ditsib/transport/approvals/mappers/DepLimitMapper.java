package ru.sberbank.ditsib.transport.approvals.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sberbank.ditsib.transport.approvals.database.model.DepLimit;
import ru.sberbank.ditsib.transport.approvals.messaging.message.LimitMessage;

@Mapper
public interface DepLimitMapper {
    
    @Mapping(target = "period", source = "periodNumber")
    @Mapping(target = "reserve", source = "reserve", defaultValue = "0L")
    DepLimit toDepLimit(LimitMessage message, @MappingTarget DepLimit limit);
    
}
