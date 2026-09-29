package ru.sber.transport.limits.providers.limits.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.database.limits.tables.records.LimitRecord;
import ru.sber.transport.limits.business.model.Limit;

/**
 * Маппер лимитов
 *
 * @author 17898164
 */
@Mapper
public interface LimitsDatabaseMapper {

    /**
     * Конвертация БД -> бизнес
     *
     * @param source исходные данные
     * @return бизнес объект
     */
    @Mapping(target = "status", source = "limitStatus")
    @Mapping(target = "sharingType", source = "limitSharingType")
    @Mapping(target = "ownerId", source = "limitOwnerId")
    @Mapping(target = "type", source = "limitType")
    @Mapping(target = "useThisLimit", source = "useMyLimit")
    Limit toBusiness(LimitRecord source);

    /**
     * Обновить данные лимита
     *
     * @param target объект для обновления
     * @param source исходные данные
     */
    @Mapping(target = "limitStatus", source = "status")
    void update(@MappingTarget LimitRecord target, Limit source);
}
