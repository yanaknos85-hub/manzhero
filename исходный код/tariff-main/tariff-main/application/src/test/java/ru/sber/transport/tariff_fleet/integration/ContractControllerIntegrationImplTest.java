package ru.sber.transport.tariff_fleet.integration;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.database.dao.FuelContractRepository;
import ru.sber.transport.tariff_fleet.database.dao.RepairContractRepository;
import ru.sber.transport.tariff_fleet.dto.FileData;
import ru.sber.transport.tariff_fleet.exception.ContractNotFoundException;
import ru.sber.transport.tariff_fleet.exception.FileDownloadException;
import ru.sber.transport.tariff_fleet.mapper.RepairContractMapper;
import ru.sber.transport.tariff_fleet.messaging.sender.RepairContractSender;
import ru.sber.transport.tariff_fleet.service.FileService;
import ru.sber.transport.tariff_fleet.service.grpc.FuelGrpcService;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
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
class ContractControllerIntegrationImplTest {
    
    public static final String CONTROLLER_URL = "/contracts";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RepairContractMapper repairContractMapper;
    @Autowired
    private RepairContractRepository repairContractRepository;
    @Autowired
    private FuelContractRepository fuelContractRepository;
    @MockitoBean
    private AuthorizationManager<?> manager;
    @MockitoBean
    private FuelGrpcService fuelGrpcService;
    @MockitoBean
    private FileService fileService;
    @MockitoBean
    private RepairContractSender repairContractSender;

    private final byte[] testImage = getTestImage();

