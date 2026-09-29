package ru.sberbank.ditsib.transport.limits.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.limits.CommonTest;
import ru.sberbank.ditsib.transport.limits.constants.SettingsNames;
import ru.sberbank.ditsib.transport.limits.dao.LimitSettingsRepository;
import ru.sberbank.ditsib.transport.limits.dto.LimitSettingsDTO;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSettings;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@SpringBootTest
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка контроллера настроек лимитов")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class LimitSettingsControllerTest extends CommonTest {
    @Autowired
    private LimitSettingsRepository limitSettingsRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorizationManager<?> authorizationManager;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(authorizationManager, "ROLE_GUEST");
    }
    
    @Test
    @DisplayName("CRUD настроек лимитов")
    void test_CRUD() throws Exception {
        List<LimitSettings> limitSettingsList1 = limitSettingsRepository.findAll();
        assertEquals(0, limitSettingsList1.size());
        
        LimitSettingsDTO limitSettingsDTO = new LimitSettingsDTO();
        limitSettingsDTO.setName(SettingsNames.DEP_LIMIT_GREEN_FROM);
        limitSettingsDTO.setValue("50");
        String limitSettingsStr = objectMapper.writeValueAsString(limitSettingsDTO);
        ResultActions result = mockMvc.perform(post("/limitsettings")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                                                     .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                                     .content(limitSettingsStr))
                                      .andExpect(status().isOk());
        LimitSettingsDTO limitSettingsDTO1 =
                objectMapper.readValue(result.andReturn().getResponse().getContentAsString(),
                                       LimitSettingsDTO.class);
        assertEquals("50", limitSettingsDTO1.getValue());
        
        List<LimitSettings> limitSettingsList2 = limitSettingsRepository.findAll();
        assertEquals(1, limitSettingsList2.size());
        
        LimitSettingsDTO limitSettingsDTO2 = new LimitSettingsDTO();
        limitSettingsDTO2.setName(SettingsNames.DEP_LIMIT_GREEN_FROM);
        limitSettingsDTO2.setValue("70");
        String limitSettingsStr2 = objectMapper.writeValueAsString(limitSettingsDTO2);
        ResultActions result2 = mockMvc.perform(post("/limitsettings")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                                                      .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                                      .content(limitSettingsStr2))
                                       .andExpect(status().isOk());
        LimitSettingsDTO limitSettingsDTO3 =
                objectMapper.readValue(result2.andReturn().getResponse().getContentAsString(),
                                       LimitSettingsDTO.class);
        assertEquals("70", limitSettingsDTO3.getValue());
        
        //perform get
        mockMvc.perform(get("/limitsettings/" + SettingsNames.DEP_LIMIT_GREEN_FROM.name())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value(SettingsNames.DEP_LIMIT_GREEN_FROM.name()))
               .andExpect(jsonPath("$.value").value("70"));
        
        var response = mockMvc.perform(
                                      get("/limitsettings")
                                              .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                              .contentType(MediaType.APPLICATION_JSON_VALUE))
                              .andExpect(status().isOk()).andReturn();
        List<LimitSettingsDTO> actual =
                objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                       new TypeReference<>() {
                                       });
        assertEquals(limitSettingsRepository.findAll().size(), actual.size());
        
        //perform delete
        mockMvc.perform(delete("/limitsettings/" + SettingsNames.DEP_LIMIT_GREEN_FROM.name())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
               .andExpect(status().isOk());
        
        List<LimitSettings> limitSettingsList3 = limitSettingsRepository.findAll();
        assertEquals(0, limitSettingsList3.size());
    }
}
