package ru.sberbank.ditsib.transport.approvals.mappers;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sberbank.ditsib.transport.approvals.TestSharedData;
import ru.sberbank.ditsib.transport.approvals.database.model.Organization;
import ru.sberbank.ditsib.transport.approvals.database.model.PurposeAndRegionApprovalSettingsItem;
import ru.sberbank.ditsib.transport.approvals.dto.TripPurposeDTO;
import ru.sberbank.ditsib.transport.approvals.dto.settings.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.sberbank.ditsib.transport.approvals.TestSharedData.*;

@DisplayName("Проверка маппера настроек согласования")
class ApprovalsSettingsMapperTest {

    private final ApprovalsSettingsMapper mapper =
            new ApprovalsSettingsMapperImpl(new PurposeAndRegionApprovalSettingsMapperImpl());

    private final TestSharedData sharedData = new TestSharedData();

    private Organization organization;
    private PurposeAndRegionApprovalSettingsItem item1;
    private PurposeAndRegionApprovalSettingsItem item2;

    private void init() {
        organization = sharedData.createOrganization(ORGANIZATION_1_ID);
        var region1 = sharedData.createGeoZone(REGION1_ID);
        var region2 = sharedData.createGeoZone(REGION2_ID);
        var tripPurpose1 = sharedData.createTripPurpose(PURPOSE1_ID);
        var tripPurpose2 = sharedData.createTripPurpose(PURPOSE2_ID);
        item1 = sharedData.createSettingsItem(region1, 1000, tripPurpose1);
        item2 = sharedData.createSettingsItem(region2, 1500, tripPurpose2);
    }

    @Test
    @DisplayName("DTO Настройки согласований заявок на такси в модель")
    void test_TaxiDtoToModel() {
        var purposeItem = PurposeAndRegionApprovalSettingsItemDTO.builder()
                .region(createGeoZoneDto(UUID.randomUUID()))
                .tripPurpose(TripPurposeDTO
                        .builder()
                        .id(UUID.randomUUID())
                        .label("purposeItem")
                        .build()).build();
        var purposeItem1 = PurposeAndRegionApprovalSettingsItemDTO.builder()
                .region(createGeoZoneDto(UUID.randomUUID()))
                .tripPurpose(TripPurposeDTO.builder()
                        .id(UUID.randomUUID())
                        .label("purposeItem1")
                        .build()).build();
        var purposeItem2 = PurposeAndRegionApprovalSettingsItemDTO.builder()
                .region(createGeoZoneDto(UUID.randomUUID()))
                .tripPurpose(TripPurposeDTO.builder()
                        .id(UUID.randomUUID())
                        .label("purposeItem2")
                        .build()).build();

        var dto = TaxiApprovalsSettingsDTO.builder()
                .id(UUID.fromString("fd9a8c66-81c0-4b37-a4f6-0c0fc6c8f97b"))
                .approvalActive(true)
                .minCostToBeApproved(500)
                .transportType("TAXI")
                .purposeAndRegionItems(Arrays.asList(purposeItem, purposeItem1, purposeItem2))
                .build();

        var model = mapper.toModel(dto);

        assertThat(model.getId()).isEqualTo(dto.getId());
        assertThat(model.getMinCostToBeApproved()).isEqualTo(dto.getMinCostToBeApproved());
        assertThat(model.getTransportType()).isEqualTo(dto.getTransportType());
        var modelPurposeItems = model.getPurposeAndRegionItems();
        var dtoPurposeItems = dto.getPurposeAndRegionItems();
        for (int i = 0; i < model.getPurposeAndRegionItems().size(); i++) {
            assertThat(modelPurposeItems.get(i).getMinCostToBeApproved())
                    .isEqualTo(dtoPurposeItems.get(i).getMinCostToBeApproved());
            assertThat(modelPurposeItems.get(i).getRegion().getId()).isEqualTo(dtoPurposeItems.get(i).getRegion().getId());
            assertThat(modelPurposeItems.get(i).getTripPurpose().getId())
                    .isEqualTo(dtoPurposeItems.get(i).getTripPurpose().getId());
            assertThat(modelPurposeItems.get(i).getTripPurpose().getLabel())
                    .isEqualTo(dtoPurposeItems.get(i).getTripPurpose().getLabel());
        }
    }

