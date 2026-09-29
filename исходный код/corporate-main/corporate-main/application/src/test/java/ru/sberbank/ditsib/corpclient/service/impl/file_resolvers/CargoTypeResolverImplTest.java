package ru.sberbank.ditsib.corpclient.service.impl.file_resolvers;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.file_works.database.migrations.DatabaseMigration;
import ru.sber.transport.file_works.services.UploadStates;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.corpclient.database.dao.CargoTypeRepository;
import ru.sberbank.ditsib.corpclient.dto.cargo.CargoTypeDto;
import ru.sberbank.ditsib.corpclient.dto.mapper.CargoTypeMapper;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sberbank.ditsib.transport.constants.cargo.CargoCategoryEnum;
import ru.sberbank.ditsib.transport.constants.cargo.CargoTypeEnum;
import ru.sberbank.ditsib.transport.messaging.messages.CargoTypeMessage;

import java.util.Comparator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Грузы. Справочник видов груза")
@Transactional
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.main.lazy-initialization=true")
@ActiveProfiles({"test", "import"})
@Import(DatabaseMigration.class)
class CargoTypeResolverImplTest extends SharedData {

    @MockitoBean
    private JwtDecoder jwtDecoder;
    
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;
    
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private UploadStates uploadStates;
    
    @Autowired
    private CargoTypeRepository repository;
    
    @Autowired
    private CargoTypeMapper mapper;
    
    @Autowired
    private CargoTypeResolverImpl resolver;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @MockitoBean(name = "cargoTypeOutput")
    private OutputBridge cargoTypeOutput;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }
    
    CargoTypeDto document;
    CargoTypeDto furniture;
    CargoTypeDto materials;
    CargoTypeDto foodProduct;
    CargoTypeDto houseHoldGoods;
    CargoTypeDto tableWare;
    CargoTypeDto tecknique;
    CargoTypeDto other;
    
    public void prepareData() {
        document = CargoTypeDto.builder()
                               .type(CargoTypeEnum.DOCUMENT)
                               .name("Бумажка важная")
                               .category(CargoCategoryEnum.OTHER)
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
                                .category(CargoCategoryEnum.OTHER)
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
                                  .category(CargoCategoryEnum.OTHER)
                                  .height(12.)
                                  .length(23.)
                                  .weight(11.)
                                  .width(23.)
                                  .active(true)
                                  .build();
        houseHoldGoods = CargoTypeDto.builder()
                                     .type(CargoTypeEnum.HOUSEHOLD_GOODS)
                                     .name("Зубачистки")
                                     .category(CargoCategoryEnum.OTHER)
                                     .height(12.)
                                     .length(23.)
                                     .weight(11.)
                                     .width(23.)
                                     .active(true)
                                     .build();
        tableWare = CargoTypeDto.builder()
                                .type(CargoTypeEnum.TABLEWARE)
                                .name("Стол")
                                .category(CargoCategoryEnum.OTHER)
                                .height(12.)
                                .length(23.)
                                .weight(11.)
                                .width(23.)
                                .active(true)
                                .build();
        tecknique = CargoTypeDto.builder()
                                .type(CargoTypeEnum.TECHNIQUE)
                                .name("Ноутбук")
                                .category(CargoCategoryEnum.OTHER)
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
    @DisplayName("Экспорт")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void exportData() {
        prepareData();
        repository.save(mapper.newDtoToEntity(document));
        repository.save(mapper.newDtoToEntity(foodProduct));
        repository.save(mapper.newDtoToEntity(furniture));
        repository.save(mapper.newDtoToEntity(tableWare));
        repository.save(mapper.newDtoToEntity(tecknique));
        repository.save(mapper.newDtoToEntity(houseHoldGoods));
        repository.save(mapper.newDtoToEntity(materials));
        repository.save(mapper.newDtoToEntity(other));
        var expected = repository.findAll();
        var result = resolver.exportData(Map.of(), new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti("id").build()));
        assertEquals(expected.size(), result.size());
    }
    
    @Test
    @DisplayName("Импорт")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void importData() throws Exception {
        var beginCount = repository.count();
        var file = new MockMultipartFile("file", "file.xlsx",
                                         "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                         getClass().getClassLoader().getResourceAsStream("load/cargoType.xlsx"));
    
        mockMvc.perform(multipart("/files/cargoType/").file(file)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
               .andExpect(status().isOk());
        await().until(() -> uploadStates.getResults("cargoType", USER1_ID).size(), equalTo(1));
        await().until(() -> uploadStates.getResults("cargoType", USER1_ID).getFirst().getFinished(), equalTo(true));
        var actualList = repository.findAll();
        var actualMessage = getMessages(cargoTypeOutput, 3, CargoTypeMessage.class) // 3 элемента в файле
            .stream().sorted(Comparator.comparing(CargoTypeMessage::getId)).toList();
        assertNotNull(actualMessage);
        assertAll("Sizes",
                  () -> assertThat(actualList).hasSize(actualMessage.size() + (int) beginCount));
    }
}