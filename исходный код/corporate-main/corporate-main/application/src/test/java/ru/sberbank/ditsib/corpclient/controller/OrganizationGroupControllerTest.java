package ru.sberbank.ditsib.corpclient.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sberbank.ditsib.corpclient.database.dao.OrganizationGroupRepository;
import ru.sberbank.ditsib.corpclient.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.dto.OrganizationGroupDTO;
import ru.sberbank.ditsib.corpclient.dto.OrganizationGroupPatchFields;
import ru.sberbank.ditsib.corpclient.dto.OrganizationGroupResponseDTO;
import ru.sberbank.ditsib.corpclient.dto.PatchData;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.utils.reflection.ReflectionUtils;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@SpringBootTest
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера групп компаний")
@ActiveProfiles("test")
class OrganizationGroupControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationGroupRepository organizationGroupRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Organization organization1, organization2, organization3, organization4;

    @BeforeEach
    void beforeEach() {
        AuthorizeUtils.authorize(roleCheckService);

        organization1 = new Organization();
        organization1.setAddress("ORGANIZATION_ADDRESS1");
        organization1.setOfficialName("ORGANIZATION_OFFICIAL_NAME1");
        organization1.setMsrn("msrn");
        organization1.setTid("tid");
        organization1.setOrganizationCode(1);
        organizationRepository.save(organization1);

        organization2 = new Organization();
        organization2.setAddress("ORGANIZATION_ADDRESS2");
        organization2.setOfficialName("ORGANIZATION_OFFICIAL_NAME2");
        organization2.setOrganizationCode(2);
        organization2.setMsrn("msrn");
        organization2.setTid("tid");
        organizationRepository.save(organization2);

        organization3 = new Organization();
        organization3.setAddress("ORGANIZATION_ADDRESS3");
        organization3.setOfficialName("ORGANIZATION_OFFICIAL_NAME13");
        organization3.setOrganizationCode(3);
        organization3.setMsrn("msrn");
        organization3.setTid("tid");
        organizationRepository.save(organization3);

        organization4 = new Organization();
        organization4.setAddress("ORGANIZATION_ADDRESS4");
        organization4.setOfficialName("ORGANIZATION_OFFICIAL_NAME4");
        organization4.setOrganizationCode(4);
        organization4.setMsrn("msrn");
        organization4.setTid("tid");
        organizationRepository.save(organization4);

    }

    @AfterEach
    void afterEach() {
        organizationRepository.deleteAll();
        organizationGroupRepository.deleteAll();
    }

    @Test
    @DisplayName("Добавление")
    void add() throws Exception {
        var organizationIds = List.of(organization1.getId(), organization2.getId(), organization3.getId());
        var organizationGroupDTO = new OrganizationGroupDTO("СБЕР", organizationIds, true);
        var response = mockMvc.perform(
                MockMvcRequestBuilders.post("/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(organizationGroupDTO))
                        .with(jwt().jwt(builder -> builder.jti("f10bcc5b-51db-4e1c-a747-2a229604f974"))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                )
                .andExpect(status().isOk());

        response.andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(organizationGroupDTO.name()))
                .andExpect(jsonPath("$.internal").value(organizationGroupDTO.internal()));

        var organizationGroup = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(), OrganizationGroupResponseDTO.class);

        var organizations = organizationRepository.findAllByIdIn(organizationIds);
        organizations.forEach(organization -> Assertions.assertEquals(organization.getOrganizationGroup().getId(), organizationGroup.id()));
    }

    @Test
    @DisplayName("Получение всех")
    void getAll() throws Exception {
        var organizationIds = List.of(organization1.getId(), organization2.getId(), organization3.getId());
        var organizationGroupDTO = new OrganizationGroupDTO("СБЕР", organizationIds, true);
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/groups")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(organizationGroupDTO))
                                .with(jwt().jwt(builder -> builder.jti("f10bcc5b-51db-4e1c-a747-2a229604f974"))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/groups")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("name", "сбер")
                                .param("page","0")
                                .param("size","10")
                                .with(jwt().jwt(builder -> builder.jti("f10bcc5b-51db-4e1c-a747-2a229604f974"))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @DisplayName("Изменение")
    void put() throws Exception {
        var organizationIds = List.of(organization1.getId(), organization2.getId(), organization3.getId());
        var organizationGroupDTO = new OrganizationGroupDTO("СБЕР", organizationIds, true);
        var newOrganizationGroupResponse = mockMvc.perform(
                        MockMvcRequestBuilders.post("/groups")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(organizationGroupDTO))
                                .with(jwt().jwt(builder -> builder.jti("f10bcc5b-51db-4e1c-a747-2a229604f974"))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var newOrganizationGroup = objectMapper.readValue(newOrganizationGroupResponse, OrganizationGroupResponseDTO.class);
        var editedOrganizationIds = List.of(organization2.getId(), organization3.getId(), organization4.getId());
        var editedOrganizationGroupDTO = new OrganizationGroupDTO("СберБанк", editedOrganizationIds, true);
        mockMvc.perform(
                        MockMvcRequestBuilders.put("/groups/"+newOrganizationGroup.id())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(editedOrganizationGroupDTO))
                                .with(jwt().jwt(builder -> builder.jti("f10bcc5b-51db-4e1c-a747-2a229604f974"))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                )
                .andExpect(status().isOk());
        var editedOrganizationGroup = organizationGroupRepository.findById(newOrganizationGroup.id());
        assert editedOrganizationGroup.isPresent();
        Assertions.assertEquals(newOrganizationGroup.id(), editedOrganizationGroup.get().getId());
        Assertions.assertEquals(editedOrganizationGroupDTO.name(), editedOrganizationGroup.get().getName());
        Assertions.assertEquals(editedOrganizationGroupDTO.internal(), editedOrganizationGroup.get().isInternal());

        var editedOrganizations = organizationRepository.findAllByOrganizationGroupId(newOrganizationGroup.id());
        editedOrganizations.forEach(organization -> Assertions.assertTrue(editedOrganizationIds.contains(organization.getId())));
    }

    @Test
    @DisplayName("Частичное изменение")
    void patch() throws Exception {
        var organizationIds = List.of(organization1.getId(), organization2.getId(), organization3.getId());
        var organizationGroupDTO = new OrganizationGroupDTO("СБЕР", organizationIds, true);
        var newOrganizationGroupResponse = mockMvc.perform(
                        MockMvcRequestBuilders.post("/groups")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(organizationGroupDTO))
                                .with(jwt().jwt(builder -> builder.jti("f10bcc5b-51db-4e1c-a747-2a229604f974"))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var newOrganizationGroup = objectMapper.readValue(newOrganizationGroupResponse, OrganizationGroupResponseDTO.class);
        var editedOrganizationIds = List.of(organization2.getId(), organization3.getId(), organization4.getId());
        var patchData1 = new PatchData<>(OrganizationGroupPatchFields.NAME, "СберБанк");
        var patchData2 = new PatchData<>(OrganizationGroupPatchFields.ORGANIZATION_IDS, ReflectionUtils.cast(editedOrganizationIds));
        var requestList = List.of(patchData1, patchData2);
        mockMvc.perform(
                        MockMvcRequestBuilders.patch("/groups/"+newOrganizationGroup.id())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestList))
                                .with(jwt().jwt(builder -> builder.jti("f10bcc5b-51db-4e1c-a747-2a229604f974"))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                )
                .andExpect(status().isOk());
        var editedOrganizationGroup = organizationGroupRepository.findById(newOrganizationGroup.id());
        assert editedOrganizationGroup.isPresent();
        Assertions.assertEquals(newOrganizationGroup.id(), editedOrganizationGroup.get().getId());
        Assertions.assertEquals("СберБанк", editedOrganizationGroup.get().getName());

        var editedOrganizations = organizationRepository.findAllByOrganizationGroupId(newOrganizationGroup.id());
        editedOrganizations.forEach(organization -> Assertions.assertTrue(editedOrganizationIds.contains(organization.getId())));
    }

    @Test
    @DisplayName("Удаление")
    void delete() throws Exception {
        var organizationIds = List.of(organization1.getId(), organization2.getId(), organization3.getId());
        var organizationGroupDTO = new OrganizationGroupDTO("СБЕР", organizationIds, true);
        var newOrganizationGroupResponse = mockMvc.perform(
                        MockMvcRequestBuilders.post("/groups")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(organizationGroupDTO))
                                .with(jwt().jwt(builder -> builder.jti("f10bcc5b-51db-4e1c-a747-2a229604f974"))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var newOrganizationGroup = objectMapper.readValue(newOrganizationGroupResponse, OrganizationGroupResponseDTO.class);
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/groups/"+newOrganizationGroup.id())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(jwt().jwt(builder -> builder.jti("f10bcc5b-51db-4e1c-a747-2a229604f974"))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                )
                .andExpect(status().isOk());
        var deletedOrganizationGroup = organizationGroupRepository.findById(newOrganizationGroup.id());
        assert deletedOrganizationGroup.isEmpty();
        var organizationsInGroup = organizationRepository.findAllByOrganizationGroupId(newOrganizationGroup.id());
        assert organizationsInGroup.isEmpty();
    }

}