    @Test
    @DisplayName("DTO Настройки согласований заявок на поездку на общественном транспорте в модель")
    void test_PublicDtoToModel() {
        var purposeItem = PurposeAndRegionApprovalSettingsItemDTO.builder()
                .region(createGeoZoneDto(UUID.randomUUID()))
                .tripPurpose(TripPurposeDTO
                        .builder()
                        .id(UUID.randomUUID())
                        .label("purposeItem")
                        .build()).build();
        var purposeItem1 = PurposeAndRegionApprovalSettingsItemDTO.builder()
                .region(createGeoZoneDto(UUID.randomUUID()))
                .tripPurpose(TripPurposeDTO.builder()
                        .id(UUID.randomUUID())
                        .label("purposeItem1")
                        .build()).build();
        var purposeItem2 = PurposeAndRegionApprovalSettingsItemDTO.builder()
                .region(createGeoZoneDto(UUID.randomUUID()))
                .tripPurpose(TripPurposeDTO.builder()
                        .id(UUID.randomUUID())
                        .label("purposeItem2")
                        .build()).build();

        var dto = PublicTrApprovalsSettingsDTO.builder()
                .id(UUID.fromString("fd9a8c66-81c0-4b37-a4f6-0c0fc6c8f97b"))
                .approvalActive(true)
                .minCostToBeApproved(500)
                .transportType("PUBLIC")
                .purposeAndRegionItems(Arrays.asList(purposeItem, purposeItem1, purposeItem2))
                .approvalDocumentCheck(true)
                .tripConfirmationActive(true)
                .tripConfirmationDocumentCheck(true)
                .affirmativeActive(true)
                .build();

        var model = mapper.toModel(dto);

        assertThat(model.getId()).isEqualTo(dto.getId());
        assertThat(model.getMinCostToBeApproved()).isEqualTo(dto.getMinCostToBeApproved());
        assertThat(model.getTransportType()).isEqualTo(dto.getTransportType());
        var modelPurposeItems = model.getPurposeAndRegionItems();
        var dtoPurposeItems = dto.getPurposeAndRegionItems();
        for (int i = 0; i < model.getPurposeAndRegionItems().size(); i++) {
            assertThat(modelPurposeItems.get(i).getMinCostToBeApproved())
                    .isEqualTo(dtoPurposeItems.get(i).getMinCostToBeApproved());
            assertThat(modelPurposeItems.get(i).getRegion().getId()).isEqualTo(dtoPurposeItems.get(i).getRegion().getId());
            assertThat(modelPurposeItems.get(i).getTripPurpose().getId())
                    .isEqualTo(dtoPurposeItems.get(i).getTripPurpose().getId());
            assertThat(modelPurposeItems.get(i).getTripPurpose().getLabel())
                    .isEqualTo(dtoPurposeItems.get(i).getTripPurpose().getLabel());
        }
        assertThat(model.isApprovalDocumentCheck()).isEqualTo(dto.isApprovalDocumentCheck());
        assertThat(model.isTripConfirmationActive()).isEqualTo(dto.isTripConfirmationActive());
        assertThat(model.isTripConfirmationDocumentCheck()).isEqualTo(dto.isTripConfirmationDocumentCheck());
        assertThat(model.isAffirmativeActive()).isEqualTo(dto.isAffirmativeActive());
    }

