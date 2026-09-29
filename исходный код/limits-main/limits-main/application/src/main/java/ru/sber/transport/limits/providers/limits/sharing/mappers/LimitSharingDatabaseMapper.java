package ru.sber.transport.limits.providers.limits.sharing.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.sber.transport.database.limits.tables.records.SharingsRecord;
import ru.sber.transport.limits.business.model.LimitSharing;

/**
 * Маппер для распределений лимитов
 */
@Mapper
public interface LimitSharingDatabaseMapper {

    /**
     * Маппер для распределений лимитов из базы в бизнес-модель
     *
     * @param source исходные данные
     * @return целевые данные
     */
    LimitSharing toBusiness(SharingsRecord source);

    /**
     * Обновление данных в базе
     *
     * @param target целевые данные
     * @param source исходные данные
     */
    void update(@MappingTarget SharingsRecord target, LimitSharing source);

}
