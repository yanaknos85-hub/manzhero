package ru.sberbank.ditsib.corpclient.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.instancio.Instancio;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.service.CheckUserAccessService;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.files.grpc.exchange.Downloader;
import ru.sber.transport.files.grpc.exchange.Uploader;
import ru.sber.transport.files.grpc.model.FileMeta;
import ru.sber.transport.files.grpc.model.Model;
import ru.sber.transport.files.grpc.service.DeleteServiceGrpc;
import ru.sber.transport.files.grpc.service.DownloadServiceGrpc;
import ru.sber.transport.files.grpc.service.UploadServiceGrpc;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.corpclient.database.dao.*;
import ru.sberbank.ditsib.corpclient.database.model.*;
import ru.sberbank.ditsib.corpclient.dto.NewOrganizationDTO;
import ru.sberbank.ditsib.corpclient.dto.OrganizationDTO;
import ru.sberbank.ditsib.corpclient.dto.OrganizationProjection;
import ru.sberbank.ditsib.corpclient.dto.OrganizationSelectDTO;
import ru.sberbank.ditsib.corpclient.mapper.OrganizationMapper;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;
import ru.sberbank.ditsib.transport.messaging.messages.UserMessage;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("unused")
@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера организаций")
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
class OrganizationControllerTest extends SharedData {

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private OrganizationGroupRepository organizationGroupRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private DSLContext context;

    @MockitoSpyBean
    private CheckUserAccessService checkAccessService;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @MockitoBean(name = "organizationsOutput")
    private OutputBridge organizationsOutput;

    @MockitoBean(name = "usersOutput")
    private OutputBridge usersOutput;

    @MockitoBean
    private Uploader uploader;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private DeleteServiceGrpc.DeleteServiceStub delete;

    @MockitoBean
    private DownloadServiceGrpc.DownloadServiceStub download;

    @MockitoBean
    private UploadServiceGrpc.UploadServiceStub update;

    @MockitoBean
    private Downloader downloader;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    public static final String IMAGES = "target/test/images/logo";

