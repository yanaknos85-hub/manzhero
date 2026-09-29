package ru.sber.transport.corporate.web.http.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.web.model.Page;

/**
 * Маппер для конвертации страницы.
 */
@Mapper
public interface PageMapper {

    @Mapping(target = "count", source = "numberOfElements")
    @Mapping(target = "total", source = "totalElements")
    Page toWeb(ru.sber.transport.dto.Page.PageData pageData);

}
