package ru.sber.transport.corporate.web.http.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.dto.Page;
import ru.sber.transport.web.model.Sort;

/**
 * Маппер для сортировки.
 */
@Mapper
public interface SortMapper {

    /**
     * Маппер сортировки.
     *
     * @param source источник.
     * @return сортировка.
     */
    @Mapping(target = "direction", source = "asc")
    Sort toWeb(Page.SortData source);

    /**
     * Маппер сортировки.
     *
     * @param source источник.
     * @return сортировка.
     */
    default Sort.DirectionEnum toDirection(boolean source) {
        return source ? Sort.DirectionEnum.ASC : Sort.DirectionEnum.DESC;
    }

}