    private static Stream<Arguments> saveLogoSource() {
        return Stream.of(
                Arguments.of("image/bmp"),
                Arguments.of("image/gif"),
                Arguments.of("image/ico"),
                Arguments.of("image/jpg"),
                Arguments.of("image/png"),
                Arguments.of("image/svg"),
                Arguments.of("image/webp"),
                Arguments.of("image/tiff")
        );
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @AfterEach
    public void clear() {
        var root = new File(IMAGES);

        if (root.isDirectory()) {
            Arrays.stream(Objects.requireNonNull(root.listFiles())).forEach(File::delete);

            root.delete();
        }
        departmentRepository.findAll().parallelStream().filter(d -> d.getHead() != null)
                .peek(d -> d.setHead(null))
                .forEach(departmentRepository::save);
        employeeRepository.deleteAll();
        positionRepository.deleteAll();
        departmentRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    @DisplayName("Добавление организации")
    void test_addOrganization() throws Exception {
        doNothing().when(checkAccessService).check();
        var site = new Contact();
        site.setContactType(ContactType.SITE);
        site.setValue("www.site.com");
        site = contactRepository.save(site);

        var email = new Contact();
        email.setContactType(ContactType.EMAIL);
        email.setValue("site@company.com");
        email = contactRepository.save(email);

        var phone = new Contact();
        phone.setContactType(ContactType.PHONE);
        phone.setValue("+70000000000");
        phone = contactRepository.save(phone);

        var contacts = new ArrayList<Contact>();
        contacts.add(site);
        contacts.add(email);
        contacts.add(phone);

        var newOrganization = Instancio.of(Organization.class)
                .ignore(field(Organization::getOrganizationGroup))
                .ignore(field(Organization::getDepartments))
                .ignore(field(Organization::getEmployees))
                .ignore(field(Organization::getTripPurposes))
                .ignore(field(Organization::getPositions))
                .ignore(field(Organization::getCargoTypes))
                .set(field(Organization::getContacts), contacts)
                .create();
        newOrganization.setMsrn("MSRN");
        newOrganization.setTid("012345678912");
        newOrganization.setContacts(contacts);
        newOrganization.setOrganizationCode(1);
        var request = objectMapper.writeValueAsString(organizationMapper.organizationToNewDTO(newOrganization));

        var response =
                mockMvc.perform(
                                post(OrganizationController.API_MAPPING)
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                        .content(request))
                        .andExpect(status().isOk());

        var expected = organizationRepository.findAll().getFirst();

        var actualMessage = getMessages(organizationsOutput, OrganizationMessage.class, Map.of());

        response
                .andExpect(jsonPath("$.id").value(expected.getId().toString()))
                .andExpect(jsonPath("$.id").value(actualMessage.getId().toString()))
                .andExpect(jsonPath("$.msrn").value(expected.getMsrn()))
                .andExpect(jsonPath("$.msrn").value(actualMessage.getMsrn()))
                .andExpect(jsonPath("$.tid").value(expected.getTid()))
                .andExpect(jsonPath("$.tid").value(actualMessage.getTid()))
                .andExpect(jsonPath("$.organizationCode").value(expected.getOrganizationCode()))
                .andExpect(jsonPath("$.organizationCode").value(actualMessage.getOrganizationCode()))
                .andExpect(jsonPath("$.contacts.length()").value(contacts.size()))
                .andExpect(jsonPath("$.contacts.length()").value(actualMessage.getContacts().size()))
                .andExpect(jsonPath("$.contacts[0].type").value(contacts.get(0).getContactType().name()))
                .andExpect(jsonPath("$.contacts[0].type").value(actualMessage.getContacts().get(0).getType()))
                .andExpect(jsonPath("$.contacts[0].value").value(contacts.get(0).getValue()))
                .andExpect(jsonPath("$.contacts[0].value").value(actualMessage.getContacts().get(0).getValue()))
                .andExpect(jsonPath("$.contacts[1].type").value(contacts.get(1).getContactType().name()))
                .andExpect(jsonPath("$.contacts[1].type").value(actualMessage.getContacts().get(1).getType()))
                .andExpect(jsonPath("$.contacts[1].value").value(contacts.get(1).getValue()))
                .andExpect(jsonPath("$.contacts[1].value").value(actualMessage.getContacts().get(1).getValue()))
                .andExpect(jsonPath("$.contacts[2].type").value(contacts.get(2).getContactType().name()))
                .andExpect(jsonPath("$.contacts[2].type").value(actualMessage.getContacts().get(2).getType()))
                .andExpect(jsonPath("$.contacts[2].value").value(contacts.get(2).getValue()))
                .andExpect(jsonPath("$.contacts[2].value").value(actualMessage.getContacts().get(2).getValue()))
                .andExpect(jsonPath("$.officialName").value(expected.getOfficialName()))
                .andExpect(jsonPath("$.officialName").value(actualMessage.getOfficialName()))
                .andExpect(jsonPath("$.address").value(expected.getAddress()))
                .andExpect(jsonPath("$.address").value(actualMessage.getAddress()))
                .andExpect(jsonPath("$.digitId").value(expected.getDigitId()))
                .andExpect(jsonPath("$.digitId").value(actualMessage.getDigitId()));
    }

    @Test
    @DisplayName("Добавление организации. Неверный сайт")
    void test_organization_wrongSite() throws Exception {
        var site = new Contact();
        site.setContactType(ContactType.SITE);
        site.setValue("www$#@.site.com");

        var email = new Contact();
        email.setContactType(ContactType.EMAIL);
        email.setValue("site@company.com");

        var phone = new Contact();
        phone.setContactType(ContactType.PHONE);
        phone.setValue("+70000000000");

        var contacts = new ArrayList<Contact>();
        contacts.add(site);
        contacts.add(email);
        contacts.add(phone);

        var newOrganization = Instancio.create(Organization.class);
        newOrganization.setMsrn("MSRN");
        newOrganization.setTid("012345678912");
        newOrganization.setContacts(contacts);
        var request = objectMapper.writeValueAsString(organizationMapper.organizationToNewDTO(newOrganization));

        var result = mockMvc.perform(
                        post(OrganizationController.API_MAPPING)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems.length()").value(1))
                .andExpect(jsonPath("$.problems[0].field").value("contacts[0].value"))
                .andExpect(jsonPath("$.problems[0].value").value("www$#@.site.com"))
                .andExpect(jsonPath("$.problems[0].constraints.length()").value(1))
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("Pattern"));
        assertThat(result.andReturn().getResponse()).isNotNull();
    }

    @Test
    @DisplayName("Добавление организации. Неверный e-mail")
    void test_organization_wrongEMail() throws Exception {
        var site = new Contact();
        site.setContactType(ContactType.SITE);
        site.setValue("www.site.com");

        var email = new Contact();
        email.setContactType(ContactType.EMAIL);
        email.setValue("s?:%;№№;%;№i#$%te@company.com");

        var phone = new Contact();
        phone.setContactType(ContactType.PHONE);
        phone.setValue("+70000000000");

        var contacts = new ArrayList<Contact>();
        contacts.add(site);
        contacts.add(email);
        contacts.add(phone);

        var newOrganization = Instancio.create(Organization.class);
        newOrganization.setMsrn("MSRN");
        newOrganization.setTid("012345678912");
        newOrganization.setContacts(contacts);
        var request = objectMapper.writeValueAsString(organizationMapper.organizationToNewDTO(newOrganization));

        var result = mockMvc.perform(
                        post(OrganizationController.API_MAPPING)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems.length()").value(1))
                .andExpect(jsonPath("$.problems[0].field").value("contacts[1].value"))
                .andExpect(jsonPath("$.problems[0].value").value("s?:%;№№;%;№i#$%te@company.com"))
                .andExpect(jsonPath("$.problems[0].constraints.length()").value(1))
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("Pattern"));
        assertThat(result.andReturn().getResponse()).isNotNull();
    }

    @Test
    @DisplayName("Добавление организации. Неверный телефон")
    void test_organization_phone() throws Exception {
        var site = new Contact();
        site.setContactType(ContactType.SITE);
        site.setValue("www.site.com");

        var email = new Contact();
        email.setContactType(ContactType.EMAIL);
        email.setValue("site@company.com");

        var phone = new Contact();
        phone.setContactType(ContactType.PHONE);
        phone.setValue("+70000000000fasdfdsafdsaf");

        var contacts = new ArrayList<Contact>();
        contacts.add(site);
        contacts.add(email);
        contacts.add(phone);

        var newOrganization = Instancio.create(Organization.class);
        newOrganization.setMsrn("MSRN");
        newOrganization.setTid("012345678912");
        newOrganization.setContacts(contacts);
        var request = objectMapper.writeValueAsString(organizationMapper.organizationToNewDTO(newOrganization));

        var result = mockMvc.perform(
                        post(OrganizationController.API_MAPPING)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems.length()").value(1))
                .andExpect(jsonPath("$.problems[0].field").value("contacts[2].value"))
                .andExpect(jsonPath("$.problems[0].value").value("+70000000000fasdfdsafdsaf"))
                .andExpect(jsonPath("$.problems[0].constraints.length()").value(1))
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("Pattern"));
        assertThat(result.andReturn().getResponse()).isNotNull();
    }

    @Test
    @DisplayName("Добавление организации с повторяющимся данными")
    void test_addOrganizationDuplicateData() throws Exception {
        doNothing().when(checkAccessService).check();
        var testOrganization1 = generateOrganization();
        testOrganization1 = organizationRepository.save(testOrganization1);
        assertNotNull(organizationRepository.findById(testOrganization1.getId()).orElse(null));
        NewOrganizationDTO duplicate = organizationMapper.organizationToNewDTO(testOrganization1);
        var request = objectMapper.writeValueAsString(duplicate);

        mockMvc.perform(
                        post(OrganizationController.API_MAPPING)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.entity.name").value("Organization"))
                .andExpect(jsonPath("$.problems.length()").value(1))
                .andExpect(jsonPath("$.problems[0].field").value("officialName"))
                .andExpect(jsonPath("$.problems[0].value").value(testOrganization1.getOfficialName()))
                .andExpect(jsonPath("$.problems[0].constraints").isEmpty())
                .andExpect(jsonPath("$.message").value(Matchers.startsWith("Conflict data on entity Organization")));
    }

    @Test
    @DisplayName("Обновление организации")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void test_updateOrganization() throws Exception {
        var testOrganization1 = Instancio.of(Organization.class)
                .ignore(field(Organization::getOrganizationGroup))
                .ignore(field(Organization::getDepartments))
                .ignore(field(Organization::getId))
                .ignore(field(Organization::getEmployees))
                .ignore(field(Organization::getTripPurposes))
                .ignore(field(Organization::getPositions))
                .ignore(field(Organization::getCargoTypes))
                .ignore(field(Organization::getContacts))
                .create();

        var toUpdate = organizationRepository.save(testOrganization1);
        testOrganization1 = toUpdate;

        var site2 = new Contact();
        site2.setContactType(ContactType.SITE);
        site2.setValue("www.site2.com");

        var email2 = new Contact();
        email2.setContactType(ContactType.EMAIL);
        email2.setValue("site2@company.com");

        var phone2 = new Contact();
        phone2.setContactType(ContactType.PHONE);
        phone2.setValue("+70000000002");

        var contacts2 = new ArrayList<Contact>();
        contacts2.add(site2);
        contacts2.add(email2);
        contacts2.add(phone2);

        toUpdate.setAddress(SharedData.ORGANIZATION_ADDRESS2);
        toUpdate.setOfficialName(SharedData.ORGANIZATION_OFFICIAL_NAME2);
        toUpdate.setDepartments(null);
        toUpdate.setPositions(null);
        toUpdate.setMsrn("MSRN 2");
        toUpdate.setTid("012345678912");
        toUpdate.setContacts(contacts2);
        toUpdate.setOrganizationCode(2);

        var testDepartment1 = Instancio.of(Department.class)
                .set(field(Department::getOrganization), testOrganization1)
                .ignore(field(Department::getHead))
                .ignore(field(Department::getChildren))
                .ignore(field(Department::getEmployees))
                .ignore(field(Department::getParent))
                .ignore(field(Department::getId))
                .create();
        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        var testPosition1 = Instancio.of(Position.class)
                .set(field(Position::getOrganization), testOrganization1)
                .ignore(field(Position::getEmployees))
                .ignore(field(Position::getId))
                .create();
        testPosition1.setHumanReadableId("hti1");
        testPosition1.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);

        var employee = Instancio.of(Employee.class)
                .set(field(Employee::getDepartment), testDepartment1)
                .set(field(Employee::getPosition), testPosition1)
                .set(field(Employee::getOrganization), testOrganization1)
                .set(field(Employee::getUserId), UUID.fromString(USER1_ID))
                .ignore(field(Employee::getDelegatedBy))
                .ignore(field(Employee::getDelegateRecords))
                .ignore(field(Employee::getAttributes))
                .ignore(field(Employee::getAvailableTransportTypes))
                .ignore(field(Employee::getPersonalCars))
                .ignore(field(Employee::getSupervisorOf))
                .create();
        employeeRepository.save(employee);

        mockMvc.perform(put(OrganizationController.API_MAPPING + toUpdate.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content("""
                                {
                                    "officialName": "%s",
                                    "address": "%s",
                                    "msrn": "%s",
                                    "tid": "%s",
                                    "organizationCode": %s,
                                    "contacts": [
                                        {
                                            "type": "PHONE",
                                            "value": "+79000000000"
                                        }
                                    ]
                                }""".formatted(SharedData.ORGANIZATION_OFFICIAL_NAME2, SharedData.ORGANIZATION_ADDRESS2, "MSRN 2", "012345678912", 2
                        )))
                .andExpect(status().isOk()).andReturn();
        var actualMessage = getMessages(organizationsOutput, OrganizationMessage.class);

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            var expected = organizationRepository.findAll().getFirst();

            assertEquals(expected.getId(), actualMessage.getId());
            assertEquals(SharedData.ORGANIZATION_ADDRESS2, actualMessage.getAddress());
            assertEquals(SharedData.ORGANIZATION_OFFICIAL_NAME2, actualMessage.getOfficialName());
            assertEquals(2, actualMessage.getOrganizationCode());
            assertThat(expected.getContacts()).hasSize(1);
            assertThat(expected.getContacts().getFirst().getContactType()).isEqualTo(ContactType.PHONE);
            assertThat(expected.getContacts().getFirst().getValue()).isEqualTo("+79000000000");
        });
    }

    @Test
    @DisplayName("Обновление организации повторяющимися данными")
    void test_updateOrganizationDuplicateData() throws Exception {
        var testOrganization1 = Instancio.of(Organization.class)
                .ignore(field(Organization::getOrganizationGroup))
                .ignore(field(Organization::getDepartments))
                .ignore(field(Organization::getEmployees))
                .ignore(field(Organization::getTripPurposes))
                .ignore(field(Organization::getPositions))
                .ignore(field(Organization::getCargoTypes))
                .ignore(field(Organization::getContacts))
                .ignore(field(Organization::getId))
                .create();
        var testOrganization2 = Instancio.of(Organization.class)
                .ignore(field(Organization::getOrganizationGroup))
                .ignore(field(Organization::getDepartments))
                .ignore(field(Organization::getEmployees))
                .ignore(field(Organization::getTripPurposes))
                .ignore(field(Organization::getPositions))
                .ignore(field(Organization::getCargoTypes))
                .ignore(field(Organization::getContacts))
                .ignore(field(Organization::getContacts))
                .ignore(field(Organization::getId))
                .create();
        testOrganization2 = organizationRepository.save(testOrganization2);

        var testPosition2 = Instancio.of(Position.class)
                .set(field(Position::getOrganization), testOrganization2)
                .ignore(field(Position::getEmployees))
                .ignore(field(Position::getId))
                .create();
        testPosition2.setHumanReadableId("hti1");
        testPosition2.setOrganization(testOrganization2);
        testPosition2 = positionRepository.save(testPosition2);

        var testDepartment2 = Instancio.of(Department.class)
                .set(field(Department::getOrganization), testOrganization2)
                .ignore(field(Department::getHead))
                .ignore(field(Department::getChildren))
                .ignore(field(Department::getEmployees))
                .ignore(field(Department::getParent))
                .ignore(field(Department::getId))
                .create();
        testDepartment2.setOrganization(testOrganization2);
        testDepartment2 = departmentRepository.save(testDepartment2);

        var testEmployee2 = Instancio.of(Employee.class)
                .set(field(Employee::getOrganization), testOrganization2)
                .set(field(Employee::getDepartment), testDepartment2)
                .set(field(Employee::getPosition), testPosition2)
                .set(field(Employee::getUserId), UUID.fromString(USER2_ID))
                .ignore(field(Employee::getAttributes))
                .ignore(field(Employee::getAvailableTransportTypes))
                .ignore(field(Employee::getSupervisorOf))
                .ignore(field(Employee::getSupervisor))
                .ignore(field(Employee::getDelegatedBy))
                .ignore(field(Employee::getDelegateRecords))
                .ignore(field(Employee::getPersonalCars))
                .ignore(field(Employee::getSlaves))
                .create();
        employeeRepository.save(testEmployee2);

        organizationRepository.save(testOrganization1);
        organizationRepository.save(testOrganization2);

        OrganizationDTO duplicate = organizationMapper.toDto(testOrganization2);
        duplicate.setTid("1234512345");
        duplicate.setOfficialName(testOrganization1.getOfficialName());
        var result = mockMvc.perform(put(OrganizationController.API_MAPPING + testOrganization2.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER2_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.problems.length()").value(1))
                .andExpect(jsonPath("$.problems[0].field").value("officialName"))
                .andExpect(jsonPath("$.problems[0].value").value(testOrganization1.getOfficialName()))
                .andExpect(jsonPath("$.problems[0].constraints").isEmpty())
                .andExpect(jsonPath("$.message").value(Matchers.startsWith("Conflict data on entity ru.sberbank.ditsib.corpclient.database.model.Organization")));

        assertThat(result.andReturn().getResponse()).isNotNull();
    }

    @Test
    @DisplayName("Удаление организации")
    void test_deleteOrganization() throws Exception {
        var testOrganization1 = Instancio.of(Organization.class)
                .ignore(field(Organization::getOrganizationGroup))
                .ignore(field(Organization::getDepartments))
                .ignore(field(Organization::getEmployees))
                .ignore(field(Organization::getTripPurposes))
                .ignore(field(Organization::getPositions))
                .ignore(field(Organization::getCargoTypes))
                .ignore(field(Organization::getContacts))
                .ignore(field(Organization::getId))
                .create();
        testOrganization1 = organizationRepository.save(testOrganization1);

        var testPosition1 = Instancio.of(Position.class)
                .set(field(Position::getOrganization), testOrganization1)
                .ignore(field(Position::getEmployees))
                .ignore(field(Position::getId))
                .create();
        testPosition1.setHumanReadableId("hti1");
        testPosition1.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);

        var testDepartment1 = Instancio.of(Department.class)
                .set(field(Department::getOrganization), testOrganization1)
                .ignore(field(Department::getEmployees))
                .ignore(field(Department::getHead))
                .ignore(field(Department::getId))
                .create();
        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        var testEmployee1 = Instancio.of(Employee.class)
                .set(field(Employee::getOrganization), testOrganization1)
                .set(field(Employee::getDepartment), testDepartment1)
                .set(field(Employee::getPosition), testPosition1)
                .set(field(Employee::getUserId), UUID.fromString(USER1_ID))
                .set(field(Employee::getOrgStructureType), OrgStructureType.INTERNAL)
                .ignore(field(Employee::getDelegateRecords))
                .ignore(field(Employee::getSupervisor))
                .ignore(field(Employee::getSupervisorOf))
                .ignore(field(Employee::getDelegatedBy))
                .ignore(field(Employee::getAttributes))
                .ignore(field(Employee::getAvailableTransportTypes))
                .ignore(field(Employee::getPersonalCars))
                .create();
        testEmployee1 = employeeRepository.save(testEmployee1);
        assertFalse(organizationRepository.findAll().isEmpty());

        mockMvc.perform(delete(OrganizationController.API_MAPPING + testDepartment1.getOrganization().getId().toString())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk()).andReturn();
        var resultList = employeeRepository.findAllByOrganizationId(testEmployee1.getOrganization().getId());
        assertEquals(1, resultList.size());

        var actualMessage = getMessages(usersOutput, UserMessage.class, Map.of("type", testEmployee1.getOrgStructureType().name()));

        assertEquals(testEmployee1.getUserId(), actualMessage.getId());
    }

    @Test
    @DisplayName("Получение  существующей организации")
    void test_getOrganization() throws Exception {
        var testOrganization1 = Instancio.of(Organization.class)
                .ignore(field(Organization::getOrganizationGroup))
                .ignore(field(Organization::getDepartments))
                .ignore(field(Organization::getEmployees))
                .ignore(field(Organization::getTripPurposes))
                .ignore(field(Organization::getPositions))
                .ignore(field(Organization::getCargoTypes))
                .ignore(field(Organization::getId))
                .ignore(field(Organization::getContacts))
                .create();
        testOrganization1 = organizationRepository.save(testOrganization1);

        var testPosition1 = Instancio.of(Position.class)
                .set(field(Position::getOrganization), testOrganization1)
                .ignore(field(Position::getEmployees))
                .ignore(field(Position::getAvailableClasses))
                .ignore(field(Position::getId))
                .create();
        testPosition1.setHumanReadableId("hti1");
        testPosition1.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);

        var testDepartment1 = Instancio.of(Department.class)
                .set(field(Department::getOrganization), testOrganization1)
                .ignore(field(Department::getHead))
                .ignore(field(Department::getEmployees))
                .ignore(field(Department::getChildren))
                .ignore(field(Department::getId))
                .create();
        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);

        var testEmployee1 = Instancio.of(Employee.class)
                .set(field(Employee::getOrganization), testOrganization1)
                .set(field(Employee::getDepartment), testDepartment1)
                .set(field(Employee::getPosition), testPosition1)
                .set(field(Employee::getUserId), UUID.fromString(USER1_ID))
                .ignore(field(Employee::getAttributes))
                .ignore(field(Employee::getAvailableTransportTypes))
                .ignore(field(Employee::getSupervisorOf))
                .ignore(field(Employee::getSupervisor))
                .ignore(field(Employee::getDelegatedBy))
                .ignore(field(Employee::getDelegateRecords))
                .ignore(field(Employee::getPersonalCars))
                .ignore(field(Employee::getSlaves))
                .create();
        employeeRepository.save(testEmployee1);
        assertTrue(organizationRepository.findById(testOrganization1.getId()).isPresent());

        var saved = mockMvc.perform(
                        get(OrganizationController.API_MAPPING
                                + testOrganization1.getId().toString())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk()).andReturn();
        assertNotNull(saved);
        OrganizationDTO actual = objectMapper.readValue(saved.getResponse().getContentAsString(StandardCharsets.UTF_8),
                OrganizationDTO.class);


        assertEquals(testOrganization1.getId(), actual.getId());
        assertEquals(testOrganization1.getOfficialName(), actual.getOfficialName());
        assertEquals(testOrganization1.getAddress(), actual.getAddress());
        assertEquals(testOrganization1.getOrganizationCode(), actual.getOrganizationCode());
    }

