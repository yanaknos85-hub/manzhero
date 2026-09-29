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
import ru.sber.transport.tariff_fleet.constant.DocumentType;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.tariff_fleet.constant.Role.ROLE_ADMIN_CORP_CLIENT;
import static ru.sber.transport.tariff_fleet.constant.Role.ROLE_DISPATCHER_SUPPORT_SERVICE;


@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера по работе с договорами")
class ContractorControllerImplTest {
    
    public static final String CONTROLLER_URL = "/contractors";
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthorizationManager<?> manager;
    
    @Test
    @SneakyThrows
    @Sql({ "/scripts/db_cleanup.sql",
           "/scripts/basic_corp_structure.sql",
           "/scripts/contractors_find_by_document_type.sql"
    })
    void getAllOrganization() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        mockMvc.perform(get(CONTROLLER_URL + "/" + DocumentType.REPAIR_AND_MAINTENANCE + "/all-organizations")
                                .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").value(hasSize(1)));
        
        mockMvc.perform(get(CONTROLLER_URL + "/" + DocumentType.FUEL + "/all-organizations")
                                .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").value(hasSize(1)));
    }
    
    @Test
    @SneakyThrows
    @Sql({ "/scripts/db_cleanup.sql",
           "/scripts/basic_corp_structure.sql",
           "/scripts/contractors_find_by_document_type.sql"
    })
    void getSelfOrganization() {
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        mockMvc.perform(get(CONTROLLER_URL + "/" + DocumentType.REPAIR_AND_MAINTENANCE + "/self-organization")
                                .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name()))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").value(hasSize(1)));
               
        mockMvc.perform(get(CONTROLLER_URL + "/" + DocumentType.FUEL + "/all-organizations")
                                .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name()))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(1));
    }
}
