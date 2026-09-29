package ru.sberbank.ditsib.corpclient.mapper;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.corpclient.database.model.messages.GeoZone;
import ru.sberbank.ditsib.corpclient.dto.GeoZoneShortDTO;

import java.util.UUID;

/**
 * Маппер гео зоны.
 */
@Mapper
public interface GeoZoneMapper {

    GeoZone geoZoneFromId(UUID id);

    GeoZoneShortDTO geoZoneToShortDTO(GeoZone geoZone);
}
