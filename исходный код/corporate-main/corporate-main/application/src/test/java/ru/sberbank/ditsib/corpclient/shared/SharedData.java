package ru.sberbank.ditsib.corpclient.shared;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.corpclient.database.model.*;
import ru.sberbank.ditsib.corpclient.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.constants.PersonalCarOwnerInfo;
import ru.sberbank.ditsib.transport.constants.PersonalTransportType;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
public abstract class SharedData {

    //Данные признаков сотрудника
    public static final String ATTRIBUTE_NAME1 = "Первый признак сотрудника";
    public static final String ATTRIBUTE_NAME2 = "Второй признак сотрудника";
    public static final String ATTRIBUTE_NAME3 = "Третий признак сотрудника";

    //Данные для теста организаций
    public static final String ORGANIZATION_OFFICIAL_NAME1 = "ПАО Сбербанк";
    public static final String ORGANIZATION_ADDRESS1 = "Россия, Москва, 117997, ул. Вавилова, 19";
    public static final String ORGANIZATION_OFFICIAL_NAME2 = "Северо-Западный банк Сбербанка";
    public static final String ORGANIZATION_ADDRESS2 = "Россия, Санкт-Петербург, 191124, улица Красного текстильщика " +
            "д.2";
    public static final String ORGANIZATION_OFFICIAL_NAME_SHARED_PART_LIKE = "%Сбербанк%";
    public static final String ORGANIZATION2_OFFICIAL_NAME_PART_LIKE = "%северо%";
    public static final int ORGANIZATION_CODE1 = 123;
    public static final int ORGANIZATION_CODE2 = 333;

    //Данные для теста должностей
    public static final String POSITION1_NAME = "Клерк";
    public static final String POSITION2_NAME = "Босс";
    public static final String POSITION3_NAME = "Босс орг-ции 2";
    public static final String POSITION_NAME_LONG = "Длинное название длинное название длинное название больше 20и " +
            "символов";

    //Данные для теста подразделений
    public static final String DEPARTMENT1_NAME = "Департамент бизнеса";
    public static final String DEPARTMENT1_CODE = "123456";
    public static final String DEPARTMENT1_LOCATION = "Санкт-Петербург";

    public static final String DEPARTMENT2_NAME = "Департамент информационных технологий";
    public static final String DEPARTMENT2_CODE = "654321";
    public static final String DEPARTMENT2_LOCATION = "Москва";

    public static final String DEPARTMENT3_NAME = "Департамент молочных технологий";
    public static final String DEPARTMENT3_CODE = "654322";
    public static final String DEPARTMENT3_LOCATION = "Простоквашино";

    public static final String DEPARTMENT4_NAME = "Департамент мясных технологий";
    public static final String DEPARTMENT4_CODE = "654344";
    public static final String DEPARTMENT4_LOCATION = "Воронеж";

    public static final String DEPARTMENT5_NAME = "Департамент клубничных технологий";
    public static final String DEPARTMENT5_CODE = "654355";
    public static final String DEPARTMENT5_LOCATION = "Мариуполь";

    //Данные для теста личных автомобилей
    public static final String AUTO_BRAND_1 = "Tesla";
    public static final String AUTO_BRAND_2 = "SUZUKI";
    public static final PersonalTransportType AUTO_TRANSPORT_TYPE_2 = PersonalTransportType.MOTORCYCLE;
    public static final String AUTO_MODEL_NAME_1 = "Model S";
    public static final String AUTO_MODEL_NAME_2 = "Bandit";
    public static final String AUTO_MODEL_COLOR_1 = "Серый";
    public static final String AUTO_MODEL_COLOR_2 = "Синий";
    public static final short AUTO_SEATS_1 = 1;
    public static final String AUTO_REG_NUMBER_1 = "с065вв78";
    public static final String AUTO_REG_NUMBER_2 = "с0766вв78";
    public static final String CAR_REG_NUMBER_2 = "с0765вв78";
    public static final String AUTO_REG_CERT_1 = "11 АА 112233";
    public static final String AUTO_REG_CERT_2 = "22 АА 112233";
    public static final int AUTO_ENGINE_VOLUME1 = 2000;
    public static final String AUTO_INSURANCE_1 = "ХХХ 0044620411";
    public static final String CAR_INSURANCE_2 = "FFF 0044620411";
    public static final PersonalCarOwnerInfo AUTO_OWNER_1 = PersonalCarOwnerInfo.USER;
    public static final PersonalCarOwnerInfo AUTO_OWNER_2 = PersonalCarOwnerInfo.SPOUSE;

