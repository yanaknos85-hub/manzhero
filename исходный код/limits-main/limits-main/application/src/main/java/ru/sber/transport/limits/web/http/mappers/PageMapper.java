package ru.sber.transport.limits.web.http.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.limits.web.model.Page;

/**
 * Маппер данных страницы
 */
@Mapper
public interface PageMapper {

    /**
     * Переводит сортировку из бизнес-слоя в слой HTTP
     *
     * @param pageData данные сортировки бизнес-слоя
     * @return данные сортировки слоя HTTP
     */
    @Mapping(target = "total", source = "totalElements")
    @Mapping(target = "count", source = "totalPages")
    Page toWeb(ru.sber.transport.dto.Page.PageData pageData);

}
