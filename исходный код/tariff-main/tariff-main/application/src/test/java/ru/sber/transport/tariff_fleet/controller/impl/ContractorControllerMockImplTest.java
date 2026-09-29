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
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.constant.ContractorType;
import ru.sber.transport.tariff_fleet.dto.ContractorDto;
import ru.sber.transport.tariff_fleet.service.ContractorService;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.tariff_fleet.constant.Role.ROLE_ADMIN_CORP_CLIENT;


@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера по работе с договорами")
class ContractorControllerMockImplTest {
    
    public static final String CONTROLLER_URL = "/contractors";
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthorizationManager<?> manager;
    @MockitoBean
    private ContractorService contractorService;
    
    @Test
    @SneakyThrows
    void getFleetOwnerOrganizations() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        var expected1 = new ContractorDto(UUID.randomUUID(), "contractorName1", ContractorType.API.name());
        var expected2 = new ContractorDto(UUID.randomUUID(), "contractorName2", ContractorType.API.name());
        doReturn(List.of(expected1, expected2)).when(contractorService).getAllActive();
        mockMvc.perform(get(CONTROLLER_URL)
                                .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.[0].id").value(expected1.id().toString()))
               .andExpect(jsonPath("$.[0].name").value(expected1.name()))
               .andExpect(jsonPath("$.[0].integrationType").value(expected1.integrationType()))
               .andExpect(jsonPath("$.[1].id").value(expected2.id().toString()))
               .andExpect(jsonPath("$.[1].name").value(expected2.name()))
               .andExpect(jsonPath("$.[1].integrationType").value(expected2.integrationType()));
    }
}