    //Данные для теста сотрудников
    public static final String EMPLOYEE_NAME1 = "Петр";
    public static final String EMPLOYEE_NAME2 = "BBB";
    public static final String EMPLOYEE_NAME3 = "Петр1";
    public static final String EMPLOYEE_NAME4 = "BBB1";
    public static final String EMPLOYEE_NAME5 = "Вася";
    public static final String EMPLOYEE_LASTNAME1 = "Семенов";
    public static final String EMPLOYEE_LASTNAME2 = "DDD";
    public static final String EMPLOYEE_LASTNAME3 = "Семеновв";
    public static final String EMPLOYEE_LASTNAME4 = "DDDD";
    public static final String EMPLOYEE_LASTNAME5 = "Васин";
    public static final String EMPLOYEE_PATRONYMIC1 = "Иванови";
    public static final String EMPLOYEE_PATRONYMIC2 = "FFF";
    public static final String EMPLOYEE_PATRONYMIC3 = "Ивановичч";
    public static final String EMPLOYEE_PATRONYMIC4 = "FFFF";
    public static final String EMPLOYEE_EMAIL1 = "AAA@AAA.COM";
    public static final String EMPLOYEE_EMAIL2 = "BBB@BBB.COM";
    public static final String EMPLOYEE_EMAIL3 = "CCC@AAA.COM";
    public static final String EMPLOYEE_EMAIL4 = "DDD@BBB.COM";
    public static final String EMPLOYEE_EMAIL5 = "XXX@BBB.COM";
    public static final String EMPLOYEE_PHONE1 = "+71234567800";
    public static final String EMPLOYEE_PHONE2 = "+72345678900";
    public static final String EMPLOYEE_PHONE3 = "+74234567800";
    public static final String EMPLOYEE_PHONE4 = "+75345678900";
    public static final String EMPLOYEE_PHONE5 = "+75388678900";
    public static final String PERSONNEL_NUMBER_1 = "0987654";
    public static final String PERSONNEL_NUMBER_2 = "9876543";
    public static final String PERSONNEL_NUMBER_3 = "1987654";
    public static final String PERSONNEL_NUMBER_4 = "2876543";
    public static final String PERSONNEL_NUMBER_5 = "2878888";
    public static final String EMPLOYEE_ATTRIBUTES_2 = "remote";

    public static final String DEPARTMENT_HUMANREADABLEID_1 = "US-0001-11";
    public static final String DEPARTMENT_HUMANREADABLEID_2 = "US-0002-22";
    public static final String DEPARTMENT_HUMANREADABLEID_3 = "US-0001-23";
    public static final String DEPARTMENT_HUMANREADABLEID_4 = "US-0001-44";
    public static final String DEPARTMENT_HUMANREADABLEID_5 = "US-0001-55";

    public static final String USER1_ID = "f10bcc5b-51db-4e1c-a747-2a229604f974";
    public static final String USER2_ID = "f10bcc5b-51db-4e1c-a747-2a229604f975";
    public static final String USER5_ID = "aa0bcc5b-51db-4e1c-a747-2a229604f975";

    //Данне для гео зон
    public static final String GEOZONE_NAME1 = "Москва";
    public static final String GEOZONE_NAME2 = "Владивосток";
    public static final String GEOZONE_CODE1 = "123456";
    public static final String GEOZONE_CODE2 = "234567";

    protected Attribute attribute1;
    protected Attribute attribute2;
    protected Attribute attribute3;

    protected Organization testOrganization1;
    protected Organization testOrganization2;

    protected Position testPosition1;
    protected Position testPosition2;
    protected Position testPosition3;
    protected Position testPositionLongName;

    protected Department testDepartment1;
    protected Department testDepartment2;
    protected Department testDepartment3;
    protected Department testDepartment4;
    protected Department testDepartment5;

