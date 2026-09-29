package ru.sberbank.ditsib.transport.limits.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.sberbank.ditsib.transport.limits.dto.LimitSpendingDTO;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSpending;
import ru.sberbank.ditsib.transport.limits.model.limit.Month;
import ru.sberbank.ditsib.transport.limits.model.limit.Quarter;

@Mapper(uses = EmployeeMapper.class)
public interface LimitSpendingMapper {

    @Mapping(target = "limitId", ignore = true)
    LimitSpendingDTO toDto(LimitSpending limitSpending);
    
    @Named("mapPeriod")
    default Integer mapInt(String source) {
        if (source.startsWith("Q")) {
            return Quarter.valueOf(source).getValue();
        } else {
            return Month.valueOf(source).getValue();
        }
    }
    
}
