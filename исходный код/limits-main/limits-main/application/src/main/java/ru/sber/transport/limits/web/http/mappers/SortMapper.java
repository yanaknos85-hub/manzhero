package ru.sber.transport.limits.web.http.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.dto.Page;
import ru.sber.transport.limits.web.model.Sort;

/**
 * Маппер данных сортировки
 */
@Mapper
public interface SortMapper {

    /**
     * Переводит сортировку из бизнес-слоя в слой HTTP
     *
     * @param sortData данные сортировки бизнес-слоя
     * @return данные сортировки слоя HTTP
     */
    @Mapping(target = "direction", source = "asc")
    Sort toWeb(Page.SortData sortData);


    /**
     * Переводит сортировку из слоя HTTP в бизнес-слой
     *
     * @param asc данные сортировки слоя HTTP
     * @return данные сортировки слоя HTTP
     */
    default Sort.DirectionEnum toWeb(boolean asc) {
        return asc ? Sort.DirectionEnum.ASC : Sort.DirectionEnum.DESC;
    }

}