    protected PersonalCar testPersonalCar1;
    protected PersonalCar testPersonalCar2;
    protected PersonalCar testPersonalCar3;
    protected PersonalCar testPersonalCar4;

    protected Employee testEmployee1;
    protected Employee testEmployee2;
    protected Employee testEmployee3;
    protected Employee testEmployee4;
    protected Employee testEmployee5;

    protected GeoZone testGeoZone1;
    protected GeoZone testGeoZone2;

    protected UUID contractorId1 = UUID.randomUUID();
    protected UUID contractorId2 = UUID.randomUUID();
    protected UUID contractorId3 = UUID.randomUUID();


    @BeforeEach
    public void createEntities() {
        testOrganization1 = new Organization();
        testOrganization1.setAddress(ORGANIZATION_ADDRESS1);
        testOrganization1.setOfficialName(ORGANIZATION_OFFICIAL_NAME1);
        testOrganization1.setOrganizationCode(ORGANIZATION_CODE1);
        testOrganization1.setMsrn(Instancio.create(String.class));
        testOrganization1.setTid(Instancio.create(String.class));

        attribute1 = new Attribute();
        attribute1.setName(ATTRIBUTE_NAME1);
        attribute2 = new Attribute();
        attribute2.setName(ATTRIBUTE_NAME2);
        attribute3 = new Attribute();
        attribute3.setName(ATTRIBUTE_NAME3);
        attribute3.setStatus(AttributeStatus.INACTIVE);

        testOrganization2 = new Organization();
        testOrganization2.setAddress(ORGANIZATION_ADDRESS2);
        testOrganization2.setOfficialName(ORGANIZATION_OFFICIAL_NAME2);
        testOrganization2.setMsrn(Instancio.create(String.class));
        testOrganization2.setTid(Instancio.create(String.class));
        testOrganization1.setOrganizationCode(ORGANIZATION_CODE2);

        testPosition1 = new Position();
        testPosition1.getAvailableClasses().add(TaxiClass.ECONOMY);
        testPosition1.setName(POSITION1_NAME);
        testPosition1.setHumanReadableId("US-022-22");
        testPosition1.setSelfApproved(false);

        testPosition2 = new Position();
        testPosition2.getAvailableClasses().add(TaxiClass.ECONOMY);
        testPosition2.getAvailableClasses().add(TaxiClass.BUSINESS);
        testPosition2.setName(POSITION2_NAME);
        testPosition2.setHumanReadableId("US-022-23");
        testPosition2.setSelfApproved(true);

        testPosition3 = new Position();
        testPosition3.getAvailableClasses().add(TaxiClass.ECONOMY);
        testPosition3.getAvailableClasses().add(TaxiClass.BUSINESS);
        testPosition3.setName(POSITION3_NAME);
        testPosition3.setSelfApproved(true);

        testPositionLongName = new Position();
        testPositionLongName.setOrganization(testOrganization1);
        testPositionLongName.getAvailableClasses().add(TaxiClass.ECONOMY);
        testPositionLongName.getAvailableClasses().add(TaxiClass.BUSINESS);
        testPositionLongName.setName(POSITION_NAME_LONG);
        testPositionLongName.setSelfApproved(true);

        testDepartment1 = new Department();
        testDepartment1.setName(DEPARTMENT1_NAME);
        testDepartment1.setCode(DEPARTMENT1_CODE);
        testDepartment1.setLocation(DEPARTMENT1_LOCATION);
        testDepartment1.setHumanReadableId(DEPARTMENT_HUMANREADABLEID_1);
        testDepartment1.setUpdateTime(OffsetDateTime.now());

        testDepartment2 = new Department();
        testDepartment2.setName(DEPARTMENT2_NAME);
        testDepartment2.setCode(DEPARTMENT2_CODE);
        testDepartment2.setLocation(DEPARTMENT2_LOCATION);
        testDepartment2.setHumanReadableId(DEPARTMENT_HUMANREADABLEID_2);
        testDepartment2.setUpdateTime(OffsetDateTime.now());

        testDepartment3 = new Department();
        testDepartment3.setName(DEPARTMENT3_NAME);
        testDepartment3.setCode(DEPARTMENT3_CODE);
        testDepartment3.setLocation(DEPARTMENT3_LOCATION);
        testDepartment3.setHumanReadableId(DEPARTMENT_HUMANREADABLEID_3);
        testDepartment3.setUpdateTime(OffsetDateTime.now());

        testDepartment4 = new Department();
        testDepartment4.setName(DEPARTMENT4_NAME);
        testDepartment4.setCode(DEPARTMENT4_CODE);
        testDepartment4.setLocation(DEPARTMENT4_LOCATION);
        testDepartment4.setHumanReadableId(DEPARTMENT_HUMANREADABLEID_4);
        testDepartment4.setUpdateTime(OffsetDateTime.now());

        testDepartment5 = new Department();
        testDepartment5.setName(DEPARTMENT5_NAME);
        testDepartment5.setCode(DEPARTMENT5_CODE);
        testDepartment5.setLocation(DEPARTMENT5_LOCATION);
        testDepartment5.setHumanReadableId(DEPARTMENT_HUMANREADABLEID_5);
        testDepartment5.setUpdateTime(OffsetDateTime.now());

        testEmployee1 = new Employee();
        testEmployee1.setId(UUID.fromString(USER1_ID));
        testEmployee1.setNew(true);
        testEmployee1.setUserId(testEmployee1.getId());
        testEmployee1.setFirstName(EMPLOYEE_NAME1);
        testEmployee1.setLastName(EMPLOYEE_LASTNAME1);
        testEmployee1.setPatronymic(EMPLOYEE_PATRONYMIC1);
        testEmployee1.setEmail(EMPLOYEE_EMAIL1);
        testEmployee1.setDepartment(testDepartment1);
        testEmployee1.setPosition(testPosition1);
        testEmployee1.setOrganization(testDepartment1.getOrganization());
        testEmployee1.getAvailableTransportTypes().add(TransportTypeEnum.TAXI);
        testEmployee1.getAvailableTransportTypes().add(TransportTypeEnum.PERSONAL);
        testEmployee1.setMobilePhone(EMPLOYEE_PHONE1);
        testEmployee1.setPersonnelNumber(PERSONNEL_NUMBER_1);
        testEmployee1.setHumanReadableId("EMPLOYEE_HUMANREADABLEID1");
        testEmployee1.setUpdateTime(OffsetDateTime.now());

        testEmployee2 = new Employee();
        testEmployee2.setId(UUID.fromString(USER2_ID));
        testEmployee2.setNew(true);
        testEmployee2.setUserId(testEmployee2.getId());
        testEmployee2.setFirstName(EMPLOYEE_NAME2);
        testEmployee2.setLastName(EMPLOYEE_LASTNAME2);
        testEmployee2.setOrganization(testDepartment2.getOrganization());
        testEmployee2.setPatronymic(EMPLOYEE_PATRONYMIC2);
        testEmployee2.setEmail(EMPLOYEE_EMAIL2);
        testEmployee2.setDepartment(testDepartment2);
        testEmployee2.setPosition(testPosition2);
        testEmployee2.getAvailableTransportTypes().add(TransportTypeEnum.PERSONAL);
        testEmployee2.setMobilePhone(EMPLOYEE_PHONE2);
        testEmployee2.setPersonnelNumber(PERSONNEL_NUMBER_2);
        testEmployee2.setSupervisor(testEmployee1);
        testEmployee2.setHumanReadableId("EMPLOYEE_HUMANREADABLEID2");
        testEmployee2.setUpdateTime(OffsetDateTime.now());


        testEmployee3 = new Employee();
        testEmployee3.setId(UUID.randomUUID());
        testEmployee3.setNew(true);
        testEmployee3.setUserId(testEmployee3.getId());
        testEmployee3.setFirstName(EMPLOYEE_NAME3);
        testEmployee3.setLastName(EMPLOYEE_LASTNAME3);
        testEmployee3.setPatronymic(EMPLOYEE_PATRONYMIC3);
        testEmployee3.setEmail(EMPLOYEE_EMAIL3);
        testEmployee3.setDepartment(testDepartment2);
        testEmployee3.setOrganization(testDepartment2.getOrganization());
        testEmployee3.setPosition(testPosition2);
        testEmployee3.getAvailableTransportTypes().add(TransportTypeEnum.TAXI);
        testEmployee3.getAvailableTransportTypes().add(TransportTypeEnum.PERSONAL);
        testEmployee3.setMobilePhone(EMPLOYEE_PHONE3);
        testEmployee3.setPersonnelNumber(PERSONNEL_NUMBER_3);
        testEmployee3.setHumanReadableId("EMPLOYEE_HUMANREADABLEID3");
        testEmployee3.setUpdateTime(OffsetDateTime.now());

        testEmployee4 = new Employee();
        testEmployee4.setId(UUID.randomUUID());
        testEmployee4.setNew(true);
        testEmployee4.setUserId(testEmployee4.getId());
        testEmployee4.setFirstName(EMPLOYEE_NAME4);
        testEmployee4.setLastName(EMPLOYEE_LASTNAME4);
        testEmployee4.setPatronymic(EMPLOYEE_PATRONYMIC4);
        testEmployee4.setEmail(EMPLOYEE_EMAIL4);
        testEmployee4.setDepartment(testDepartment2);
        testEmployee4.setOrganization(testDepartment2.getOrganization());
        testEmployee4.setPosition(testPosition2);
        testEmployee4.getAvailableTransportTypes().add(TransportTypeEnum.TAXI);
        testEmployee4.getAvailableTransportTypes().add(TransportTypeEnum.PERSONAL);
        testEmployee4.setMobilePhone(EMPLOYEE_PHONE4);
        testEmployee4.setPersonnelNumber(PERSONNEL_NUMBER_4);
        testEmployee4.setSupervisor(testEmployee3);
        testEmployee4.setHumanReadableId("EMPLOYEE_HUMANREADABLEID4");
        testEmployee4.setUpdateTime(OffsetDateTime.now());

        // Без отчества
        testEmployee5 = new Employee();
        testEmployee5.setId(UUID.fromString(USER5_ID));
        testEmployee5.setNew(true);
        testEmployee5.setUserId(testEmployee5.getId());
        testEmployee5.setFirstName(EMPLOYEE_NAME5);
        testEmployee5.setLastName(EMPLOYEE_LASTNAME5);
        testEmployee5.setEmail(EMPLOYEE_EMAIL5);
        testEmployee5.setDepartment(testDepartment1);
        testEmployee5.setPosition(testPosition1);
        testEmployee5.setOrganization(testDepartment1.getOrganization());
        testEmployee5.getAvailableTransportTypes().add(TransportTypeEnum.TAXI);
        testEmployee5.getAvailableTransportTypes().add(TransportTypeEnum.PERSONAL);
        testEmployee5.setMobilePhone(EMPLOYEE_PHONE5);
        testEmployee5.setPersonnelNumber(PERSONNEL_NUMBER_5);
        testEmployee5.setHumanReadableId("EMPLOYEE_HUMANREADABLEID5");
        testEmployee5.setUpdateTime(OffsetDateTime.now());


        testPersonalCar1 = new PersonalCar();
        testPersonalCar1.setBrandName(AUTO_BRAND_1);
        testPersonalCar1.setModel(AUTO_MODEL_NAME_1);
        testPersonalCar1.setColor(AUTO_MODEL_COLOR_1);
        testPersonalCar1.setPassengerSeatsCount(AUTO_SEATS_1);
        testPersonalCar1.setEngineVolume(AUTO_ENGINE_VOLUME1);
        testPersonalCar1.setInsuranceNumber(AUTO_INSURANCE_1);
        testPersonalCar1.setOwnerInfo(AUTO_OWNER_1);
        testPersonalCar1.setRegistrationNumber(AUTO_REG_NUMBER_1);
        testPersonalCar1.setRegistrationCertificate(AUTO_REG_CERT_1);
        testPersonalCar1.setEmployee(testEmployee1);
        testPersonalCar1.setPersDataAccept("eae55a76e6f9a7fd9c7670da6e9eb7c1");


        testPersonalCar2 = new PersonalCar();
        testPersonalCar2.setColor(AUTO_MODEL_COLOR_2);
        testPersonalCar2.setTransportType(AUTO_TRANSPORT_TYPE_2);
        testPersonalCar2.setBrandName(AUTO_BRAND_2);
        testPersonalCar2.setModel(AUTO_MODEL_NAME_2);
        testPersonalCar2.setInsuranceNumber(CAR_INSURANCE_2);
        testPersonalCar2.setOwnerInfo(AUTO_OWNER_2);
        testPersonalCar2.setRegistrationNumber(CAR_REG_NUMBER_2);
        testPersonalCar2.setRegistrationCertificate(AUTO_REG_CERT_2);
        testPersonalCar2.setEmployee(testEmployee1);
        testPersonalCar1.setPersDataAccept("eae55a76e6f9a7fd9c7670da6e9eb7c1");

        testPersonalCar3 = new PersonalCar();
        testPersonalCar3.setBrandName(AUTO_BRAND_1);
        testPersonalCar3.setModel(AUTO_MODEL_NAME_1);
        testPersonalCar3.setColor(AUTO_MODEL_COLOR_1);
        testPersonalCar3.setPassengerSeatsCount(AUTO_SEATS_1);
        testPersonalCar3.setEngineVolume(AUTO_ENGINE_VOLUME1);
        testPersonalCar3.setInsuranceNumber(AUTO_INSURANCE_1);
        testPersonalCar3.setOwnerInfo(AUTO_OWNER_1);
        testPersonalCar3.setRegistrationNumber(AUTO_REG_NUMBER_2);
        testPersonalCar3.setRegistrationCertificate(AUTO_REG_CERT_1);
        testPersonalCar3.setEmployee(testEmployee1);


        testPersonalCar4 = new PersonalCar();
        testPersonalCar4.setBrandName(AUTO_BRAND_1);
        testPersonalCar4.setModel(AUTO_MODEL_NAME_1);
        testPersonalCar4.setColor(AUTO_MODEL_COLOR_1);
        testPersonalCar4.setPassengerSeatsCount(AUTO_SEATS_1);
        testPersonalCar4.setEngineVolume(AUTO_ENGINE_VOLUME1);
        testPersonalCar4.setInsuranceNumber(AUTO_INSURANCE_1);
        testPersonalCar4.setOwnerInfo(AUTO_OWNER_1);
        testPersonalCar4.setRegistrationNumber(AUTO_REG_NUMBER_2);
        testPersonalCar4.setRegistrationCertificate(AUTO_REG_CERT_2);
        testPersonalCar4.setEmployee(testEmployee1);

        testGeoZone1 = new GeoZone();
        testGeoZone1.setId(UUID.randomUUID());
        testGeoZone1.setName(GEOZONE_NAME1);
        testGeoZone1.setCode(GEOZONE_CODE1);

        testGeoZone2 = new GeoZone();
        testGeoZone2.setId(UUID.randomUUID());
        testGeoZone2.setName(GEOZONE_NAME2);
        testGeoZone2.setCode(GEOZONE_CODE2);
    }

