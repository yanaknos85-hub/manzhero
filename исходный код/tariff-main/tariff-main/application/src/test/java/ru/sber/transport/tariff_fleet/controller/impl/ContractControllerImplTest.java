package ru.sber.transport.tariff_fleet.controller.impl;

import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.dto.AbstractContractPatchDto;
import ru.sber.transport.tariff_fleet.dto.AbstractContractPostAllOrganizationsDto;
import ru.sber.transport.tariff_fleet.dto.AbstractContractPostDto;
import ru.sber.transport.tariff_fleet.dto.AbstractContractPostSelfOrganizationDto;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbContractGetDto;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbGetContractByIdDto;
import ru.sber.transport.tariff_fleet.dto.repair.RepairContractGetAllOrganizationsDto;
import ru.sber.transport.tariff_fleet.dto.repair.RepairContractGetSelfOrganizationDto;
import ru.sber.transport.tariff_fleet.service.ContractService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
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
class ContractControllerImplTest {

    public static final String CONTROLLER_URL = "/contracts";
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthorizationManager<?> manager;
    @MockitoBean
    private ContractService contractService;


    @Test
    @SneakyThrows
    void create() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        doNothing().when(contractService).create(any(AbstractContractPostDto.class));
        var request = String.format("""
                                      {
                                          "documentType": "EWB",
                                          "contractorOrganizationId": "cb9f17e7-f658-43ca-a70f-40c1c93ad0a6",
                                          "inspectionType": "MEDIC",
                                          "amount": 9999999999,
                                          "edfOperatorId": "2AL",
                                          "edfCode": "QtYxWfGYISewoO",
                                          "contractorMedicalLicense": {
                                              "series": "xvkMlZqCHggHqNBBh",
                                              "number": "BNajVxsxdnskNxEFCsSz",
                                              "issueDate": "%s",
                                              "expiryDate": "%s"
                                          },
                                          "number": "032gvbnnaf42cvmnbv",
                                          "uvhd": "gh4389f0gf9ug3g443",
                                          "period": {
                                              "start": "%s",
                                              "end": "%s"
                                          }
                                      }
                                    """, LocalDate.now(), LocalDate.now().plusYears(1), LocalDate.now(), LocalDate.now().plusYears(1));
        mockMvc.perform(post(CONTROLLER_URL)
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
        verify(contractService, times(1)).create(any(AbstractContractPostDto.class));
    }

