package ru.sberbank.ditsib.corpclient.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import ru.sberbank.ditsib.corpclient.database.model.ActiveStatus;
import ru.sberbank.ditsib.corpclient.dto.EmployeeStatus;

/**
 * Маппер статусов
 */
@Mapper
public interface ActiveStatusMapper {

    /**
     * Маппинг статуса в признак
     *
     * @param activeStatus статус
     * @return признак
     */
    @Named("deleted")
    default boolean toDeletedDto(ActiveStatus activeStatus) {
        return ActiveStatus.INACTIVE.equals(activeStatus);
    }

    /**
     * Маппинг статуса в признак
     *
     * @param activeStatus статус
     * @return признак
     */
    @Named("active")
    default boolean toActiveDto(ActiveStatus activeStatus) {
        return ActiveStatus.ACTIVE.equals(activeStatus);
    }

    /**
     * Маппинг статуса в строку
     *
     * @param activeStatus статус
     * @return строка
     */
    default String toDto(ActiveStatus activeStatus) {
        return activeStatus.name();
    }

    /**
     * Маппинг признака в статус
     *
     * @param active признак
     * @return статус
     */
    default ActiveStatus toActiveModel(boolean active) {
        return active ? ActiveStatus.ACTIVE : ActiveStatus.INACTIVE;
    }

    /**
     * Маппинг статуса в модель
     *
     * @param status статус
     * @return модель
     */
    EmployeeStatus toEmployeeStatus(ActiveStatus status);

}
