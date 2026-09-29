package ru.sberbank.ditsib.transport.approvals;

import org.instancio.Instancio;
import ru.sber.transport.approvals.messaging.ApprovalsSettingsMessage;
import ru.sber.transport.approvals.messaging.OtherTrTypesApprovalsSettingsMessage;
import ru.sber.transport.approvals.messaging.PublicApprovalsSettingsMessage;
import ru.sberbank.ditsib.transport.approvals.database.model.*;
import ru.sberbank.ditsib.transport.approvals.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.approvals.database.model.messages.Tariff;
import ru.sberbank.ditsib.transport.approvals.database.model.messages.TaxiTariff;
import ru.sberbank.ditsib.transport.approvals.messaging.message.TariffMessage;
import ru.sberbank.ditsib.transport.approvals.messaging.message.TaxiTariffMessage;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static ru.sberbank.ditsib.transport.constants.TransportServiceType.EMPLOYEE_TRANSPORTATION;
import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.TAXI;

public class TestSharedData {
    
    public static final UUID CARSHARING_ID = TransportTypeEnum.CARSHARING.getId();
    public static final UUID TAXI_ID = TAXI.getId();
    public static final UUID PERSONAL_ID = TransportTypeEnum.PERSONAL.getId();
    public static final UUID PUBLIC_ID = TransportTypeEnum.PUBLIC.getId();
    
    public static final UUID REGION1_ID = UUID.fromString("cc2eb25b-b5d0-429b-bf1e-ccccccccccc1");
    public static final UUID REGION2_ID = UUID.fromString("cc2eb25b-b5d0-429b-bf1e-ccccccccccc2");
    public static final UUID REGION3_ID = UUID.fromString("cc2eb25b-b5d0-429b-bf1e-ccccccccccc3");
    public static final UUID PURPOSE1_ID = UUID.fromString("cc2eb25b-b5d0-429b-bf1e-eeeeeeeeeee1");
    public static final UUID PURPOSE2_ID = UUID.fromString("cc2eb25b-b5d0-429b-bf1e-eeeeeeeeeee2");
    public static final UUID PURPOSE3_ID = UUID.fromString("cc2eb25b-b5d0-429b-bf1e-eeeeeeeeeee3");
    public static final UUID CONTRACT1_ID = UUID.fromString("cc2eb25b-b5d0-429b-bf1e-bbbbbbbbbbb1");
    public static final UUID CONTRACT2_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-bbbbbbbbbbb2");
    public static final UUID CONTRACT3_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-bbbbbbbbbbb3");
    public static final UUID TARIFF1_ID = UUID.fromString("cc2eb25b-b5d0-429b-bf1e-aaaaaaaaaaa1");
    public static final UUID TARIFF2_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-aaaaaaaaaaa2");
    public static final UUID TARIFF3_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-aaaaaaaaaaa3");
    public static final UUID ORGANIZATION_1_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-ddddddddddd1");
    public static final UUID ORGANIZATION_2_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-ddddddddddd2");
    public static final UUID EMPLOYEE1_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-eeeeeeeeeee1");
    public static final UUID DEPARTMENT1_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-dddddeeeeee1");

    /**
     * Создать сообщение о тарифе. Пишется regionId, region не пишется
     * @param tariffId ID тарифа
     * @param regionId ID геозоны
     * @param organizationId ID корп.клиента
     * @param humanReadableId человекочитаемый id. Может быть null и тогда поле сгенерится случайно
     * @param transportTypeId ID типа транспорта
     * @param contractId ID контракта
     * @param deleted флаг удаения тарифа
     * @return TariffMessage
     */
    public TariffMessage createTariffMessage(UUID tariffId, UUID regionId, UUID organizationId, String humanReadableId,
                                             UUID transportTypeId, UUID contractId, boolean deleted) {
        return TariffMessage.builder()
                            .id(tariffId)
                            .humanReadableId(humanReadableId == null
                                             ? "TF-" + Instancio.create(Integer.class) + "-" + Instancio.create(Integer.class)
                                             : humanReadableId)
                            .regionId(regionId)
                            .organizationId(organizationId)
                            .transportTypeId(transportTypeId)
                            .contractId(contractId)
                            .deleted(deleted)
                            .build();
    }
    
