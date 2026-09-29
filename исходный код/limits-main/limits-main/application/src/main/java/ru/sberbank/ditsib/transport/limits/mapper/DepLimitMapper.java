package ru.sberbank.ditsib.transport.limits.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.limits.dto.v2.DepLimitPrimaryV2DTO;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;

import java.math.BigDecimal;

/**
 * Mapper лимита департамента
 */
@Mapper(imports = BigDecimal.class, uses = SumMapper.class)
public interface DepLimitMapper {

    /**
     * Convert source to model.
     *
     * @param depLimitPrimaryDTO source.
     * @return model.
     */
    @Mapping(target = "economy", expression = "java(BigDecimal.ZERO)")
    @Mapping(target = "limitStatus", constant = "PLANNING")
    @Mapping(target = "limitType", constant = "DEPARTMENT")
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "limitOwner", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "humanReadableId", ignore = true)
    DepLimit toModel(DepLimitPrimaryV2DTO depLimitPrimaryDTO);
}
