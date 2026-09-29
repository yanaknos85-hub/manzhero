package ru.sber.transport.tariff_fleet.integration;

import lombok.SneakyThrows;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.database.dao.FuelTariffRepository;
import ru.sber.transport.tariff_fleet.database.dao.RepairTariffRepository;
import ru.sber.transport.tariff_fleet.database.dao.TariffRepository;
import ru.sber.transport.tariff_fleet.database.model.RepairTariff;
import ru.sber.transport.tariff_fleet.exception.TariffNotFoundException;
import ru.sber.transport.tariff_fleet.service.grpc.FuelGrpcService;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.tariff_fleet.constant.Role.ROLE_ADMIN_CORP_CLIENT;
import static ru.sber.transport.tariff_fleet.constant.Role.ROLE_DISPATCHER_SUPPORT_SERVICE;

@DisplayName("Проверка контроллера по работе с договорами")
@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
class TariffControllerIntegrationImplTest {

    public static final String CONTROLLER_URL = "/tariffs";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FuelTariffRepository fuelTariffRepository;
    @Autowired
    private RepairTariffRepository repairTariffRepository;
    @Autowired
    private TariffRepository tariffRepository;
    @MockitoBean
    private AuthorizationManager<?> manager;
    @MockitoBean
    private FuelGrpcService fuelGrpcService;

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/tariff_fuel_and_repair.sql"})
    void getRepairTariffByIdSelfOrganization() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        var tariffId = UUID.fromString("7b6245cc-89a1-43aa-bf15-abf2835e5268");
        mockMvc.perform(get(CONTROLLER_URL + "/" + tariffId + "/self-organization")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("7b6245cc-89a1-43aa-bf15-abf2835e5268"))
                .andExpect(jsonPath("$.isFieldService").value("true"))
                .andExpect(jsonPath("$.departmentName").value("ПАО «Сбербанк России» (ЦА)"));
    }

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/tariff_fuel_and_repair.sql"})
    void getFuelTariffByIdAllOrganizations() {
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        var tariffId = UUID.fromString("7b6245cc-89a1-43aa-bf15-abf2835e5267");
        mockMvc.perform(get(CONTROLLER_URL + "/" + tariffId + "/all-organizations")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("7b6245cc-89a1-43aa-bf15-abf2835e5267"))
                .andExpect(jsonPath("$.departmentName").exists())
                .andExpect(jsonPath("$.discount").value("0.0"));

    }

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/tariff_fuel_and_repair.sql"})
    void deactivateTariffByIdSelfOrganization() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        var tariffId = UUID.fromString("7b6245cc-89a1-43aa-bf15-abf2835e5267");
        mockMvc.perform(patch(CONTROLLER_URL + "/" + tariffId + "/deactivate/self-organization")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
                .andExpect(status().isOk());
        var tariff = tariffRepository.findById(tariffId).orElseThrow(() -> new TariffNotFoundException(tariffId));
        assertFalse(tariff.isActive());
    }

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/tariff_fuel_and_repair.sql"})
    void deactivateTariffByIdAllOrganizations() {
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        var tariffId = UUID.fromString("153b8956-ae66-4bca-84eb-28a209353a1d");
        mockMvc.perform(patch(CONTROLLER_URL + "/" + tariffId + "/deactivate/all-organizations")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name()))))
                .andExpect(status().isOk());
        var tariff = tariffRepository.findById(tariffId).orElseThrow(() -> new TariffNotFoundException(tariffId));
        assertFalse(tariff.isActive());
    }

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/tariff_fuel_and_repair.sql"})
    void createTariffFuelSelfOrganization() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        var request = """
                {
                "documentType": "FUEL",
                "contractId":  "7b6245cc-89a1-43aa-bf15-abf2835e5269",
                "departmentId": "482e6dcb-03a9-4927-90b4-c7081114a9d8",
                "discount": "0.25"
                }
                """;
        mockMvc.perform(post(CONTROLLER_URL + "/self-organization")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated());
        assertTrue(fuelTariffRepository.findAll().stream().anyMatch(it -> it.getDiscount().compareTo(BigDecimal.valueOf(0.25)) == 0));
    }

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/tariff_fuel_and_repair.sql"})
    void createTariffRepairSelfOrganization() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        var request = """
                {
                "documentType": "REPAIR_AND_MAINTENANCE",
                "contractId":  "153b8956-ae66-4bca-84eb-28a209353a1f",
                "isFieldService": "true",
                "hourNormalizedPrice": "1",
                "detailDiscountPrice": "1",
                "workWarranty": "1",
                "mileageWarranty": "1",
                "detailWarranty": "99",
                "departmentId": "482e6dcb-03a9-4927-90b4-c7081114a9d8"
                }
                """;
        mockMvc.perform(post(CONTROLLER_URL + "/self-organization")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated());
        var actual = repairTariffRepository.findAll().stream()
                .filter(el -> el.getDetailWarranty() == 99)
                .findAny()
                .orElseThrow(() -> new JUnitException("Repair tariff not found"));
        Hibernate.initialize(actual.getDepartment());
        assertThat(actual)
                .extracting(
                        RepairTariff::getDetailWarranty,
                        el -> el.getDepartment().getId(),
                        el -> el.getDepartment().getDepartmentName()
                )
                .containsExactlyInAnyOrder(
                        99,
                        UUID.fromString("482e6dcb-03a9-4927-90b4-c7081114a9d8"),
                        "ПАО «Сбербанк России» (ЦА)"
                );
    }

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/tariff_fuel_and_repair.sql"})
    void createTariffFuelAllOrganization() {
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        var request = """
                {
                "documentType": "FUEL",
                "contractId":  "7b6245cc-89a1-43aa-bf15-abf2835e5269",
                "departmentId": "482e6dcb-03a9-4927-90b4-c7081114a9d8",
                "discount": "0.25"
                }
                """;
        mockMvc.perform(post(CONTROLLER_URL + "/all-organizations")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name())))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated());
        assertTrue(fuelTariffRepository.findAll().stream().anyMatch(it -> it.getDiscount().compareTo(BigDecimal.valueOf(0.25)) == 0));
    }

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/tariff_fuel_and_repair.sql"})
    void createTariffRepairAllOrganization() {
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        var request = """
                {
                "documentType": "REPAIR_AND_MAINTENANCE",
                "contractId":  "153b8956-ae66-4bca-84eb-28a209353a1f",
                "isFieldService": "true",
                "hourNormalizedPrice": "1",
                "detailDiscountPrice": "1",
                "workWarranty": "1",
                "mileageWarranty": "1",
                "detailWarranty": "99",
                "departmentId": "482e6dcb-03a9-4927-90b4-c7081114a9d8"
                }
                """;
        mockMvc.perform(post(CONTROLLER_URL + "/all-organizations")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name())))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated());
        var actual = repairTariffRepository.findAll().stream()
                .filter(el -> el.getDetailWarranty() == 99)
                .findAny()
                .orElseThrow(() -> new JUnitException("Repair tariff not found"));
        Hibernate.initialize(actual.getDepartment());
        assertThat(actual)
                .extracting(
                        RepairTariff::getDetailWarranty,
                        el -> el.getDepartment().getId(),
                        el -> el.getDepartment().getDepartmentName()
                )
                .containsExactlyInAnyOrder(
                        99,
                        UUID.fromString("482e6dcb-03a9-4927-90b4-c7081114a9d8"),
                        "ПАО «Сбербанк России» (ЦА)"
                );
    }

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/tariff_fuel_and_repair.sql"})
    void searchFuelTariffs() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        var request =
                """
                {
                "documentType": "FUEL",
                "pageSetting": {
                    "pageNumber": 0,
                    "pageSize": 10
                },
                "contractId": "7b6245cc-89a1-43aa-bf15-abf2835e5267",
                "humanReadableId": "001"
                }
                """;
        mockMvc.perform(post(CONTROLLER_URL + "/search/self-organization")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("7b6245cc-89a1-43aa-bf15-abf2835e5267"))
                .andExpect(jsonPath("$.content[0].contractNumber").value("FUEL_CONTRACT_001"))
                .andExpect(jsonPath("$.content[0].humanReadableId").value("TARIFF_001"));

    }

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/tariff_fuel_and_repair.sql"})
    void searchRepairTariffs() {
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        var request =
                """
                        {
                        "documentType": "REPAIR_AND_MAINTENANCE",
                        "pageSetting": {
                            "pageNumber": 0,
                            "pageSize": 10
                        },
                        "organizationId": "cb9f17e7-f658-43ca-a70f-40c1c93ad0a6",
                        "humanReadableId": "TARIFF"
                        }
                        """;
        mockMvc.perform(post(CONTROLLER_URL + "/search/all-organizations")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name())))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].humanReadableId").value("TARIFF_002"))
                .andExpect(jsonPath("$.content[1].humanReadableId").value("TARIFF_004"))
                .andExpect(jsonPath("$.content[0].departmentName").value("ПАО «Сбербанк России» (ЦА)"))
                .andExpect(jsonPath("$.content[1].departmentName").value("ПАО «Сбербанк России» (ЦА)"));
    }
}