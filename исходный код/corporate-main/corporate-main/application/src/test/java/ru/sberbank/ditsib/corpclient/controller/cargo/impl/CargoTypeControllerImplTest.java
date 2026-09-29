package ru.sberbank.ditsib.corpclient.controller.cargo.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.corpclient.database.dao.*;
import ru.sberbank.ditsib.corpclient.database.model.CargoGroup;
import ru.sberbank.ditsib.corpclient.database.model.CargoType;
import ru.sberbank.ditsib.corpclient.dto.cargo.CargoTypeDto;
import ru.sberbank.ditsib.corpclient.dto.mapper.CargoTypeMapper;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sberbank.ditsib.transport.constants.cargo.CargoCategoryEnum;
import ru.sberbank.ditsib.transport.constants.cargo.CargoTypeEnum;
import ru.sberbank.ditsib.transport.messaging.messages.CargoTypeMessage;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@SpringBootTest(properties = {"spring.main.lazy-initialization=true", "spring.jpa.show-sql=true"})
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера справочника видов груза")
class CargoTypeControllerImplTest extends SharedData {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean(name = "cargoTypeOutput")
    private OutputBridge cargoTypeOutput;

    @Autowired
    private CargoTypeRepository repository;

    @Autowired
    private CargoGroupRepository cargoGroupRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionRepository positionRepository;

    CargoTypeDto document;
    CargoTypeDto furniture;
    CargoTypeDto materials;
    CargoTypeDto foodProduct;
    CargoTypeDto houseHoldGoods;
    CargoTypeDto tableWare;
    CargoTypeDto technique;
    CargoTypeDto other;

    @Autowired
    private CargoTypeMapper mapper;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @Transactional
    @AfterEach
    void afterEach() {
        repository.deleteAllInBatch();
        positionRepository.findAll().forEach(p -> {
            p.setAvailableClasses(null);
            positionRepository.save(p);
        });
        employeeRepository.findAll().forEach(e -> {
            e.setAvailableTransportTypes(null);
            employeeRepository.save(e);
        });
        departmentRepository.findAll().forEach(d -> {
            d.setHead(null);
            departmentRepository.save(d);
        });
        employeeRepository.deleteAllInBatch();
        positionRepository.deleteAllInBatch();
        departmentRepository.deleteAllInBatch();
        organizationRepository.deleteAllInBatch();
    }

