package ru.sberbank.ditsib.transport.approvals.controller.impl;

import org.junit.jupiter.api.BeforeEach;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sberbank.ditsib.transport.approvals.database.model.Organization;
import ru.sberbank.ditsib.transport.approvals.database.model.PurposeAndRegionApprovalSettingsItem;
import ru.sberbank.ditsib.transport.approvals.database.model.TripPurpose;
import ru.sberbank.ditsib.transport.approvals.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.approvals.dto.settings.GeoZoneDTO;
import ru.sberbank.ditsib.transport.approvals.dto.settings.NewPurposeAndRegionApprovalSettingsItemDTO;

import java.util.UUID;

public class ApprovalsSettingsSharedTestData extends KafkaTest {
    
    public static final String USER1_ID = "f10bcc5b-51db-4e1c-a747-2a229604f974";
    public static final String USER2_ID = "f10bcc5b-51db-4e1c-a747-2a229604f975";
    public static final String ORGANIZATION_ID_1 = "6871004b-581d-4fac-8bf0-0f4014bf2a09";
    public static final String ORGANIZATION_ID_2 = "c661d65f-46f9-4007-bd1f-495199c35da2";
    public static final String PURPOSE_ID_1 = "45ebeef6-9157-4464-a349-0944a1031914";
    public static final String PURPOSE_ID_2 = "45ebeef6-9157-4464-a349-0944a1031915";
    public static final String PURPOSE_LABEL_1 = "Purpose 1";
    public static final String PURPOSE_LABEL_2 = "Purpose 2";
    public static final UUID REGION_ID_1 = UUID.fromString("45ebeef6-9157-4464-a349-aaaaaaaaaaa1");
    public static final UUID REGION_ID_2 = UUID.fromString("45ebeef6-9157-4464-a349-aaaaaaaaaaa2");
    
    public TripPurpose purpose1;
    public TripPurpose purpose2;
    public Organization organization1;
    public Organization organization2;
    public PurposeAndRegionApprovalSettingsItem item1;
    public PurposeAndRegionApprovalSettingsItem item2;
    public NewPurposeAndRegionApprovalSettingsItemDTO newPurposeAndRegionDTO1;
    public NewPurposeAndRegionApprovalSettingsItemDTO newPurposeAndRegionDTO2;
    public GeoZone geoZone1;
    public GeoZone geoZone2;
    
    @BeforeEach
    public void createEntities() {
        organization1 = Organization.builder()
                                    .id(UUID.fromString(ORGANIZATION_ID_1))
                                    .digitId(1L)
                                    .build();
        organization2 = Organization.builder()
                                    .id(UUID.fromString(ORGANIZATION_ID_2))
                                    .digitId(2L)
                                    .build();
    
        purpose1 = TripPurpose.builder()
                              .label(PURPOSE_LABEL_1)
                              .id(UUID.fromString(PURPOSE_ID_1))
                              .build();
    
        purpose2 = TripPurpose.builder()
                              .label(PURPOSE_LABEL_2)
                              .id(UUID.fromString(PURPOSE_ID_2))
                              .build();
        
        geoZone1 = createGeoZone(REGION_ID_1);
        geoZone2 = createGeoZone(REGION_ID_2);
        
        item1 = PurposeAndRegionApprovalSettingsItem.builder()
                                                    .region(geoZone1)
                                                    .minCostToBeApproved(500)
                                                    .tripPurpose(purpose1)
                                                    .build();
        item2 = PurposeAndRegionApprovalSettingsItem.builder()
                                                    .region(geoZone2)
                                                    .minCostToBeApproved(500)
                                                    .tripPurpose(purpose2)
                                                    .build();
        
        var geoZoneDto1 = GeoZoneDTO.builder().id(REGION_ID_1).build();
        var geoZoneDto2 = GeoZoneDTO.builder().id(REGION_ID_2).build();

        newPurposeAndRegionDTO1 = NewPurposeAndRegionApprovalSettingsItemDTO.builder()
                                                                            .purposeId(purpose1.getId())
                                                                            .regionId(geoZoneDto1.getId())
                                                                            .minCostToBeApproved(250)
                                                                            .build();
    
        newPurposeAndRegionDTO2 = NewPurposeAndRegionApprovalSettingsItemDTO.builder()
                                                                            .purposeId(purpose2.getId())
                                                                            .regionId(geoZoneDto2.getId())
                                                                            .minCostToBeApproved(250)
                                                                            .build();

    }
    
    protected GeoZone createGeoZone(UUID id) {
        var geoZone = new GeoZone();
        geoZone.setCode((long) (Math.random() * 10000000) + "");
        geoZone.setId(id);
        geoZone.setParentId(UUID.randomUUID());
        geoZone.setName("Region" + (long) (Math.random() * 100));
        return geoZone;
    }
}
