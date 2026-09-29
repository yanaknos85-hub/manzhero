package ru.sberbank.ditsib.transport.approvals.mappers;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import ru.sber.transport.approvals.messaging.ApprovalsSettingsMessage;
import ru.sber.transport.approvals.messaging.OtherTrTypesApprovalsSettingsMessage;
import ru.sber.transport.approvals.messaging.PublicApprovalsSettingsMessage;
import ru.sberbank.ditsib.transport.approvals.database.model.*;
import ru.sberbank.ditsib.transport.approvals.dto.settings.*;

import java.util.List;

/**
 * Маппер настроек согласования
 */
@Mapper(uses = { PurposeAndRegionApprovalSettingsMapper.class })
public interface ApprovalsSettingsMapper {
    
    @Mapping(target = "organizationId", source = "organization.id")
    TaxiApprovalsSettingsDTO toDto(TaxiApprovalsSettings source);
    
    @Mapping(target = "organizationId", source = "organization.id")
    GroupTransferApprovalsSettingsDTO toDto(GroupTransferApprovalsSettings source);
    
    @Mapping(target = "organizationId", source = "organization.id")
    PublicTrApprovalsSettingsDTO toDto(PublicTrApprovalsSettings source);
    
    @Mapping(target = "organizationId", source = "organization.id")
    OtherTrTypesApprovalsSettingsDTO toDto(OtherTrTypesApprovalsSettings source);

    TaxiApprovalsSettings toModel(TaxiApprovalsSettingsDTO source);

    PublicTrApprovalsSettings toModel(PublicTrApprovalsSettingsDTO source);
    
    OtherTrTypesApprovalsSettings toModel(OtherTrTypesApprovalsSettingsDTO source);
    
    TaxiApprovalsSettings toModel(NewTaxiApprovalsSettingsDTO source);
    
    GroupTransferApprovalsSettings toModel(NewGroupTransferApprovalsSettingsDTO source);
    
    PublicTrApprovalsSettings toModel(NewPublicTrApprovalsSettingsDTO source);
    
    OtherTrTypesApprovalsSettings toModel(NewOtherTrTypesApprovalsSettingsDTO source);

    @Mapping(target = "tripPurposeId", source = "tripPurpose.id")
    @Mapping(target = "regionId", source = "region.id")
    ApprovalsSettingsMessage.PurposeAndRegionApprovalSettingsItem toMessageItem(PurposeAndRegionApprovalSettingsItem item);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    List<ApprovalsSettingsMessage.PurposeAndRegionApprovalSettingsItem> toMessageItems(
            List<PurposeAndRegionApprovalSettingsItem> items
    );
    
    @Mapping(target = "organizationId", source = "organization.id")
    ApprovalsSettingsMessage toTaxiMessage(TaxiApprovalsSettings taxiSettings);
    
    @Mapping(target = "organizationId", source = "organization.id")
    ApprovalsSettingsMessage toGroupTransferMessage(GroupTransferApprovalsSettings taxiSettings);
    
    @Mapping(target = "organizationId", source = "organization.id")
    PublicApprovalsSettingsMessage toPublicMessage(PublicTrApprovalsSettings publicSettings);
    
    @Mapping(target = "organizationId", source = "organization.id")
    OtherTrTypesApprovalsSettingsMessage toOtherTrMessage(OtherTrTypesApprovalsSettings otherTrSettings);
}