    @Transactional
    @BeforeEach
    public void prepareData() {
        AuthorizeUtils.authorize(roleCheckService);

        testOrganization1 = organizationRepository.save(testOrganization1);
        testOrganization2 = organizationRepository.save(testOrganization2);
        testDepartment1.setOrganization(testOrganization1);
        testDepartment2.setOrganization(testOrganization2);
        testDepartment1 = departmentRepository.save(testDepartment1);
        testDepartment2 = departmentRepository.save(testDepartment2);
        testPosition1.setHumanReadableId("PS-001-1");
        testPosition2.setHumanReadableId("PS-001-2");
        testPosition1.setOrganization(testOrganization1);
        testPosition2.setOrganization(testOrganization2);
        positionRepository.save(testPosition1);
        positionRepository.save(testPosition2);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee2.setDepartment(testDepartment2);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        testEmployee2.setOrganization(testDepartment2.getOrganization());
        testEmployee1.setOrganization(testOrganization1);
        testEmployee1 = employeeRepository.save(testEmployee1);
        testEmployee2.setOrganization(testOrganization2);
        testEmployee2 = employeeRepository.save(testEmployee2);

        document = CargoTypeDto.builder()
                .type(CargoTypeEnum.DOCUMENT)
                .name("Бумажка важная")
                .category(CargoCategoryEnum.CORRESPONDENCE)
                .height(12.)
                .length(23.)
                .weight(11.)
                .width(23.)
                .volume(12. * 23. * 23.)
                .active(true)
                .build();
        furniture = CargoTypeDto.builder()
                .type(CargoTypeEnum.FURNITURE)
                .name("Тумбочка")
                .category(CargoCategoryEnum.REGULAR)
                .height(12.)
                .length(23.)
                .weight(11.)
                .width(23.)
                .active(true)
                .build();
        materials = CargoTypeDto.builder()
                .type(CargoTypeEnum.MATERIALS)
                .name("Шпаклевка")
                .category(CargoCategoryEnum.OTHER)
                .height(12.)
                .length(23.)
                .weight(11.)
                .width(23.)
                .active(true)
                .build();
        foodProduct = CargoTypeDto.builder()
                .type(CargoTypeEnum.FOOD_PRODUCTS)
                .name("Молоко")
                .category(CargoCategoryEnum.REGULAR)
                .height(12.)
                .length(23.)
                .weight(11.)
                .width(23.)
                .active(true)
                .build();
        houseHoldGoods = CargoTypeDto.builder()
                .type(CargoTypeEnum.HOUSEHOLD_GOODS)
                .name("Зубачистки")
                .category(CargoCategoryEnum.REGULAR)
                .height(12.)
                .length(23.)
                .weight(11.)
                .width(23.)
                .active(true)
                .build();
        tableWare = CargoTypeDto.builder()
                .type(CargoTypeEnum.TABLEWARE)
                .name("Стол")
                .category(CargoCategoryEnum.REGULAR)
                .height(12.)
                .length(23.)
                .weight(11.)
                .width(23.)
                .active(true)
                .build();
        technique = CargoTypeDto.builder()
                .type(CargoTypeEnum.TECHNIQUE)
                .name("Ноутбук")
                .category(CargoCategoryEnum.REGULAR)
                .height(12.)
                .length(23.)
                .weight(11.)
                .width(23.)
                .active(true)
                .build();
        other = CargoTypeDto.builder()
                .type(CargoTypeEnum.OTHER)
                .name("Кинга")
                .category(CargoCategoryEnum.OTHER)
                .height(12.)
                .length(23.)
                .weight(11.)
                .width(23.)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Получение ROLE_ENGINEER_CORP_CLIENT")
    void getTypeTestByEngineer() throws Exception {
        CargoType cargoType = mapper.newDtoToEntity(document);
        cargoType.setOrganization(testOrganization1);
        var saved = repository.save(cargoType);
        var response =
                mockMvc.perform(get("/%s/cargo/type/%s".formatted(testOrganization1.getId(), saved.getId()))
                                .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                        .andExpect(status().isOk());
        response.andExpect(jsonPath("$.length()").value(12));
        response.andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.name").value(saved.getName()))
                .andExpect(jsonPath("$.category").value(saved.getCategory().name()))
                .andExpect(jsonPath("$.type").value(saved.getType().name()))
                .andExpect(jsonPath("$.length").value(String.valueOf(saved.getLength())))
                .andExpect(jsonPath("$.height").value(String.valueOf(saved.getHeight())))
                .andExpect(jsonPath("$.volume").value(String.valueOf(saved.getVolume())))
                .andExpect(jsonPath("$.width").value(String.valueOf(saved.getWidth())))
                .andExpect(jsonPath("$.organizationId").value(String.valueOf(testOrganization1.getId())))
                .andExpect(jsonPath("$.universal").value(String.valueOf(false)))
                .andExpect(jsonPath("$.weight").value(String.valueOf(saved.getWeight())));
    }

    @Test
    @DisplayName("Получение ROLE_ADMIN_DATA_MASTER")
    void getTypeTestByAdmin() throws Exception {
        CargoType cargoType = mapper.newDtoToEntity(document);
        cargoType.setOrganization(testOrganization1);
        var saved = repository.save(cargoType);
        var response =
                mockMvc.perform(get("/%s/cargo/type/%s".formatted(testOrganization1.getId(), saved.getId()))
                                .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                        .andExpect(status().isOk());
        response.andExpect(jsonPath("$.length()").value(12));
        response.andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.name").value(saved.getName()))
                .andExpect(jsonPath("$.category").value(saved.getCategory().name()))
                .andExpect(jsonPath("$.type").value(saved.getType().name()))
                .andExpect(jsonPath("$.length").value(String.valueOf(saved.getLength())))
                .andExpect(jsonPath("$.height").value(String.valueOf(saved.getHeight())))
                .andExpect(jsonPath("$.volume").value(String.valueOf(saved.getVolume())))
                .andExpect(jsonPath("$.width").value(String.valueOf(saved.getWidth())))
                .andExpect(jsonPath("$.organizationId").value(String.valueOf(testOrganization1.getId())))
                .andExpect(jsonPath("$.universal").value(String.valueOf(false)))
                .andExpect(jsonPath("$.weight").value(String.valueOf(saved.getWeight())));
    }

    @Test
    @DisplayName("Получение ROLE_ADMIN_DATA_MASTER")
    void getTypeTestByAdmin2() throws Exception {
        CargoType cargoType = mapper.newDtoToEntity(document);
        var saved = repository.save(cargoType);
        var response =
                mockMvc.perform(get("/%s/cargo/type/%s".formatted(testOrganization1.getId(), saved.getId()))
                                .with(jwt().jwt(builder -> builder.claim("data_master", true).jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                        .andExpect(status().isOk());
        response.andExpect(jsonPath("$.length()").value(11));
        response.andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.name").value(saved.getName()))
                .andExpect(jsonPath("$.category").value(saved.getCategory().name()))
                .andExpect(jsonPath("$.type").value(saved.getType().name()))
                .andExpect(jsonPath("$.length").value(String.valueOf(saved.getLength())))
                .andExpect(jsonPath("$.height").value(String.valueOf(saved.getHeight())))
                .andExpect(jsonPath("$.volume").value(String.valueOf(saved.getVolume())))
                .andExpect(jsonPath("$.width").value(String.valueOf(saved.getWidth())))
                .andExpect(jsonPath("$.universal").value(String.valueOf(true)))
                .andExpect(jsonPath("$.weight").value(String.valueOf(saved.getWeight())));
    }

    @Test
    @DisplayName("Добавление ROLE_ENGINEER_CORP_CLIENT")
    void addTypeEngineer() throws Exception {
        var request = objectMapper.writeValueAsString(document);
        var response = mockMvc.perform(
                        post("/%s/cargo/type".formatted(testOrganization1.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isOk());
        assertThat(repository.count()).isEqualTo(1);
        var actualMessage = getMessages(cargoTypeOutput, CargoTypeMessage.class);
        assertNotNull(actualMessage);
        assertNotNull(actualMessage.getId());
        assertEquals(document.getName(), actualMessage.getName());
        response.andExpect(jsonPath("$.name").value(document.getName()))
                .andExpect(jsonPath("$.category").value(document.getCategory().name()))
                .andExpect(jsonPath("$.type").value(document.getType().name()))
                .andExpect(jsonPath("$.length").value(String.valueOf(document.getLength())))
                .andExpect(jsonPath("$.height").value(String.valueOf(document.getHeight())))
                .andExpect(jsonPath("$.volume").value(String.valueOf(document.getVolume())))
                .andExpect(jsonPath("$.width").value(String.valueOf(document.getWidth())))
                .andExpect(jsonPath("$.organizationId").value(String.valueOf(testOrganization1.getId())))
                .andExpect(jsonPath("$.universal").value(String.valueOf(false)))
                .andExpect(jsonPath("$.weight").value(String.valueOf(document.getWeight())));
    }

    @Test
    @DisplayName("Добавление без организации")
    void addTypeWithoutOrganization() throws Exception {
        var request = objectMapper.writeValueAsString(document);
        var response = mockMvc.perform(
                        post("/cargo/type")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isOk());
        assertThat(repository.count()).isEqualTo(1);
        var actualMessage = getMessages(cargoTypeOutput, CargoTypeMessage.class);
        assertNotNull(actualMessage);
        assertNotNull(actualMessage.getId());
        assertEquals(document.getName(), actualMessage.getName());
        response.andExpect(jsonPath("$.name").value(document.getName()))
                .andExpect(jsonPath("$.category").value(document.getCategory().name()))
                .andExpect(jsonPath("$.type").value(document.getType().name()))
                .andExpect(jsonPath("$.length").value(String.valueOf(document.getLength())))
                .andExpect(jsonPath("$.height").value(String.valueOf(document.getHeight())))
                .andExpect(jsonPath("$.volume").value(String.valueOf(document.getVolume())))
                .andExpect(jsonPath("$.width").value(String.valueOf(document.getWidth())))
                .andExpect(jsonPath("$.organizationId").value(String.valueOf(testOrganization1.getId())))
                .andExpect(jsonPath("$.weight").value(String.valueOf(document.getWeight())));
    }

    @Test
    @DisplayName("Добавление категории OTHER")
    void addTypeWrongCargoCategory() throws Exception {
        var request = objectMapper.writeValueAsString(other);
        mockMvc.perform(
                        post("/cargo/type")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isPreconditionFailed());
        assertThat(repository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("Добавление ROLE_ADMIN_DATA_MASTER")
    void addTypeAdmin() throws Exception {
        var request = objectMapper.writeValueAsString(document);
        var response = mockMvc.perform(
                        post("/%s/cargo/type".formatted(testOrganization1.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isOk());
        assertThat(repository.count()).isEqualTo(1);
        var actualMessage = getMessages(cargoTypeOutput, CargoTypeMessage.class);
        assertNotNull(actualMessage);
        assertNotNull(actualMessage.getId());
        assertEquals(document.getName(), actualMessage.getName());
        response.andExpect(jsonPath("$.name").value(document.getName()))
                .andExpect(jsonPath("$.category").value(document.getCategory().name()))
                .andExpect(jsonPath("$.type").value(document.getType().name()))
                .andExpect(jsonPath("$.length").value(String.valueOf(document.getLength())))
                .andExpect(jsonPath("$.height").value(String.valueOf(document.getHeight())))
                .andExpect(jsonPath("$.volume").value(String.valueOf(document.getVolume())))
                .andExpect(jsonPath("$.width").value(String.valueOf(document.getWidth())))
                .andExpect(jsonPath("$.organizationId").value(String.valueOf(testOrganization1.getId())))
                .andExpect(jsonPath("$.universal").value(String.valueOf(false)))
                .andExpect(jsonPath("$.weight").value(String.valueOf(document.getWeight())));
    }

    @Autowired
    private TransactionTemplate transactionTemplate;
    @Test
    @DisplayName("Изменение ROLE_ENGINEER_CORP_CLIENT")
    void updateTypeEngineer() throws Exception {

        CargoType cargoType = mapper.newDtoToEntity(document);
        cargoType.setOrganization(testOrganization1);
        var saved = transactionTemplate.execute(status ->
            repository.save(cargoType)
        );
        var group = CargoGroup.builder()
                .name("Studio")
                .cargoType(saved)
                .count(4)
                .build();

        saved.getCargoGroups().add(group);
        var reSaved = transactionTemplate.execute(status ->
                repository.save(cargoType)
        );


        var db = transactionTemplate.execute(status ->
                repository.findById(saved.getId())
        );
        assertThat(db.get().getId()).isEqualTo(saved.getId());
        assertThat(db.isPresent()).isTrue();
        assertThat(saved.getCargoGroups()).isNotEmpty();
        assertThat(db.get().getCargoGroups()).isNotEmpty();

        document.setHeight(1000.);
        document.setVolume(document.getHeight() * document.getLength() * document.getWidth());
        var request = objectMapper.writeValueAsString(document);
        var response = mockMvc.perform(
                        post("/%s/cargo/type/%s".formatted(testOrganization1.getId(), saved.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isOk());
        assertThat(repository.count()).isEqualTo(2);
        var actualMessage = getMessages(cargoTypeOutput, 2, CargoTypeMessage.class);
        assertThat(actualMessage)
                .hasSize(2);

        response.andExpect(jsonPath("$.name").value(document.getName()))
                .andExpect(jsonPath("$.category").value(document.getCategory().name()))
                .andExpect(jsonPath("$.type").value(document.getType().name()))
                .andExpect(jsonPath("$.length").value(String.valueOf(document.getLength())))
                .andExpect(jsonPath("$.height").value(String.valueOf(document.getHeight())))
                .andExpect(jsonPath("$.volume").value(String.valueOf(document.getVolume())))
                .andExpect(jsonPath("$.width").value(String.valueOf(document.getWidth())))
                .andExpect(jsonPath("$.organizationId").value(String.valueOf(testOrganization1.getId())))
                .andExpect(jsonPath("$.universal").value(String.valueOf(false)))
                .andExpect(jsonPath("$.weight").value(String.valueOf(document.getWeight())));

        CargoTypeDto actualDto =
                objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                        CargoTypeDto.class);


        var actual =  repository.findById(actualDto.getId());
        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get().getCargoGroups()).isNotEmpty();
        assertThat(actual.get().getCargoGroups().size()).isEqualTo(1);

        assertThat(cargoGroupRepository.findAll().size()).isEqualTo(1);
        var expectedGroup = cargoGroupRepository.findAll().get(0);

        assertThat(actual.get().getCargoGroups().get(0).getId()).isEqualTo(expectedGroup.getId());
        assertThat(actual.get().getCargoGroups().get(0).getCargoType().getId())
                .isEqualTo(expectedGroup.getCargoType().getId());

        var oldType = repository.findById(saved.getId()).get();
        assertThat(oldType.isActive()).isFalse();
        assertThat(oldType.getCargoGroups()).isEmpty();

    }

    @Test
    @DisplayName("Изменение ROLE_ADMIN_DATA_MASTER")
    void updateTypeEngineerAdmin() throws Exception {
        CargoType cargoType = mapper.newDtoToEntity(document);
        cargoType.setOrganization(testOrganization1);
        var saved = repository.save(cargoType);
        document.setHeight(1000.);
        document.setVolume(document.getHeight() * document.getLength() * document.getWidth());
        document.setUniversal(true);
        var request = objectMapper.writeValueAsString(document);
        var response = mockMvc.perform(
                        post("/%s/cargo/type/%s".formatted(testOrganization1.getId(), saved.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(jwt().jwt(builder -> builder.claim("data_master", true).jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isOk());
        assertThat(repository.count()).isEqualTo(2);
        var actualMessage = getMessages(cargoTypeOutput, 2, CargoTypeMessage.class);
        assertThat(actualMessage)
                .hasSize(2);

        response.andExpect(jsonPath("$.name").value(document.getName()))
                .andExpect(jsonPath("$.category").value(document.getCategory().name()))
                .andExpect(jsonPath("$.type").value(document.getType().name()))
                .andExpect(jsonPath("$.length").value(String.valueOf(document.getLength())))
                .andExpect(jsonPath("$.height").value(String.valueOf(document.getHeight())))
                .andExpect(jsonPath("$.volume").value(String.valueOf(document.getVolume())))
                .andExpect(jsonPath("$.width").value(String.valueOf(document.getWidth())))
                .andExpect(jsonPath("$.organizationId").doesNotExist())
                .andExpect(jsonPath("$.universal").value(String.valueOf(true)))
                .andExpect(jsonPath("$.weight").value(String.valueOf(document.getWeight())));
    }

    @Test
    @DisplayName("Удаление ROLE_ADMIN_DATA_MASTER")
    void deleteTypeAdmin() throws Exception {
        var saved = repository.save(mapper.newDtoToEntity(document));
        assertThat(repository.count()).isEqualTo(1);

        mockMvc.perform(delete("/%s/cargo/type/%s".formatted(testOrganization1.getId(), saved.getId()))
                        .with(jwt().jwt(builder -> builder.claim("data_master", true).jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        assertThat(new ArrayList<>(repository.findAll())).size().isEqualTo(1);
        var actualMessage = getMessages(cargoTypeOutput, CargoTypeMessage.class);
        assertNotNull(actualMessage);
        assertNotNull(actualMessage.getId());
        assertTrue(actualMessage.isDeleted());
    }

    @Test
    @DisplayName("Удаление ROLE_ENGINEER_CORP_CLIENT")
    void deleteTypeEngineer() throws Exception {
        CargoType cargoType = mapper.newDtoToEntity(document);
        cargoType.setOrganization(testOrganization1);
        var saved = repository.save(cargoType);
        assertThat(repository.count()).isEqualTo(1);

        mockMvc.perform(delete("/%s/cargo/type/%s".formatted(testOrganization1.getId(), saved.getId()))
                        .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        assertThat(new ArrayList<>(repository.findAll())).size().isEqualTo(1);
        var actualMessage = getMessages(cargoTypeOutput, CargoTypeMessage.class);
        assertNotNull(actualMessage);
        assertNotNull(actualMessage.getId());
        assertTrue(actualMessage.isDeleted());
    }

    @Test
    @DisplayName("Получение всех ROLE_ADMIN_DATA_MASTER")
    void getTypesAdmin() throws Exception {
        repository.save(mapper.newDtoToEntity(document));
        repository.save(mapper.newDtoToEntity(foodProduct));
        repository.save(mapper.newDtoToEntity(furniture));
        repository.save(mapper.newDtoToEntity(tableWare));
        repository.save(mapper.newDtoToEntity(technique));
        repository.save(mapper.newDtoToEntity(houseHoldGoods));
        repository.save(mapper.newDtoToEntity(materials));
        repository.save(mapper.newDtoToEntity(other));
        var response =
                mockMvc.perform(get("/%s/cargo/type".formatted(testOrganization1.getId()))
                                .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(8));

        for (var i = 0; i < repository.count(); i++) {
            var expected = repository.findAll().get(i);
            response.andExpect(jsonPath("$[" + i + "].id").value(expected.getId().toString()))
                    .andExpect(jsonPath("$[" + i + "].name").value(expected.getName()))
                    .andExpect(jsonPath("$[" + i + "].category").value(expected.getCategory().name()))
                    .andExpect(jsonPath("$[" + i + "].type").value(expected.getType().name()))
                    .andExpect(jsonPath("$[" + i + "].length").value(String.valueOf(expected.getLength())))
                    .andExpect(jsonPath("$[" + i + "].height").value(String.valueOf(expected.getHeight())))
                    .andExpect(jsonPath("$[" + i + "].volume").value(String.valueOf(expected.getVolume())))
                    .andExpect(jsonPath("$[" + i + "].width").value(String.valueOf(expected.getWidth())))
                    .andExpect(jsonPath("$[" + i + "].weight").value(String.valueOf(expected.getWeight())));
        }
    }

    @Test
    @DisplayName("Получение всех видов грузов")
    void getAllTypes() throws Exception {
        var response =
                mockMvc.perform(get("/cargo/type/types")
                                .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                        .andExpect(status().isOk());
        response.andExpect(jsonPath("$.length()").value(CargoTypeEnum.values().length));
    }

    @Test
    @DisplayName("Получение всех категорий")
    void getAllCategory() throws Exception {
        var response =
                mockMvc.perform(get("/cargo/type/categories")
                                .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                        .andExpect(status().isOk());
        response.andExpect(jsonPath("$.length()").value(CargoCategoryEnum.values().length - 1))
                .andExpect(jsonPath("$[*].name",
                        containsInAnyOrder(Arrays.stream(CargoCategoryEnum.values()).filter(c -> c != CargoCategoryEnum.OTHER).map(Enum::name).toArray())));
    }

    @Test
    @DisplayName("Получение несуществующего типа")
    void getNotExists() throws Exception {
        var response =
                mockMvc.perform(get("/%s/cargo/type/%s".formatted(testOrganization1.getId(), UUID.fromString("a5fd508b-395a-4c31-963b-4ddf244c7999")))
                                .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                        .andExpect(status().is(404));
        response.andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$.message").value("Data not found: Entity: CargoType, ID: a5fd508b-395a-4c31-963b-4ddf244c7999"));
    }

    @Test
    @DisplayName("Изменение несуществующего")
    void updateNotExists() throws Exception {
        document.setId(UUID.fromString("a5fd508b-395a-4c31-963b-4ddf244c7999"));
        var request = objectMapper.writeValueAsString(document);
        var response =
                mockMvc.perform(post("/%s/cargo/type/%s".formatted(testOrganization1.getId(), UUID.fromString("a5fd508b-395a-4c31-963b-4ddf244c7999")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                        .andExpect(status().is(404));
        response.andExpect(jsonPath("$.message").value("Data not found: Entity: CargoType, ID: a5fd508b-395a-4c31-963b-4ddf244c7999"));
    }

    @Test
    @DisplayName("Добавление дублирующего значения")
    void insertDuplicate() throws Exception {
        repository.save(mapper.newDtoToEntity(document));
        document.setId(null);
        var request = objectMapper.writeValueAsString(document);
        var response =
                mockMvc.perform(post("/%s/cargo/type".formatted(testOrganization1.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                        .andExpect(status().is(409));
        response.andExpect(jsonPath("$.message").value("Conflict data on entity CargoType. Conflicted: {name=Бумажка важная}"));
    }

    @Test
    @DisplayName("Удаление несуществующего")
    void deleteNotExists() throws Exception {
        var response =
                mockMvc.perform(delete("/%s/cargo/type/%s".formatted(testOrganization1.getId(), UUID.fromString("a5fd508b-395a-4c31-963b-4ddf244c7999")))
                                .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                        .andExpect(status().is(404));
        response.andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$.message").value("Data not found: Entity: CargoType, ID: a5fd508b-395a-4c31-963b-4ddf244c7999"));
    }

    @Test
    @DisplayName("Поиск без организации user")
    void searchWithEmptyOrganizationUser() throws Exception {
        repository.save(mapper.newDtoToEntity(document));
        repository.save(mapper.newDtoToEntity(furniture));

        var response =
                mockMvc.perform(get("/cargo/type/search?text=%s".formatted("бумажка"))
                                .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                        .andExpect(status().isOk());

        response.andExpect(jsonPath("$.length()").value(1));

        var furnitureBig = mapper.newDtoToEntity(CargoTypeDto.builder()
                .type(CargoTypeEnum.FURNITURE)
                .name("Тумбочка большая")
                .category(CargoCategoryEnum.OTHER)
                .height(12.)
                .length(23.)
                .weight(11.)
                .width(23.)
                .organizationId(testOrganization2.getId())
                .active(true)
                .build());

        furnitureBig.setOrganization(testOrganization2);

        repository.save(furnitureBig);

        response = mockMvc.perform(get("/cargo/type/search?text=%s".formatted("тумбочка"))
                        .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        response.andExpect(jsonPath("$.length()").value(1));

        response = mockMvc.perform(get("/cargo/type/search?text=%s".formatted("скамейка"))
                        .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        response.andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Поиск без организации admin")
    void searchWithEmptyOrganizationAdmin() throws Exception {
        repository.save(mapper.newDtoToEntity(document));
        repository.save(mapper.newDtoToEntity(furniture));

        var response =
                mockMvc.perform(get("/cargo/type/search?text=%s".formatted("бумажка"))
                                .with(jwt().jwt(builder -> builder.claim("data_master", true)
                                                .jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                        .andExpect(status().isOk());

        response.andExpect(jsonPath("$.length()").value(1));

        var furnitureBig = mapper.newDtoToEntity(CargoTypeDto.builder()
                .type(CargoTypeEnum.FURNITURE)
                .name("Тумбочка большая")
                .category(CargoCategoryEnum.OTHER)
                .height(12.)
                .length(23.)
                .weight(11.)
                .width(23.)
                .organizationId(testOrganization1.getId())
                .active(true)
                .build());

        furnitureBig.setOrganization(testOrganization1);

        repository.save(furnitureBig);

        response = mockMvc.perform(get("/cargo/type/search?text=%s".formatted("тумбочка"))
                        .with(jwt().jwt(builder -> builder.claim("data_master", true)
                                        .jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        response.andExpect(jsonPath("$.length()").value(2));

        response = mockMvc.perform(get("/cargo/type/search?text=%s".formatted("скамейка"))
                        .with(jwt().jwt(builder -> builder.claim("data_master", true)
                                        .jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        response.andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Поиск ROLE_ADMIN_DATA_MASTER")
    void searchAdmin() throws Exception {
        repository.save(mapper.newDtoToEntity(document));
        repository.save(mapper.newDtoToEntity(furniture));

        var response =
                mockMvc.perform(get("/%s/cargo/type/search?text=%s".formatted(testOrganization1.getId(), "бумажка"))
                                .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                        .andExpect(status().isOk());

        response.andExpect(jsonPath("$.length()").value(1));

        var furnitureBig = mapper.newDtoToEntity(CargoTypeDto.builder()
                .type(CargoTypeEnum.FURNITURE)
                .name("Тумбочка большая")
                .category(CargoCategoryEnum.OTHER)
                .height(12.)
                .length(23.)
                .weight(11.)
                .width(23.)
                .organizationId(testOrganization1.getId())
                .active(true)
                .build());

        furnitureBig.setOrganization(testOrganization1);

        repository.save(furnitureBig);

        response = mockMvc.perform(get("/%s/cargo/type/search?text=%s".formatted(testOrganization1.getId(), "тумбочка"))
                        .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        response.andExpect(jsonPath("$.length()").value(2));

        response = mockMvc.perform(get("/%s/cargo/type/search?text=%s".formatted(testOrganization1.getId(), "скамейка"))
                        .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        response.andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Поиск ROLE_ENGINEER_CORP_CLIENT")
    void searchEngineer() throws Exception {
        CargoType cargoTypeDocument = mapper.newDtoToEntity(document);
        cargoTypeDocument.setOrganization(testOrganization1);
        repository.save(cargoTypeDocument);

        repository.save(mapper.newDtoToEntity(furniture));

        var response =
                mockMvc.perform(get("/%s/cargo/type/search?text=%s".formatted(testOrganization1.getId(), "бумажка"))
                                .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                        .andExpect(status().isOk());

        response.andExpect(jsonPath("$.length()").value(1));

        var furnitureBig = mapper.newDtoToEntity(CargoTypeDto.builder()
                .type(CargoTypeEnum.FURNITURE)
                .name("Тумбочка большая")
                .category(CargoCategoryEnum.OTHER)
                .height(12.)
                .length(23.)
                .weight(11.)
                .width(23.)
                .organizationId(testOrganization1.getId())
                .active(true)
                .build());

        furnitureBig.setOrganization(testOrganization1);

        repository.save(furnitureBig);

        response = mockMvc.perform(get("/%s/cargo/type/search?text=%s".formatted(testOrganization1.getId(), "тумбочка"))
                        .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        response.andExpect(jsonPath("$.length()").value(2));

        CargoType cargoTypeMaterials = mapper.newDtoToEntity(materials);
        cargoTypeMaterials.setOrganization(testOrganization2);
        repository.save(cargoTypeMaterials);

        response = mockMvc.perform(get("/%s/cargo/type/search?text=%s".formatted(testOrganization1.getId(), "Шпаклевка"))
                        .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        response.andExpect(jsonPath("$.length()").value(0));
    }
}