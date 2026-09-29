package ru.sber.transport.tariff_fleet.controller.impl;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.dto.GetAllActiveOrganizationNamesDto;
import ru.sber.transport.tariff_fleet.service.FleetOwnerOrganizationService;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.tariff_fleet.constant.Role.ROLE_ADMIN_CORP_CLIENT;

@DisplayName("Проверка контроллера по работе с организациями владельцев автопарков")
@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
class FleetOwnerOrganizationControllerImplTest {
    
    public static final String CONTROLLER_URL = "/fleet-owner-organizations";
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthorizationManager<?> manager;
    @MockitoBean
    private FleetOwnerOrganizationService fleetOwnerOrganizationService;
    
    @SneakyThrows
    @Test
    @Sql({ "/scripts/db_cleanup.sql", "/scripts/basic_corp_structure.sql" })
    void getFleetOwnerOrganizations() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        var expected1 = new GetAllActiveOrganizationNamesDto(UUID.randomUUID(), "officialName1");
        var expected2 = new GetAllActiveOrganizationNamesDto(UUID.randomUUID(), "officialName2");
        doReturn(List.of(expected1, expected2)).when(fleetOwnerOrganizationService).getAllActive();
        mockMvc.perform(get(CONTROLLER_URL)
                                .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.[0].id").value(expected1.id().toString()))
               .andExpect(jsonPath("$.[0].name").value(expected1.name()))
               .andExpect(jsonPath("$.[1].id").value(expected2.id().toString()))
               .andExpect(jsonPath("$.[1].name").value(expected2.name()));
    }
}