    @Test
    @DisplayName("Получение списка организаций")
    void test_getOrganizationList() throws Exception {
        var testOrganization1 = generateOrganization();
        testOrganization1 = organizationRepository.save(testOrganization1);
        var testOrganization2 = generateOrganization();
        testOrganization2 = organizationRepository.save(testOrganization2);
        var testDepartment1 = generateDepartment();
        var testDepartment2 = generateDepartment();
        testDepartment1.setOrganization(testOrganization1);
        testDepartment2.setOrganization(testOrganization2);
        testDepartment1 = departmentRepository.save(testDepartment1);
        testDepartment2 = departmentRepository.save(testDepartment2);
        var testPosition1 = Instancio.of(Position.class)
                .ignore(field(Position::getEmployees))
                .ignore(field(Position::getAvailableClasses))
                .ignore(field(Position::getId))
                .create();
        var testPosition2 = Instancio.of(Position.class)
                .ignore(field(Position::getEmployees))
                .ignore(field(Position::getAvailableClasses))
                .ignore(field(Position::getId))
                .create();
        testPosition1.setOrganization(testOrganization1);
        testPosition2.setOrganization(testOrganization2);
        testPosition1 = positionRepository.save(testPosition1);
        testPosition2 = positionRepository.save(testPosition2);
        var testEmployee1 = Instancio.of(Employee.class)
                .set(field(Employee::getDepartment), testDepartment1)
                .set(field(Employee::getPosition), testPosition1)
                .set(field(Employee::getOrganization), testOrganization1)
                .set(field(Employee::getUserId), UUID.fromString(USER1_ID))
                .ignore(field(Employee::getDelegatedBy))
                .ignore(field(Employee::getDelegateRecords))
                .ignore(field(Employee::getAttributes))
                .ignore(field(Employee::getAvailableTransportTypes))
                .ignore(field(Employee::getSupervisorOf))
                .ignore(field(Employee::getPersonalCars))
                .create();
        var testEmployee2 = Instancio.of(Employee.class)
                .set(field(Employee::getDepartment), testDepartment2)
                .set(field(Employee::getPosition), testPosition2)
                .set(field(Employee::getOrganization), testOrganization2)
                .ignore(field(Employee::getDelegatedBy))
                .ignore(field(Employee::getDelegateRecords))
                .ignore(field(Employee::getAttributes))
                .ignore(field(Employee::getAvailableTransportTypes))
                .ignore(field(Employee::getPersonalCars))
                .ignore(field(Employee::getSupervisorOf))
                .ignore(field(Employee::getPersonalCars))
                .create();
        testEmployee1 = employeeRepository.save(testEmployee1);
        testEmployee2.setSupervisor(testEmployee1);
        testEmployee2.setOrganization(testDepartment2.getOrganization());
        employeeRepository.save(testEmployee2);
        var organizationGroup = new OrganizationGroup();
        organizationGroup.setName("Name");
        organizationGroup.setInternal(true);
        organizationGroup = organizationGroupRepository.save(organizationGroup);
        testOrganization1.setOrganizationGroup(organizationGroup);
        testOrganization2.setOrganizationGroup(organizationGroup);
        organizationRepository.save(testOrganization1);
        organizationRepository.save(testOrganization2);

        mockMvc.perform(
                        get(OrganizationController.API_MAPPING)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .param("groupId", organizationGroup.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        var result = mockMvc.perform(
                        get(OrganizationController.API_MAPPING)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .param("groupId", organizationGroup.getId().toString())
                                .param("projection", OrganizationProjection.SELECT.name()))
                .andExpect(status().isOk())
                .andReturn();
        var responseList = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<OrganizationSelectDTO>>() {
        });
        assertFalse(responseList.isEmpty());
    }

    @Test
    @DisplayName("Получение списка сотрудников организации")
    void test_getEmployees() throws Exception {
        var testOrganization1 = generateOrganization();
        testOrganization1 = organizationRepository.save(testOrganization1);
        var testDepartment1 = generateDepartment();
        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);
        var testPosition1 = Instancio.of(Position.class)
                .ignore(field(Position::getEmployees))
                .ignore(field(Position::getAvailableClasses))
                .ignore(field(Position::getId))
                .set(field(Position::getHumanReadableId), "PS-001-1")
                .set(field(Position::getOrganization), testOrganization1)
                .create();
        var testPosition2 = Instancio.of(Position.class)
                .ignore(field(Position::getEmployees))
                .ignore(field(Position::getAvailableClasses))
                .ignore(field(Position::getId))
                .set(field(Position::getHumanReadableId), "PS-001-2")
                .set(field(Position::getOrganization), testOrganization1)
                .create();
        testPosition1 = positionRepository.save(testPosition1);
        testPosition2 = positionRepository.save(testPosition2);
        var testEmployee1 = Instancio.of(Employee.class)
                .set(field(Employee::getDepartment), testDepartment1)
                .set(field(Employee::getPosition), testPosition1)
                .set(field(Employee::getOrganization), testOrganization1)
                .set(field(Employee::getUserId), UUID.fromString(USER1_ID))
                .ignore(field(Employee::getDelegatedBy))
                .ignore(field(Employee::getDelegateRecords))
                .ignore(field(Employee::getAttributes))
                .ignore(field(Employee::getAvailableTransportTypes))
                .ignore(field(Employee::getSupervisorOf))
                .ignore(field(Employee::getPersonalCars))
                .create();
        var testEmployee2 = Instancio.of(Employee.class)
                .set(field(Employee::getDepartment), testDepartment1)
                .set(field(Employee::getPosition), testPosition2)
                .set(field(Employee::getOrganization), testOrganization1)
                .set(field(Employee::getUserId), UUID.fromString(USER2_ID))
                .ignore(field(Employee::getDelegatedBy))
                .ignore(field(Employee::getDelegateRecords))
                .ignore(field(Employee::getAttributes))
                .ignore(field(Employee::getAvailableTransportTypes))
                .ignore(field(Employee::getSupervisorOf))
                .ignore(field(Employee::getPersonalCars))
                .create();
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        testEmployee2.setOrganization(testDepartment1.getOrganization());
        testEmployee1.setPosition(testPosition1);
        testEmployee2.setPosition(testPosition2);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee2.setDepartment(testDepartment1);
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);

