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
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.file_works.dto.PageInfo;
import ru.sber.transport.file_works.services.UploadStates;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.corpclient.database.dao.*;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.database.model.TripPurpose;
import ru.sberbank.ditsib.corpclient.database.model.TripPurpose_;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sberbank.ditsib.transport.constants.TripPurposeType;
import ru.sberbank.ditsib.transport.messaging.messages.TripPurposeMessage;

import java.io.ByteArrayInputStream;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
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
@DisplayName("Импорт/экспорт целей поездки")
@ActiveProfiles({"test", "import"})
class TripPurposeImportExportTest extends SharedData {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean(name = "purposeOutput")
    private OutputBridge purposeOutput;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TripPurposeRepository tripPurposeRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private UploadStates uploadStates;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    private List<Organization> organizations;

    @BeforeEach
    void setup() {
        tripPurposeRepository.deleteAllInBatch();
        employeeRepository.deleteAllInBatch();
        positionRepository.deleteAllInBatch();
        departmentRepository.deleteAllInBatch();
        organizationRepository.deleteAllInBatch();
        organizations = new ArrayList<>();
        for (var i = 0; i < 5; i++) {
            var organization = new Organization();
            organization.setDigitId(i + 1L);
            organization.setOfficialName(String.format("Организация %02d", i + 1));
            organization.setAddress(String.format("Адрес %02d", i + 1));
            organization.setMsrn(String.format("ОГРН %02d", i + 1));
            organization.setTid(String.format("ИНН %02d", i + 1));
            organizations.add(organization);
        }

        organizationRepository.saveAll(organizations);
    }

    @AfterEach
    void deleteTestFolder() {
        tripPurposeRepository.deleteAllInBatch();
        employeeRepository.deleteAllInBatch();
        positionRepository.deleteAllInBatch();
        departmentRepository.deleteAllInBatch();
        organizationRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("Экспорт")
    void test_export() throws Exception {
        var organizations = organizationRepository.findAll();
        var count = 100;
        for (var i = 0; i < count; i++) {
            var tripPurpose = new TripPurpose();

            tripPurpose.setLabel(String.format("Цель %03d", i));
            tripPurpose.setActive(i % 2 == 0);
            tripPurpose.setOrganization(organizations.get(i % 5));

            tripPurposeRepository.save(tripPurpose);
        }

        var content = mockMvc.perform(get("/files/tripPurpose/")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
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

        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheet("Цели поездки");

            assertThat(sheet).isNotNull();

            var header = sheet.getRow(0);

            assertThat(header.getPhysicalNumberOfCells()).isEqualTo(4);
            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("Организация");
            assertThat(header.getCell(1).getStringCellValue()).isEqualTo("Название");
            assertThat(header.getCell(2).getStringCellValue()).isEqualTo("Активность");
            assertThat(header.getCell(3).getStringCellValue()).isEqualTo("Тип");

            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(count + 1);

            var actualList = tripPurposeRepository.findAllWithOrganization(Sort.by(TripPurpose_.LABEL));

            for (int i = 0, rowIndex = 1; i < count / 2; i++, rowIndex++) {
                var row = sheet.getRow(rowIndex);

                var actual = actualList.get(i);

                assertThat(row.getCell(0).getStringCellValue()).isEqualTo(actual.getOrganization().getOfficialName());
                assertThat(row.getCell(1).getStringCellValue()).isEqualTo(actual.getLabel());
                assertThat(row.getCell(2).getBooleanCellValue()).isEqualTo(actual.isActive());
                assertThat(row.getCell(3).getStringCellValue()).isEqualTo(actual.getPurposeType().getRusName());
            }
        }
    }

    @Test
    @DisplayName("Импорт")
    void test_import() throws Exception {
        var file = new MockMultipartFile("file", "file.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                getClass().getClassLoader().getResourceAsStream("load/tripPurposes.xlsx"));

        mockMvc.perform(multipart("/files/tripPurpose/").file(file)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        await().until(() -> uploadStates.getResults("tripPurpose", USER1_ID).size(), equalTo(1));
        await().until(() -> uploadStates.getResults("tripPurpose", USER1_ID).getFirst().getFinished(), equalTo(true));
        assertThat(uploadStates.getResults("tripPurpose", USER1_ID).getFirst().getPages().stream()
                .allMatch(page -> page.getExceptionStrings().isEmpty()))
                .isTrue();
        assertThat(uploadStates.getResults("tripPurpose", USER1_ID).getFirst().getPages().stream().map(PageInfo::getRow)
                .flatMap(Collection::stream).allMatch(page -> page.getExceptionStrings().isEmpty()))
                .isTrue();
        var actualList = tripPurposeRepository.findAll();
        var messageList = getMessages(purposeOutput, 15, TripPurposeMessage.class)
                .stream()
                .sorted(Comparator.comparing(TripPurposeMessage::getLabel)).toList();
        assertThat(actualList).hasSize(15); // из файла load/tripPurposes.xlsx
        assertThat(messageList).hasSize(15); // из файла load/tripPurposes.xlsx

        for (int index = 0, organizationIndex = 0; index < actualList.size(); index++, organizationIndex++) {
            if (organizationIndex == 5) { // количество различных организаций в файле
                organizationIndex = 0;
            }
            var actual = actualList.get(index);

            var organization = organizations.get(organizationIndex);

            assertThat(actual.getLabel()).isEqualTo(String.format("Цель %02d", index + 1));
            assertThat(actual.isActive()).isEqualTo(index % 2 == 0);
            assertThat(actual.getOrganization().getOfficialName()).isEqualTo(organization.getOfficialName());
            assertThat(actual.getPurposeType()).isEqualTo(
                    TripPurposeType.values()[index % TripPurposeType.values().length]);

            assertThat(actual.getPurposeType()).isEqualTo(
                    TripPurposeType.values()[index % TripPurposeType.values().length]);
            var isCount =
                    (int) messageList.stream().filter(t -> t.getId().equals(actual.getId())).count();
            assertThat(isCount).isEqualTo(1);
            var isDelete = (int) messageList.stream().filter(TripPurposeMessage::isDeleted).count();
            assertThat(isDelete).isEqualTo(7);
        }
    }
}
