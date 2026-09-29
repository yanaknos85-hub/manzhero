package ru.sber.transport.corporate.business.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.corporate.business.model.Position;

/**
 * Бизнес-маппер данных.
 */
@Mapper
public interface PositionBusinessMapper {

    /**
     * Обновление данных.
     *
     * @param target цель обновления.
     * @param source исходные данные.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "humanReadableId", ignore = true)
    @Mapping(target = "noApproveRequired", ignore = true)
    void update(@MappingTarget Position target, Position source);
}
