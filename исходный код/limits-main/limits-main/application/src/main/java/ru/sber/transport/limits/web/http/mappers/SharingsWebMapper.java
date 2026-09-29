package ru.sber.transport.limits.web.http.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.limits.business.model.LimitSharing;
import ru.sber.transport.limits.business.model.LimitSharingPerPeriod;
import ru.sber.transport.limits.web.model.OldSharing;
import ru.sber.transport.limits.web.model.OldSharingPerPeriod;

/**
 * Маппер данных распределений
 */
@Mapper(uses = SumWebMapper.class)
public interface SharingsWebMapper {

    /**
     * Маппер данных распределений
     * @param source источник
     * @return результат
     */
    @Mapping(target = "limitSharingPerPeriodDTO", source = "periods")
    @Mapping(target = "sum", source = "sum", qualifiedByName = "multiply")
    @Mapping(target = "balance", source = "remains", qualifiedByName = "multiply")
    OldSharing toWeb(LimitSharing source);

    /**
     * Маппер данных распределений
     * @param source источник
     * @return результат
     */
    @Mapping(target = "sum", source = "sum", qualifiedByName = "multiply")
    @Mapping(target = "balance", source = "balance", qualifiedByName = "multiply")
    OldSharingPerPeriod toWeb(LimitSharingPerPeriod source);
}
