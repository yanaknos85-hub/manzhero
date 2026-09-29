package ru.sber.transport.tariff_fleet.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
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
import ru.sber.transport.tariff_fleet.dto.OrganizationsDepartmentsSearchDto;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.sber.transport.tariff_fleet.TestData.EMPLOYEE_1_ID;
import static ru.sber.transport.tariff_fleet.constant.Role.ROLE_ADMIN_CORP_CLIENT;

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedPostgres
@DisplayName("Проверка контроллера по работе с организациями")
public class OrganizationControllerImplTest {
    
    public static final String CONTROLLER_URL = "/organization";
    
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthorizationManager<?> manager;
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @SneakyThrows
    @Sql("/scripts/organization.sql")
    @DisplayName("Получение списка названий всех активных организаций")
    void getAllActiveNames() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        mockMvc.perform(get(CONTROLLER_URL)
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$[:1].id").value("46a3b980-d801-499b-87eb-be0bf3f2de53"))
               .andExpect(jsonPath("$[:1].name").value("AAA Inc"))
               .andExpect(jsonPath("$[1:2].id").value("d41d56ab-6e33-4977-89be-5a3eaf5e72a3"))
               .andExpect(jsonPath("$[1:2].name").value("BBB Inc"));
    }
    
    @Test
    @SneakyThrows
    @Sql({ "/scripts/organization.sql", "/scripts/departments.sql" })
    @DisplayName("Получение списка департаментов организации по id")
    void getOrganizationsDepartments() {
        var searchDto = new OrganizationsDepartmentsSearchDto(List.of(UUID.fromString("46a3b980-d801-499b-87eb-be0bf3f2de53")));
        mockMvc.perform(post(CONTROLLER_URL + "/department")
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(searchDto)))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$[*]").value(Matchers.hasSize(1)))
               .andExpect(jsonPath("$[0].organizationName").value("AAA Inc"))
               .andExpect(jsonPath("$[0].departmentList[*]").value(Matchers.hasSize(2)));
        
    }
}
