package ru.sberbank.ditsib.corpclient.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Import;
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
import ru.sberbank.ditsib.corpclient.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

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
@SpringBootTest(properties = {"spring.main.lazy-initialization=true", "export.tempDir=target/test_data/files"})
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Импорт/экспорт организаций")
@ActiveProfiles({"test", "import"})
@Import(DatabaseMigration.class)
class OrganizationImportExportTest extends SharedData {

    public static final String USER_ID = "username";
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrganizationRepository organizationRepository;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean(name = "organizationsOutput")
    private OutputBridge organizationsOutput;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private UploadStates uploadStates;

    @MockitoBean
    private DiscoveryClient discoveryClient;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    @Test
    @DisplayName("Импорт")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void test_import() throws Exception {
        var file = new MockMultipartFile("file", "file.xls", "application/vnd.ms-excel",
                getClass().getClassLoader().getResourceAsStream("load/organizations.xls"));

        mockMvc.perform(multipart("/files/organizations/").file(file)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        await().until(() -> uploadStates.getResults("organizations", USER_ID).size(), equalTo(1));
        await().until(() -> uploadStates.getResults("organizations", USER_ID).getFirst().getFinished(), equalTo(true));
        assertThat(uploadStates.getResults("organizations", USER_ID).getFirst().getPages().stream().allMatch(page -> page.getExceptionStrings().isEmpty()))
                .isTrue();
        assertThat(uploadStates.getResults("organizations", USER_ID).getFirst().getPages().stream().map(PageInfo::getRow)
                .flatMap(Collection::stream).allMatch(page -> page.getExceptionStrings().isEmpty()))
                .isTrue();
        var count = 100;
        assertThat(organizationRepository.count()).isEqualTo(count);

        var actualList = organizationRepository.findAll()
                .stream().sorted(Comparator.comparing(Organization::getOfficialName)).toList();

        var messages = getMessages(organizationsOutput, count, OrganizationMessage.class)
                .stream()
                .sorted(Comparator.comparing(OrganizationMessage::getOfficialName))
                .toList();

        for (var i = 0; i < count; i++) {
            var actual = actualList.get(i);
            var actualMessage = messages.get(i);

            assertThat(actual.getOfficialName()).isEqualTo(String.format("Название %03d", i));
            assertThat(actual.getAddress()).isEqualTo(String.format("Адрес %03d", i));

            assertThat(actualMessage.getOfficialName()).isEqualTo(String.format("Название %03d", i));
            assertThat(actualMessage.getAddress()).isEqualTo(String.format("Адрес %03d", i));
        }
    }

    @Test
    @DisplayName("Экспорт")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void test_export() throws Exception {
        var count = 100;
        for (var i = 0; i < count; i++) {
            var organization = new Organization();
            organization.setAddress(String.format("Адрес %03d", i));
            organization.setOfficialName(String.format("Название %03d", i));
            organization.setOrganizationCode(i);
            organization.setMsrn("MSRN");
            organization.setTid("TID");

            organizationRepository.save(organization);
        }

        var content = mockMvc.perform(get("/files/organizations/")
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
            var sheet = excel.getSheet("Организации");

            assertThat(sheet).isNotNull();

            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(count + 1); // 1 - заголовок

            var row = sheet.getRow(0);
            assertThat(row.getCell(0).getStringCellValue()).isEqualTo("Название");
            assertThat(row.getCell(1).getStringCellValue()).isEqualTo("Адрес");

            var expectedList = organizationRepository.findAll();

            for (int rowIndex = 1, index = 0; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++, index++) {
                var actualRow = sheet.getRow(rowIndex);
                var expected = expectedList.get(index);

                assertThat(actualRow.getCell(0).getStringCellValue()).isEqualTo(expected.getOfficialName());
                assertThat(actualRow.getCell(1).getStringCellValue()).isEqualTo(expected.getAddress());
                assertThat((int) actualRow.getCell(7).getNumericCellValue()).isEqualTo(expected.getOrganizationCode());
            }
        }
    }

}
