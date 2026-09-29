package ru.sber.transport.corporate.providers.trip_purpose;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.corporate.business.model.TripPurpose;
import ru.sber.transport.database.corporate.tables.records.TripPurposeRecord;

/**
 * Маппер объектов базы данных в бизнес.
 */
@Mapper
public interface TripPurposeDatabaseMapper {

    /**
     * Маппер объекта базы данных в бизнес.
     *
     * @param source бизнес объект в базе данных.
     * @return бизнес объект.
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "label", source = "label")
    TripPurpose toBusiness(TripPurposeRecord source);
}