    @Test
    @DisplayName(
            "DTO Настройки согласований заявок на поездку на любом транспорте, кроме общественного и такси, в модель")
    void test_OtherDtoToModel() {
        var purposeItem = PurposeAndRegionApprovalSettingsItemDTO.builder()
                .region(createGeoZoneDto(UUID.randomUUID()))
                .tripPurpose(TripPurposeDTO
                        .builder()
                        .id(UUID.randomUUID())
                        .label("purposeItem")
                        .build()).build();
        var purposeItem1 = PurposeAndRegionApprovalSettingsItemDTO.builder()
                .region(createGeoZoneDto(UUID.randomUUID()))
                .tripPurpose(TripPurposeDTO.builder()
                        .id(UUID.randomUUID())
                        .label("purposeItem1")
                        .build()).build();
        var purposeItem2 = PurposeAndRegionApprovalSettingsItemDTO.builder()
                .region(createGeoZoneDto(UUID.randomUUID()))
                .tripPurpose(TripPurposeDTO.builder()
                        .id(UUID.randomUUID())
                        .label("purposeItem2")
                        .build()).build();

        var dto = OtherTrTypesApprovalsSettingsDTO.builder()
                .id(UUID.fromString("fd9a8c66-81c0-4b37-a4f6-0c0fc6c8f97b"))
                .approvalActive(true)
                .minCostToBeApproved(500)
                .transportType("PUBLIC")
                .purposeAndRegionItems(Arrays.asList(purposeItem, purposeItem1, purposeItem2))
                .tripApprovalActive(true)
                .build();

        var model = mapper.toModel(dto);

        assertThat(model.getId()).isEqualTo(dto.getId());
        assertThat(model.getMinCostToBeApproved()).isEqualTo(dto.getMinCostToBeApproved());
        assertThat(model.getTransportType()).isEqualTo(dto.getTransportType());
        var modelPurposeItems = model.getPurposeAndRegionItems();
        var dtoPurposeItems = dto.getPurposeAndRegionItems();
        for (int i = 0; i < model.getPurposeAndRegionItems().size(); i++) {
            assertThat(modelPurposeItems.get(i).getMinCostToBeApproved())
                    .isEqualTo(dtoPurposeItems.get(i).getMinCostToBeApproved());
            assertThat(modelPurposeItems.get(i).getRegion().getId()).isEqualTo(dtoPurposeItems.get(i).getRegion().getId());
            assertThat(modelPurposeItems.get(i).getTripPurpose().getId())
                    .isEqualTo(dtoPurposeItems.get(i).getTripPurpose().getId());
            assertThat(modelPurposeItems.get(i).getTripPurpose().getLabel())
                    .isEqualTo(dtoPurposeItems.get(i).getTripPurpose().getLabel());
        }
        assertThat(model.isTripApprovalActive()).isEqualTo(dto.isTripApprovalActive());
    }

    @Test
    @DisplayName(
            "Новые настройки согласований заявок на поездку на такси такси в модель")
    void test_newTaxiDtoToModel() {
        var newPurposeItem1 = NewPurposeAndRegionApprovalSettingsItemDTO.builder()
                .purposeId(UUID.fromString(
                        "b91a0733-9e72-4507-ad02-146741b918cc"))
                .minCostToBeApproved(500)
                .regionId(UUID.randomUUID())
                .build();

        var newPurposeItem2 = NewPurposeAndRegionApprovalSettingsItemDTO.builder()
                .purposeId(UUID.fromString(
                        "5814c1b5-d799-4c1e-a062-c3329480a238"))
                .minCostToBeApproved(500)
                .regionId(UUID.randomUUID())
                .build();

        var newDto = NewTaxiApprovalsSettingsDTO.builder()
                .approvalActive(true)
                .minCostToBeApproved(500)
                .purposeAndRegionItems(Arrays.asList(newPurposeItem1, newPurposeItem2))
                .build();

        var model = mapper.toModel(newDto);

        assertThat(model.getMinCostToBeApproved()).isEqualTo(newDto.getMinCostToBeApproved());
        var modelPurposeItems = model.getPurposeAndRegionItems();
        var dtoPurposeItems = newDto.getPurposeAndRegionItems();
        for (int i = 0; i < model.getPurposeAndRegionItems().size(); i++) {
            assertThat(modelPurposeItems.get(i).getMinCostToBeApproved())
                    .isEqualTo(dtoPurposeItems.get(i).getMinCostToBeApproved());
            assertThat(modelPurposeItems.get(i).getRegion().getId()).isEqualTo(dtoPurposeItems.get(i).getRegionId());
        }
    }


    @Test
    @DisplayName(
            "Новые настройки согласований заявок на поездку на общественном транспорте, в модель")
    void test_newPublicTrDtoToModel() {
        var newPurposeItem1 = NewPurposeAndRegionApprovalSettingsItemDTO.builder()
                .purposeId(UUID.fromString(
                        "b91a0733-9e72-4507-ad02-146741b918cc"))
                .minCostToBeApproved(500)
                .regionId(UUID.randomUUID())
                .build();

        var newPurposeItem2 = NewPurposeAndRegionApprovalSettingsItemDTO.builder()
                .purposeId(UUID.fromString(
                        "5814c1b5-d799-4c1e-a062-c3329480a238"))
                .minCostToBeApproved(500)
                .regionId(UUID.randomUUID())
                .build();

        var newDto = NewPublicTrApprovalsSettingsDTO.builder()
                .approvalActive(true)
                .minCostToBeApproved(500)
                .purposeAndRegionItems(Arrays.asList(newPurposeItem1, newPurposeItem2))
                .tripConfirmationActive(false)
                .affirmativeActive(false)
                .approvalDocumentCheck(false)
                .tripConfirmationDocumentCheck(false)
                .build();

        var model = mapper.toModel(newDto);

        assertThat(model.getMinCostToBeApproved()).isEqualTo(newDto.getMinCostToBeApproved());
        var modelPurposeItems = model.getPurposeAndRegionItems();
        var dtoPurposeItems = newDto.getPurposeAndRegionItems();
        for (int i = 0; i < model.getPurposeAndRegionItems().size(); i++) {
            assertThat(modelPurposeItems.get(i).getMinCostToBeApproved())
                    .isEqualTo(dtoPurposeItems.get(i).getMinCostToBeApproved());
            assertThat(modelPurposeItems.get(i).getRegion().getId()).isEqualTo(dtoPurposeItems.get(i).getRegionId());
        }
        assertThat(model.isTripConfirmationActive()).isEqualTo(newDto.isTripConfirmationActive());
        assertThat(model.isAffirmativeActive()).isEqualTo(newDto.isAffirmativeActive());
        assertThat(model.isApprovalDocumentCheck()).isEqualTo(newDto.isApprovalDocumentCheck());
        assertThat(model.isTripConfirmationDocumentCheck()).isEqualTo(newDto.isTripConfirmationDocumentCheck());
    }

