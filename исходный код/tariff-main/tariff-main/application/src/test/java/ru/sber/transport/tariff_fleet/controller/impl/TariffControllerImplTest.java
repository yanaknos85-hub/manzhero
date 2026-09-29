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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.dto.AbstractTariffPatchDto;
import ru.sber.transport.tariff_fleet.dto.AbstractTariffPostDto;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbTariffGetByIdDto;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbTariffGetDto;
import ru.sber.transport.tariff_fleet.service.ContractService;
import ru.sber.transport.tariff_fleet.service.HumanReadableIdService;
import ru.sber.transport.tariff_fleet.service.tariff.TariffService;

import java.util.List;
import java.util.UUID;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.tariff_fleet.constant.Role.ROLE_ADMIN_CORP_CLIENT;

@DisplayName("Проверка контроллера по работе с тарифами")
@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
class TariffControllerImplTest {
    
    public static final String CONTROLLER_URL = "/tariffs";
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthorizationManager<?> manager;
    @MockitoBean
    private TariffService tariffService;
    @MockitoBean
    private ContractService contractService;
    @MockitoBean
    private HumanReadableIdService humanReadableIdService;
    
    @SneakyThrows
    @Test
    @Sql({ "/scripts/db_cleanup.sql",
           "/scripts/basic_corp_structure.sql" })
    void create() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        doReturn(UUID.randomUUID().toString()).when(humanReadableIdService).createHumanReadableIdByUserId(any(UUID.class));
        doNothing().when(contractService).createTariff(any(AbstractTariffPostDto.class), anyString());
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
    @Sql({ "/scripts/db_cleanup.sql", "/scripts/basic_corp_structure.sql" })
    void search() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        var expected1 = Instancio.create(EwbTariffGetDto.class);
        var expected2 = Instancio.create(EwbTariffGetDto.class);
        var pageRequest = PageRequest.of(0, 10);
        doReturn(new PageImpl<>(List.of(expected1, expected2), pageRequest, 2)).when(tariffService).search(any());
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
    
    @Test
    @SneakyThrows
    void getById() {
        var tariffId = UUID.randomUUID();
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        doReturn(Instancio.of(EwbTariffGetByIdDto.class).set(field(EwbTariffGetByIdDto::getId), tariffId).create())
                .when(tariffService).getById(tariffId);
        mockMvc.perform(get(CONTROLLER_URL + "/" + tariffId)
                                .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(tariffId.toString()));
    }
    
    @Test
    @SneakyThrows
    void edit() {
        var tariffId = UUID.randomUUID();
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        doNothing().when(contractService).editTariff(any(UUID.class), any(AbstractTariffPatchDto.class));
        var request = """
                      {
                        "documentType": "EWB",
                        "amount": 999999
                      }
                      """;
        mockMvc.perform(patch(CONTROLLER_URL + "/" + tariffId)
                                .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                                .content(request)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk());
    }
}