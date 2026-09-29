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
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.file_works.database.migrations.DatabaseMigration;
import ru.sber.transport.file_works.dto.PageInfo;
import ru.sber.transport.file_works.services.UploadStates;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.corpclient.database.dao.*;
import ru.sberbank.ditsib.corpclient.database.model.Department;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.database.model.Position;
import ru.sberbank.ditsib.corpclient.database.model.messages.GeoZone;
import ru.sberbank.ditsib.corpclient.dto.FileExportFilterDTO;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@SpringBootTest
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Импорт/экспорт подразделения")
@ActiveProfiles({"test", "import"})
@Import(DatabaseMigration.class)
class DepartmentImportExportTest extends SharedData {

    public static final String USER_ID = "e759f5f7-53a9-4727-aef7-1e87b15adb0f";

    @MockitoBean
    private JwtDecoder decoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private UploadStates uploadStates;

    @Autowired
    private GeoZoneRepository geoZoneRepository;

    @MockitoBean(name = "departmentsOutput")
    private OutputBridge departmentsOutput;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    @AfterEach
    void deleteTestFolder() {
        departmentRepository.findAll().forEach(d -> {
            d.setHead(null);
            departmentRepository.save(d);
        });
        employeeRepository.deleteAllInBatch();
        positionRepository.deleteAllInBatch();
        departmentRepository.deleteAllInBatch();
        organizationRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("Импорт")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void test_import() throws Exception {
        employeeRepository.deleteAllInBatch();
        departmentRepository.deleteAllInBatch();
        positionRepository.deleteAllInBatch();
        organizationRepository.deleteAllInBatch();

        var organizationParent = new Organization();

        organizationParent.setOfficialName("Организация 001");
        organizationParent.setAddress("Адрес");
        organizationParent.setMsrn("msrn");
        organizationParent.setTid("tid");

        organizationParent = organizationRepository.saveAndFlush(organizationParent);

        var departmentParent = new Department();

        departmentParent.setHumanReadableId("HRI");
        departmentParent.setOrganization(organizationParent);
        departmentParent.setName("Подразделениее");
        departmentParent.setCode("Кодд");
        departmentParent.setUpdateTime(OffsetDateTime.now());

        departmentParent = departmentRepository.saveAndFlush(departmentParent);

        var positionParent = new Position();

        positionParent.setName("Должностьь");
        positionParent.setOrganization(organizationParent);
        positionParent.setHumanReadableId("PHRI");

        positionParent = positionRepository.save(positionParent);

        var employeeParent = new Employee();

        employeeParent.setId(UUID.randomUUID());
        employeeParent.setNew(true);
        employeeParent.setPersonnelNumber("1");
        employeeParent.setLastName("Last");
        employeeParent.setFirstName("First");
        employeeParent.setDepartment(departmentParent);
        employeeParent.setOrganization(departmentParent.getOrganization());
        employeeParent.setPosition(positionParent);
        employeeParent.setHumanReadableId("EHRI");
        employeeParent.setUserId(UUID.fromString(USER_ID));
        employeeParent.setUpdateTime(OffsetDateTime.now());

        employeeRepository.saveAndFlush(employeeParent);

        var count = 4;
        for (var i = 0; i < count; i++) {
            var geoZone = new GeoZone();
            geoZone.setCode(String.format("Код %03d", i));
            geoZone.setName(String.format("Местоположение %03d", i));
            geoZone.setId(UUID.randomUUID());
            geoZoneRepository.save(geoZone);
        }
        var file = new MockMultipartFile("file", "file.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                getClass().getClassLoader().getResourceAsStream("load/departments.xlsx"));

        mockMvc.perform(multipart("/files/department/").file(file)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        await().until(() -> uploadStates.getResults("department", USER_ID).size(), equalTo(1));
        await().until(() -> uploadStates.getResults("department", USER_ID).getFirst().getFinished(), equalTo(true));
        assertThat(uploadStates.getResults("department", USER_ID).getFirst().getPages().stream()
                .allMatch(page -> page.getExceptionStrings().isEmpty()))
                .isTrue();
        assertThat(uploadStates.getResults("department", USER_ID).getFirst().getPages().stream().map(PageInfo::getRow)
                .flatMap(Collection::stream).allMatch(page -> page.getExceptionStrings().isEmpty())).isTrue();
        assertThat(departmentRepository.count()).isEqualTo(3 + 1); // 1 - инфо о подразделении рук-ля

        var actualList = departmentRepository.findAll(Sort.by("code"));

        var messages = getMessages(departmentsOutput, 3, DepartmentMessage.class) // В файле 3 подразделения
                .stream()
                .sorted(Comparator.comparing(DepartmentMessage::getCode)).toList();

        assertDatabase(actualList);

        assertMessages(messages, actualList);
    }

    private void assertDatabase(List<Department> actual) {
        var actualOrganization = organizationRepository.findById(actual.getFirst().getOrganization().getId()).orElseThrow();

        // организация под индексом 0 - организация для создания рук-ля. В тесте не участвует.
        assertThat(actualOrganization.getOfficialName()).isEqualTo("Организация 001");
        assertThat(actual.getFirst().getName()).isEqualTo("Подразделение 001");
        assertThat(actual.getFirst().getLevelName()).isEqualTo("1");
        assertThat(actual.getFirst().getLevelCode()).isEqualTo(101);
        assertThat(actual.getFirst().getCode()).isEqualTo("201");
        assertThat(actual.getFirst().getLocation()).isEqualTo("Местоположение 001");
        assertThat(actual.getFirst().getParent()).isNull();
        assertThat(actual.getFirst().getGeozone()).isNotNull();

        actualOrganization = organizationRepository.findById(actual.get(1).getOrganization().getId()).orElseThrow();
        assertThat(actualOrganization.getOfficialName()).isEqualTo("Организация 001");
        assertThat(actual.get(1).getName()).isEqualTo("Подразделение 002");
        assertThat(actual.get(1).getLevelName()).isEqualTo("2");
        assertThat(actual.get(1).getLevelCode()).isEqualTo(102);
        assertThat(actual.get(1).getCode()).isEqualTo("202");
        assertThat(actual.get(1).getLocation()).isEqualTo("Местоположение 002");
        assertThat(actual.get(1).getParent().getId()).isEqualTo(actual.getFirst().getId());
        assertThat(actual.get(1).getGeozone()).isNotNull();

        actualOrganization = organizationRepository.findById(actual.get(2).getOrganization().getId()).orElseThrow();
        assertThat(actualOrganization.getOfficialName()).isEqualTo("Организация 001");
        assertThat(actual.get(2).getName()).isEqualTo("Подразделение 003");
        assertThat(actual.get(2).getLevelName()).isEqualTo("3");
        assertThat(actual.get(2).getLevelCode()).isEqualTo(103);
        assertThat(actual.get(2).getCode()).isEqualTo("203");
        assertThat(actual.get(2).getLocation()).isEqualTo("Местоположение 003");
        assertThat(actual.get(2).getParent().getId()).isEqualTo(actual.get(1).getId());
        assert actual.get(2).getHead().getId() != null;
        assertThat(employeeRepository.findById(Objects.requireNonNull(actual.get(2).getHead().getId())).orElseThrow().getPersonnelNumber()).isEqualTo("1");
        assertThat(actual.get(2).getGeozone()).isNotNull();
    }

    @Test
    @DisplayName("Экспорт")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void test_export() throws Exception {
        var organizationParent = createOrganization();

        var departmentParent = createDepartment(organizationParent);

        Position positionParent = createPosition(organizationParent, "Должностьь", "PHRI");

        var employeeParent = new Employee();

        employeeParent.setId(UUID.randomUUID());
        employeeParent.setNew(true);
        employeeParent.setPersonnelNumber("1");
        employeeParent.setLastName("Last");
        employeeParent.setFirstName("First");
        employeeParent.setDepartment(departmentParent);
        employeeParent.setPosition(positionParent);
        employeeParent.setOrganization(departmentParent.getOrganization());
        employeeParent.setHumanReadableId("EHRI");
        employeeParent.setUpdateTime(OffsetDateTime.now());

        employeeRepository.save(employeeParent);

        var count = 100;
        for (var i = 0; i < count; i++) {
            Position position = createPosition(organizationParent, String.format("Должность %03d", i), "PHRI " + i);

            var department = new Department();
            department.setName(String.format("Подразделение %03d", i + 1));
            department.setCode(String.format("Код %03d", i + 1));
            department.setLevelName(String.format("Уровень %03d", i + 1));
            department.setLocation(String.format("Местоположение %03d", i + 1));
            department.setLevelCode(i);
            department.setHumanReadableId(String.format("HRI %03d", i + 1));
            department.setOrganization(organizationParent);
            department.setUpdateTime(OffsetDateTime.now());
            department.setParent(departmentRepository.findAll().getFirst());

            departmentRepository.save(department);

            var employee = new Employee();
            employee.setId(UUID.randomUUID());
            employee.setNew(true);
            employee.setFirstName("First name " + i);
            employee.setLastName("Last name " + i);
            employee.setPatronymic("Patronymic " + i);
            employee.setPersonnelNumber("Personal number " + i);
            employee.setPosition(position);
            employee.setHumanReadableId("EHRI " + i);
            employee.setDepartment(department);
            employee.setOrganization(department.getOrganization());
            employee.setUpdateTime(OffsetDateTime.now());

            employee = employeeRepository.save(employee);

            department.setHead(employee);
        }

        var filter = new FileExportFilterDTO(organizationParent.getId());
        var filterStr = objectMapper.writeValueAsString(filter);
        var encodedFilter = Base64.getEncoder().encodeToString(filterStr.getBytes(StandardCharsets.UTF_8));

        var content = mockMvc.perform(get("/files/department/?filters=" + encodedFilter)
                        .with(jwt().jwt(builder -> builder.jti("e759f5f7-53a9-4727-aef7-1e87b15adb0f")).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
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
            var sheet = excel.getSheet("Подразделения");

            assertThat(sheet).isNotNull();

            // 1 - заголовок, 1 - искусственно добавленное подразделение
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(count + 1 + 1);

            var row = sheet.getRow(0);
            assertThat(row.getPhysicalNumberOfCells()).isEqualTo(8);
            assertThat(row.getCell(0).getStringCellValue()).isEqualTo("Организация");
            assertThat(row.getCell(1).getStringCellValue()).isEqualTo("Подразделение");
            assertThat(row.getCell(2).getStringCellValue()).isEqualTo("Уровень");
            assertThat(row.getCell(3).getStringCellValue()).isEqualTo("Код уровня");
            assertThat(row.getCell(4).getStringCellValue()).isEqualTo("Код");
            assertThat(row.getCell(5).getStringCellValue()).isEqualTo("Местоположение");
            assertThat(row.getCell(6).getStringCellValue()).isEqualTo("Родительское подразделение");
            assertThat(row.getCell(7).getStringCellValue()).isEqualTo("ТН руководителя");

            var expectedList = departmentRepository.findAll();

            // Первая строка - служебные, тестовые данные.
            var rowIndex = 2;
            var index = 1;
            for (; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++, index++) {
                var actualRow = sheet.getRow(rowIndex);
                var expected = expectedList.get(index);
                var organization = organizationRepository.findById(expected.getOrganization().getId()).orElseThrow();

                assertThat(actualRow.getPhysicalNumberOfCells()).isEqualTo(8);
                assertThat(actualRow.getCell(0).getStringCellValue()).isEqualTo(organization.getOfficialName());
                assertThat(actualRow.getCell(1).getStringCellValue()).isEqualTo(expected.getName());
                assertThat(actualRow.getCell(2).getStringCellValue()).isEqualTo(Optional.ofNullable(expected.getLevelName()).orElse(""));
                assertThat(actualRow.getCell(3).getNumericCellValue()).isEqualTo(Optional.ofNullable(expected.getLevelCode()).map(Integer::doubleValue).orElse(0.0));
                assertThat(actualRow.getCell(4).getStringCellValue()).isEqualTo(expected.getCode());
                assertThat(actualRow.getCell(5).getStringCellValue()).isEqualTo(Optional.ofNullable(expected.getLocation()).orElse(""));
                assertThat(actualRow.getCell(6).getStringCellValue()).isEqualTo(expected.getParent().getCode());
            }
        }
    }

    private Organization createOrganization() {
        var organizationParent = new Organization();

        organizationParent.setOfficialName("Организацияя");
        organizationParent.setAddress("Адрес");
        organizationParent.setMsrn("msrn");
        organizationParent.setTid("tid");

        organizationParent = organizationRepository.save(organizationParent);
        return organizationParent;
    }

    private Position createPosition(Organization organizationParent, String positionNae, String hri) {
        var positionParent = new Position();

        positionParent.setName(positionNae);
        positionParent.setOrganization(organizationParent);
        positionParent.setHumanReadableId(hri);

        positionParent = positionRepository.save(positionParent);
        return positionParent;
    }

    private Department createDepartment(Organization organizationParent) {
        var departmentParent = new Department();

        departmentParent.setHumanReadableId("HRI" + new SecureRandom().nextInt());
        departmentParent.setOrganization(organizationParent);
        departmentParent.setName("Подразделениее");
        departmentParent.setCode("Кодд");
        departmentParent.setUpdateTime(OffsetDateTime.now());

        departmentParent = departmentRepository.save(departmentParent);
        return departmentParent;
    }

    private void assertMessages(List<DepartmentMessage> messages, List<Department> actual) {
        assertThat(messages.getFirst().getOrganizationId())
                .isEqualTo(organizationRepository.findByOfficialName("Организация 001").orElseThrow().getId());
        assertThat(messages.getFirst().getDepartmentName()).isEqualTo("Подразделение 001");
        assertThat(messages.getFirst().getCode()).isEqualTo("201");
        assertThat(messages.getFirst().getLocation()).isEqualTo("Местоположение 001");
        assertThat(messages.getFirst().getParentId()).isNull();
        assertThat(messages.getFirst().getDepartmentHeadId()).isNull();

        assertThat(messages.get(1).getOrganizationId())
                .isEqualTo(organizationRepository.findByOfficialName("Организация 001").orElseThrow().getId());
        assertThat(messages.get(1).getDepartmentName()).isEqualTo("Подразделение 002");
        assertThat(messages.get(1).getCode()).isEqualTo("202");
        assertThat(messages.get(1).getLocation()).isEqualTo("Местоположение 002");
        assertThat(messages.get(1).getParentId()).isEqualTo(actual.getFirst().getId());
        assertThat(messages.get(1).getDepartmentHeadId()).isNull();

        assertThat(messages.get(2).getOrganizationId())
                .isEqualTo(organizationRepository.findByOfficialName("Организация 001").orElseThrow().getId());
        assertThat(messages.get(2).getDepartmentName()).isEqualTo("Подразделение 003");
        assertThat(messages.get(2).getCode()).isEqualTo("203");
        assertThat(messages.get(2).getLocation()).isEqualTo("Местоположение 003");
        assertThat(messages.get(2).getParentId()).isEqualTo(actual.get(1).getId());
        assertThat(messages.get(2).getDepartmentHeadId()).isEqualTo(
                employeeRepository.findByPersonnelNumber("1").orElseThrow().getId());
    }

}
