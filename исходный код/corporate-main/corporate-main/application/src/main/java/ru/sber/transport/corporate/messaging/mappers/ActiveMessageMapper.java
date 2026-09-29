package ru.sber.transport.corporate.messaging.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import ru.sber.transport.corporate.business.model.Active;

/**
 * Маппер статусов.
 */
@Mapper
public interface ActiveMessageMapper {

    /**
     * Квалификатор конвертера для признака удаления
     */
    String MAP_DELETED = "deleted";

    /**
     * Квалификатор конвертера для признака активности
     */
    String MAP_ACTIVE = "active";

    /**
     * Маппинг признака удаления
     *
     * @param status статус
     * @return признак удаления
     */
    @Named(MAP_DELETED)
    default boolean deleted(Active status) {
        return Active.INACTIVE.equals(status);
    }

    /**
     * Маппинг признака активности
     *
     * @param status статус
     * @return признак активности
     */
    @Named(MAP_ACTIVE)
    default boolean active(Active status) {
        return Active.ACTIVE.equals(status);
    }

    /**
     * Конвертация сообщения в модель,
     *
     * @param payload сообщение.
     * @return модель.
     */
    default Active toBusiness(boolean payload) {
        return payload ? Active.ACTIVE : Active.INACTIVE;
    }

}