    /**
     * Создать сообщение о тарифе такси. Пишется regionId, region не пишется
     * @param tariffId ID тарифа
     * @param regionId ID геозоны
     * @param humanReadableId человекочитаемый id. Может быть null и тогда поле сгенерится случайно
     * @param contractId ID контракта
     * @param organizationId ID корп.клиента
     * @param taxiClass класс такси
     * @param delete флаг удаления
     * @return TaxiTariffMessage
     */
    public TaxiTariffMessage createTaxiTariffMessage(
            UUID tariffId, UUID regionId, String humanReadableId, UUID contractId, UUID organizationId,
            TaxiClass taxiClass, boolean delete) {
        return TaxiTariffMessage.builder()
                                .id(tariffId)
                                .humanReadableId(humanReadableId == null
                                                 ? "TF-" + Instancio.create(Integer.class) + "-" + Instancio.create(Integer.class)
                                                 : humanReadableId)
                                .regionId(regionId)
                                .transportType(TAXI.name())
                                .contractId(contractId)
                                .organizationId(organizationId)
                                .deleted(delete)
                                .serviceType(EMPLOYEE_TRANSPORTATION.name())
                                .taxiClass(taxiClass.name())
                                .build();
    }
    
    /**
     * Проверить записанный в БД тариф по приходу сообщения. Проверяется regionId, region не проверяется
     * @param tariff Tariff
     * @param message TariffMessage
     */
    public void checkTariff (Tariff tariff, TariffMessage message){
        assertThat(tariff).isNotNull();
        assertThat(message).isNotNull();
        assertThat(tariff.getId()).isEqualTo(message.getId());
        assertThat(tariff.getHumanReadableId()).isEqualTo(message.getHumanReadableId());
        assertThat(tariff.getOrganizationId()).isEqualTo(message.getOrganizationId());
        assertThat(tariff.getRegionId()).isEqualTo(message.getRegionId());
        assertThat(tariff.getContractId()).isEqualTo(message.getContractId());
    }
    
    /**
     * Проверить записанный в БД тариф по приходу сообщения. Проверяется regionId, region не проверяется
     * @param tariff Tariff
     * @param message TariffMessage
     */
    public void checkTaxiTariff (TaxiTariff tariff, TaxiTariffMessage message){
        assertThat(tariff).isNotNull();
        assertThat(message).isNotNull();
        assertThat(tariff.getId()).isEqualTo(message.getId());
        assertThat(tariff.getHumanReadableId()).isEqualTo(message.getHumanReadableId());
        assertThat(tariff.getTransportType()).isEqualTo(TAXI.name());
        assertThat(message.getTransportType()).isEqualTo(TAXI.name());
        assertThat(tariff.getServiceType()).isEqualTo(EMPLOYEE_TRANSPORTATION);
        assertThat(message.getServiceType()).isEqualTo(EMPLOYEE_TRANSPORTATION.name());
        assertThat(tariff.getRegionId()).isEqualTo(message.getRegionId());
        assertThat(tariff.getContractId()).isEqualTo(message.getContractId());
        assertThat(tariff.getTaxiClass().name()).isEqualTo(message.getTaxiClass());
    }
    
    /**
     * Создать геозону
     * @param id ID геозоны
     * @return GeoZone
     */
    public GeoZone createGeoZone(UUID id) {
        GeoZone geoZone = new GeoZone();
        geoZone.setCode(Instancio.create(Integer.class) + "");
        geoZone.setId(id);
        geoZone.setParentId(UUID.randomUUID());
        geoZone.setName("Region #" + Instancio.create(Integer.class));
        return geoZone;
    }
    
    /**
     * Создать цель
     * @param id ID цели
     * @return TripPurpose
     */
    public TripPurpose createTripPurpose(UUID id) {
        return TripPurpose.builder()
                          .id(id)
                          .label("Purpose #" + Instancio.create(Integer.class))
                          .build();
    }
    
    public Organization createOrganization(UUID id) {
        return Instancio.of(Organization.class)
                .set(field(Organization::getId), id)
                .set(field(Organization::isActive), true)
                .create();
    }
    
    public Department createDepartment(UUID id, UUID organizationId) {
        return Instancio.of(Department.class)
                .set(field(Department::getId), id)
                .set(field(Department::getOrganizationId), organizationId)
                .set(field(Department::getParentId), null)
                .set(field(Department::getDepartmentHeadId), null)
                .set(field(Department::isActive), true)
                .set(field(Department::getApprovers), Collections.emptyList())
                .create();
    }
    
    public Employee createEmployee(UUID id, UUID departmentId, UUID positionId) {
        return Instancio.of(Employee.class)
                .set(field(Employee::getId), id)
                .set(field(Employee::getDepartmentId), departmentId)
                .set(field(Employee::getPositionId), positionId)
                .set(field(Employee::getSupervisorId), null)
                .set(field(Employee::isActive), true)
                .set(field(Employee::getApproveDepartments), Collections.emptyList())
                .create();
    }
    
