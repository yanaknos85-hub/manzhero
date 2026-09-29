package ru.sberbank.ditsib.transport.limits.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.limits.web.http.model.LimitWebFilter;
import ru.sberbank.ditsib.transport.limits.dto.LimitSearchDTO;

/**
 * Маппер для работы с фильтрами
 */
@Mapper
public interface FilterWebMapper {

    /**
     * Конвертация из запроса в фильтр
     * @param limitSearchDTO запрос
     * @return фильтр
     */
    @Mapping(target = "humanReadableId", source = "humanReadableLimitId")
    @Mapping(target = "parentId", source = "parentLimitId")
    @Mapping(target = "serviceType", source = "limitServiceType")
    @Mapping(target = "status", source = "limitStatus")
    @Mapping(target = "type", source = "limitType")
    LimitWebFilter toBusiness(LimitSearchDTO limitSearchDTO);

}