        String mapping = OrganizationController.getEmployeeApiMappingByOrgId(
                testOrganization1.getId());
        mockMvc.perform(get(mapping)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("Получение списка сотрудников организации по подстроке ФИО")
    void test_getEmployeesBYFIO() throws Exception {
        var testOrganization1 = generateOrganization();
        testOrganization1 = organizationRepository.save(testOrganization1);
        var testDepartment1 = generateDepartment();
        testDepartment1.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);
        var testPosition1 = Instancio.of(Position.class)
                .ignore(field(Position::getEmployees))
                .ignore(field(Position::getAvailableClasses))
                .ignore(field(Position::getId))
                .set(field(Position::getHumanReadableId), "PS-001-1")
                .set(field(Position::getOrganization), testOrganization1)
                .create();
        var testPosition2 = Instancio.of(Position.class)
                .ignore(field(Position::getEmployees))
                .ignore(field(Position::getAvailableClasses))
                .ignore(field(Position::getId))
                .set(field(Position::getHumanReadableId), "PS-001-2")
                .set(field(Position::getOrganization), testOrganization1)
                .create();
        testPosition1 = positionRepository.save(testPosition1);
        testPosition2 = positionRepository.save(testPosition2);
        var testEmployee1 = Instancio.of(Employee.class)
                .set(field(Employee::getDepartment), testDepartment1)
                .set(field(Employee::getPosition), testPosition1)
                .set(field(Employee::getOrganization), testOrganization1)
                .set(field(Employee::getUserId), UUID.fromString(USER1_ID))
                .ignore(field(Employee::getDelegatedBy))
                .ignore(field(Employee::getDelegateRecords))
                .ignore(field(Employee::getAttributes))
                .ignore(field(Employee::getAvailableTransportTypes))
                .ignore(field(Employee::getSupervisorOf))
                .ignore(field(Employee::getPersonalCars))
                .create();
        var testEmployee2 = Instancio.of(Employee.class)
                .set(field(Employee::getDepartment), testDepartment1)
                .set(field(Employee::getPosition), testPosition2)
                .set(field(Employee::getOrganization), testOrganization1)
                .set(field(Employee::getUserId), UUID.fromString(USER2_ID))
                .ignore(field(Employee::getDelegatedBy))
                .ignore(field(Employee::getDelegateRecords))
                .ignore(field(Employee::getAttributes))
                .ignore(field(Employee::getAvailableTransportTypes))
                .ignore(field(Employee::getSupervisorOf))
                .ignore(field(Employee::getPersonalCars))
                .create();
        employeeRepository.save(testEmployee1);
        testEmployee2.setDepartment(testDepartment1);
        testEmployee2.setOrganization(testDepartment1.getOrganization());
        employeeRepository.save(testEmployee2);

        String searchSubstring = testEmployee1.getLastName() + " " + testEmployee1.getFirstName() +
                (testEmployee1.getPatronymic() == null ? "" :
                        " " + testEmployee1.getPatronymic());
        searchSubstring = searchSubstring.substring(1, searchSubstring.length() - 2);

        var result = mockMvc.perform(
                        get(OrganizationController.getEmployeeApiMappingByOrgId(testOrganization1.getId()) + "search?fio=" +
                                searchSubstring)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(Objects.requireNonNull(testEmployee1.getId()).toString()))
                .andExpect(jsonPath("$.content[0].userId").value(testEmployee1.getUserId().toString()))
                .andExpect(jsonPath("$.content[0].firstName").value(testEmployee1.getFirstName()))
                .andExpect(jsonPath("$.content[0].lastName").value(testEmployee1.getLastName()))
                .andExpect(jsonPath("$.content[0].patronymic").value(testEmployee1.getPatronymic()))
                .andExpect(jsonPath("$.content[0].personnelNumber").value(testEmployee1.getPersonnelNumber()));
        assertThat(result.andReturn().getResponse()).isNotNull();
    }