    protected Employee generateEmployee(Department department, Position position, Organization organization) {
        var employeeId = UUID.randomUUID();
        var surname = generateSurname();
        var name = generateName();
        var patronymic = generatePatronymic();

        return Employee.builder()
                .id(employeeId)
                .department(department)
                .firstName(name)
                .lastName(surname)
                .patronymic(patronymic)
                .position(position)
                .organization(organization)
                .humanReadableId("US-0%s-00%s-%s".formatted(new Random().nextInt(10), new Random().nextInt(100), new Random().nextInt(50)))
                .personnelNumber(String.valueOf(new Random().nextInt(1_200_000, 1_800_000)))
                .updateTime(OffsetDateTime.now())
                .build();
    }

    private String generateSurname() {
        String[] surnameArray = {
                "Рожков",
                "Быков",
                "Устинов",
                "Комиссаров",
                "Мартынов",
                "Павлов",
                "Маслов",
                "Панов",
                "Кошелев",
                "Титов",
                "Кириллов",
                "Капустин",
                "Кудряшов",
                "Калашников",
                "Поляков",
                "Ермаков",
                "Лобанов",
                "Ларионов",
                "Калашников",
                "Игнатьев",
                "Рогов",
                "Трофимов",
                "Сидоров",
                "Егоров",
                "Титов",
                "Ширяев",
                "Самсонов",
                "Молчанов",
                "Аксёнов",
                "Афанасьев",
                "Гаврилов",
                "Буров",
                "Ситников",
                "Медведев",
                "Белов",
                "Крылов",
                "Федотов",
                "Кудрявцев",
                "Мухин",
                "Горшков",
                "Романов",
                "Федосеев",
                "Захаров",
                "Алексеев",
                "Ершов",
                "Соловьёв",
                "Блохин",
                "Панов",
                "Игнатов",
                "Харитонов",
        };
        return surnameArray[new Random().nextInt(surnameArray.length)];
    }

