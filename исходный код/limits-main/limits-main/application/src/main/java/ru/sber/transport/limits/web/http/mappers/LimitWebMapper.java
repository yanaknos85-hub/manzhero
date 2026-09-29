package ru.sber.transport.limits.web.http.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.limits.business.model.Limit;
import ru.sber.transport.limits.web.model.LimitData;
import ru.sber.transport.limits.web.model.NewLimit;

/**
 * Веб-маппер для работы с лимитами
 */
@Mapper(uses = {StatusWebMapper.class, SharingTypeWebMapper.class, SumWebMapper.class})
public interface LimitWebMapper {

    /**
     * Преобразовать лимит в модель для веб-слоя
     *
     * @param limit лимит
     * @return модель для веб-слоя
     */
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "limitSharingDTOList", ignore = true)
    @Mapping(target = "init", source = "reserve")
    @Mapping(target = "reserve", source = "reserve", qualifiedByName = "multiply")
    @Mapping(target = "remains", source = "sum")
    @Mapping(target = "sum", source = "sum", qualifiedByName = "multiply")
    @Mapping(target = "saved", source = "economy")
    @Mapping(target = "economy", source = "economy", qualifiedByName = "multiply")
    @Mapping(target = "limitType", source = "type")
    @Mapping(target = "limitServiceType", source = "serviceType")
    @Mapping(target = "limitSharingType", source = "sharingType")
    @Mapping(target = "limitOwner", source = "ownerId")
    @Mapping(target = "limitStatus", source = "status")
    @Mapping(target = "parentLimitId", source = "parentId")
    LimitData toWeb(Limit limit);

    /**
     * Преобразовать модель для веб-слоя в лимит
     *
     * @param result модель для веб-слоя
     * @return лимит
     */
    Limit toBusiness(NewLimit result);
}