    @Test
    @DisplayName("Получение списка сотрудников по подстроке ФИО независимо от организации")
    void test_getAllEmployeesBYFIO() throws Exception {
        var testOrganization1 = generateOrganization();
        var testOrganization2 = Instancio.of(Organization.class)
                .ignore(field(Organization::getOrganizationGroup))
                .ignore(field(Organization::getDepartments))
                .ignore(field(Organization::getEmployees))
                .ignore(field(Organization::getTripPurposes))
                .ignore(field(Organization::getContacts))
                .ignore(field(Organization::getPositions))
                .ignore(field(Organization::getCargoTypes))
                .ignore(field(Organization::getContacts))
                .ignore(field(Organization::getId))
                .create();
        testOrganization1 = organizationRepository.save(testOrganization1);
        organizationRepository.save(testOrganization2);
        var testDepartment1 = Instancio.of(Department.class)
                .ignore(field(Department::getHead))
                .ignore(field(Department::getEmployees))
                .ignore(field(Department::getId))
                .create();
        var testDepartment2 = Instancio.of(Department.class)
                .ignore(field(Department::getHead))
                .ignore(field(Department::getEmployees))
                .ignore(field(Department::getId))
                .create();
        testDepartment1.setOrganization(testOrganization1);
        testDepartment2.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);
        testDepartment2 = departmentRepository.save(testDepartment2);
        var testPosition1 = Instancio.of(Position.class)
                .ignore(field(Position::getEmployees))
                .ignore(field(Position::getId))
                .create();
        var testPosition2 = Instancio.of(Position.class)
                .ignore(field(Position::getEmployees))
                .ignore(field(Position::getId))
                .create();
        testPosition1.setHumanReadableId("PS-001-1");
        testPosition2.setHumanReadableId("PS-001-2");
        testPosition1.setOrganization(testOrganization1);
        testPosition2.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);
        testPosition2 = positionRepository.save(testPosition2);

        var testEmployee1 = Instancio.of(Employee.class)
                .set(field(Employee::getOrganization), testOrganization1)
                .set(field(Employee::getDepartment), testDepartment1)
                .set(field(Employee::getPosition), testPosition1)
                .ignore(field(Employee::getPersonalCars))
                .ignore(field(Employee::getSupervisorOf))
                .ignore(field(Employee::getSupervisor))
                .ignore(field(Employee::getDelegatedBy))
                .ignore(field(Employee::getDelegateRecords))
                .ignore(field(Employee::getAttributes))
                .ignore(field(Employee::getAvailableTransportTypes))
                .set(field(Employee::getPosition), testPosition1)
                .set(field(Employee::getDepartment), testDepartment1)
                .set(field(Employee::getUserId), UUID.fromString(USER1_ID))
                .create();
        var testEmployee2 = Instancio.of(Employee.class)
                .ignore(field(Employee::getPersonalCars))
                .ignore(field(Employee::getSupervisorOf))
                .ignore(field(Employee::getSupervisor))
                .ignore(field(Employee::getDelegatedBy))
                .ignore(field(Employee::getDelegateRecords))
                .ignore(field(Employee::getAttributes))
                .ignore(field(Employee::getAvailableTransportTypes))
                .set(field(Employee::getPosition), testPosition2)
                .set(field(Employee::getUserId), UUID.fromString(USER2_ID))
                .set(field(Employee::getDepartment), testDepartment2)
                .create();
        employeeRepository.save(testEmployee1);

        testEmployee2.setDepartment(testDepartment2);
        testEmployee2.setLastName(testEmployee1.getLastName());
        testEmployee2.setOrganization(testDepartment2.getOrganization());
        employeeRepository.save(testEmployee2);

        mockMvc.perform(
                        get(OrganizationController.getEmployeeApiMappingByOrgId(testOrganization1.getId()) + "search?fio=" +
                                testEmployee2.getLastName())
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
        ;
    }

    @ParameterizedTest
    @DisplayName("Сохранение логотипа")
    @MethodSource("saveLogoSource")
    void should_saveLogo(String extension) throws Exception {
        var organization = new Organization();
        organization.setOfficialName("Name");
        organization.setAddress("Address");
        organization.setMsrn("msrn");
        organization.setTid("tid");

        organization = organizationRepository.save(organization);

        var resource = Paths.get("src/test/resources/testLogo/logo." + extension.split("/")[1]);
        assertThat(resource).isNotNull();
        var fileName = resource.toFile();
        assertThat(fileName).isNotNull();

        var contentStream = new FileInputStream(fileName);
        var file = new MockMultipartFile("logo", "logo." + extension.split("/")[1], extension,
                contentStream);

        long size = Files.size(resource);
        mockMvc.perform(multipart("/logo/" + organization.getId()).file(file)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .header(HttpHeaders.CONTENT_LENGTH, size))
                .andExpect(status().isOk());

        var requestCaptor = ArgumentCaptor.forClass(InputStream.class);

        verify(uploader).upload(eq(Model.MetaRequest.Type.REGULAR), requestCaptor.capture(), eq(organization.getId().toString()), eq(extension), eq(size));

        try (var fis = new FileInputStream(fileName)) {
            assertThat(requestCaptor.getValue().readAllBytes()).isEqualTo(fis.readAllBytes());
        }
    }

    @Test
    @DisplayName("Сохранение логотипа. Не картинка")
    void should_saveLogo_noImage() throws Exception {
        var organization = new Organization();
        organization.setOfficialName("Name");
        organization.setAddress("Address");
        organization.setMsrn("msrn");
        organization.setTid("tid");
        organization.setOrganizationCode(1);

        organization = organizationRepository.save(organization);

        var resource = getClass().getClassLoader().getResource("application.yml");
        assertThat(resource).isNotNull();
        var fileName = resource.getFile();
        assertThat(fileName).isNotNull();

        var file = new MockMultipartFile("logo", "logo.yml", "application/yml", new FileInputStream(fileName));

        mockMvc.perform(multipart("/logo/" + organization.getId()).file(file).header("Content-Length", 5495)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("Сохранение логотипа. Не авторизован")
    void should_saveLogo_noAuthorized() throws Exception {
        var resource = getClass().getClassLoader().getResource("testLogo/logo.jpg");
        assertThat(resource).isNotNull();
        var fileName = resource.getFile();
        assertThat(fileName).isNotNull();

        var file = new MockMultipartFile("logo", new FileInputStream(fileName));

        mockMvc.perform(multipart("/logo/" + UUID.randomUUID()).file(file).with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @DisplayName("Получение логотипа")
    @MethodSource("saveLogoSource")
    void should_downloadLogo(String extension) throws Exception {
        var organization = new Organization();
        organization.setOfficialName("Name");
        organization.setAddress("Address");
        organization.setMsrn("msrn");
        organization.setTid("tid");

        organization = organizationRepository.save(organization);

        var root = new File(IMAGES);
        if (!root.isDirectory()) {
            assertThat(root.mkdirs()).isTrue();
        }

        var resource = getClass().getClassLoader().getResource("testLogo/logo." + extension.split("/")[1]);
        assertThat(resource).isNotNull();
        var fileName = resource.getFile();
        assertThat(fileName).isNotNull();

        var srcFile = new File(fileName);

        var srcBytes = new byte[0];
        try (var fis = new FileInputStream(srcFile);
             var baos = new ByteArrayOutputStream()) {
            IOUtils.copy(fis, baos);
            srcBytes = baos.toByteArray();
        }
        assertThat(srcBytes).isNotEmpty();

        FileUtils.copyFile(srcFile, new File(IMAGES + "/" + organization.getId() + ".file"));
        try (var fos = new FileOutputStream(IMAGES + "/" + organization.getId() + ".mime")) {
            fos.write((extension).getBytes());
        }

        var fileMeta = new FileMeta(organization.getId().toString(), MediaType.parseMediaType(extension), new File(IMAGES + "/" + organization.getId() + ".file").length());

        when(downloader.meta(Model.MetaRequest.Type.REGULAR, organization.getId().toString()))
                .then(inv -> fileMeta);

        byte[] finalSrcBytes = srcBytes;
        when(downloader.download(eq(fileMeta), anyLong(), anyLong())).then(inv -> ByteBuffer.wrap(finalSrcBytes).slice(inv.getArgument(1, Long.class).intValue(), Math.min(inv.getArgument(2, Long.class).intValue(), finalSrcBytes.length)).array());

        mockMvc.perform(get("/logo/" + organization.getId())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(extension))
                .andExpect(content().bytes(srcBytes));

        //noinspection ResultOfMethodCallIgnored
        new File("./images").delete();
    }

    @Test
    @DisplayName("Получение логотипа. Нет организации")
    void should_downloadLogo_noUser() throws Exception {
        var uuid = UUID.randomUUID();
        mockMvc.perform(get("/logo/" + uuid)
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Data not found: Entity: Organization, ID: " + uuid))
                .andExpect(jsonPath("$.entity.name").value("Organization"))
                .andExpect(jsonPath("$.entity.id").value(uuid.toString()));
    }

    @Test
    @DisplayName("Получение логотипа. Нет авторизован")
    void should_downloadLogo_noAuthorized() throws Exception {
        mockMvc.perform(get("/logo/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Тест ресендера")
    @Transactional
    void test_resendOk() throws Exception {
        var site = new Contact();
        site.setContactType(ContactType.SITE);
        site.setValue("www.site.com");

        var email = new Contact();
        email.setContactType(ContactType.EMAIL);
        email.setValue("site@company.com");

        var phone = new Contact();
        phone.setContactType(ContactType.PHONE);
        phone.setValue("+70000000000");

        var contacts = new ArrayList<Contact>();
        contacts.add(site);
        contacts.add(email);
        contacts.add(phone);

        var testOrganization1 = Instancio.create(Organization.class);
        testOrganization1.setMsrn("MSRN");
        testOrganization1.setTid("TID");
        testOrganization1.setContacts(contacts);
        testOrganization1.setOrganizationCode(1);

        mockMvc.perform(put(OrganizationController.API_MAPPING + "resend-all?key=Волшебное_слово")
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    @DisplayName("Получение списка сотрудников по подстроке ФИО независимо от организации, тип оргструктуры internal")
    void test_getAllEmployeesBYFIO2() throws Exception {
        var testOrganization1 = Instancio.of(Organization.class)
                .ignore(field(Organization::getOrganizationGroup))
                .ignore(field(Organization::getDepartments))
                .ignore(field(Organization::getEmployees))
                .ignore(field(Organization::getTripPurposes))
                .ignore(field(Organization::getPositions))
                .ignore(field(Organization::getCargoTypes))
                .ignore(field(Organization::getId))
                .ignore(field(Organization::getContacts))
                .create();
        var testOrganization2 = Instancio.of(Organization.class)
                .ignore(field(Organization::getOrganizationGroup))
                .ignore(field(Organization::getDepartments))
                .ignore(field(Organization::getEmployees))
                .ignore(field(Organization::getTripPurposes))
                .ignore(field(Organization::getPositions))
                .ignore(field(Organization::getCargoTypes))
                .ignore(field(Organization::getId))
                .ignore(field(Organization::getContacts))
                .create();
        testOrganization1 = organizationRepository.save(testOrganization1);
        testOrganization2 = organizationRepository.save(testOrganization2);
        var testDepartment1 = Instancio.of(Department.class)
                .ignore(field(Department::getHead))
                .ignore(field(Department::getEmployees))
                .ignore(field(Department::getId))
                .create();
        var testDepartment2 = Instancio.of(Department.class)
                .ignore(field(Department::getHead))
                .ignore(field(Department::getEmployees))
                .ignore(field(Department::getId))
                .create();
        testDepartment1.setOrganization(testOrganization1);
        testDepartment2.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);
        testDepartment2 = departmentRepository.save(testDepartment2);
        var testPosition1 = Instancio.of(Position.class)
                .ignore(field(Position::getEmployees))
                .ignore(field(Position::getAvailableClasses))
                .ignore(field(Position::getId))
                .create();
        var testPosition2 = Instancio.of(Position.class)
                .ignore(field(Position::getEmployees))
                .ignore(field(Position::getAvailableClasses))
                .ignore(field(Position::getId))
                .create();
        testPosition1.setHumanReadableId("PS-001-1");
        testPosition2.setHumanReadableId("PS-001-2");
        testPosition1.setOrganization(testOrganization1);
        testPosition2.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);
        testPosition2 = positionRepository.save(testPosition2);
        var testEmployee1 = Instancio.of(Employee.class)
                .ignore(field(Employee::getPersonalCars))
                .ignore(field(Employee::getSupervisorOf))
                .ignore(field(Employee::getSupervisor))
                .ignore(field(Employee::getDelegatedBy))
                .ignore(field(Employee::getDelegateRecords))
                .ignore(field(Employee::getAttributes))
                .ignore(field(Employee::getAvailableTransportTypes))
                .set(field(Employee::getOrganization), testOrganization1)
                .set(field(Employee::getUserId), UUID.fromString(USER1_ID))
                .set(field(Employee::getDepartment), testDepartment1)
                .set(field(Employee::getPosition), testPosition1)
                .create();
        var testEmployee2 = Instancio.of(Employee.class)
                .ignore(field(Employee::getPersonalCars))
                .ignore(field(Employee::getSupervisorOf))
                .ignore(field(Employee::getSupervisor))
                .ignore(field(Employee::getDelegatedBy))
                .ignore(field(Employee::getDelegateRecords))
                .ignore(field(Employee::getAttributes))
                .ignore(field(Employee::getAvailableTransportTypes))
                .set(field(Employee::getDepartment), testDepartment2)
                .set(field(Employee::getOrganization), testOrganization2)
                .set(field(Employee::getPosition), testPosition2)
                .create();
        var testEmployee3 = Instancio.of(Employee.class)
                .ignore(field(Employee::getPersonalCars))
                .ignore(field(Employee::getSupervisorOf))
                .ignore(field(Employee::getSupervisor))
                .ignore(field(Employee::getDelegatedBy))
                .ignore(field(Employee::getDelegateRecords))
                .ignore(field(Employee::getAttributes))
                .ignore(field(Employee::getAvailableTransportTypes))
                .set(field(Employee::getDepartment), testDepartment2)
                .set(field(Employee::getOrganization), testOrganization2)
                .set(field(Employee::getPosition), testPosition2)
                .create();

        testEmployee1.setOrgStructureType(OrgStructureType.INTERNAL);
        testEmployee1 = employeeRepository.save(testEmployee1);

        testEmployee2.setDepartment(testDepartment2);
        testEmployee2.setLastName(testEmployee1.getLastName());
        testEmployee2.setOrganization(testDepartment2.getOrganization());

        testEmployee2.setOrgStructureType(OrgStructureType.EXTERNAL);
        employeeRepository.save(testEmployee2);


        testEmployee3.setLastName(testEmployee1.getLastName());
        testEmployee3.setOrganization(testEmployee1.getOrganization());
        testEmployee3.setOrgStructureType(OrgStructureType.INTERNAL);
        employeeRepository.save(testEmployee3);

        mockMvc.perform(
                        get("/employees/search?fio=" + testEmployee1.getLastName())
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))).andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("Получение списка сотрудников по подстроке ФИО: Фамилия Имя независимо от организации, тип оргструктуры internal")
    void test_getAllEmployeesBYFIO3() throws Exception {
        var testOrganization1 = generateOrganization();
        var testOrganization2 = generateOrganization();
        testOrganization1 = organizationRepository.save(testOrganization1);
        testOrganization2 = organizationRepository.save(testOrganization2);
        var testDepartment1 = generateDepartment();
        var testDepartment2 = generateDepartment();
        testDepartment1.setOrganization(testOrganization1);
        testDepartment2.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);
        testDepartment2 = departmentRepository.save(testDepartment2);
        var testPosition1 = Instancio.of(Position.class)
                .ignore(field(Position::getEmployees))
                .ignore(field(Position::getAvailableClasses))
                .ignore(field(Position::getId))
                .create();
        var testPosition2 = Instancio.of(Position.class)
                .ignore(field(Position::getEmployees))
                .ignore(field(Position::getAvailableClasses))
                .ignore(field(Position::getId))
                .create();
        testPosition1.setHumanReadableId("PS-001-1");
        testPosition2.setHumanReadableId("PS-001-2");
        testPosition1.setOrganization(testOrganization1);
        testPosition2.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);
        testPosition2 = positionRepository.save(testPosition2);

        var testEmployee1 = generateEmployee(testDepartment1, testPosition1, testOrganization1);
        var testEmployee2 = generateEmployee(testDepartment2, testPosition2, testOrganization2);
        var testEmployee3 = generateEmployee(testDepartment1, testPosition1, testOrganization1);

        testEmployee1.setOrgStructureType(OrgStructureType.INTERNAL);
        testEmployee1.setUserId(UUID.fromString(USER1_ID));
        employeeRepository.save(testEmployee1);

        testEmployee2.setDepartment(testDepartment2);
        testEmployee2.setLastName(testEmployee1.getLastName());
        testEmployee2.setOrganization(testDepartment2.getOrganization());

        testEmployee2.setOrgStructureType(OrgStructureType.EXTERNAL);
        employeeRepository.save(testEmployee2);

        testEmployee3.setLastName(testEmployee1.getLastName());
        testEmployee3.setFirstName(testEmployee1.getFirstName());
        testEmployee3.setOrganization(testEmployee1.getOrganization());
        testEmployee3.setOrgStructureType(OrgStructureType.INTERNAL);
        employeeRepository.save(testEmployee3);

        var t = mockMvc.perform(
                get("/employees/" + "search?fio=" +
                        testEmployee1.getLastName() + " " +
                        testEmployee1.getFirstName())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))));
        t.andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("Получение списка сотрудников по подстроке ФИО независимо от организации, тип оргструктуры external")
    void test_getAllEmployeesBYFIO4() throws Exception {
        var testOrganization1 = generateOrganization();
        var testOrganization2 = generateOrganization();
        testOrganization1 = organizationRepository.save(testOrganization1);
        testOrganization2 = organizationRepository.save(testOrganization2);
        var testDepartment1 = generateDepartment();
        var testDepartment2 = generateDepartment();
        testDepartment1.setOrganization(testOrganization1);
        testDepartment2.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);
        testDepartment2 = departmentRepository.save(testDepartment2);
        var testPosition1 = Instancio.of(Position.class)
                .ignore(field(Position::getEmployees))
                .ignore(field(Position::getAvailableClasses))
                .ignore(field(Position::getId))
                .create();
        var testPosition2 = Instancio.of(Position.class)
                .ignore(field(Position::getEmployees))
                .ignore(field(Position::getAvailableClasses))
                .ignore(field(Position::getId))
                .create();
        testPosition1.setHumanReadableId("PS-001-1");
        testPosition2.setHumanReadableId("PS-001-2");
        testPosition1.setOrganization(testOrganization1);
        testPosition2.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);
        testPosition2 = positionRepository.save(testPosition2);

        var testEmployee1 = Instancio.of(Employee.class)
                .set(field(Employee::getDepartment), testDepartment1)
                .set(field(Employee::getPosition), testPosition1)
                .set(field(Employee::getOrganization), testOrganization1)
                .set(field(Employee::getUserId), UUID.fromString(USER1_ID))
                .ignore(field(Employee::getDelegatedBy))
                .ignore(field(Employee::getDelegateRecords))
                .ignore(field(Employee::getAttributes))
                .ignore(field(Employee::getAvailableTransportTypes))
                .ignore(field(Employee::getPersonalCars))
                .ignore(field(Employee::getSupervisorOf))
                .create();
        var testEmployee2 = Instancio.of(Employee.class)
                .set(field(Employee::getDepartment), testDepartment2)
                .set(field(Employee::getPosition), testPosition2)
                .set(field(Employee::getOrganization), testOrganization2)
                .set(field(Employee::getUserId), UUID.fromString(USER2_ID))
                .ignore(field(Employee::getDelegatedBy))
                .ignore(field(Employee::getDelegateRecords))
                .ignore(field(Employee::getAttributes))
                .ignore(field(Employee::getAvailableTransportTypes))
                .ignore(field(Employee::getPersonalCars))
                .ignore(field(Employee::getSupervisorOf))
                .create();
        var testEmployee3 = generateEmployee(testDepartment2, testPosition2, testOrganization2);
        testEmployee1.setOrgStructureType(OrgStructureType.INTERNAL);
        employeeRepository.save(testEmployee1);

        testEmployee2.setDepartment(testDepartment2);
        testEmployee2.setLastName(testEmployee1.getLastName());
        testEmployee2.setOrganization(testDepartment2.getOrganization());

        testEmployee2.setOrgStructureType(OrgStructureType.EXTERNAL);
        employeeRepository.save(testEmployee2);

        testEmployee3.setLastName(testEmployee2.getLastName());
        testEmployee3.setOrganization(testEmployee2.getOrganization());
        testEmployee3.setOrgStructureType(OrgStructureType.EXTERNAL);
        employeeRepository.save(testEmployee3);

        mockMvc.perform(
                        get("/employees/" + "search?fio=" +
                                testEmployee1.getLastName())
                                .with(jwt().jwt(builder -> builder.jti(USER2_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))).andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("Получение списка сотрудников по подстроке ФИО: Фамилия Имя независимо от организации, тип оргструктуры external")
    void test_getAllEmployeesBYFIO5() throws Exception {
        var testOrganization1 = generateOrganization();
        var testOrganization2 = generateOrganization();
        testOrganization1 = organizationRepository.save(testOrganization1);
        testOrganization2 = organizationRepository.save(testOrganization2);
        var testDepartment1 = generateDepartment();
        var testDepartment2 = generateDepartment();
        testDepartment1.setOrganization(testOrganization1);
        testDepartment2.setOrganization(testOrganization1);
        testDepartment1 = departmentRepository.save(testDepartment1);
        testDepartment2 = departmentRepository.save(testDepartment2);
        var testPosition1 = Instancio.of(Position.class)
                .ignore(field(Position::getEmployees))
                .ignore(field(Position::getAvailableClasses))
                .ignore(field(Position::getId))
                .create();
        var testPosition2 = Instancio.of(Position.class)
                .ignore(field(Position::getEmployees))
                .ignore(field(Position::getAvailableClasses))
                .ignore(field(Position::getId))
                .create();
        testPosition1.setHumanReadableId("PS-001-1");
        testPosition2.setHumanReadableId("PS-001-2");
        testPosition1.setOrganization(testOrganization1);
        testPosition2.setOrganization(testOrganization1);
        testPosition1 = positionRepository.save(testPosition1);
        testPosition2 = positionRepository.save(testPosition2);

        var testEmployee1 = Instancio.of(Employee.class)
                .set(field(Employee::getDepartment), testDepartment1)
                .set(field(Employee::getPosition), testPosition1)
                .set(field(Employee::getOrganization), testOrganization1)
                .set(field(Employee::getUserId), UUID.fromString(USER1_ID))
                .ignore(field(Employee::getDelegatedBy))
                .ignore(field(Employee::getDelegateRecords))
                .ignore(field(Employee::getAttributes))
                .ignore(field(Employee::getAvailableTransportTypes))
                .ignore(field(Employee::getPersonalCars))
                .ignore(field(Employee::getSupervisorOf))
                .create();
        var testEmployee2 = Instancio.of(Employee.class)
                .set(field(Employee::getDepartment), testDepartment2)
                .set(field(Employee::getPosition), testPosition2)
                .set(field(Employee::getOrganization), testOrganization2)
                .set(field(Employee::getUserId), UUID.fromString(USER2_ID))
                .ignore(field(Employee::getDelegatedBy))
                .ignore(field(Employee::getDelegateRecords))
                .ignore(field(Employee::getAttributes))
                .ignore(field(Employee::getAvailableTransportTypes))
                .ignore(field(Employee::getPersonalCars))
                .ignore(field(Employee::getSupervisorOf))
                .create();
        var testEmployee3 = generateEmployee(testDepartment2, testPosition2, testOrganization2);
        var testEmployee4 = generateEmployee(testDepartment1, testPosition1, testOrganization1);
        testEmployee1.setOrgStructureType(OrgStructureType.INTERNAL);
        employeeRepository.save(testEmployee1);

        testEmployee2.setDepartment(testDepartment2);
        testEmployee2.setLastName(testEmployee1.getLastName());
        testEmployee2.setOrganization(testDepartment2.getOrganization());

        testEmployee2.setOrgStructureType(OrgStructureType.EXTERNAL);
        employeeRepository.save(testEmployee2);

        testEmployee3.setLastName(testEmployee2.getLastName());
        testEmployee3.setOrganization(testEmployee2.getOrganization());
        testEmployee3.setOrgStructureType(OrgStructureType.EXTERNAL);
        employeeRepository.save(testEmployee3);

        testEmployee4.setLastName(testEmployee2.getLastName());
        testEmployee4.setOrganization(testEmployee1.getOrganization());
        testEmployee4.setOrgStructureType(OrgStructureType.EXTERNAL);
        employeeRepository.save(testEmployee4);

        mockMvc.perform(
                        get("/employees/" + "search?fio=" +
                                testEmployee2.getLastName() + " " +
                                testEmployee2.getFirstName()
                        )
                                .with(jwt().jwt(builder -> builder.jti(USER2_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))).andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    private static Organization generateOrganization() {
        return Instancio.of(Organization.class)
                .ignore(field(Organization::getOrganizationGroup))
                .ignore(field(Organization::getDepartments))
                .ignore(field(Organization::getEmployees))
                .ignore(field(Organization::getTripPurposes))
                .ignore(field(Organization::getPositions))
                .ignore(field(Organization::getContacts))
                .ignore(field(Organization::getId))
                .ignore(field(Organization::getCargoTypes))
                .create();
    }

    private Department generateDepartment() {
        return Instancio.of(Department.class)
                .ignore(field(Department::getEmployees))
                .ignore(field(Department::getHead))
                .ignore(field(Department::getChildren))
                .ignore(field(Department::getId))
                .create();
    }

    protected Employee generateEmployee(Department testDepartment, Position testPosition, Organization testOrganization) {
        var employee = Instancio.of(Employee.class)
                .set(field(Employee::getDepartment), testDepartment)
                .set(field(Employee::getPosition), testPosition)
                .set(field(Employee::getOrganization), testOrganization)
                .ignore(field(Employee::getDelegatedBy))
                .ignore(field(Employee::getDelegateRecords))
                .ignore(field(Employee::getAttributes))
                .ignore(field(Employee::getAvailableTransportTypes))
                .ignore(field(Employee::getPersonalCars))
                .ignore(field(Employee::getSupervisorOf))
                .create();
        employee.setUserId(employee.getId());
        return employee;
    }

}
