package ru.sberbank.ditsib.corpclient.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sberbank.ditsib.corpclient.database.dao.AttributeRepository;
import ru.sberbank.ditsib.corpclient.database.model.Attribute;
import ru.sberbank.ditsib.corpclient.database.model.AttributeStatus;
import ru.sberbank.ditsib.corpclient.dto.AttributeDto;
import ru.sberbank.ditsib.corpclient.dto.NewAttributeDto;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера справочника Признаков сотрудника")
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
class AttributeControllerTest extends SharedData {

    @MockitoBean
    private JwtDecoder jwtDecoder;
    
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ObjectMapper objectMapper;
    
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private AttributeRepository attributeRepository;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }
    
    @AfterEach
    public void clear() {
        attributeRepository.deleteAllInBatch();
    }
    
    @Test
    @DisplayName("Добавление признаков сотрудника")
    @Transactional
    void test_addAttributes() throws Exception {
        attributeRepository.save(attribute1);
        
        NewAttributeDto attributeDto = NewAttributeDto.builder().name(ATTRIBUTE_NAME2).build();
        var request = objectMapper.writeValueAsString(attributeDto);
        
        var response =
                mockMvc.perform(
                               post("/self/attributes")
                                       .contentType(MediaType.APPLICATION_JSON_VALUE)
                                       .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                       .content(request))
                       .andExpect(status().isOk()).andReturn();
        
        AttributeDto actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                     AttributeDto.class);
        assertNotNull(actual.getId());
        assertNotNull(AttributeStatus.ACTIVE.equals(actual.getStatus()));
    }
    
    @Test
    @DisplayName("Удаление признаков сотрудника")
    @Transactional
    void test_deleteAttributes() throws Exception {
        Attribute attributeOne = attributeRepository.save(attribute1);
        Attribute attributeTwo = attributeRepository.save(attribute2);
        
        mockMvc.perform(delete("/self/attributes/" + attributeTwo.getId().toString())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
               .andExpect(status().isOk()).andReturn();
        
        assertEquals(2, attributeRepository.count());
        var attribute = attributeRepository.findById(attributeTwo.getId());
        assertTrue(attribute.isPresent());
        assertEquals(AttributeStatus.INACTIVE, attribute.get().getStatus());
        
        assertTrue(attributeRepository.findActiveByName(attributeOne.getName()).isPresent());
        assertTrue(attributeRepository.findActiveByName(attributeTwo.getName()).isEmpty());
        
        assertEquals(2, attributeRepository.findAll().size());
    }
    
    @Test
    @DisplayName("Удаление несуществующего признака ")
    void test_deleteNonExistAttribute() throws Exception {
        String rndUUID = UUID.randomUUID().toString();
        Exception resolvedException =  mockMvc.perform(delete("/self/attributes/" + rndUUID)
                                                               .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                                              .andExpect(status().isNotFound()).andReturn().getResolvedException();
        Assertions.assertNotNull(resolvedException);
        assertEquals(resolvedException.getClass(), EntityNotFoundException.class);
    }
    
    @Test
    @DisplayName("Получение всех признаков сотрудника")
    @Transactional
    void test_getAllAttributes() throws Exception {
        attributeRepository.save(attribute1);
        attribute2.setStatus(AttributeStatus.INACTIVE);
        attributeRepository.save(attribute2);
        
        MvcResult response = mockMvc.perform(get("/self/attributes")
                                                     .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                                    .andExpect(status().isOk()).andReturn();
        
        List<AttributeDto> attributeDtoList = objectMapper.readValue(
                response.getResponse().getContentAsString(StandardCharsets.UTF_8),new TypeReference<>() {});
        
        assertEquals(2, attributeDtoList.size());
    }
    
    @Test
    @DisplayName("Получение активных признаков сотрудника")
    @Transactional
    void test_getActiveAttributes() throws Exception {
        Attribute attributeOne = attributeRepository.save(attribute1);
        attribute2.setStatus(AttributeStatus.INACTIVE);
        
        MvcResult response = mockMvc.perform(get("/self/attributes/active")
                                                     .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                                    .andExpect(status().isOk()).andReturn();
        
        List<AttributeDto> attributeDtoList = objectMapper.readValue(
                response.getResponse().getContentAsString(StandardCharsets.UTF_8),new TypeReference<>() {});
        
        assertEquals(1, attributeDtoList.size());
        assertEquals(attributeOne.getId(), attributeDtoList.getFirst().getId());
    }
}