    @Test
    @SneakyThrows
    @Sql({ "/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/contractor.sql" })
    @Transactional
    void createRepairContractAllOrganizations() {
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        var request = """
                      {
                      	"documentType": "REPAIR_AND_MAINTENANCE",
                      	"number": "4387345-1",
                      	"start": "2025-08-14",
                      	"end": "2025-08-16",
                      	"contractorId": "a033de41-2228-40ba-bc13-e97849625484",
                      	"organizationId": "cb9f17e7-f658-43ca-a70f-40c1c93ad0a6",
                      	"amountWithVat": 344444,
                      	"amountWithoutVat": 344333,
                      	"name": "Автосервис",
                      	"logo": null,
                      	"servicePoints": [
                            {"address": "Москва, Красная площадь, дом 1", "latitude": 55.755826, "longitude": 37.617298},
                            {"address": "Москва, Красная площадь, дом 2", "latitude": 55.755836, "longitude": 37.617278},
                            {"address": "Москва, Красная площадь, дом 3", "latitude": 55.755846, "longitude": 37.617288}
                        ]
                      }
                      """.trim();
        mockMvc.perform(post(CONTROLLER_URL + "/all-organizations")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name())))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated());
        var result = repairContractRepository.findAll();
        assertEquals(344444L, result.get(0).getAmountWithVat());
        assertEquals(3, result.get(0).getServicePoints().size());
    }

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/contracts_find_by_id_with_servicepoints.sql"})
    void getRepairContractByIdSelfWithServicePoints() {
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        var contractId = UUID.fromString("153b8956-ae66-4bca-84eb-28a209353a1e");
        when(fileService.get(anyString())).thenReturn(new FileData("png", testImage));
        mockMvc.perform(get(CONTROLLER_URL + "/" + contractId + "/self-organization")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(contractId.toString()))
                .andExpect(jsonPath("$.name").value("Подрядчик №22"))
                .andExpect(jsonPath("$.logo").value(Base64.getMimeEncoder().encodeToString(testImage)));


    }

    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/contracts_find_by_id_with_servicepoints.sql"})
    void getFuelContractByIdSelfWithServicePoints() throws Exception {
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        var contractId = UUID.fromString("7b6245cc-89a1-43aa-bf15-abf2835e5267");
        when(fileService.get(anyString())).thenReturn(new FileData("png", testImage));
        mockMvc.perform(get(CONTROLLER_URL + "/" + contractId + "/self-organization")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(contractId.toString()))
                .andExpect(jsonPath("$.name").value("Подрядчик №11"))
                .andExpect(jsonPath("$.logo").value(Base64.getMimeEncoder().encodeToString(testImage)));
    }

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/contracts_find_by_id_with_servicepoints.sql"})
    void getRepairContractByIdAllWithServicePoints() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        var contractId = UUID.fromString("153b8956-ae66-4bca-84eb-28a209353a1e");
        when(fileService.get(anyString())).thenReturn(new FileData("png", testImage));
        mockMvc.perform(get(CONTROLLER_URL + "/" + contractId + "/all-organizations")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(contractId.toString()))
                .andExpect(jsonPath("$.logo").value(Base64.getMimeEncoder().encodeToString(testImage)));
    }

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/contracts_find_by_id_with_servicepoints.sql"})
    void getFuelContractByIdAllWithServicePoints() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        var contractId = UUID.fromString("7b6245cc-89a1-43aa-bf15-abf2835e5267");
        when(fileService.get(anyString())).thenReturn(new FileData("png", testImage));
        mockMvc.perform(get(CONTROLLER_URL + "/" + contractId + "/all-organizations")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(contractId.toString()))
                .andExpect(jsonPath("$.logo").value(Base64.getMimeEncoder().encodeToString(testImage)));
    }

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/contracts_update_meta_and_servicepoints.sql"})
    void updateRepairContractSelfOrganization() {
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        var contractId = UUID.fromString("7b6245cc-89a1-43aa-bf15-abf2835e5268");
        var request = """
                {
                    "documentType": "REPAIR_AND_MAINTENANCE",
                    "amountWithVat": "997",
                    "servicePoints": [
                        {"address": "Москва, Красная площадь, дом 1", "latitude": 55.755826, "longitude": 37.617298},
                        {"address": "Москва, Красная площадь, дом 2", "latitude": 55.755836, "longitude": 37.617278},
                        {"address": "Москва, Красная площадь, дом 3", "latitude": 55.755846, "longitude": 37.617288}
                    ]
                }
                """.trim();
        mockMvc.perform(patch(CONTROLLER_URL + "/" + contractId + "/self-organization")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name())))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());
        when(fileService.get(anyString())).thenReturn(new FileData("png", testImage));
        mockMvc.perform(get(CONTROLLER_URL + "/" + contractId + "/self-organization")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(contractId.toString()))
                .andExpect(jsonPath("$.amountWithVat").value("997")
                );
    }

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/contracts_update_meta_and_servicepoints.sql"})
    @Transactional
    void updateRepairContractAllOrganizations() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        var contractId = UUID.fromString("153b8956-ae66-4bca-84eb-28a209353a1e");
        var request = """
                {
                    "documentType": "REPAIR_AND_MAINTENANCE",
                    "amountWithVat": "997",
                    "servicePoints": [
                        {"address": "Москва, Красная площадь, дом 1", "latitude": 55.755826, "longitude": 37.617298},
                        {"address": "Москва, Красная площадь, дом 2", "latitude": 55.755836, "longitude": 37.617278},
                        {"address": "Москва, Красная площадь, дом 3", "latitude": 55.755846, "longitude": 37.617288}
                    ]
                }
                """.trim();
        mockMvc.perform(patch(CONTROLLER_URL + "/" + contractId + "/all-organizations")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());
        when(fileService.get(anyString())).thenReturn(new FileData("png", testImage));
        mockMvc.perform(get(CONTROLLER_URL + "/" + contractId + "/all-organizations")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(contractId.toString()))
                .andExpect(jsonPath("$.amountWithVat").value("997"));

        assertTrue(
                repairContractRepository.findById(contractId).map(
                        contract -> contract.getServicePoints()
                                .stream()
                                .anyMatch(it -> "Москва, Красная площадь, дом 1".equals(it.getAddress()))
                        )
                        .orElse(false)

        );
    }

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/contracts_update_meta_and_servicepoints.sql"})
    void updateFuelContractSelfOrganization() {
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        var contractId = UUID.fromString("153b8956-ae66-4bca-84eb-28a209353a1d");
        var request = """
                {
                    "documentType": "FUEL",
                    "amountWithVat": "998",
                    "name": "Заправки Лупы",
                    "servicePoints": [
                        {"address": "Москва, Красная площадь, дом 1", "latitude": 55.755826, "longitude": 37.617298},
                        {"address": "Москва, Красная площадь, дом 2", "latitude": 55.755836, "longitude": 37.617278},
                        {"address": "Москва, Красная площадь, дом 3", "latitude": 55.755846, "longitude": 37.617288}
                    ]
                }
                """.trim();
        mockMvc.perform(patch(CONTROLLER_URL + "/" + contractId + "/self-organization")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name())))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());
        when(fileService.get(anyString())).thenReturn(new FileData("png", testImage));
        mockMvc.perform(get(CONTROLLER_URL + "/" + contractId + "/self-organization")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(contractId.toString()))
                .andExpect(jsonPath("$.amountWithVat").value("998"))
                .andExpect(jsonPath("$.name").value("Заправки Лупы")
                );
    }

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/contracts_update_meta_and_servicepoints.sql"})
    void updateFuelContractAllOrganizations() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        var contractId = UUID.fromString("7b6245cc-89a1-43aa-bf15-abf2835e5267");
        var request = """
                {
                    "documentType": "FUEL",
                    "amountWithVat": "999",
                    "name": "Заправки Пупы",
                    "servicePoints": [
                        {"address": "Москва, Красная площадь, дом 1", "latitude": 55.755826, "longitude": 37.617298},
                        {"address": "Москва, Красная площадь, дом 2", "latitude": 55.755836, "longitude": 37.617278},
                        {"address": "Москва, Красная площадь, дом 3", "latitude": 55.755846, "longitude": 37.617288}
                    ]
                }
                """.trim();
        mockMvc.perform(patch(CONTROLLER_URL + "/" + contractId + "/all-organizations")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());
        when(fileService.get(anyString())).thenReturn(new FileData("png", testImage));
        mockMvc.perform(get(CONTROLLER_URL + "/" + contractId + "/all-organizations")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(contractId.toString()))
                .andExpect(jsonPath("$.amountWithVat").value("999"))
                .andExpect(jsonPath("$.name").value("Заправки Пупы")
                );
    }

    @SneakyThrows
    @Test
    @Transactional
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/deactivate_contracts_and_servicepoints.sql"})
    void deactivateContractSelfOrganization() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        var contractId = UUID.fromString("7b6245cc-89a1-43aa-bf15-abf2835e5268");
        mockMvc.perform(patch(CONTROLLER_URL + "/" + contractId + "/deactivate/self-organization")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
                .andExpect(status().isOk());
        repairContractRepository.findById(contractId).ifPresentOrElse(
                it -> Assertions.assertFalse(it.getContract().isActive()),
                () -> {throw new ContractNotFoundException(contractId);});
    }

    @SneakyThrows
    @Test
    @Transactional
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/deactivate_contracts_and_servicepoints.sql"})
    void deactivateRepairContractAllOrganizations() {
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        var contractId = UUID.fromString("153b8956-ae66-4bca-84eb-28a209353a1e");
        mockMvc.perform(patch(CONTROLLER_URL + "/" + contractId + "/deactivate/all-organizations")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name()))))
                .andExpect(status().isOk());
        repairContractRepository.findById(contractId).ifPresentOrElse(
                it -> Assertions.assertFalse(it.getContract().isActive()),
                () -> {throw new ContractNotFoundException(contractId);});
    }

    @SneakyThrows
    @Test
    @Transactional
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/deactivate_contracts_and_servicepoints.sql"})
    void deactivateFuelContractSelfOrganization() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        var contractId = UUID.fromString("153b8956-ae66-4bca-84eb-28a209353a1d");
        mockMvc.perform(patch(CONTROLLER_URL + "/" + contractId + "/deactivate/self-organization")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
                .andExpect(status().isOk());
        fuelContractRepository.findById(contractId).ifPresentOrElse(
                it -> Assertions.assertFalse(it.getContract().isActive()),
                () -> {throw new ContractNotFoundException(contractId);});
    }

    @SneakyThrows
    @Test
    @Transactional
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/deactivate_contracts_and_servicepoints.sql"})
    void deactivateFuelContractAllOrganizations() {
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        var contractId = UUID.fromString("7b6245cc-89a1-43aa-bf15-abf2835e5267");
        mockMvc.perform(patch(CONTROLLER_URL + "/" + contractId + "/deactivate/all-organizations")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name()))))
                .andExpect(status().isOk());
        fuelContractRepository.findById(contractId).ifPresentOrElse(
                it -> Assertions.assertFalse(it.getContract().isActive()),
                () -> {throw new ContractNotFoundException(contractId);});
    }


    private byte[] getTestImage() {
        var file = "/image/test-image.png";
        try {
            return new ClassPathResource(file).getContentAsByteArray();
        } catch (IOException e) {
            throw new FileDownloadException(file);
        }
    }
}