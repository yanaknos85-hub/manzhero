package ru.sberbank.ditsib.transport.limits.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.limits.dto.ChildLimitDTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitV2DTO;
import ru.sberbank.ditsib.transport.limits.model.GetLimitDTO;
import ru.sberbank.ditsib.transport.limits.model.limit.*;

import java.math.BigDecimal;
import java.util.Optional;


/**
 * Маппер, работающий с лимитами.
 */
@Mapper(uses = {EmployeeMapper.class, DepartmentMapper.class, LimitSharingMapper.class})
public interface LimitMapper {

    default Long toMessage(BigDecimal source) {
        return Optional.ofNullable(source)
                .map(it -> it.multiply(BigDecimal.valueOf(100)))
                .map(BigDecimal::longValue).orElse(null);
    }
    
    default String toMessage(Period period) {
        return Optional.ofNullable(period).map(Period::name).orElse(null);
    }

    @Mapping(target = "economy", ignore = true)
    @Mapping(target = "department", source = "department")
    @Mapping(target = "parentLimitId", source = "parent.id")
    @Mapping(target = "limitType", constant = "DEPARTMENT")
    @Mapping(target = "owner", source = "department.departmentHead.id", qualifiedByName = "mapEmployee")
    @Mapping(target = "limitOwner", source = "limitOwner")
    @Mapping(target = "reserve", source = "reserve")
    GetLimitV2DTO toDto(DepLimit source);

    @Mapping(target = "parentLimitId", source = "parent.id")
    @Mapping(target = "limitType", constant = "EMPLOYEE")
    @Mapping(target = "owner", source = "employee", qualifiedByName = "mapEmployee")
    @Mapping(target = "limitOwner", source = "limitOwner")
    GetLimitV2DTO toDto(EmpLimit source);

    default GetLimitV2DTO toDto(Limit source) {
        if (source instanceof EmpLimit empLimit) {
            return toDto(empLimit);
        }
        if (source instanceof DepLimit depLimit) {
            return toDto(depLimit);
        }
        throw new IllegalArgumentException("Unknown limit type '%s'".formatted(source.getClass().getSimpleName()));
    }

    @Mapping(target = "limitOwner", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "limitSharingDTOList", ignore = true)
    @Mapping(target = "owner", ignore = true)
    GetLimitDTO convertDto(ChildLimitDTO childLimit);
}