    public TaxiApprovalsSettings createTaxiSettings(
            boolean needApproval, int minCostKop, Organization org, String transportType,
            List<PurposeAndRegionApprovalSettingsItem> purposeAndRegionItems) {
        return TaxiApprovalsSettings.builder()
                                    .approvalActive(needApproval)
                                    .minCostToBeApproved(minCostKop)
                                    .organization(org)
                                    .transportType(transportType)
                                    .purposeAndRegionItems(purposeAndRegionItems)
                                    .build();
    }
    
    public PublicTrApprovalsSettings createPublicSettings(
            Organization org, String transportType, boolean approvalDocumentCheck,
            boolean tripConfirmationDocumentCheck, boolean tripConfirmationActive, boolean affirmativeActive) {
        return PublicTrApprovalsSettings.builder()
                                        .organization(org)
                                        .transportType(transportType)
                                        .approvalDocumentCheck(approvalDocumentCheck)
                                        .tripConfirmationDocumentCheck(tripConfirmationDocumentCheck)
                                        .tripConfirmationActive(tripConfirmationActive)
                                        .affirmativeActive(affirmativeActive)
                                        .build();
    }
    
    public OtherTrTypesApprovalsSettings createOtherTrSettings(
            boolean needApproval, int minCostKop, Organization org, String transportType,
            List<PurposeAndRegionApprovalSettingsItem> purposeAndRegionItems, boolean needTripApproval) {
        return OtherTrTypesApprovalsSettings.builder()
                                            .approvalActive(needApproval)
                                            .tripApprovalActive(needTripApproval)
                                            .minCostToBeApproved(minCostKop)
                                            .organization(org)
                                            .transportType(transportType)
                                            .purposeAndRegionItems(purposeAndRegionItems)
                                            .build();
    }
    
    public PurposeAndRegionApprovalSettingsItem createSettingsItem(GeoZone region, int minCostKop, TripPurpose purpose) {
        return PurposeAndRegionApprovalSettingsItem.builder()
                                                   .region(region)
                                                   .minCostToBeApproved(minCostKop)
                                                   .tripPurpose(purpose)
                                                   .build();
    }
    
    public void commonCheck(ApprovalsSettingsMessage message, ApprovalsSettings settings) {
        assertThat(message).isNotNull();
        assertThat(message.getId()).isEqualTo(settings.getId());
        assertThat(message.getOrganizationId()).isEqualTo(settings.getOrganization().getId());
        assertThat(message.getTransportType()).isEqualTo(settings.getTransportType());
        assertThat(message.getMinCostToBeApproved()).isEqualTo(settings.getMinCostToBeApproved());
        assertThat(message.getPurposeAndRegionItems()).hasSameSizeAs(settings.getPurposeAndRegionItems());
        assertThat(message.isDeleted()).isFalse();
        for (PurposeAndRegionApprovalSettingsItem item : settings.getPurposeAndRegionItems()) {
            List<ApprovalsSettingsMessage.PurposeAndRegionApprovalSettingsItem> filteredMessageItem =
                    message.getPurposeAndRegionItems().stream()
                           .filter(mi -> mi.getRegionId().equals(item.getRegion().getId()) &&
                                         mi.getTripPurposeId().equals(item.getTripPurpose().getId()))
                           .toList();
            assertThat(filteredMessageItem).hasSize(1);
            assertThat(filteredMessageItem.getFirst().getMinCostToBeApproved()).isEqualTo(item.getMinCostToBeApproved());
        }
    }
    
    public void publicCheck(PublicApprovalsSettingsMessage message, PublicTrApprovalsSettings settings) {
        commonCheck(message, settings);
        assertThat(message.isAffirmativeActive()).isEqualTo(settings.isAffirmativeActive());
        assertThat(message.isTripConfirmationActive()).isEqualTo(settings.isTripConfirmationActive());
        assertThat(message.isApprovalDocumentCheck()).isEqualTo(settings.isApprovalDocumentCheck());
        assertThat(message.isTripConfirmationDocumentCheck()).isEqualTo(settings.isTripConfirmationDocumentCheck());
    }
    
    public void otherTrCheck(OtherTrTypesApprovalsSettingsMessage message, OtherTrTypesApprovalsSettings settings) {
        commonCheck(message, settings);
        assertThat(message.isTripApprovalActive()).isEqualTo(settings.isTripApprovalActive());
    }
}