    @Test
    @DisplayName(
            "Новые настройки согласований заявок на поездку на любом транспорте, кроме общественного и такси, в модель")
    void test_newOtherTrDtoToModel() {
        var newPurposeItem1 = NewPurposeAndRegionApprovalSettingsItemDTO.builder()
                .purposeId(UUID.fromString(
                        "b91a0733-9e72-4507-ad02-146741b918cc"))
                .minCostToBeApproved(500)
                .regionId(UUID.randomUUID())
                .build();

        var newPurposeItem2 = NewPurposeAndRegionApprovalSettingsItemDTO.builder()
                .purposeId(UUID.fromString(
                        "5814c1b5-d799-4c1e-a062-c3329480a238"))
                .minCostToBeApproved(500)
                .regionId(UUID.randomUUID())
                .build();

        var newDto = NewOtherTrTypesApprovalsSettingsDTO.builder()
                .approvalActive(true)
                .minCostToBeApproved(500)
                .purposeAndRegionItems(Arrays.asList(newPurposeItem1, newPurposeItem2))
                .build();

        var model = mapper.toModel(newDto);

        assertThat(model.getMinCostToBeApproved()).isEqualTo(newDto.getMinCostToBeApproved());
        var modelPurposeItems = model.getPurposeAndRegionItems();
        var dtoPurposeItems = newDto.getPurposeAndRegionItems();
        for (int i = 0; i < model.getPurposeAndRegionItems().size(); i++) {
            assertThat(modelPurposeItems.get(i).getMinCostToBeApproved())
                    .isEqualTo(dtoPurposeItems.get(i).getMinCostToBeApproved());
            assertThat(modelPurposeItems.get(i).getRegion().getId()).isEqualTo(dtoPurposeItems.get(i).getRegionId());
        }
        assertThat(model.isTripApprovalActive()).isEqualTo(newDto.isTripApprovalActive());
    }

    @Test
    void toTaxiMessage() {
        init();
        var settings = sharedData.createTaxiSettings(true,
                700,
                organization,
                "TAXI",
                List.of(item1, item2));
        settings.setId(UUID.randomUUID());
        var message = mapper.toTaxiMessage(settings);
        sharedData.commonCheck(message, settings);
    }

    @Test
    void toPublicTrMessage() {
        init();
        var settings = sharedData.createPublicSettings(
                organization, "PUBLIC", true, false, true, false);
        settings.setId(UUID.randomUUID());
        settings.setApprovalActive(true);
        settings.setMinCostToBeApproved(700);
        settings.setPurposeAndRegionItems(List.of(item1, item2));
        var message = mapper.toPublicMessage(settings);
        sharedData.publicCheck(message, settings);
    }

    @Test
    void toOtherTrMessage() {
        init();
        var settings = sharedData.createOtherTrSettings(true, 700, organization, "CARSHARING", List.of(item1, item2), false);
        settings.setId(UUID.randomUUID());
        var message = mapper.toOtherTrMessage(settings);
        sharedData.otherTrCheck(message, settings);
    }


    private GeoZoneDTO createGeoZoneDto(UUID id) {
        GeoZoneDTO geoZone = new GeoZoneDTO();
        geoZone.setCode(Instancio.create(Long.class));
        geoZone.setId(id);
        geoZone.setParentId(UUID.randomUUID());
        geoZone.setName("Region" + Instancio.create(Long.class));
        return geoZone;
    }
}
