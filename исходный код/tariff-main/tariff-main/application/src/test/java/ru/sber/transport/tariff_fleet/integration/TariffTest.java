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

@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
class TariffTest {

    public static final String CONTROLLER_URL = "/tariffs";
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthorizationManager<?> manager;
    @MockitoBean
    private EwbGrpcService ewbGrpcService;

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/edf_operator.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_contract.sql",
            "/scripts/fleet_owner_organization.sql"})
    void createTariff() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        var request = """
                {
                  "documentType": "EWB",
                  "contractId": "5627f0b0-cac0-49e2-be7f-db026fa42435",
                  "departmentId": "482e6dcb-03a9-4927-90b4-c7081114a9d8",
                  "amount": 999999
                }
                """;
        mockMvc.perform(post(CONTROLLER_URL)
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/edf_operator.sql",
            "/scripts/ewb_contract.sql",
            "/scripts/fleet_owner_organization.sql",
            "/scripts/ewb_tariff.sql"})
    void search() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        var request = """
                  {
                    "documentType": "EWB",
                    "humanReadableId": "00000003",
                    "active": true,
                    "pageSetting": {
                      "page": 0,
                      "size": 10
                    },
                    "organizationId": "cb9f17e7-f658-43ca-a70f-40c1c93ad0a6",
                    "departmentId": "482e6dcb-03a9-4927-90b4-c7081114a9d8",
                    "contractOrganizationId": "621c288d-e348-46e5-a319-cbf61ef1e396",
                    "inspectionType": "TECHNIC"
                  }
                """;
        mockMvc.perform(post(CONTROLLER_URL + "/search")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/edf_operator.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_contract.sql",
            "/scripts/fleet_owner_organization.sql",
            "/scripts/ewb_tariff.sql"})
    void deactivate() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        doReturn(false).when(ewbGrpcService).haveActiveEwb(anyList(), any(LocalDate.class));
        mockMvc.perform(patch(CONTROLLER_URL + "/3c2d9e7a-cc85-7b05-8692-263cb759408d/deactivate")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
                .andExpect(status().isOk());
    }
}