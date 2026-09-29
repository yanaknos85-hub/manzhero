package ru.sber.transport.limits.providers.limits.sharing.period.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.sber.transport.database.limits.tables.records.LimitSharingPerPeriodRecord;
import ru.sber.transport.limits.business.model.LimitSharingPerPeriod;

/**
 * Маппер данных распределений по периодам
 */
@Mapper
public interface LimitSharingPerPeriodDatabaseMapper {

    /**
     * Маппер данных распределений по периодам из базы в бизнес-модель
     *
     * @param source исходные данные
     * @return целевые данные
     */
    LimitSharingPerPeriod toBusiness(LimitSharingPerPeriodRecord source);

    /**
     * Обновление данных в базе
     *
     * @param target целевые данные
     * @param source исходные данные
     */
    void update(@MappingTarget LimitSharingPerPeriodRecord target, LimitSharingPerPeriod source);

}
