package ru.sberbank.ditsib.corpclient.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.file_works.database.migrations.DatabaseMigration;
import ru.sber.transport.file_works.dto.PageInfo;
import ru.sber.transport.file_works.services.UploadStates;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.corpclient.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.corpclient.database.dao.PositionRepository;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.database.model.Position;
import ru.sberbank.ditsib.corpclient.dto.FileExportFilterDTO;
import ru.sberbank.ditsib.corpclient.dto.PositionDTO;
import ru.sberbank.ditsib.corpclient.dto.PositionSearchDTO;
import ru.sberbank.ditsib.corpclient.service.PositionService;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@SpringBootTest(properties = "export.tempDir=target/test_data/files")
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@DisplayName("Импорт/экспорт должностей")
@ActiveProfiles({"test", "import"})
@Import(DatabaseMigration.class)
class PositionImportExportTest extends SharedData {

    public static final String USER_ID = "username";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private PositionService positionService;

    @MockitoBean(name = "positionOutput")
    private OutputBridge positionOutput;

    @Autowired
    private UploadStates uploadStates;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    @AfterEach
    void deleteTestFolder() {
        positionRepository.findAll().forEach(p -> {
            p.setAvailableClasses(null);
            positionRepository.saveAndFlush(p);
        });
        positionRepository.deleteAllInBatch();
        organizationRepository.deleteAllInBatch();
    }

    @BeforeEach
    void setup() {
        positionRepository.findAll().stream().peek(p -> p.setAvailableClasses(null)).forEach(positionRepository::save);
        positionRepository.deleteAllInBatch();
        organizationRepository.deleteAllInBatch();

        organizationRepository.saveAll(IntStream.range(0, 100).mapToObj(i -> {
            var organization = new Organization();

            organization.setOfficialName(String.format("Организация %03d", i));
            organization.setAddress(String.format("Адрес %03d", i));
            organization.setTid(String.format("ИНН %03d", i));
            organization.setMsrn(String.format("ОГРН %03d", i));
            organization.setDigitId((long) i + 1);

            return organization;
        }).toList());
    }

    @Test
    @DisplayName("Импорт")
    void test_import() throws Exception {
        var file = new MockMultipartFile("file", "file.xlsx", "application/vnd.openxmlformats-officedocument" +
                ".spreadsheetml.sheet",
                getClass().getClassLoader().getResourceAsStream("load/positions.xlsx"));

        mockMvc.perform(multipart("/files/position/").file(file)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        await().until(() -> uploadStates.getResults("position", USER_ID).size(), equalTo(1));
        await().until(() -> uploadStates.getResults("position", USER_ID).getFirst().getFinished(), equalTo(true));
        assertThat(uploadStates.getResults("position", USER_ID).getFirst().getPages().stream().allMatch(page -> page.getExceptionStrings().isEmpty()))
                .isTrue();
        assertThat(uploadStates.getResults("position", USER_ID).getFirst().getPages().stream().map(PageInfo::getRow)
                .flatMap(Collection::stream).allMatch(page -> page.getExceptionStrings().isEmpty()))
                .isTrue();
        var count = 100;
        assertThat(positionRepository.count()).isEqualTo(count);

        var actualList = new ArrayList<>(positionService.search(PositionSearchDTO.builder().build()));
        actualList.sort(Comparator.comparing(PositionDTO::getName));

        var messages = getMessages(positionOutput, count, PositionMessage.class).stream()
                .sorted(Comparator.comparing(PositionMessage::getPositionName)).toList();

        for (var i = 0; i < count; i++) {
            var actual = actualList.get(i);
            var actualMessage = messages.get(i);

            var organization = organizationRepository.findById(actual.getOrganizationId()).orElseThrow();

            assertThat(actual.getName()).isEqualTo(String.format("Должность %03d", i));
            assertThat(organization.getOfficialName()).isEqualTo(String.format("Организация %03d", i));
            assertThat(actual.isSelfApproved()).isEqualTo(i % 2 == 0);
            assertFalse(actual.getAvailableClasses().isEmpty());

            assertThat(actualMessage.getPositionName()).isEqualTo(String.format("Должность %03d", i));
        }
    }

    @Test
    @DisplayName("Экспорт")
    void test_export() throws Exception {
        var count = 100;
        var organizations = organizationRepository.findAll();
        var positions = new ArrayList<Position>();
        for (var i = 0; i < count; i++) {
            var position = new Position();
            position.setName(String.format("Должность %03d", i));
            position.setSelfApproved(i % 2 == 0);
            position.setOrganization(organizations.getFirst());
            position.setHumanReadableId(String.format("HumanReadable %03d", i));

            switch (i % 3) {
                case 0 -> position.setAvailableClasses(Set.of(TaxiClass.ECONOMY));
                case 1 ->
                        position.setAvailableClasses(Set.of(TaxiClass.ECONOMY, TaxiClass.COMFORT, TaxiClass.COMFORT_PLUS));
                case 2 ->
                        position.setAvailableClasses(Set.of(TaxiClass.ECONOMY, TaxiClass.COMFORT, TaxiClass.COMFORT_PLUS,
                                TaxiClass.BUSINESS));
                default -> {
                }
            }
            positions.add(position);
        }
        positionRepository.saveAll(positions);

        var filter = new FileExportFilterDTO(organizations.getFirst().getId());
        var filterStr = objectMapper.writeValueAsString(filter);
        var encodedFilter = Base64.getEncoder().encodeToString(filterStr.getBytes(StandardCharsets.UTF_8));

        var content = mockMvc.perform(get("/files/position/?filters=" + encodedFilter)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        var response = new ObjectMapper().readValue(content, new TypeReference<Map<String, String>>() {
        });

        var bytes = await()
                .until(() -> mockMvc.perform(get(response.get("result_url"))
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))),
                        w -> !Objects.equals(w.andReturn().getResponse().getHeader("Content-Type"), "application/json")
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsByteArray();

        try (var bais = new ByteArrayInputStream(bytes);
             var excel = new XSSFWorkbook(bais)) {
            var sheet = excel.getSheet("Должности");

            assertThat(sheet).isNotNull();

            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(count + 1); // 1 - заголовок

            var row = sheet.getRow(0);
            assertThat(row.getCell(0).getStringCellValue()).isEqualTo("Организация");
            assertThat(row.getCell(1).getStringCellValue()).isEqualTo("Название");
            assertThat(row.getCell(2).getStringCellValue()).isEqualTo("Автосогласование");
            assertThat(row.getCell(3).getStringCellValue()).isEqualTo("Классы");

            var expectedList = positionRepository.findAll();

            for (int rowIndex = 1, index = 0; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++, index++) {
                var actualRow = sheet.getRow(rowIndex);
                var expected = expectedList.get(index);
                var organization = organizationRepository.findById(expected.getOrganization().getId()).orElseThrow();

                assertThat(actualRow.getCell(0).getStringCellValue()).isEqualTo(organization.getOfficialName());
                assertThat(actualRow.getCell(1).getStringCellValue()).isEqualTo(expected.getName());
                assertThat(actualRow.getCell(2).getBooleanCellValue()).isEqualTo(expected.isSelfApproved());
            }
        }
    }

}
