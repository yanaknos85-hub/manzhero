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
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.corpclient.database.dao.*;
import ru.sberbank.ditsib.corpclient.database.model.CargoGroup;
import ru.sberbank.ditsib.corpclient.dto.cargo.CargoGroupDto;
import ru.sberbank.ditsib.corpclient.dto.cargo.CargoTypeDto;
import ru.sberbank.ditsib.corpclient.dto.mapper.CargoTypeMapper;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sberbank.ditsib.transport.constants.cargo.CargoCategoryEnum;
import ru.sberbank.ditsib.transport.constants.cargo.CargoTypeEnum;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
@DisplayName("Проверка контроллера справочника групп грузов")
class CargoGroupControllerImplTest extends SharedData {

    public static final String BASE_URL = "/cargo/type/group";
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private TransactionTemplate transactionTemplate;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

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
    @DisplayName("Добавление ROLE_ENGINEER_CORP_CLIENT")
    void addGroup() throws Exception {
        var saved = repository.save(mapper.newDtoToEntity(document));

        var request = objectMapper.writeValueAsString(
                CargoGroupDto.builder()
                        .name("Studio")
                        .cargoTypeId(saved.getId())
                        .count(2)
                .build());
        var response = mockMvc.perform(
                        post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isOk());
        assertThat(repository.count()).isEqualTo(1);

        response
                .andExpect(jsonPath("$.count").value("2"))
                .andExpect(jsonPath("$.name").value(document.getName()))
                .andExpect(jsonPath("$.category").value(document.getCategory().name()))
                .andExpect(jsonPath("$.type").value(document.getType().name()))
                .andExpect(jsonPath("$.length").value(String.valueOf(document.getLength())))
                .andExpect(jsonPath("$.height").value(String.valueOf(document.getHeight())))
                .andExpect(jsonPath("$.volume").value(String.valueOf(document.getVolume())))
                .andExpect(jsonPath("$.width").value(String.valueOf(document.getWidth())))
                .andExpect(jsonPath("$.universal").value(String.valueOf(true)))
                .andExpect(jsonPath("$.weight").value(String.valueOf(document.getWeight())));
    }

    @Test
    @DisplayName("Удаление группы")
    void deleteGroup() throws Exception {
        var saved = repository.save(mapper.newDtoToEntity(document));
        var savedGroup = cargoGroupRepository.save(CargoGroup.builder()
                .name("Studio")
                .cargoType(saved)
                .count(2)
                .build());

        assertThat(cargoGroupRepository.count()).isEqualTo(1);

        mockMvc.perform(delete(BASE_URL+"/%s".formatted(savedGroup.getId()))
                        .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN_DATA_MASTER"))))
                .andExpect(status().isOk());


        assertThat(cargoGroupRepository.count()).isEqualTo(0);

    }

    @Test
    @DisplayName("Получение всех грузовых типов для группы Studiо")
    void getStudioTypes() throws Exception {
        repository.save(mapper.newDtoToEntity(document));
        var foodEntity = repository.save(mapper.newDtoToEntity(foodProduct));
        repository.save(mapper.newDtoToEntity(furniture));
        var tableEntity = repository.save(mapper.newDtoToEntity(tableWare));
        var techniqueEntity = repository.save(mapper.newDtoToEntity(technique));
        repository.save(mapper.newDtoToEntity(houseHoldGoods));
        repository.save(mapper.newDtoToEntity(materials));
        repository.save(mapper.newDtoToEntity(other));

        cargoGroupRepository.saveAll(List.of(
                CargoGroup.builder()
                        .name("Студия")
                        .cargoType(foodEntity)
                        .count(2)
                        .build(),
                CargoGroup.builder()
                        .name("Студия")
                        .cargoType(tableEntity)
                        .count(3)
                        .build(),
                CargoGroup.builder()
                        .name("Студия")
                        .cargoType(techniqueEntity)
                        .count(4)
                        .build()
                ));

        var response =
                mockMvc.perform(get((BASE_URL + "/search/?name=%s").formatted(
                         "Студия"))
                                .with(jwt().jwt(builder -> builder.jti(Objects.requireNonNull(testEmployee1.getId()).toString()))
                                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(3));

        System.out.println("response: "+response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        var expectedArray = repository.findAllById(List.of(foodEntity.getId(), tableEntity.getId(), techniqueEntity.getId()));
        for (var i = 0; i < 3; i++) {
            var expected = expectedArray.get(i);
            response.andExpect(jsonPath("$[" + i + "].id").value(expected.getId().toString()))
                    .andExpect(jsonPath("$[" + i + "].name").value(expected.getName()))
                    .andExpect(jsonPath("$[" + i + "].category").value(expected.getCategory().name()))
                    .andExpect(jsonPath("$[" + i + "].type").value(expected.getType().name()))
                    .andExpect(jsonPath("$[" + i + "].length").value(String.valueOf(expected.getLength())))
                    .andExpect(jsonPath("$[" + i + "].height").value(String.valueOf(expected.getHeight())))
                    .andExpect(jsonPath("$[" + i + "].volume").value(String.valueOf(expected.getVolume())))
                    .andExpect(jsonPath("$[" + i + "].width").value(String.valueOf(expected.getWidth())))
                    .andExpect(jsonPath("$[" + i + "].weight").value(String.valueOf(expected.getWeight())))
                    .andExpect(jsonPath("$[" + i + "].universal").value(String.valueOf(true)))
                    .andExpect(jsonPath("$[" + i + "].count").value(String.valueOf(i + 2)));
        }
    }
}