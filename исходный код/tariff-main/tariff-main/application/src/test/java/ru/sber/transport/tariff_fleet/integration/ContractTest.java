package ru.sber.transport.tariff_fleet.integration;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
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
import ru.sber.transport.tariff_fleet.service.grpc.EwbGrpcService;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.tariff_fleet.constant.Role.ROLE_ADMIN_CORP_CLIENT;
import static ru.sber.transport.tariff_fleet.constant.Role.ROLE_DISPATCHER_SUPPORT_SERVICE;

@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
class ContractTest {

    public static final String CONTROLLER_URL = "/contracts";
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthorizationManager<?> manager;
    @MockitoBean
    private EwbGrpcService ewbGrpcService;

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql", "/scripts/edf_operator.sql", "/scripts/basic_corp_structure.sql"})
    void create() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
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
    }

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql", "/scripts/basic_corp_structure.sql", "/scripts/edf_operator.sql", "/scripts/ewb_contract.sql"})
    void search() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
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
                """, LocalDate.of(2024, 1, 1), LocalDate.now().plusYears(1));
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
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/contractor.sql",
            "/scripts/repair_contract.sql"})
    void searchAllOrganizations() {
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        var request = String.format("""
                {
                	"documentType": "REPAIR_AND_MAINTENANCE",
                	"contractorId": "a033de41-2228-40ba-bc13-e97849625484",
                	"organizationId": "cb9f17e7-f658-43ca-a70f-40c1c93ad0a6",
                	"number": "9999",
                	"active": "false",
                	"start": "%s",
                	"end": "%s",
                	"pageSetting": {
                		"page": 0,
                		"size": 10
                	}
                }
                """, LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(1));
        mockMvc.perform(post(CONTROLLER_URL + "/search/all-organizations")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name())))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.numberOfElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value("5805f22f-30e3-440f-91b4-4b5fe2c28405"));
    }

    @Test
    @SneakyThrows
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/contractor.sql",
            "/scripts/repair_contract.sql"})
    void searchAllOrganizationsNoFilters() {
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
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
                .andExpect(jsonPath("$.numberOfElements").value(3))
                .andExpect(jsonPath("$.content[0].id").value("5805f22f-30e3-440f-91b4-4b5fe2c28405"));
    }

    @Test
    @SneakyThrows
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/contractor.sql",
            "/scripts/repair_contract.sql"})
    void searchSelfOrganization() {
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        var request = String.format("""
                {
                	"documentType": "REPAIR_AND_MAINTENANCE",
                	"contractorId": "a033de41-2228-40ba-bc13-e97849625484",
                	"number": "9999",
                	"active": "false",
                	"start": "%s",
                	"end": "%s",
                	"pageSetting": {
                		"page": 0,
                		"size": 10
                	}
                }
                """, LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(1));
        mockMvc.perform(post(CONTROLLER_URL + "/search/self-organization")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name())))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.numberOfElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value("5805f22f-30e3-440f-91b4-4b5fe2c28405"));
    }

    @Test
    @SneakyThrows
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/contractor.sql",
            "/scripts/repair_contract.sql"})
    void searchSelfOrganizationNoFilters() {
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
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
                .andExpect(jsonPath("$.numberOfElements").value(3))
                .andExpect(jsonPath("$.content[0].id").value("5805f22f-30e3-440f-91b4-4b5fe2c28405"));
    }

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/edf_operator.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_contract.sql",
            "/scripts/fleet_owner_organization.sql"})
    void edit() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
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

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/edf_operator.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_contract.sql"})
    void deactivate() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        doReturn(false).when(ewbGrpcService).haveActiveEwb(anyList(), any(LocalDate.class));
        mockMvc.perform(patch(CONTROLLER_URL + "/5627f0b0-cac0-49e2-be7f-db026fa42435/deactivate")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
                .andExpect(status().isOk());
    }
}