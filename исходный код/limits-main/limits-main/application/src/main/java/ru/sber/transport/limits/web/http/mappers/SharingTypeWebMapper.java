package ru.sber.transport.limits.web.http.mappers;

import org.mapstruct.Mapper;

/**
 * Маппинг типов распределений
 */
@Mapper
public interface SharingTypeWebMapper {

    /**
     * Преобразовать тип распределения из веб-слоя в бизнес-модель
     *
     * @param type тип распределения
     * @return бизнес-модель
     */
    default ru.sber.transport.limits.business.model.SharingType toBusiness(ru.sber.transport.limits.web.model.SharedType type) {
        if (type == null) {
            return null;
        }
        return ru.sber.transport.limits.business.model.SharingType.valueOf(type.name());
    }

    /**
     * Преобразовать тип распределения в модель для веб-слоя
     *
     * @param type тип распределения
     * @return модель для веб-слоя
     */
    default ru.sber.transport.limits.web.model.SharedType toWeb(ru.sber.transport.limits.business.model.SharingType type) {
        if (type == null) {
            return null;
        }
        return ru.sber.transport.limits.web.model.SharedType.valueOf(type.name());
    }

}
