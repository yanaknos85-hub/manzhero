package ru.sberbank.ditsib.corpclient.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.authorization.service.CheckUserAccessService;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.corpclient.database.dao.*;
import ru.sberbank.ditsib.corpclient.database.model.DocumentCode;
import ru.sberbank.ditsib.corpclient.database.model.DocumentType;
import ru.sberbank.ditsib.corpclient.database.model.docs.EmployeeDocument;
import ru.sberbank.ditsib.corpclient.database.model.PersonalCar;
import ru.sberbank.ditsib.corpclient.dto.PersonalCarDTO;
import ru.sberbank.ditsib.corpclient.dto.docs.*;
import ru.sberbank.ditsib.corpclient.mapper.PersonalCarMapper;
import ru.sberbank.ditsib.corpclient.service.ActivePersonalCarService;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.PersonalTransportType;
import ru.sberbank.ditsib.transport.messaging.messages.PersonalCarMessage;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sberbank.ditsib.corpclient.controller.PersonalCarController.getApiMappingByIds;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера личных ТС")
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@Transactional
class PersonalCarControllerTest extends SharedData {
    
    @Autowired
    private PersonalCarMapper personalCarMapper;
    
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ObjectMapper objectMapper;
    
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private PositionRepository positionRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private PersonalCarRepository personalCarRepository;
    
    @Autowired
    private ActivePersonalCarService activePersonalCarService;
    
    @MockitoSpyBean
    private CheckUserAccessService checkUserAccessService;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @MockitoBean(name = "personalCarsOutput")
    private OutputBridge personalCarsOutput;

