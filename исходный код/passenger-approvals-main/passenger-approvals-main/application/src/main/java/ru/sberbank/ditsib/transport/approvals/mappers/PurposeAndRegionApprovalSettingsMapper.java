package ru.sberbank.ditsib.transport.approvals.mappers;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import ru.sberbank.ditsib.transport.approvals.database.model.PurposeAndRegionApprovalSettingsItem;
import ru.sberbank.ditsib.transport.approvals.database.model.TripPurpose;
import ru.sberbank.ditsib.transport.approvals.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.approvals.dto.TripPurposeDTO;
import ru.sberbank.ditsib.transport.approvals.dto.settings.GeoZoneDTO;
import ru.sberbank.ditsib.transport.approvals.dto.settings.NewPurposeAndRegionApprovalSettingsItemDTO;
import ru.sberbank.ditsib.transport.approvals.dto.settings.PurposeAndRegionApprovalSettingsItemDTO;

import java.util.List;

@Mapper
public interface PurposeAndRegionApprovalSettingsMapper {

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<PurposeAndRegionApprovalSettingsItem> listDtoToModel(List<PurposeAndRegionApprovalSettingsItemDTO> source);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<PurposeAndRegionApprovalSettingsItem> newListDtoToModel(List<NewPurposeAndRegionApprovalSettingsItemDTO> source);
    
    GeoZoneDTO geoZoneToDto(GeoZone region);
    TripPurposeDTO purposeToDto(TripPurpose purpose);
    
    @Mapping(target = "tripPurpose.id", source = "purposeId")
    @Mapping(target = "region.id", source = "regionId")
    PurposeAndRegionApprovalSettingsItem newDtoToModel(NewPurposeAndRegionApprovalSettingsItemDTO source);
    
    PurposeAndRegionApprovalSettingsItemDTO modelToPurpose(PurposeAndRegionApprovalSettingsItem source);

}
