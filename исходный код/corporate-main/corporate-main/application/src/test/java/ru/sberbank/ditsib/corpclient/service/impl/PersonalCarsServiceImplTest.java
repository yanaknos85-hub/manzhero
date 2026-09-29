package ru.sberbank.ditsib.corpclient.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.corpclient.dto.docs.*;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
class PersonalCarsServiceImplTest {

    @Test
    @DisplayName("Заполнение id сотрудника в водительском удостоверении, если он не был указан")
    void fillEmployeeId_driverLic() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        DriverLicDTO driverLic = DriverLicDTO.builder().build();
        UUID employeeId = UUID.randomUUID();

        var result = cut.fillEmployeeId(driverLic, employeeId);

        assertEquals(employeeId, result.employeeId());
    }

    @Test
    @DisplayName("Заполнение id сотрудника в свидетельстве о браке, если он не был указан")
    void fillEmployeeId_marriageCert() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        MarriageCertificateDTO marriageCertificateDTO = MarriageCertificateDTO.builder().build();
        UUID employeeId = UUID.randomUUID();

        var result = cut.fillEmployeeId(marriageCertificateDTO, employeeId);

        assertEquals(employeeId, result.employeeId());
    }

    @Test
    @DisplayName("Заполнение id сотрудника в ОСАГО, если он не был указан")
    void fillEmployeeId_osago() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        OsagoDTO osagoDTO = OsagoDTO.builder().build();
        UUID employeeId = UUID.randomUUID();

        var result = cut.fillEmployeeId(osagoDTO, employeeId);

        assertEquals(employeeId, result.employeeId());
    }

    @Test
    @DisplayName("Заполнение id автомобиля в ОСАГО, если он не был указан")
    void fillCarId_osago() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        OsagoDTO osagoDTO = OsagoDTO.builder().build();
        UUID carId = UUID.randomUUID();

        var result = cut.fillCarId(osagoDTO, carId);

        assertEquals(carId, result.carId());
    }

    @Test
    @DisplayName("Заполнение id сотрудника в ПТС, если он не был указан")
    void fillEmployeeId_PassportTs() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        PassportTsDTO passportTsDTO = PassportTsDTO.builder().build();
        UUID employeeId = UUID.randomUUID();

        var result = cut.fillEmployeeId(passportTsDTO, employeeId);

        assertEquals(employeeId, result.employeeId());
    }

    @Test
    @DisplayName("Заполнение id автомобиля в ПТС, если он не был указан")
    void fillCarId_PassportTs() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        PassportTsDTO passportTsDTO = PassportTsDTO.builder().build();
        UUID carId = UUID.randomUUID();

        var result = cut.fillCarId(passportTsDTO, carId);

        assertEquals(carId, result.carId());
    }

    @Test
    @DisplayName("Заполнение id сотрудника в согласии на обработку ПДн, если он не был указан")
    void fillEmployeeId_AgreementPdn() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        AgreementPdnDTO agreementPdnDTO = AgreementPdnDTO.builder().build();
        UUID employeeId = UUID.randomUUID();

        var result = cut.fillEmployeeId(agreementPdnDTO, employeeId);

        assertEquals(employeeId, result.employeeId());
    }

    @Test
    @DisplayName("Заполнение id автомобиля в согласии на обработку ПДн, если он не был указан")
    void fillCarId_AgreementPdn() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        AgreementPdnDTO agreementPdnDTO = AgreementPdnDTO.builder().build();
        UUID carId = UUID.randomUUID();

        var result = cut.fillCarId(agreementPdnDTO, carId);

        assertEquals(carId, result.carId());
    }

    @Test
    @DisplayName("Если данных о согласии на обработку ПДн раньше не было, то согласие из запроса считается новым")
    void isNew_AgreementPdn_null() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var result = cut.isNew(null, (AgreementPdnDTO) null);

        assertTrue(result);
    }

    @Test
    @DisplayName("Если в существующем согласии на обработку ПДн нет id, то согласие из запроса считается новым")
    void isNew_AgreementPdn_id_is_null() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        AgreementPdnDTO oldAgreementPdnDTO = AgreementPdnDTO.builder().build();
        var result = cut.isNew(oldAgreementPdnDTO, null);

        assertTrue(result);
    }

    @Test
    @DisplayName("Если в существующем согласии на обработку ПДн id не совпадает с id согласия из запроса, то последнее считается новым")
    void isNew_AgreementPdn_notEqualId() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        AgreementPdnDTO oldAgreementPdnDTO = AgreementPdnDTO.builder().id(UUID.randomUUID()).build();
        AgreementPdnDTO newAgreementPdnDTO = AgreementPdnDTO.builder().id(UUID.randomUUID()).build();
        var result = cut.isNew(oldAgreementPdnDTO, newAgreementPdnDTO);

        assertTrue(result);
    }

    @Test
    @DisplayName("Если данных ПТС раньше не было, то ПТС из запроса считается новым")
    void isNew_PassportTs_null() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var result = cut.isNew(null, (PassportTsDTO) null);

        assertTrue(result);
    }

    @Test
    @DisplayName("Если в существующем ПТС нет id, то ПТС из запроса считается новым")
    void isNew_PassportTs_id_is_null() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        PassportTsDTO oldPassportTsDTO = PassportTsDTO.builder().build();
        var result = cut.isNew(oldPassportTsDTO, null);

        assertTrue(result);
    }

    @Test
    @DisplayName("Если в существующем ПТС id не совпадает с id ПТС из запроса, то последний считается новым")
    void isNew_PassportTs_notEqualId() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        PassportTsDTO oldPassportTsDTO = PassportTsDTO.builder().id(UUID.randomUUID()).build();
        PassportTsDTO newPassportTsDTO = PassportTsDTO.builder().id(UUID.randomUUID()).build();
        var result = cut.isNew(oldPassportTsDTO, newPassportTsDTO);

        assertTrue(result);
    }

    @Test
    @DisplayName("Если данных ОСАГО раньше не было, то ОСАГО из запроса считается новым")
    void isNew_Osago_null() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var result = cut.isNew(null, (OsagoDTO) null);

        assertTrue(result);
    }

    @Test
    @DisplayName("Если в существующем ОСАГО нет id, то ОСАГО из запроса считается новым")
    void isNew_Osago_id_is_null() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        OsagoDTO oldOsagoDTO = OsagoDTO.builder().build();
        var result = cut.isNew(oldOsagoDTO, null);

        assertTrue(result);
    }

    @Test
    @DisplayName("Если в существующем ОСАГО id не совпадает с id ОСАГО из запроса, то последний считается новым")
    void isNew_Osago_notEqualId() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        OsagoDTO oldOsagoDTO = OsagoDTO.builder().id(UUID.randomUUID()).build();
        OsagoDTO newOsagoDTO = OsagoDTO.builder().id(UUID.randomUUID()).build();
        var result = cut.isNew(oldOsagoDTO, newOsagoDTO);

        assertTrue(result);
    }

    @Test
    @DisplayName("Если id ОСАГО совпадает, но поменялись одновременно дата начала действия, серия и номер, то считаем, что это новый документ")
    void isNew_Osago_EqualId_AllKeyFieldsChanged() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        OsagoDTO oldOsagoDTO = OsagoDTO
                .builder()
                .id(UUID.randomUUID())
                .startTimeDocument(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
                .seria("seria1")
                .number("number1")
                .build();
        OsagoDTO newOsagoDTO = OsagoDTO
                .builder()
                .id(oldOsagoDTO.id())
                .startTimeDocument(LocalDateTime.of(2024, 1, 31, 0, 0, 0))
                .seria("seria2")
                .number("number2")
                .build();
        var result = cut.isNew(oldOsagoDTO, newOsagoDTO);

        assertTrue(result);
    }

    @Test
    @DisplayName("Если id ОСАГО совпадает, но одновременно дата начала действия, серия и номер не поменялись, то считаем, что это старый документ")
    void isNew_Osago_EqualId_NotAllKeyFieldsChanged() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        OsagoDTO oldOsagoDTO = OsagoDTO
                .builder()
                .id(UUID.randomUUID())
                .startTimeDocument(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
                .seria("seria1")
                .number("number1")
                .build();
        OsagoDTO newOsagoDTO = OsagoDTO
                .builder()
                .id(oldOsagoDTO.id())
                .startTimeDocument(LocalDateTime.of(2024, 1, 31, 0, 0, 0))
                .seria("seria1")
                .number("number2")
                .build();
        var result = cut.isNew(oldOsagoDTO, newOsagoDTO);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если данных о браке раньше не было, то свидетельство о браке из запроса считается новым")
    void isNew_MarriageCertificate_null() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var result = cut.isNew(null, (MarriageCertificateDTO) null);

        assertTrue(result);
    }

    @Test
    @DisplayName("Если в существующем свидетельстве нет id, то свидетельство о браке из запроса считается новым")
    void isNew_MarriageCertificate_id_is_null() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        MarriageCertificateDTO oldMarriageCertificateDTO = MarriageCertificateDTO.builder().build();
        var result = cut.isNew(oldMarriageCertificateDTO, null);

        assertTrue(result);
    }

    @Test
    @DisplayName("Если в существующем свидетельстве id не совпадает с id свидетельства о браке из запроса, то последнее считается новым")
    void isNew_MarriageCertificate_notEqualId() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        MarriageCertificateDTO oldMarriageCertificateDTO = MarriageCertificateDTO.builder().id(UUID.randomUUID()).build();
        MarriageCertificateDTO newMarriageCertificateDTO = MarriageCertificateDTO.builder().id(UUID.randomUUID()).build();
        var result = cut.isNew(oldMarriageCertificateDTO, newMarriageCertificateDTO);

        assertTrue(result);
    }

    @Test
    @DisplayName("Если id свидетельства о браке совпадает, но поменялись одновременно дата выдачи, серия и номер, то считаем, что это новый документ")
    void isNew_MarriageCertificate_EqualId_AllKeyFieldsChanged() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        MarriageCertificateDTO oldMarriageCertificateDTO = MarriageCertificateDTO
                .builder()
                .id(UUID.randomUUID())
                .issueDateDocument(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
                .seria("seria1")
                .number("number1")
                .build();
        MarriageCertificateDTO newMarriageCertificateDTO = MarriageCertificateDTO
                .builder()
                .id(oldMarriageCertificateDTO.id())
                .issueDateDocument(LocalDateTime.of(2024, 1, 31, 0, 0, 0))
                .seria("seria2")
                .number("number2")
                .build();
        var result = cut.isNew(oldMarriageCertificateDTO, newMarriageCertificateDTO);

        assertEquals(oldMarriageCertificateDTO.id(), newMarriageCertificateDTO.id());
        assertTrue(result);
    }

    @Test
    @DisplayName("Если id свидетельства о браке совпадает, но одновременно дата выдачи, серия и номер не поменялись, то считаем, что это старый документ")
    void isNew_MarriageCertificate_EqualId_NotAllKeyFieldsChanged() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        MarriageCertificateDTO oldMarriageCertificateDTO = MarriageCertificateDTO
                .builder()
                .id(UUID.randomUUID())
                .issueDateDocument(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
                .seria("seria1")
                .number("number1")
                .build();
        MarriageCertificateDTO newMarriageCertificateDTO = MarriageCertificateDTO
                .builder()
                .id(oldMarriageCertificateDTO.id())
                .issueDateDocument(LocalDateTime.of(2024, 1, 31, 0, 0, 0))
                .seria("seria1")
                .number("number2")
                .build();
        var result = cut.isNew(oldMarriageCertificateDTO, newMarriageCertificateDTO);

        assertEquals(oldMarriageCertificateDTO.id(), newMarriageCertificateDTO.id());
        assertFalse(result);
    }

    @Test
    @DisplayName("Если данных о в/у раньше не было, то в/у из запроса считается новым")
    void isNew_DriverLic_null() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var result = cut.isNew(null, (DriverLicDTO) null);

        assertTrue(result);
    }

    @Test
    @DisplayName("Если в существующем в/у нет id, то в/у из запроса считается новым")
    void isNew_DriverLic_id_is_null() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        DriverLicDTO oldDriverLicDTO = DriverLicDTO.builder().build();
        var result = cut.isNew(oldDriverLicDTO, null);

        assertTrue(result);
    }

    @Test
    @DisplayName("Если в существующем в/у id не совпадает с id в/у из запроса, то последнее считается новым")
    void isNew_DriverLic_notEqualId() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        DriverLicDTO oldDriverLicDTO = DriverLicDTO.builder().id(UUID.randomUUID()).build();
        DriverLicDTO newDriverLicDTO = DriverLicDTO.builder().id(UUID.randomUUID()).build();
        var result = cut.isNew(oldDriverLicDTO, newDriverLicDTO);

        assertTrue(result);
    }

    @Test
    @DisplayName("Если id в/у совпадает, но поменялись одновременно дата выдачи, серия и номер, то считаем, что это новый документ")
    void isNew_DriverLic_EqualId_allKeyFieldsChanged() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        DriverLicDTO oldDriverLicDTO = DriverLicDTO.builder()
                .id(UUID.randomUUID())
                .issueDateDocument(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
                .seria("seria1")
                .number("1")
                .build();
        DriverLicDTO newDriverLicDTO = DriverLicDTO.builder()
                .id(oldDriverLicDTO.id())
                .issueDateDocument(LocalDateTime.of(2024, 1, 31, 0, 0, 0))
                .seria("seria2")
                .number("2")
                .build();
        var result = cut.isNew(oldDriverLicDTO, newDriverLicDTO);

        assertTrue(result);
    }

    @Test
    @DisplayName("Если id в/у совпадает, но одновременно дата выдачи, серия и номер не поменялись, то считаем, что это старый документ и нужно просто обновить атрибуты")
    void isNew_DriverLic_EqualId_notAllKeyFieldsChanged() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        DriverLicDTO oldDriverLicDTO = DriverLicDTO.builder()
                .id(UUID.randomUUID())
                .issueDateDocument(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
                .seria("seria1")
                .number("1")
                .build();
        DriverLicDTO newDriverLicDTO = DriverLicDTO.builder()
                .id(oldDriverLicDTO.id())
                .issueDateDocument(LocalDateTime.of(2024, 1, 31, 0, 0, 0))
                .seria("seria1")
                .number("2")
                .build();
        var result = cut.isNew(oldDriverLicDTO, newDriverLicDTO);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если информации по старому варианту соглашения об обработке ПДн нет, то считаем, что документ отличается")
    void isEqual_AgreementPdn_oldAgreementPdnIsNull() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var result = cut.isEqual(null, AgreementPdnDTO.builder().build());

        assertFalse(result);
    }

    @Test
    @DisplayName("Если информации по новому варианту соглашения об обработке ПДн нет, то считаем, что документ отличается")
    void isEqual_AgreementPdn_newAgreementPdnIsNull() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var result = cut.isEqual(AgreementPdnDTO.builder().build(), null);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если id сотрудника, имя/размер/формат файла совпадают, то соглашения об обработке ПДн считаем эквивалентными")
    void isEqual_AgreementPdn_AllKeyFieldsEqual() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var employeeId = UUID.randomUUID();
        AgreementPdnDTO oldAgreementPdnDTO = AgreementPdnDTO
                .builder()
                .employeeId(employeeId)
                .fileName("fileName")
                .fileFormat("fileFormat")
                .fileSize(12345)
                .build();
        AgreementPdnDTO newAgreementPdnDTO = AgreementPdnDTO
                .builder()
                .employeeId(employeeId)
                .fileName("fileName")
                .fileFormat("fileFormat")
                .fileSize(12345)
                .build();
        var result = cut.isEqual(oldAgreementPdnDTO, newAgreementPdnDTO);

        assertTrue(result);
    }

    @Test
    @DisplayName("Если id сотрудника или не все из полей (имя/размер/формат файла) совпадают, то соглашения об обработке ПДн считаем разными")
    void isEqual_AgreementPdn_NotAllKeyFieldsEqual() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var employeeId = UUID.randomUUID();
        AgreementPdnDTO oldAgreementPdnDTO = AgreementPdnDTO
                .builder()
                .employeeId(employeeId)
                .fileName("fileName")
                .fileFormat("fileFormat")
                .fileSize(12345)
                .build();
        AgreementPdnDTO newAgreementPdnDTO = AgreementPdnDTO
                .builder()
                .employeeId(employeeId)
                .fileName("fileName")
                .fileFormat("fileFormat2")
                .fileSize(12345)
                .build();
        var result = cut.isEqual(oldAgreementPdnDTO, newAgreementPdnDTO);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если информации по старому варианту ПТС нет, то считаем, что документы отличается")
    void isEqual_PassportTs_oldPassportTsIsNull() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var result = cut.isEqual(null, PassportTsDTO.builder().build());

        assertFalse(result);
    }

    @Test
    @DisplayName("Если информации по новому варианту ПТС нет, то считаем, что документы отличается")
    void isEqual_PassportTs_newPassportTsIsNull() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var result = cut.isEqual(PassportTsDTO.builder().build(), null);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если id сотрудника и все ключевые поля ПТС совпадают, то считаем документы эквивалентными")
    void isEqual_PassportTs_AllKeyFieldsPassportTsEqual() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var employeeId = UUID.randomUUID();
        PassportTsDTO oldPassportTsDTO = PassportTsDTO
                .builder()
                .employeeId(employeeId)
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .color("color")
                .passengerSeatsCount(4)
                .vin("vin")
                .engineVolume(1998)
                .enginePower("enginePower")

                .build();
        PassportTsDTO newPassportTsDTO = PassportTsDTO
                .builder()
                .employeeId(employeeId)
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .color("color")
                .passengerSeatsCount(4)
                .vin("vin")
                .engineVolume(1998)
                .enginePower("enginePower")
                .build();
        var result = cut.isEqual(oldPassportTsDTO, newPassportTsDTO);

        assertTrue(result);
    }

    @Test
    @DisplayName("Если id сотрудника или не все ключевые поля ПТС совпадают, то считаем документы разными")
    void isEqual_PassportTs_NotAllKeyFieldsPassportTsEqual_1() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var employeeId = UUID.randomUUID();
        PassportTsDTO oldPassportTsDTO = PassportTsDTO
                .builder()
                .employeeId(employeeId)
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .color("color")
                .passengerSeatsCount(4)
                .vin("vin")
                .engineVolume(1998)
                .enginePower("enginePower")

                .build();
        PassportTsDTO newPassportTsDTO = PassportTsDTO
                .builder()
                .employeeId(employeeId)
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .color("color")
                .passengerSeatsCount(4)
                .vin("vin")
                .engineVolume(1998)
                .enginePower("enginePower2")
                .build();
        var result = cut.isEqual(oldPassportTsDTO, newPassportTsDTO);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если id сотрудника или не все ключевые поля ПТС совпадают, то считаем документы разными")
    void isEqual_PassportTs_NotAllKeyFieldsPassportTsEqual_2() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var employeeId = UUID.randomUUID();
        PassportTsDTO oldPassportTsDTO = PassportTsDTO
                .builder()
                .employeeId(employeeId)
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .color("color")
                .passengerSeatsCount(4)
                .vin("vin")
                .engineVolume(1998)
                .enginePower("enginePower")

                .build();
        PassportTsDTO newPassportTsDTO = PassportTsDTO
                .builder()
                .employeeId(UUID.randomUUID())
                .fileName(oldPassportTsDTO.fileName())
                .fileSize(oldPassportTsDTO.fileSize())
                .fileFormat(oldPassportTsDTO.fileFormat())
                .color(oldPassportTsDTO.color())
                .passengerSeatsCount(oldPassportTsDTO.passengerSeatsCount())
                .vin(oldPassportTsDTO.vin())
                .engineVolume(oldPassportTsDTO.engineVolume())
                .enginePower(oldPassportTsDTO.enginePower())
                .build();
        var result = cut.isEqual(oldPassportTsDTO, newPassportTsDTO);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если id сотрудника или не все ключевые поля ПТС совпадают, то считаем документы разными")
    void isEqual_PassportTs_NotAllKeyFieldsPassportTsEqual_3() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var employeeId = UUID.randomUUID();
        PassportTsDTO oldPassportTsDTO = PassportTsDTO
                .builder()
                .employeeId(employeeId)
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .color("color")
                .passengerSeatsCount(4)
                .vin("vin")
                .engineVolume(1998)
                .enginePower("enginePower")

                .build();
        PassportTsDTO newPassportTsDTO = PassportTsDTO
                .builder()
                .employeeId(oldPassportTsDTO.employeeId())
                .fileName("newFileName3")
                .fileSize(oldPassportTsDTO.fileSize())
                .fileFormat(oldPassportTsDTO.fileFormat())
                .color(oldPassportTsDTO.color())
                .passengerSeatsCount(oldPassportTsDTO.passengerSeatsCount())
                .vin(oldPassportTsDTO.vin())
                .engineVolume(oldPassportTsDTO.engineVolume())
                .enginePower(oldPassportTsDTO.enginePower())
                .build();
        var result = cut.isEqual(oldPassportTsDTO, newPassportTsDTO);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если id сотрудника или не все ключевые поля ПТС совпадают, то считаем документы разными")
    void isEqual_PassportTs_NotAllKeyFieldsPassportTsEqual_4() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var employeeId = UUID.randomUUID();
        PassportTsDTO oldPassportTsDTO = PassportTsDTO
                .builder()
                .employeeId(employeeId)
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .color("color")
                .passengerSeatsCount(4)
                .vin("vin")
                .engineVolume(1998)
                .enginePower("enginePower")

                .build();
        PassportTsDTO newPassportTsDTO = PassportTsDTO
                .builder()
                .employeeId(oldPassportTsDTO.employeeId())
                .fileName(oldPassportTsDTO.fileName())
                .fileSize(oldPassportTsDTO.fileSize() + 4)
                .fileFormat(oldPassportTsDTO.fileFormat())
                .color(oldPassportTsDTO.color())
                .passengerSeatsCount(oldPassportTsDTO.passengerSeatsCount())
                .vin(oldPassportTsDTO.vin())
                .engineVolume(oldPassportTsDTO.engineVolume())
                .enginePower(oldPassportTsDTO.enginePower())
                .build();
        var result = cut.isEqual(oldPassportTsDTO, newPassportTsDTO);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если id сотрудника или не все ключевые поля ПТС совпадают, то считаем документы разными")
    void isEqual_PassportTs_NotAllKeyFieldsPassportTsEqual_5() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var employeeId = UUID.randomUUID();
        PassportTsDTO oldPassportTsDTO = PassportTsDTO
                .builder()
                .employeeId(employeeId)
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .color("color")
                .passengerSeatsCount(4)
                .vin("vin")
                .engineVolume(1998)
                .enginePower("enginePower")

                .build();
        PassportTsDTO newPassportTsDTO = PassportTsDTO
                .builder()
                .employeeId(oldPassportTsDTO.employeeId())
                .fileName(oldPassportTsDTO.fileName())
                .fileSize(oldPassportTsDTO.fileSize())
                .fileFormat("newFileFormat5")
                .color(oldPassportTsDTO.color())
                .passengerSeatsCount(oldPassportTsDTO.passengerSeatsCount())
                .vin(oldPassportTsDTO.vin())
                .engineVolume(oldPassportTsDTO.engineVolume())
                .enginePower(oldPassportTsDTO.enginePower())
                .build();
        var result = cut.isEqual(oldPassportTsDTO, newPassportTsDTO);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если id сотрудника или не все ключевые поля ПТС совпадают, то считаем документы разными")
    void isEqual_PassportTs_NotAllKeyFieldsPassportTsEqual_6() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var employeeId = UUID.randomUUID();
        PassportTsDTO oldPassportTsDTO = PassportTsDTO
                .builder()
                .employeeId(employeeId)
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .color("color")
                .passengerSeatsCount(4)
                .vin("vin")
                .engineVolume(1998)
                .enginePower("enginePower")

                .build();
        PassportTsDTO newPassportTsDTO = PassportTsDTO
                .builder()
                .employeeId(oldPassportTsDTO.employeeId())
                .fileName(oldPassportTsDTO.fileName())
                .fileSize(oldPassportTsDTO.fileSize())
                .fileFormat(oldPassportTsDTO.fileFormat())
                .color("newColor6")
                .passengerSeatsCount(oldPassportTsDTO.passengerSeatsCount())
                .vin(oldPassportTsDTO.vin())
                .engineVolume(oldPassportTsDTO.engineVolume())
                .enginePower(oldPassportTsDTO.enginePower())
                .build();
        var result = cut.isEqual(oldPassportTsDTO, newPassportTsDTO);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если id сотрудника или не все ключевые поля ПТС совпадают, то считаем документы разными")
    void isEqual_PassportTs_NotAllKeyFieldsPassportTsEqual_7() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var employeeId = UUID.randomUUID();
        PassportTsDTO oldPassportTsDTO = PassportTsDTO
                .builder()
                .employeeId(employeeId)
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .color("color")
                .passengerSeatsCount(4)
                .vin("vin")
                .engineVolume(1998)
                .enginePower("enginePower")

                .build();
        PassportTsDTO newPassportTsDTO = PassportTsDTO
                .builder()
                .employeeId(oldPassportTsDTO.employeeId())
                .fileName(oldPassportTsDTO.fileName())
                .fileSize(oldPassportTsDTO.fileSize())
                .fileFormat(oldPassportTsDTO.fileFormat())
                .color(oldPassportTsDTO.color())
                .passengerSeatsCount(oldPassportTsDTO.passengerSeatsCount() + 1)
                .vin(oldPassportTsDTO.vin())
                .engineVolume(oldPassportTsDTO.engineVolume())
                .enginePower(oldPassportTsDTO.enginePower())
                .build();
        var result = cut.isEqual(oldPassportTsDTO, newPassportTsDTO);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если id сотрудника или не все ключевые поля ПТС совпадают, то считаем документы разными")
    void isEqual_PassportTs_NotAllKeyFieldsPassportTsEqual_8() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var employeeId = UUID.randomUUID();
        PassportTsDTO oldPassportTsDTO = PassportTsDTO
                .builder()
                .employeeId(employeeId)
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .color("color")
                .passengerSeatsCount(4)
                .vin("vin")
                .engineVolume(1998)
                .enginePower("enginePower")

                .build();
        PassportTsDTO newPassportTsDTO = PassportTsDTO
                .builder()
                .employeeId(oldPassportTsDTO.employeeId())
                .fileName(oldPassportTsDTO.fileName())
                .fileSize(oldPassportTsDTO.fileSize())
                .fileFormat(oldPassportTsDTO.fileFormat())
                .color(oldPassportTsDTO.color())
                .passengerSeatsCount(oldPassportTsDTO.passengerSeatsCount())
                .vin("newVin8")
                .engineVolume(oldPassportTsDTO.engineVolume())
                .enginePower(oldPassportTsDTO.enginePower())
                .build();
        var result = cut.isEqual(oldPassportTsDTO, newPassportTsDTO);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если id сотрудника или не все ключевые поля ПТС совпадают, то считаем документы разными")
    void isEqual_PassportTs_NotAllKeyFieldsPassportTsEqual_9() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var employeeId = UUID.randomUUID();
        PassportTsDTO oldPassportTsDTO = PassportTsDTO
                .builder()
                .employeeId(employeeId)
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .color("color")
                .passengerSeatsCount(4)
                .vin("vin")
                .engineVolume(1998)
                .enginePower("enginePower")

                .build();
        PassportTsDTO newPassportTsDTO = PassportTsDTO
                .builder()
                .employeeId(oldPassportTsDTO.employeeId())
                .fileName(oldPassportTsDTO.fileName())
                .fileSize(oldPassportTsDTO.fileSize())
                .fileFormat(oldPassportTsDTO.fileFormat())
                .color(oldPassportTsDTO.color())
                .passengerSeatsCount(oldPassportTsDTO.passengerSeatsCount())
                .vin(oldPassportTsDTO.vin())
                .engineVolume(oldPassportTsDTO.engineVolume() + 9)
                .enginePower(oldPassportTsDTO.enginePower())
                .build();
        var result = cut.isEqual(oldPassportTsDTO, newPassportTsDTO);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если информации по старому варианту ОСАГО нет, то считаем, что документы отличается")
    void isEqual_Osago_oldOsagoIsNull() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var result = cut.isEqual(null, OsagoDTO.builder().build());

        assertFalse(result);
    }

    @Test
    @DisplayName("Если информации по новому варианту ОСАГО нет, то считаем, что документы отличается")
    void isEqual_Osago_newOsagoIsNull() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var result = cut.isEqual(OsagoDTO.builder().build(), null);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если id сотрудника и все ключевые поля ОСАГО совпадают, то считаем документы эквивалентными")
    void isEqual_Osago_AllKeyFieldsOsagoEqual() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        OsagoDTO oldOsago = OsagoDTO
                .builder()
                .employeeId(UUID.randomUUID())
                .carId(UUID.randomUUID())
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .seria("seria")
                .number("number")
                .startTimeDocument(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
                .finalTimeDocument(LocalDateTime.of(2034, 1, 29, 23, 59, 59))
                .registrationNumber("H123PE777")
                .build();
        OsagoDTO newOsago = OsagoDTO
                .builder()
                .employeeId(oldOsago.employeeId())
                .carId(oldOsago.carId())
                .fileName(oldOsago.fileName())
                .fileSize(oldOsago.fileSize())
                .fileFormat(oldOsago.fileFormat())
                .seria(oldOsago.seria())
                .number(oldOsago.number())
                .startTimeDocument(oldOsago.startTimeDocument())
                .finalTimeDocument(oldOsago.finalTimeDocument())
                .registrationNumber(oldOsago.registrationNumber())
                .build();
        var result = cut.isEqual(oldOsago, newOsago);

        assertTrue(result);
    }

    @Test
    @DisplayName("Если id сотрудника или не все ключевые поля ОСАГО совпадают, то считаем документы разными")
    void isEqual_Osago_NotAllKeyFieldsOsagoEqual_1() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        OsagoDTO oldOsago = OsagoDTO
                .builder()
                .employeeId(UUID.randomUUID())
                .carId(UUID.randomUUID())
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .seria("seria")
                .number("number")
                .startTimeDocument(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
                .finalTimeDocument(LocalDateTime.of(2034, 1, 29, 23, 59, 59))
                .registrationNumber("H123PE777")
                .build();
        OsagoDTO newOsago = OsagoDTO
                .builder()
                .employeeId(oldOsago.employeeId())
                .carId(oldOsago.carId())
                .fileName(oldOsago.fileName())
                .fileSize(oldOsago.fileSize() + 1) // здесь разница
                .fileFormat(oldOsago.fileFormat())
                .seria(oldOsago.seria())
                .number(oldOsago.number())
                .startTimeDocument(oldOsago.startTimeDocument())
                .finalTimeDocument(oldOsago.finalTimeDocument())
                .registrationNumber(oldOsago.registrationNumber())
                .build();
        var result = cut.isEqual(oldOsago, newOsago);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если id сотрудника или не все ключевые поля ОСАГО совпадают, то считаем документы разными")
    void isEqual_Osago_NotAllKeyFieldsOsagoEqual_2() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        OsagoDTO oldOsago = OsagoDTO
                .builder()
                .employeeId(UUID.randomUUID())
                .carId(UUID.randomUUID())
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .seria("seria")
                .number("number")
                .startTimeDocument(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
                .finalTimeDocument(LocalDateTime.of(2034, 1, 29, 23, 59, 59))
                .registrationNumber("H123PE777")
                .build();
        OsagoDTO newOsago = OsagoDTO
                .builder()
                .employeeId(UUID.randomUUID())
                .carId(oldOsago.carId())
                .fileName(oldOsago.fileName())
                .fileSize(oldOsago.fileSize()) // здесь разница
                .fileFormat(oldOsago.fileFormat())
                .seria(oldOsago.seria())
                .number(oldOsago.number())
                .startTimeDocument(oldOsago.startTimeDocument())
                .finalTimeDocument(oldOsago.finalTimeDocument())
                .registrationNumber(oldOsago.registrationNumber())
                .build();
        var result = cut.isEqual(oldOsago, newOsago);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если id сотрудника или не все ключевые поля ОСАГО совпадают, то считаем документы разными")
    void isEqual_Osago_NotAllKeyFieldsOsagoEqual_3() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        OsagoDTO oldOsago = OsagoDTO
                .builder()
                .employeeId(UUID.randomUUID())
                .carId(UUID.randomUUID())
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .seria("seria")
                .number("number")
                .startTimeDocument(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
                .finalTimeDocument(LocalDateTime.of(2034, 1, 29, 23, 59, 59))
                .registrationNumber("H123PE777")
                .build();
        OsagoDTO newOsago = OsagoDTO
                .builder()
                .employeeId(oldOsago.employeeId())
                .carId(oldOsago.carId())
                .fileName("newFileName3")
                .fileSize(oldOsago.fileSize()) // здесь разница
                .fileFormat(oldOsago.fileFormat())
                .seria(oldOsago.seria())
                .number(oldOsago.number())
                .startTimeDocument(oldOsago.startTimeDocument())
                .finalTimeDocument(oldOsago.finalTimeDocument())
                .registrationNumber(oldOsago.registrationNumber())
                .build();
        var result = cut.isEqual(oldOsago, newOsago);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если id сотрудника или не все ключевые поля ОСАГО совпадают, то считаем документы разными")
    void isEqual_Osago_NotAllKeyFieldsOsagoEqual_4() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        OsagoDTO oldOsago = OsagoDTO
                .builder()
                .employeeId(UUID.randomUUID())
                .carId(UUID.randomUUID())
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .seria("seria")
                .number("number")
                .startTimeDocument(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
                .finalTimeDocument(LocalDateTime.of(2034, 1, 29, 23, 59, 59))
                .registrationNumber("H123PE777")
                .build();
        OsagoDTO newOsago = OsagoDTO
                .builder()
                .employeeId(oldOsago.employeeId())
                .carId(oldOsago.carId())
                .fileName(oldOsago.fileName())
                .fileSize(oldOsago.fileSize()) // здесь разница
                .fileFormat("newFileFormat4")
                .seria(oldOsago.seria())
                .number(oldOsago.number())
                .startTimeDocument(oldOsago.startTimeDocument())
                .finalTimeDocument(oldOsago.finalTimeDocument())
                .registrationNumber(oldOsago.registrationNumber())
                .build();
        var result = cut.isEqual(oldOsago, newOsago);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если id сотрудника или не все ключевые поля ОСАГО совпадают, то считаем документы разными")
    void isEqual_Osago_NotAllKeyFieldsOsagoEqual_5() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        OsagoDTO oldOsago = OsagoDTO
                .builder()
                .employeeId(UUID.randomUUID())
                .carId(UUID.randomUUID())
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .seria("seria")
                .number("number")
                .startTimeDocument(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
                .finalTimeDocument(LocalDateTime.of(2034, 1, 29, 23, 59, 59))
                .registrationNumber("H123PE777")
                .build();
        OsagoDTO newOsago = OsagoDTO
                .builder()
                .employeeId(oldOsago.employeeId())
                .carId(oldOsago.carId())
                .fileName(oldOsago.fileName())
                .fileSize(oldOsago.fileSize()) // здесь разница
                .fileFormat(oldOsago.fileFormat())
                .seria("newSeria5")
                .number(oldOsago.number())
                .startTimeDocument(oldOsago.startTimeDocument())
                .finalTimeDocument(oldOsago.finalTimeDocument())
                .registrationNumber(oldOsago.registrationNumber())
                .build();
        var result = cut.isEqual(oldOsago, newOsago);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если id сотрудника или не все ключевые поля ОСАГО совпадают, то считаем документы разными")
    void isEqual_Osago_NotAllKeyFieldsOsagoEqual_6() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        OsagoDTO oldOsago = OsagoDTO
                .builder()
                .employeeId(UUID.randomUUID())
                .carId(UUID.randomUUID())
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .seria("seria")
                .number("number")
                .startTimeDocument(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
                .finalTimeDocument(LocalDateTime.of(2034, 1, 29, 23, 59, 59))
                .registrationNumber("H123PE777")
                .build();
        OsagoDTO newOsago = OsagoDTO
                .builder()
                .employeeId(oldOsago.employeeId())
                .carId(oldOsago.carId())
                .fileName(oldOsago.fileName())
                .fileSize(oldOsago.fileSize()) // здесь разница
                .fileFormat(oldOsago.fileFormat())
                .seria(oldOsago.seria())
                .number("newNumber6")
                .startTimeDocument(oldOsago.startTimeDocument())
                .finalTimeDocument(oldOsago.finalTimeDocument())
                .registrationNumber(oldOsago.registrationNumber())
                .build();
        var result = cut.isEqual(oldOsago, newOsago);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если id сотрудника или не все ключевые поля ОСАГО совпадают, то считаем документы разными")
    void isEqual_Osago_NotAllKeyFieldsOsagoEqual_7() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        OsagoDTO oldOsago = OsagoDTO
                .builder()
                .employeeId(UUID.randomUUID())
                .carId(UUID.randomUUID())
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .seria("seria")
                .number("number")
                .startTimeDocument(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
                .finalTimeDocument(LocalDateTime.of(2034, 1, 29, 23, 59, 59))
                .registrationNumber("H123PE777")
                .build();
        OsagoDTO newOsago = OsagoDTO
                .builder()
                .employeeId(oldOsago.employeeId())
                .carId(oldOsago.carId())
                .fileName(oldOsago.fileName())
                .fileSize(oldOsago.fileSize()) // здесь разница
                .fileFormat(oldOsago.fileFormat())
                .seria(oldOsago.seria())
                .number(oldOsago.number())
                .startTimeDocument(oldOsago.startTimeDocument().minusDays(7))
                .finalTimeDocument(oldOsago.finalTimeDocument())
                .registrationNumber(oldOsago.registrationNumber())
                .build();
        var result = cut.isEqual(oldOsago, newOsago);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если id сотрудника или не все ключевые поля ОСАГО совпадают, то считаем документы разными")
    void isEqual_Osago_NotAllKeyFieldsOsagoEqual_8() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        OsagoDTO oldOsago = OsagoDTO
                .builder()
                .employeeId(UUID.randomUUID())
                .carId(UUID.randomUUID())
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .seria("seria")
                .number("number")
                .startTimeDocument(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
                .finalTimeDocument(LocalDateTime.of(2034, 1, 29, 23, 59, 59))
                .registrationNumber("H123PE777")
                .build();
        OsagoDTO newOsago = OsagoDTO
                .builder()
                .employeeId(oldOsago.employeeId())
                .carId(oldOsago.carId())
                .fileName(oldOsago.fileName())
                .fileSize(oldOsago.fileSize()) // здесь разница
                .fileFormat(oldOsago.fileFormat())
                .seria(oldOsago.seria())
                .number(oldOsago.number())
                .startTimeDocument(oldOsago.startTimeDocument())
                .finalTimeDocument(oldOsago.finalTimeDocument().plusDays(8))
                .registrationNumber(oldOsago.registrationNumber())
                .build();
        var result = cut.isEqual(oldOsago, newOsago);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если id сотрудника или не все ключевые поля ОСАГО совпадают, то считаем документы разными")
    void isEqual_Osago_NotAllKeyFieldsOsagoEqual_9() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        OsagoDTO oldOsago = OsagoDTO
                .builder()
                .employeeId(UUID.randomUUID())
                .carId(UUID.randomUUID())
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .seria("seria")
                .number("number")
                .startTimeDocument(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
                .finalTimeDocument(LocalDateTime.of(2034, 1, 29, 23, 59, 59))
                .registrationNumber("H123PE777")
                .build();
        OsagoDTO newOsago = OsagoDTO
                .builder()
                .employeeId(oldOsago.employeeId())
                .carId(oldOsago.carId())
                .fileName(oldOsago.fileName())
                .fileSize(oldOsago.fileSize()) // здесь разница
                .fileFormat(oldOsago.fileFormat())
                .seria(oldOsago.seria())
                .number(oldOsago.number())
                .startTimeDocument(oldOsago.startTimeDocument())
                .finalTimeDocument(oldOsago.finalTimeDocument())
                .registrationNumber("newRegistrationNumber9")
                .build();
        var result = cut.isEqual(oldOsago, newOsago);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если id сотрудника или не все ключевые поля ОСАГО совпадают, то считаем документы разными")
    void isEqual_Osago_NotAllKeyFieldsOsagoEqual_10() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        OsagoDTO oldOsago = OsagoDTO
                .builder()
                .employeeId(UUID.randomUUID())
                .carId(UUID.randomUUID())
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .seria("seria")
                .number("number")
                .startTimeDocument(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
                .finalTimeDocument(LocalDateTime.of(2034, 1, 29, 23, 59, 59))
                .registrationNumber("H123PE777")
                .build();
        OsagoDTO newOsago = OsagoDTO
                .builder()
                .employeeId(UUID.randomUUID())
                .carId(oldOsago.carId())
                .fileName(oldOsago.fileName())
                .fileSize(oldOsago.fileSize()) // здесь разница
                .fileFormat(oldOsago.fileFormat())
                .seria(oldOsago.seria())
                .number(oldOsago.number())
                .startTimeDocument(oldOsago.startTimeDocument())
                .finalTimeDocument(oldOsago.finalTimeDocument())
                .registrationNumber(oldOsago.registrationNumber())
                .build();
        var result = cut.isEqual(oldOsago, newOsago);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если информации по старому варианту свидетельства о браке нет, то считаем, что документы отличается")
    void isEqual_oldMarriageCertificateIsNull() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var result = cut.isEqual(null, MarriageCertificateDTO.builder().build());

        assertFalse(result);
    }

    @Test
    @DisplayName("Если информации по старому варианту свидетельства о браке нет, то считаем, что документы отличается")
    void isEqual_newMarriageCertificateIsNull() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var result = cut.isEqual(MarriageCertificateDTO.builder().build(), null);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если id сотрудника и все ключевые поля свидетельства о браке совпадают, то считаем документы эквивалентными")
    void isEqual_AllKeyFieldsMarriageCertificateEqual() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var oldMarriageCertificateDTO = MarriageCertificateDTO
                .builder()
                .employeeId(UUID.randomUUID())
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .seria("seria")
                .number("number")
                .issueDateDocument(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
                .build();
        var newMarriageCertificateDTO = MarriageCertificateDTO
                .builder()
                .employeeId(oldMarriageCertificateDTO.employeeId())
                .fileName(oldMarriageCertificateDTO.fileName())
                .fileSize(oldMarriageCertificateDTO.fileSize())
                .fileFormat(oldMarriageCertificateDTO.fileFormat())
                .seria(oldMarriageCertificateDTO.seria())
                .number(oldMarriageCertificateDTO.number())
                .issueDateDocument(oldMarriageCertificateDTO.issueDateDocument())
                .build();
        var result = cut.isEqual(oldMarriageCertificateDTO, newMarriageCertificateDTO);

        assertTrue(result);
    }

    @Test
    @DisplayName("Если id сотрудника или не все ключевые поля свидетельства о браке совпадают, то считаем документы разными")
    void isEqual_NotAllKeyFieldsMarriageCertificateEqual() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var oldMarriageCertificateDTO = MarriageCertificateDTO
                .builder()
                .employeeId(UUID.randomUUID())
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .seria("seria")
                .number("number")
                .issueDateDocument(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
                .build();
        var newMarriageCertificateDTO = MarriageCertificateDTO
                .builder()
                .employeeId(oldMarriageCertificateDTO.employeeId())
                .fileName(oldMarriageCertificateDTO.fileName())
                .fileSize(oldMarriageCertificateDTO.fileSize() + 1)
                .fileFormat(oldMarriageCertificateDTO.fileFormat())
                .seria(oldMarriageCertificateDTO.seria())
                .number(oldMarriageCertificateDTO.number())
                .issueDateDocument(oldMarriageCertificateDTO.issueDateDocument())
                .build();
        var result = cut.isEqual(oldMarriageCertificateDTO, newMarriageCertificateDTO);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если информации по старому варианту в/у нет, то считаем, что документы отличается")
    void isEqual_DriverLic_oldDriverLicIsNull() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var result = cut.isEqual(null, DriverLicDTO.builder().build());

        assertFalse(result);
    }

    @Test
    @DisplayName("Если информации по новому варианту в/у нет, то считаем, что документы отличается")
    void isEqual_DriverLic_newDriverLicIsNull() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        var result = cut.isEqual(DriverLicDTO.builder().build(), null);

        assertFalse(result);
    }

    @Test
    @DisplayName("Если id сотрудника и все ключевые поля в/у совпадают, то считаем документы эквивалентными")
    void isEqual_DriverLic_AllKeyFieldsDriverLicEqual() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        DriverLicDTO oldDriverLicDTO = DriverLicDTO
                .builder()
                .employeeId(UUID.randomUUID())
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .seria("seria")
                .number("number")
                .issueDateDocument(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
                .issue("issue")
                .placeIssue("placeIssue")
                .finalTimeDocument(LocalDateTime.of(2034, 1, 29, 23, 59, 59))
                .categoria("categoria")
                .build();
        DriverLicDTO newDriverLicDTO = DriverLicDTO
                .builder()
                .employeeId(oldDriverLicDTO.employeeId())
                .fileName(oldDriverLicDTO.fileName())
                .fileSize(oldDriverLicDTO.fileSize())
                .fileFormat(oldDriverLicDTO.fileFormat())
                .seria(oldDriverLicDTO.seria())
                .number(oldDriverLicDTO.number())
                .issueDateDocument(oldDriverLicDTO.issueDateDocument())
                .issue(oldDriverLicDTO.issue())
                .placeIssue(oldDriverLicDTO.placeIssue())
                .finalTimeDocument(oldDriverLicDTO.finalTimeDocument())
                .categoria(oldDriverLicDTO.categoria())
                .build();
        var result = cut.isEqual(oldDriverLicDTO, newDriverLicDTO);

        assertTrue(result);
    }

    @Test
    @DisplayName("Если id сотрудника или не все ключевые поля в/у совпадают, то считаем документы разными")
    void isEqual_DriverLic_NotAllKeyFieldsDriverLicEqual() {
        var cut = new PersonalCarsServiceImpl(null, null, null, null, null, null);
        DriverLicDTO oldDriverLicDTO = DriverLicDTO
                .builder()
                .employeeId(UUID.randomUUID())
                .fileName("fileName")
                .fileSize(12345)
                .fileFormat("fileFormat")
                .seria("seria")
                .number("number")
                .issueDateDocument(LocalDateTime.of(2024, 1, 30, 0, 0, 0))
                .issue("issue")
                .placeIssue("placeIssue")
                .finalTimeDocument(LocalDateTime.of(2034, 1, 29, 23, 59, 59))
                .categoria("categoria")
                .build();
        DriverLicDTO newDriverLicDTO = DriverLicDTO
                .builder()
                .employeeId(oldDriverLicDTO.employeeId())
                .fileName(oldDriverLicDTO.fileName())
                .fileSize(oldDriverLicDTO.fileSize() + 1)
                .fileFormat(oldDriverLicDTO.fileFormat())
                .seria(oldDriverLicDTO.seria())
                .number(oldDriverLicDTO.number())
                .issueDateDocument(oldDriverLicDTO.issueDateDocument())
                .issue(oldDriverLicDTO.issue())
                .placeIssue(oldDriverLicDTO.placeIssue())
                .finalTimeDocument(oldDriverLicDTO.finalTimeDocument())
                .categoria(oldDriverLicDTO.categoria())
                .build();
        var result = cut.isEqual(oldDriverLicDTO, newDriverLicDTO);

        assertFalse(result);
    }
}