    @Autowired
    private EmployeeDocumentRepository employeeDocumentRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }
    
    @BeforeEach
    public void persistNeeded() {
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
        testPosition1 = positionRepository.save(testPosition1);
        testPosition2 = positionRepository.save(testPosition2);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee2.setDepartment(testDepartment2);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        testEmployee2.setOrganization(testDepartment2.getOrganization());
        testEmployee1 = employeeRepository.save(testEmployee1);
        testEmployee2 = employeeRepository.save(testEmployee2);
        testEmployee1.setNew(false);
        testEmployee2.setNew(false);
    }
    
    @Test
    @DisplayName("Добавление автомобиля")
    void test_addPersonalCar() throws Exception {
        doNothing().when(checkUserAccessService).check(testOrganization1.getId());
        doNothing().when(checkUserAccessService).check(testOrganization2.getId());
        
        testPersonalCar1.setEmployee(testEmployee1);
        var request = objectMapper.writeValueAsString(personalCarMapper.personalCarToNewDTO(testPersonalCar1));

        assert testEmployee1.getId() != null;
        var response =
                mockMvc.perform(
                               post(getApiMappingByIds(testOrganization1.getId(), testDepartment1.getId(),
                                                       testEmployee1.getId()))
                                       .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                       .contentType(MediaType.APPLICATION_JSON_VALUE)
                                       .content(request))
                       .andExpect(status().isOk()).andReturn();
        
        PersonalCarDTO actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                       PersonalCarDTO.class);
        PersonalCar expected = personalCarRepository.findById(actual.getId()).orElse(null);
        
        assertNotNull(expected);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(testPersonalCar1.getBrandName(), actual.getBrandName());
        assertEquals(testPersonalCar1.getColor(), actual.getColor());
        assertEquals(testPersonalCar1.getPassengerSeatsCount(), actual.getPassengerSeatsCount());
        assertEquals(PersonalTransportType.CAR, actual.getTransportType());
        assertEquals(testPersonalCar1.getRegistrationCertificate(), actual.getRegistrationCertificate());
        assertEquals(testPersonalCar1.getRegistrationNumber(), actual.getRegistrationNumber());
        
        var actualMessage = getMessages(personalCarsOutput, PersonalCarMessage.class);
        
        assertNotNull(actualMessage);
        assertEquals(expected.getId(), actualMessage.getId());
        assertEquals(testEmployee1.getId(), actualMessage.getEmployeeId());
        assertEquals(testPersonalCar1.getBrandName(), actualMessage.getBrandName());
        assertEquals(testPersonalCar1.getRegistrationCertificate(), actualMessage.getRegistrationCertificate());
        assertEquals(testPersonalCar1.getRegistrationNumber(), actualMessage.getRegistrationNumber());
        
        testPersonalCar2.setEmployee(testEmployee2);
        request = objectMapper.writeValueAsString(personalCarMapper.personalCarToNewDTO(testPersonalCar2));

        assert testEmployee2.getId() != null;
        response =
                mockMvc.perform(
                               post(getApiMappingByIds(testOrganization2.getId(), testDepartment2.getId(),
                                                       testEmployee2.getId()))
                                       .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                       .contentType(MediaType.APPLICATION_JSON_VALUE)
                                       .content(request))
                       .andExpect(status().isOk()).andReturn();
        
        actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                        PersonalCarDTO.class);
        assertEquals(0,actual.getPassengerSeatsCount());
        assertEquals(PersonalTransportType.MOTORCYCLE, actual.getTransportType());
    }

    @Test
    @DisplayName("Добавление автомобиля одновременно с документами")
    void test_addPersonalCarWithDocs() throws Exception {

        documentTypeRepository.save(new DocumentType(DocumentCode.DRIVER_LIC, "Водительское удостоверение"));
        documentTypeRepository.save(new DocumentType(DocumentCode.MARRIAGE_CERTIFICATE, "Свидетельство об браке"));
        documentTypeRepository.save(new DocumentType(DocumentCode.OSAGO, "Полис ОСАГО"));
        documentTypeRepository.save(new DocumentType(DocumentCode.PASSPORT_TS, "ПТС"));
        documentTypeRepository.save(new DocumentType(DocumentCode.AGREEMENT_PDN, "Согласие на обработку ПДн"));

        doNothing().when(checkUserAccessService).check(testOrganization1.getId());
        doNothing().when(checkUserAccessService).check(testOrganization2.getId());

        testPersonalCar1.setEmployee(testEmployee1);
        var personalCarDTO = personalCarMapper.personalCarToNewDTO(testPersonalCar1);

        var driverLic = DriverLicDTO.builder()
                .fileName("filename.jpg")
                .fileSize(12345)
                .fileFormat("jpg")
                .seria("seria")
                .number("123456")
                .issueDateDocument(LocalDateTime.now())
                .issue("Инспектор МОТОТР")
                .placeIssue("ГИБДД")
                .finalTimeDocument(LocalDateTime.now().plusYears(10))
                .categoria("B B1 M")
                .build();

        var osago = OsagoDTO.builder()
                .fileName("filename.png")
                .fileSize(223344)
                .fileFormat("png")
                .seria("AB")
                .number("123456")
                .startTimeDocument(LocalDateTime.now())
                .finalTimeDocument(LocalDateTime.now().plusYears(1))
                .registrationNumber("Н123УЕ777")
                .build();

        var passportTs = PassportTsDTO.builder()
                .fileName("ff.gif")
                .fileSize(12345)
                .fileFormat("gif")
                .color("Черный")
                .passengerSeatsCount(4)
                .vin("XZ1234CVB12345Z")
                .engineVolume(1998)
                .enginePower("150л.с.")
                .build();

        var marriageCertificate = MarriageCertificateDTO.builder()
                .fileName("file")
                .fileSize(232323)
                .fileFormat("txt")
                .seria("IV")
                .number("123123")
                .issueDateDocument(LocalDateTime.now())
                .build();

        var agreementPdn = AgreementPdnDTO.builder()
                .fileName("fg")
                .fileSize(1)
                .fileFormat("xxx")
                .build();

        var documents = new DocumentsDTO(driverLic, osago, passportTs, marriageCertificate, agreementPdn);

        personalCarDTO.setDocuments(documents);

        var request = objectMapper.writeValueAsString(personalCarDTO);

        assert testEmployee1.getId() != null;
        var response =
                mockMvc.perform(
                                post(getApiMappingByIds(testOrganization1.getId(), testDepartment1.getId(),
                                        testEmployee1.getId()))
                                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .content(request))
                        .andExpect(status().isOk()).andReturn();

        PersonalCarDTO actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                PersonalCarDTO.class);
        PersonalCar expected = personalCarRepository.findById(actual.getId()).orElse(null);

        assertNotNull(expected);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(testPersonalCar1.getBrandName(), actual.getBrandName());
        assertEquals("Черный", actual.getColor()); // Теперь цвет берется из ПТС
        assertEquals(4, actual.getPassengerSeatsCount()); // Теперь кол-во пассажирских мест берется из ПТС
        assertEquals(PersonalTransportType.CAR, actual.getTransportType());
        assertEquals(testPersonalCar1.getRegistrationCertificate(), actual.getRegistrationCertificate());
        assertEquals("Н123УЕ777", actual.getRegistrationNumber()); // Рег. номер теперь берется из ОСАГО

        var actualMessage = getMessages(personalCarsOutput, PersonalCarMessage.class);

        assertNotNull(actualMessage);
        assertEquals(expected.getId(), actualMessage.getId());
        assertEquals(testEmployee1.getId(), actualMessage.getEmployeeId());
        assertEquals(testPersonalCar1.getBrandName(), actualMessage.getBrandName());
        assertEquals(1998, actualMessage.getEngineVolume());
        assertEquals(testPersonalCar1.getRegistrationCertificate(), actualMessage.getRegistrationCertificate());
        assertEquals("Н123УЕ777", actualMessage.getRegistrationNumber());

        assertEquals(testEmployee1.getId(), actual.getDocuments().driverLic().employeeId());
        assertEquals(testEmployee1.getId(), actual.getDocuments().marriageCertificate().employeeId());
        assertEquals(testEmployee1.getId(), actual.getDocuments().osago().employeeId());
        assertEquals(testEmployee1.getId(), actual.getDocuments().passportTs().employeeId());
        assertEquals(testEmployee1.getId(), actual.getDocuments().agreementPdn().employeeId());
        assertEquals(actual.getId(), actual.getDocuments().osago().carId());
        assertEquals(actual.getId(), actual.getDocuments().passportTs().carId());
        assertEquals(actual.getId(), actual.getDocuments().agreementPdn().carId());
    }
    
    @Test
    @DisplayName("Изменение автомобиля")
    void test_updatePersonalCar() throws Exception {
        employeeRepository.save(testEmployee2);
        testPersonalCar1.setEmployee(testEmployee1);
        personalCarRepository.save(testPersonalCar1);

        var documentType = documentTypeRepository.save(new DocumentType(DocumentCode.AGREEMENT_PDN, "Согласие на обработку ПДн"));

        EmployeeDocument ed = new EmployeeDocument();
        ed.setId(UUID.randomUUID());
        ed.setDocumentType(documentType);
        ed.setEmployeeId(testEmployee1.getId());
        ed.setFileName("fileName.txt");
        ed.setFileFormat("txt");
        ed.setFileSize(12345);
        ed.setCarId(testPersonalCar1.getId());
        ed.setIssueDateDocument(LocalDateTime.now());
        ed.updateAuthorInfo(UUID.randomUUID());
        employeeDocumentRepository.save(ed);
        
        PersonalCar old = personalCarRepository.findById(testPersonalCar1.getId()).orElse(null);
        assert old != null;
        old.setInsuranceNumber(CAR_INSURANCE_2);
        old.setRegistrationNumber(CAR_REG_NUMBER_2);
        
        var request = objectMapper.writeValueAsString(personalCarMapper.personalCarToDTO(old));

        assert testEmployee1.getId() != null;
        System.out
                .println(getApiMappingByIds(testOrganization1.getId(), testDepartment1.getId(), testEmployee1.getId()));
        
        mockMvc.perform(
                       put(getApiMappingByIds(testOrganization1.getId(),
                                              testDepartment1.getId(),
                                              testEmployee1.getId()) +
                           testPersonalCar1.getId().toString())
                               .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                               .contentType(MediaType.APPLICATION_JSON_VALUE)
                               .content(request))
               .andExpect(status().isOk()).andReturn();
        
        var actual = getMessages(personalCarsOutput, PersonalCarMessage.class);
        
        PersonalCar expected = personalCarRepository.findById(actual.getId()).orElse(null);
        
        assertNotNull(expected);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(CAR_INSURANCE_2, actual.getInsuranceNumber());
        assertEquals(CAR_REG_NUMBER_2, actual.getRegistrationNumber());
    }
    
    @Test
    @DisplayName("Удаление персонального авто")
    void test_deletePersonalCar() throws Exception {
        personalCarRepository.saveAndFlush(testPersonalCar1);
        assertFalse(positionRepository.findAll().isEmpty());
        assert testEmployee1.getId() != null;
        mockMvc.perform(delete(getApiMappingByIds(testOrganization1.getId(),
                                                  testDepartment1.getId(),
                                                  testEmployee1.getId()) +
                               testPersonalCar1.getId().toString())
                        .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk()).andReturn();
        
        assertEquals(0, personalCarRepository.findAll().size());
        
        var actualMessage = getMessages(personalCarsOutput, PersonalCarMessage.class);
        
        assertEquals(testPersonalCar1.getId(), actualMessage.getId());
        assertTrue(actualMessage.isDeleted());
    }
    
    @Test
    @DisplayName("Удаление несуществующего автомобиля")
    void test_deleteNonExistentCar() throws Exception {
        String rndUUID = UUID.randomUUID().toString();
        assert testEmployee1.getId() != null;
        Exception resolvedException =
                mockMvc.perform(
                               delete(getApiMappingByIds(testOrganization1.getId(), testDepartment1.getId(),
                                                         testEmployee1.getId()) +
                                      rndUUID)
                                       .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                       .contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(status().isNotFound()).andReturn().getResolvedException();
        assertNotNull(resolvedException);
        assertEquals(EntityNotFoundException.class, resolvedException.getClass());
    }
    
    @Test
    @DisplayName("Получение  существующего автомобиля")
    void test_getExistingCar() throws Exception {
        testPersonalCar1.setEmployee(testEmployee1);
        personalCarRepository.save(testPersonalCar1);
        assertTrue(personalCarRepository.findById(testPersonalCar1.getId()).isPresent());

        assert testEmployee1.getId() != null;
        var saved =
                mockMvc.perform(
                               get(getApiMappingByIds(testOrganization1.getId(),
                                                      testDepartment1.getId(),
                                                      testEmployee1.getId()) +
                                   testPersonalCar1.getId().toString())
                                       .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                       .contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(status().isOk()).andReturn();
        assertNotNull(saved);
        PersonalCarDTO actual = objectMapper.readValue(saved.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                       PersonalCarDTO.class);

        assertEquals(testPersonalCar1.getId(), actual.getId());
        assertEquals(testPersonalCar1.getBrandName(), actual.getBrandName());
        assertEquals(testPersonalCar1.getRegistrationCertificate(), actual.getRegistrationCertificate());
        assertEquals(testPersonalCar1.getRegistrationNumber(), actual.getRegistrationNumber());
        assertTrue(actual.isPersDataAccept());
    }
    
    
    @Test
    @DisplayName("Получение  отсутствующего автомобиля")
    void test_getAbsentCar() throws Exception {
        String rndUUID = UUID.randomUUID().toString();
        assert testEmployee1.getId() != null;
        Exception resolvedException = mockMvc.perform(
                                                     get(getApiMappingByIds(testOrganization1.getId(), testDepartment1.getId(),
                                                                            testEmployee1.getId()) + rndUUID)
                                                             .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                                             .contentType(MediaType.APPLICATION_JSON_VALUE))
                                             .andExpect(status().isNotFound())
                                             .andReturn().getResolvedException();
        assertNotNull(resolvedException);
        assertEquals(resolvedException.getClass(), EntityNotFoundException.class);
    }
    
    @Test
    @DisplayName("Получение  автомобиля по несуществующем сотруднику")
    void test_getCarByNonExistentEmployee() throws Exception {
        personalCarRepository.save(testPersonalCar1);
        UUID rndUUID = UUID.randomUUID();
        Exception resolvedException = mockMvc.perform(
                                                     get(getApiMappingByIds(testOrganization1.getId(),
                                                                            testDepartment1.getId(),
                                                                            rndUUID)
                                                         + testPersonalCar1.getId().toString())
                                                             .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                                             .contentType(MediaType.APPLICATION_JSON_VALUE))
                                             .andExpect(status().isNotFound())
                                             .andReturn().getResolvedException();
        assertNotNull(resolvedException);
        assertEquals(resolvedException.getClass(), EntityNotFoundException.class);
    }
    
    @Test
    @DisplayName("Получение списка автомобилей  сотрудника")
    void test_getCarsByUser() throws Exception {
        testPersonalCar1.setEmployee(testEmployee1);
        testPersonalCar2.setEmployee(testEmployee2);
        employeeRepository.save(testEmployee2);
        var car1 = personalCarRepository.save(testPersonalCar1);
        personalCarRepository.saveAndFlush(testPersonalCar2);

        DocumentType documentType = new DocumentType();
        documentType.setDocumentCode(DocumentCode.DRIVER_LIC);
        documentType.setName("Водительское удостоверение");
        documentType = documentTypeRepository.save(documentType);

        EmployeeDocument driverLic = new EmployeeDocument();
        driverLic.setId(UUID.randomUUID());
        driverLic.setCreationTime(LocalDateTime.now());
        driverLic.setCreationUser(UUID.randomUUID());
        driverLic.setDocumentType(documentType);
        driverLic.setEmployeeId(testEmployee1.getId());
        driverLic.setCarId(car1.getId());
        driverLic.setFileName("fileName");
        driverLic.setFileSize(123);
        driverLic.setFileFormat("pdf");
        driverLic.setSeria("seria");
        driverLic.setNumber("123456");
        driverLic.setIssueDateDocument(LocalDate.of(2015,12,30).atStartOfDay());
        driverLic.setIssue("ГИБДД");
        driverLic.setPlaceIssue("Арбат, 1, Москва");
        driverLic.setStartTimeDocument(LocalDate.of(2016, 1,1).atStartOfDay());
        driverLic.setFinalTimeDocument(LocalDate.of(2026,1,1).atStartOfDay());
        driverLic.setCategoria("B");
        employeeDocumentRepository.save(driverLic);

        assert testEmployee1.getId() != null;
        var saved = mockMvc.perform(
                                   get(getApiMappingByIds(testOrganization1.getId(), testDepartment1.getId(), testEmployee1.getId()))
                                           .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                           .andExpect(status().isOk()).andReturn();
        assertNotNull(saved);
        List<PersonalCarDTO> savedList = objectMapper.readValue(saved.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                                new TypeReference<>() {
                                                                });
        assertEquals(1, savedList.size());
        assertEquals(testPersonalCar1.getId(), savedList.getFirst().getId());
    }
    
    @Test
    @DisplayName("Получение  активного автомобиля для текущего пользователя")
    void test_getActivePersonalCar() throws Exception {
        testPersonalCar1.setEmployee(testEmployee1);
        personalCarRepository.save(testPersonalCar1);
        assertTrue(personalCarRepository.findById(testPersonalCar1.getId()).isPresent());
        
        var saved =
                mockMvc.perform(
                               get("/self/cars")
                                       .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                       .contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(status().isOk()).andReturn();
        assertNotNull(saved);
        PersonalCarDTO actual = objectMapper.readValue(saved.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                       PersonalCarDTO.class);
        
        
        assertEquals(testPersonalCar1.getId(), actual.getId());
        assertEquals(testPersonalCar1.getBrandName(), actual.getBrandName());
        assertEquals(testPersonalCar1.getRegistrationCertificate(), actual.getRegistrationCertificate());
        assertEquals(testPersonalCar1.getRegistrationNumber(), actual.getRegistrationNumber());
        assertTrue(actual.isPersDataAccept());
    }
    
    @Test
    @DisplayName("Сохранение активного автомобиля для текущего пользователя")
    void test_saveActiveCar() throws Exception {
        testPersonalCar1.setEmployee(testEmployee1);
        testPersonalCar2.setEmployee(testEmployee1);
        employeeRepository.save(testEmployee1);
        personalCarRepository.save(testPersonalCar1);
        personalCarRepository.saveAndFlush(testPersonalCar2);
        
        mockMvc.perform(
                       put("/self/cars?personalCarId=" + testPersonalCar2.getId())
                               .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
               .andExpect(status().isOk()).andReturn();
        
        
        PersonalCar saved = activePersonalCarService.getActive(testEmployee1.getId());
        assertNotNull(saved);
        assertEquals(saved.getId(), testPersonalCar2.getId());
    }
    
    @Test
    @DisplayName("Сохранение активного автомобиля для пользователя не являющегося владельцем ")
    void test_saveActiveCarIncorrectEmployee() throws Exception {
        testPersonalCar1.setEmployee(testEmployee1);
        testPersonalCar2.setEmployee(testEmployee2);
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        personalCarRepository.save(testPersonalCar1);
        personalCarRepository.saveAndFlush(testPersonalCar2);
        
        var result = mockMvc.perform(
                       put("/self/cars?personalCarId=" + testPersonalCar2.getId())
                               .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
               .andExpect(status().isNotFound()).andReturn();
        assertThat(result.getResponse()).isNotNull();
    }
    
}