    @Test
    @SneakyThrows
    void createError() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        doNothing().when(contractService).create(any(AbstractContractPostDto.class));
        var request1 = String.format("""
                                       {
                                           "documentType": "EWB",
                                           "contractorOrganizationId": "cb9f17e7-f658-43ca-a70f-40c1c93ad0a6",
                                           "inspectionType": "MEDIC",
                                           "amount": null,
                                           "edfOperatorId": "2AL",
                                           "edfCode": "QtYxWfGYISewoO",
                                           "contractorMedicalLicense": {
                                               "series": "xvkMlZqCHggHqNBBh",
                                               "number": "BNajVxsxdnskNxEFCsSz",
                                               "issueDate": "%s",
                                               "expiryDate": "%s"
                                           },
                                           "number": "032gvbnnaf42cvmnbv",
                                           "uvhd": "gh4389f0gf9ug3g443",
                                           "period": {
                                               "start": "%s",
                                               "end": "%s"
                                           }
                                       }
                                     """, LocalDate.now(), LocalDate.now().plusYears(1), LocalDate.now(), LocalDate.now().plusYears(1));
        mockMvc.perform(post(CONTROLLER_URL)
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                        .content(request1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.amount").value("не должно равняться null"));
        var request2 = String.format("""
                                       {
                                           "documentType": "EWB",
                                           "contractorOrganizationId": "cb9f17e7-f658-43ca-a70f-40c1c93ad0a6",
                                           "inspectionType": "MEDIC",
                                           "amount": -1,
                                           "edfOperatorId": "2AL",
                                           "edfCode": "QtYxWfGYISewoO",
                                           "contractorMedicalLicense": {
                                               "series": "xvkMlZqCHggHqNBBh",
                                               "number": "BNajVxsxdnskNxEFCsSz",
                                               "issueDate": "%s",
                                               "expiryDate": "%s"
                                           },
                                           "number": "032gvbnnaf42cvmnbv",
                                           "uvhd": "gh4389f0gf9ug3g443",
                                           "period": {
                                               "start": "%s",
                                               "end": "%s"
                                           }
                                       }
                                     """, LocalDate.now(), LocalDate.now().plusYears(1), LocalDate.now(), LocalDate.now().plusYears(1));
        mockMvc.perform(post(CONTROLLER_URL)
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                        .content(request2)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.amount").value("должно быть больше или равно 0"));
    }

    @Test
    @SneakyThrows
    void createAllOrganizations() {
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        doNothing().when(contractService).createAllOrganizations(any(AbstractContractPostAllOrganizationsDto.class));
        var request = """
                      {
                      	"documentType": "FUEL",
                      	"number": "4387345-1",
                      	"start": "2025-11-11",
                      	"end": "2025-11-11",
                      	"organizationId": "8fe10e6d-de29-43a2-a5df-4452cece3a22",
                      	"contractorId": "71b74612-608b-450c-b17c-f0d405c57a0b",
                      	"amountWithVat": 344444,
                      	"amountWithoutVat": 344333,
                      	"name": "Автосервис",
                      	"logo": null,
                      	"servicePoints": [
                      		{
                      			"address": "г. Москва, ул. Ленина, д. 123",
                      			"latitude": 55.755826,
                      			"longitude": 37.617298
                      		}
                      	]
                      }
                      """;
        mockMvc.perform(post(CONTROLLER_URL + "/all-organizations")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name())))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated());
        verify(contractService, times(1)).createAllOrganizations(any(AbstractContractPostAllOrganizationsDto.class));
    }

    @Test
    @SneakyThrows
    void createSelfOrganization() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        doNothing().when(contractService).createSelfOrganization(any(AbstractContractPostSelfOrganizationDto.class), any());
        var request = """
                      {
                      	"documentType": "FUEL",
                      	"number": "4387345-1",
                      	"start": "2025-11-11",
                      	"end": "2025-11-11",
                      	"contractorId": "71b74612-608b-450c-b17c-f0d405c57a0b",
                      	"amountWithVat": 344444,
                      	"amountWithoutVat": 344333,
                      	"name": "Автосервис",
                      	"logo": null,
                      	"servicePoints": [
                      		{
                      			"address": "г. Москва, ул. Ленина, д. 123",
                      			"latitude": 55.755826,
                      			"longitude": 37.617298
                      		}
                      	]
                      }
                      """;
        mockMvc.perform(post(CONTROLLER_URL + "/self-organization")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated());
        verify(contractService, times(1)).createSelfOrganization(any(AbstractContractPostSelfOrganizationDto.class),
                eq(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8")));
    }

    @SneakyThrows
    @Test
    void search() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        var expected1 = Instancio.create(EwbContractGetDto.class);
        var expected2 = Instancio.create(EwbContractGetDto.class);
        var pageRequest = PageRequest.of(0, 10);
        doReturn(new PageImpl<>(List.of(expected1, expected2), pageRequest, 2)).when(contractService).search(any());
        var request = String.format("""
                                      {
                                        "documentType": "EWB",
                                        "number": "11111",
                                        "active": true,
                                        "period": {
                                          "start": "%s",
                                          "end": "%s"
                                        },
                                        "pageSetting": {
                                          "page": 0,
                                          "size": 10
                                        },
                                        "contractorOrganizationId": "cb9f17e7-f658-43ca-a70f-40c1c93ad0a6",
                                        "inspectionType": "MEDIC"
                                      }
                                    """, LocalDate.of(2024, 1, 1), LocalDate.of(2025, 12, 12));
        mockMvc.perform(post(CONTROLLER_URL + "/search")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").value(1));
    }
    
    @Test
    @SneakyThrows
    void searchAllOrganizationsNoFilters() {
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        var expected1 = Instancio.create(RepairContractGetAllOrganizationsDto.class);
        var expected2 = Instancio.create(RepairContractGetAllOrganizationsDto.class);
        var pageRequest = PageRequest.of(0, 10);
        doReturn(new PageImpl<>(List.of(expected1, expected2), pageRequest, 2)).when(contractService).searchAllOrganizations(any());
        var request = """
                      {
                      	"documentType": "REPAIR_AND_MAINTENANCE",
                      	"pageSetting": {
                      		"page": 0,
                      		"size": 10
                      	}
                      }
                      """;
        mockMvc.perform(post(CONTROLLER_URL + "/search/all-organizations")
                                .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name())))
                                .content(request)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalPages").value(1))
               .andExpect(jsonPath("$.numberOfElements").value(2));
    }

    @Test
    @SneakyThrows
    void searchAllOrganizationsWithFilters() {
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        var expected1 = Instancio.create(RepairContractGetAllOrganizationsDto.class);
        var expected2 = Instancio.create(RepairContractGetAllOrganizationsDto.class);
        var pageRequest = PageRequest.of(0, 10);
        doReturn(new PageImpl<>(List.of(expected1, expected2), pageRequest, 2)).when(contractService).searchAllOrganizations(any());
        var request = """
                      {
                      	"documentType": "REPAIR_AND_MAINTENANCE",
                      	"contractorId": "6e6e04b7-1912-422a-820a-1a6777dfde1b",
                      	"organizationId": "6e6e04b7-1912-422a-820a-1a6777dfde1b",
                      	"number": "4387345-13",
                      	"active": "true",
                      	"start": "2025-04-26",
                      	"end": "2026-04-27",
                      	"pageSetting": {
                      		"page": 0,
                      		"size": 10
                      	}
                      }
                      """;
        mockMvc.perform(post(CONTROLLER_URL + "/search/all-organizations")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name())))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.numberOfElements").value(2));
    }
    
    @Test
    @SneakyThrows
    void searchSelfOrganizationNoFilters() {
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        var expected1 = Instancio.create(RepairContractGetSelfOrganizationDto.class);
        var expected2 = Instancio.create(RepairContractGetSelfOrganizationDto.class);
        var pageRequest = PageRequest.of(0, 10);
        doReturn(new PageImpl<>(List.of(expected1, expected2), pageRequest, 2)).when(contractService).searchSelfOrganization(any(), any());
        var request = """
                      {
                      	"documentType": "REPAIR_AND_MAINTENANCE",
                      	"contractorId": "6e6e04b7-1912-422a-820a-1a6777dfde1b",
                      	"number": "4387345-13",
                      	"active": "true",
                      	"start": "2025-04-26",
                      	"end": "2026-04-27",
                      	"pageSetting": {
                      		"page": 0,
                      		"size": 10
                      	}
                      }
                      """;
        mockMvc.perform(post(CONTROLLER_URL + "/search/self-organization")
                                .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name())))
                                .content(request)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalPages").value(1))
               .andExpect(jsonPath("$.numberOfElements").value(2));
    }

    @Test
    @SneakyThrows
    void searchSelfOrganizationWithFilters() {
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        var expected1 = Instancio.create(RepairContractGetSelfOrganizationDto.class);
        var expected2 = Instancio.create(RepairContractGetSelfOrganizationDto.class);
        var pageRequest = PageRequest.of(0, 10);
        doReturn(new PageImpl<>(List.of(expected1, expected2), pageRequest, 2)).when(contractService).searchSelfOrganization(any(), any());
        var request = """
                      {
                      	"documentType": "REPAIR_AND_MAINTENANCE",
                      	"pageSetting": {
                      		"page": 0,
                      		"size": 10
                      	}
                      }
                      """;
        mockMvc.perform(post(CONTROLLER_URL + "/search/self-organization")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name())))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.numberOfElements").value(2));
    }

    @Test
    @SneakyThrows
    void editContract() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        doNothing().when(contractService).edit(any(UUID.class), any(AbstractContractPatchDto.class));
        var request = String.format("""
                                    {
                                      "documentType": "EWB",
                                      "inspectionType": "MEDIC",
                                      "edfOperatorId": "2BM",
                                      "edfCode": "Qt#RF3f3ewoO",
                                      "contractorMedicalLicense": {
                                          "series": "xf#$vc5u21HqNBBh",
                                          "number": "BNajVxFGE89thbSF43",
                                          "issueDate": "%s",
                                          "expiryDate": "%s"
                                      },
                                      "uvhd": "gh4389f0gf9ug3g443"
                                    }
                                    """, LocalDate.now(), LocalDate.now().plusYears(1));
        mockMvc.perform(patch(CONTROLLER_URL + "/5627f0b0-cac0-49e2-be7f-db026fa42435")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void getContractById() {
        var id = UUID.fromString("5627f0b0-cac0-49e2-be7f-db026fa42435");
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        doReturn(Instancio.of(EwbGetContractByIdDto.class).set(field(EwbGetContractByIdDto::getId), id).create())
                .when(contractService).get(id);
        mockMvc.perform(get(CONTROLLER_URL + "/" + id)
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }


}