    private String generateName() {
        String[] nameArray = {
                "Кондратий",
                "Нинель",
                "Платон",
                "Вальтер",
                "Лазарь",
                "Макар",
                "Велорий",
                "Овидий",
                "Карл",
                "Ермак",
                "Николай",
                "Пантелеймон",
                "Влас",
                "Трофим",
                "Устин",
                "Ян",
                "Авраам",
                "Лазарь",
                "Михаил",
                "Владлен",
                "Аверьян",
                "Август",
                "Эльдар",
                "Аввакуум",
                "Захар",
                "Яков",
                "Глеб",
                "Платон",
                "Емельян",
                "Нисон",
                "Нелли",
                "Аркадий",
                "Авраам",
                "Ефрем",
                "Наум",
                "Юстин",
                "Александр",
                "Велорий",
                "Остап",
                "Адам",
                "Юлий",
                "Наум",
                "Максим",
                "Григорий",
                "Модест",
                "Валерий",
                "Пантелей",
                "Егор",
                "Соломон",
                "Глеб",
        };
        return nameArray[new Random().nextInt(nameArray.length)];
    }

    private String generatePatronymic() {
        String[] patronymicArray = {
                "Миронович",
                "Пётрович",
                "Робертович",
                "Федосеевич",
                "Дамирович",
                "Гордеевич",
                "Егорович",
                "Альбертович",
                "Лукьевич",
                "Кириллович",
                "Протасьевич",
                "Егорович",
                "Рубенович",
                "Филатович",
                "Агафонович",
                "Андреевич",
                "Рубенович",
                "Митрофанович",
                "Глебович",
                "Евгеньевич",
                "Дмитрьевич",
                "Владиславович",
                "Святославович",
                "Юлианович",
                "Эдуардович",
                "Авдеевич",
                "Еремеевич",
                "Антонович",
                "Рудольфович",
                "Артёмович",
                "Филиппович",
                "Натанович",
                "Ярославович",
                "Владиславович",
                "Аристархович",
                "Петрович",
                "Леонидович",
                "Вениаминович",
                "Даниилович",
                "Артемович",
                "Максович",
                "Григорьевич",
                "Вадимович",
                "Геласьевич",
                "Леонидович",
                "Яковлевич",
                "Донатович",
                "Михайлович",
                "Федотович",
                "Лукьевич",
        };
        return patronymicArray[new Random().nextInt(patronymicArray.length)];
    }

    protected  <T> T getMessages(OutputBridge outputBridge, Class<T> messageClass) {
        var captor = ArgumentCaptor.forClass(messageClass);
        verify(outputBridge).send(captor.capture());
        return captor.getValue();
    }

    protected  <T> T getMessages(OutputBridge outputBridge, Class<T> messageClass, Map<String, Object> headers) {
        var captor = ArgumentCaptor.forClass(messageClass);
        verify(outputBridge).send(captor.capture(), eq(headers));
        return captor.getValue();
    }

    protected  <T> List<T> getMessages(OutputBridge outputBridge, int count, Class<T> messageClass) {
        var captor = ArgumentCaptor.forClass(messageClass);
        verify(outputBridge, times(count)).send(captor.capture());
        assertThat(captor.getAllValues()).hasSize(count);
        return captor.getAllValues();
    }
}
