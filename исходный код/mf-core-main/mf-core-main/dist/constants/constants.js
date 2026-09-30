var _a;
import { _env } from '../utils/utils';
export var NETWORK_LOOP = _env('REACT_APP_NETWORK_LOOP') || '';
export var IS_REMOTE = _env('REACT_APP_REMOTE') === 'TRUE';
export var IS_PROD = _env('PROD') === 'TRUE';
export var IS_DEV = _env('NODE_ENV') === 'development';
export var HOST = window.location.host;
export var IS_LOCAL = /localhost/.test(HOST);
var API_DOMAIN = 'api';
var API_PATH = 'api';
var API_SUBDOMAIN = HOST.replace(/^[^.]+\./g, '');
var EXCLUDED_DOMENS = ['sbertransport', 'nt', 'st', 'ift', 'psi', 'hf'];
var FIRST_DOMEN = HOST.split('.')[0];
var IS_EXECEPTION = EXCLUDED_DOMENS.includes(FIRST_DOMEN);
var IS_EXTERNAL = /sbertransport\.ru/.test(HOST);
// Хост в зависимости от стенда на котором запускается - заменяется на прямой адрес api
// client.ift.sbertransport.sigma.sbrf.ru -> заменяем api.ift.sbertransport.sigma.sbrf.ru
var apiUrl = "//".concat(API_DOMAIN, ".").concat(API_SUBDOMAIN, "/").concat(API_PATH);
// Хост, начинающийся с названия домена из списка EXCLUDED_DOMENS - дополняется поддоменом api.
// sbertransport.sigma.sbrf.ru -> добавляем api.sbertransport.sigma.sbrf.ru
if (IS_EXECEPTION) {
    apiUrl = "//".concat(API_DOMAIN, ".").concat(HOST, "/").concat(API_PATH);
}
// Хост ака Внешний, заканчивающийся на sbertransport.ru - не меняется!
// sbertransport.ru  -> не трогаем
// Локалхост также - не меняется и подчиняется правилам прокси дев сервера (scripts/devServer.js)
if (IS_EXTERNAL || IS_LOCAL) {
    apiUrl = "/".concat(API_PATH);
}
export var API_URL = apiUrl;
export var MOCKED_API_PREFIX = _env('REACT_APP_MOCKED_API_PREFIX') || 'mock/';
export var routes;
(function (routes) {
    routes["Failure"] = "/oauth/failure";
    routes["SudirApi"] = "/api/sudir/oauth2/authorization/sudir";
})(routes || (routes = {}));
export var SYSTEM_MESSAGES;
(function (SYSTEM_MESSAGES) {
    SYSTEM_MESSAGES["authGlobalError"] = "\u041F\u0440\u043E\u0431\u043B\u0435\u043C\u044B \u0441 \u0430\u0432\u0442\u043E\u0440\u0438\u0437\u0430\u0446\u0438\u0435\u0439. authStore \u043D\u0435 \u0438\u043D\u0438\u0446\u0438\u0430\u043B\u0438\u0437\u0438\u0440\u043E\u0432\u0430\u043D";
    SYSTEM_MESSAGES["employeeEditSuccess"] = "\u0418\u043D\u0444\u043E\u0440\u043C\u0430\u0446\u0438\u044F \u0412\u0430\u0448\u0435\u0433\u043E \u043F\u0440\u043E\u0444\u0438\u043B\u044F \u0438\u0437\u043C\u0435\u043D\u0435\u043D\u0430";
    SYSTEM_MESSAGES["savingWithAddressDuplicates"] = "\u041F\u0443\u043D\u043A\u0442 \u043E\u0442\u043F\u0440\u0430\u0432\u043B\u0435\u043D\u0438\u044F \u0438 \u043D\u0430\u0437\u043D\u0430\u0447\u0435\u043D\u0438\u044F \u043D\u0435 \u0434\u043E\u043B\u0436\u043D\u044B \u0441\u043E\u0432\u043F\u0430\u0434\u0430\u0442\u044C. \u0414\u043E\u0431\u0430\u0432\u044C\u0442\u0435 \u043F\u0440\u043E\u043C\u0435\u0436\u0443\u0442\u043E\u0447\u043D\u044B\u0439 \u0430\u0434\u0440\u0435\u0441 \u0438\u043B\u0438 \u0438\u0437\u043C\u0435\u043D\u0438\u0442\u0435 \u043F\u0443\u043D\u043A\u0442 \u043D\u0430\u0437\u043D\u0430\u0447\u0435\u043D\u0438\u044F!";
    SYSTEM_MESSAGES["savingWithWrongSum"] = "\u041A \u0441\u043E\u0436\u0430\u043B\u0435\u043D\u0438\u044E, \u043D\u0430 \u0442\u0435\u043A\u0443\u0449\u0438\u0439 \u043C\u043E\u043C\u0435\u043D\u0442, \u0440\u0435\u0437\u0435\u0440\u0432\u0438\u0440\u043E\u0432\u0430\u043D\u0438\u0435 \u0434\u043B\u044F \u0441\u043E\u0442\u0440\u0443\u0434\u043D\u0438\u043A\u0430 \u043D\u0430 \u043E\u0431\u0449\u0435\u0441\u0442\u0432\u0435\u043D\u043D\u044B\u0439 \u0432\u0438\u0434 \u0442\u0440\u0430\u043D\u0441\u043F\u043E\u0440\u0442\u0430 \u043D\u0435\u0432\u043E\u0437\u043C\u043E\u0436\u043D\u043E \u043D\u0430 \u0443\u043A\u0430\u0437\u0430\u043D\u043D\u0443\u044E \u0441\u0443\u043C\u043C\u0443";
    SYSTEM_MESSAGES["departmentAddSuccess"] = "\u041F\u043E\u0434\u0440\u0430\u0437\u0434\u0435\u043B\u0435\u043D\u0438\u0435 \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0441\u043E\u0437\u0434\u0430\u043D\u043E";
    SYSTEM_MESSAGES["departmentDeleteSuccess"] = "\u041F\u043E\u0434\u0440\u0430\u0437\u0434\u0435\u043B\u0435\u043D\u0438\u0435 \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0443\u0434\u0430\u043B\u0435\u043D\u043E";
    SYSTEM_MESSAGES["departmentEditSuccess"] = "\u0418\u043D\u0444\u043E\u0440\u043C\u0430\u0446\u0438\u044F \u043E \u043F\u043E\u0434\u0440\u0430\u0437\u0434\u0435\u043B\u0435\u043D\u0438\u0438 \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0441\u043E\u0445\u0440\u0430\u043D\u0435\u043D\u0430";
    SYSTEM_MESSAGES["addressLableIsNotUniq"] = "\u0410\u0434\u0440\u0435\u0441 \u0441 \u0442\u0430\u043A\u0438\u043C \u043D\u0430\u0437\u0432\u0430\u043D\u0438\u0435\u043C \u0443\u0436\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442";
    SYSTEM_MESSAGES["addressIsNotUniq"] = "\u0423\u043A\u0430\u0437\u0430\u043D\u043D\u044B\u0439 \u0430\u0434\u0440\u0435\u0441 \u0443\u0436\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442";
    SYSTEM_MESSAGES["addressDeleteSuccess"] = "\u0410\u0434\u0440\u0435\u0441 \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0443\u0434\u0430\u043B\u0451\u043D";
    SYSTEM_MESSAGES["addressAddSuccess"] = "\u0410\u0434\u0440\u0435\u0441 \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0434\u043E\u0431\u0430\u0432\u043B\u0435\u043D";
    SYSTEM_MESSAGES["limitRequestCreateSuccess"] = "\u0417\u0430\u044F\u0432\u043A\u0430 \u043D\u0430 \u043B\u0438\u043C\u0438\u0442 \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0441\u043E\u0437\u0434\u0430\u043D\u0430";
    SYSTEM_MESSAGES["limitRequestEditSuccess"] = "\u0417\u0430\u044F\u0432\u043A\u0430 \u043D\u0430 \u043B\u0438\u043C\u0438\u0442 \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0438\u0437\u043C\u0435\u043D\u0435\u043D\u0430";
    SYSTEM_MESSAGES["limitRequestCancelSuccess"] = "\u0417\u0430\u044F\u0432\u043A\u0430 \u043D\u0430 \u043B\u0438\u043C\u0438\u0442 \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u043E\u0442\u043C\u0435\u043D\u0435\u043D\u0430";
    SYSTEM_MESSAGES["limitIsApproved"] = "\u0417\u0430\u044F\u0432\u043A\u0430 \u043D\u0430 \u043B\u0438\u043C\u0438\u0442 \u043E\u0434\u043E\u0431\u0440\u0435\u043D\u0430";
    SYSTEM_MESSAGES["simulationSuccess"] = "\u0421\u0438\u043C\u0443\u043B\u044F\u0446\u0438\u044F \u0438\u043C\u043F\u043E\u0440\u0442\u0430 \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043D\u0430";
    SYSTEM_MESSAGES["fileUploadSuccess"] = "\u0424\u0430\u0439\u043B \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043D";
    SYSTEM_MESSAGES["delegateSuccessfull"] = "\u0414\u0435\u043B\u0435\u0433\u0430\u0442 \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u043D\u0430\u0437\u043D\u0430\u0447\u0435\u043D";
    SYSTEM_MESSAGES["delegateDeleteSuccess"] = "\u0414\u0435\u043B\u0435\u0433\u0430\u0442 \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0443\u0434\u0430\u043B\u0435\u043D";
})(SYSTEM_MESSAGES || (SYSTEM_MESSAGES = {}));
export var TaxiClassEnum;
(function (TaxiClassEnum) {
    TaxiClassEnum["ECONOMY"] = "ECONOMY";
    TaxiClassEnum["COMFORT"] = "COMFORT";
    TaxiClassEnum["COMFORT_PLUS"] = "COMFORT_PLUS";
    TaxiClassEnum["PERSONAL"] = "PERSONAL";
    TaxiClassEnum["BUSINESS"] = "BUSINESS";
    TaxiClassEnum["TAXI"] = "TAXI";
    TaxiClassEnum["CARSHARING"] = "CARSHARING";
    TaxiClassEnum["BICYCLE"] = "BICYCLE";
    TaxiClassEnum["WALK"] = "WALK";
    TaxiClassEnum["PUBLIC"] = "PUBLIC";
    TaxiClassEnum["SCOOTER"] = "SCOOTER";
    TaxiClassEnum["YANDEX"] = "YANDEX";
    TaxiClassEnum["CITYMOBIL"] = "CITYMOBIL";
    TaxiClassEnum["UBER"] = "UBER";
    TaxiClassEnum["VIP_BUS"] = "VIP_BUS";
    TaxiClassEnum["SMALL_BUS"] = "SMALL_BUS";
    TaxiClassEnum["MIDDLE_BUS"] = "MIDDLE_BUS";
    TaxiClassEnum["LARGE_BUS"] = "LARGE_BUS";
})(TaxiClassEnum || (TaxiClassEnum = {}));
export var TaxiClassTitlesEnum;
(function (TaxiClassTitlesEnum) {
    TaxiClassTitlesEnum["TAXI"] = "\u0422\u0430\u043A\u0441\u0438";
    TaxiClassTitlesEnum["ECONOMY"] = "\u042D\u043A\u043E\u043D\u043E\u043C";
    TaxiClassTitlesEnum["COMFORT"] = "\u041A\u043E\u043C\u0444\u043E\u0440\u0442";
    TaxiClassTitlesEnum["COMFORT_PLUS"] = "\u041A\u043E\u043C\u0444\u043E\u0440\u0442+";
    TaxiClassTitlesEnum["PERSONAL"] = "\u041B\u0438\u0447\u043D\u044B\u0439";
    TaxiClassTitlesEnum["BUSINESS"] = "\u0411\u0438\u0437\u043D\u0435\u0441";
    TaxiClassTitlesEnum["CARSHARING"] = "\u041A\u0430\u0440\u0448\u0435\u0440\u0438\u043D\u0433";
    TaxiClassTitlesEnum["BICYCLE"] = "\u0412\u0435\u043B\u043E\u0441\u0438\u043F\u0435\u0434";
    TaxiClassTitlesEnum["WALK"] = "\u041F\u0435\u0448\u043A\u043E\u043C";
    TaxiClassTitlesEnum["PUBLIC"] = "\u041E\u0431\u0449\u0435\u0441\u0442\u0432\u0435\u043D\u043D\u044B\u0439";
    TaxiClassTitlesEnum["SCOOTER"] = "\u0421\u0430\u043C\u043E\u043A\u0430\u0442";
    TaxiClassTitlesEnum["YANDEX"] = "Yandex Go";
    TaxiClassTitlesEnum["CITYMOBIL"] = "\u0421\u0438\u0442\u0438\u043C\u043E\u0431\u0438\u043B";
    TaxiClassTitlesEnum["UBER"] = "Uber";
    TaxiClassTitlesEnum["VIP_BUS"] = "\u0410\u0432\u0442\u043E\u0431\u0443\u0441 \u0434\u043E 9 \u043C\u0435\u0441\u0442";
    TaxiClassTitlesEnum["SMALL_BUS"] = "\u0410\u0432\u0442\u043E\u0431\u0443\u0441 \u043E\u0442 10 \u0434\u043E 21 \u043C\u0435\u0441\u0442\u0430";
    TaxiClassTitlesEnum["MIDDLE_BUS"] = "\u0410\u0432\u0442\u043E\u0431\u0443\u0441 \u043E\u0442 22 \u0434\u043E 41 \u043C\u0435\u0441\u0442";
    TaxiClassTitlesEnum["LARGE_BUS"] = "\u0410\u0432\u0442\u043E\u0431\u0443\u0441 \u043E\u0442 42 \u0434\u043E 55 \u043C\u0435\u0441\u0442";
    TaxiClassTitlesEnum["BUS"] = "\u0410\u0432\u0442\u043E\u0431\u0443\u0441";
})(TaxiClassTitlesEnum || (TaxiClassTitlesEnum = {}));
export var TransportTypeEnum;
(function (TransportTypeEnum) {
    TransportTypeEnum["TAXI"] = "TAXI";
    TransportTypeEnum["PUBLIC"] = "PUBLIC";
    TransportTypeEnum["PERSONAL"] = "PERSONAL";
    TransportTypeEnum["CARSHARING"] = "CARSHARING";
    TransportTypeEnum["BICYCLE"] = "BICYCLE";
    TransportTypeEnum["WALK"] = "WALK";
    TransportTypeEnum["SCOOTER"] = "SCOOTER";
    TransportTypeEnum["DEDICATED"] = "DEDICATED";
    TransportTypeEnum["COURIER"] = "COURIER";
    TransportTypeEnum["INTERREGIONAL"] = "INTERREGIONAL";
    TransportTypeEnum["PRIVATE"] = "PRIVATE";
})(TransportTypeEnum || (TransportTypeEnum = {}));
export var TransportTypeTitlesEnum;
(function (TransportTypeTitlesEnum) {
    TransportTypeTitlesEnum["TAXI"] = "\u0422\u0430\u043A\u0441\u0438";
    TransportTypeTitlesEnum["PUBLIC"] = "\u041E\u0431\u0449\u0435\u0441\u0442\u0432\u0435\u043D\u043D\u044B\u0439";
    TransportTypeTitlesEnum["PERSONAL"] = "\u041B\u0438\u0447\u043D\u044B\u0439";
    TransportTypeTitlesEnum["CARSHARING"] = "\u041A\u0430\u0440\u0448\u0435\u0440\u0438\u043D\u0433";
    TransportTypeTitlesEnum["BICYCLE"] = "\u0412\u0435\u043B\u043E\u0441\u0438\u043F\u0435\u0434";
    TransportTypeTitlesEnum["WALK"] = "\u041F\u0435\u0448\u043A\u043E\u043C";
    TransportTypeTitlesEnum["SCOOTER"] = "\u0421\u0430\u043C\u043E\u043A\u0430\u0442";
    TransportTypeTitlesEnum["DEDICATED"] = "\u0414\u043E\u0441\u0442\u0430\u0432\u043A\u0430";
    TransportTypeTitlesEnum["COURIER"] = "\u041A\u0443\u0440\u044C\u0435\u0440";
    TransportTypeTitlesEnum["INTERREGIONAL"] = "\u041C\u0435\u0436\u0440\u0435\u0433\u0438\u043E\u043D\u0430\u043B\u044C\u043D\u0430\u044F";
    TransportTypeTitlesEnum["PRIVATE"] = "\u0427\u0430\u0441\u0442\u043D\u0430\u044F";
})(TransportTypeTitlesEnum || (TransportTypeTitlesEnum = {}));
export var TransportTypeOptions = [
    {
        label: TransportTypeTitlesEnum[TransportTypeEnum.TAXI],
        value: TransportTypeEnum.TAXI,
    },
    {
        label: TransportTypeTitlesEnum[TransportTypeEnum.PERSONAL],
        value: TransportTypeEnum.PERSONAL,
    },
    {
        label: TransportTypeTitlesEnum[TransportTypeEnum.PUBLIC],
        value: TransportTypeEnum.PUBLIC,
    },
    {
        label: TransportTypeTitlesEnum[TransportTypeEnum.CARSHARING],
        value: TransportTypeEnum.CARSHARING,
    },
    {
        label: TransportTypeTitlesEnum[TransportTypeEnum.DEDICATED],
        value: TransportTypeEnum.DEDICATED,
    },
];
export var OsagoUploadResponseParams;
(function (OsagoUploadResponseParams) {
    OsagoUploadResponseParams[OsagoUploadResponseParams["vehicleMark"] = 'vehicleInfo.vehicleMark'] = "vehicleMark";
    OsagoUploadResponseParams[OsagoUploadResponseParams["vehicleModel"] = 'vehicleInfo.vehicleModel'] = "vehicleModel";
    OsagoUploadResponseParams[OsagoUploadResponseParams["ownerLastName"] = 'owner.personalData.lastName'] = "ownerLastName";
    OsagoUploadResponseParams[OsagoUploadResponseParams["ownerFirstName"] = 'owner.personalData.firstName'] = "ownerFirstName";
    OsagoUploadResponseParams[OsagoUploadResponseParams["ownerMiddleName"] = 'owner.personalData.middleName'] = "ownerMiddleName";
    OsagoUploadResponseParams[OsagoUploadResponseParams["endDate"] = 'contractConditions.endDate'] = "endDate";
    OsagoUploadResponseParams[OsagoUploadResponseParams["insurerLastName"] = 'insurer.personalData.lastName'] = "insurerLastName";
    OsagoUploadResponseParams[OsagoUploadResponseParams["insurerFirstName"] = 'insurer.personalData.firstName'] = "insurerFirstName";
    OsagoUploadResponseParams[OsagoUploadResponseParams["vehicleInfoNumber"] = 'vehicleInfo.vehicleDocuments.number'] = "vehicleInfoNumber";
    OsagoUploadResponseParams[OsagoUploadResponseParams["contractSeries"] = 'contractSeries'] = "contractSeries";
    OsagoUploadResponseParams[OsagoUploadResponseParams["contractNumber"] = 'contractNumber'] = "contractNumber";
    OsagoUploadResponseParams[OsagoUploadResponseParams["vehicleInfoSeries"] = 'vehicleInfo.vehicleDocuments.series'] = "vehicleInfoSeries";
    OsagoUploadResponseParams[OsagoUploadResponseParams["plateNumber"] = 'vehicleInfo.plateNumber'] = "plateNumber";
    OsagoUploadResponseParams[OsagoUploadResponseParams["type"] = 'vehicleInfo.vehicleDocuments.type'] = "type";
    OsagoUploadResponseParams[OsagoUploadResponseParams["vinNumber"] = 'vehicleInfo.vinNumber'] = "vinNumber";
    OsagoUploadResponseParams[OsagoUploadResponseParams["driversLastName"] = 'drivers.personalData.lastName'] = "driversLastName";
    OsagoUploadResponseParams[OsagoUploadResponseParams["driversFirstName"] = 'drivers.personalData.firstName'] = "driversFirstName";
    OsagoUploadResponseParams[OsagoUploadResponseParams["driversMiddleName"] = 'drivers.personalData.middleName'] = "driversMiddleName";
    OsagoUploadResponseParams[OsagoUploadResponseParams["driversSeries"] = 'drivers.driverLicenses.series'] = "driversSeries";
    OsagoUploadResponseParams[OsagoUploadResponseParams["driversNumber"] = 'drivers.driverLicenses.number'] = "driversNumber";
})(OsagoUploadResponseParams || (OsagoUploadResponseParams = {}));
export var PersonalOwnerInformation;
(function (PersonalOwnerInformation) {
    PersonalOwnerInformation["USER"] = "USER";
    PersonalOwnerInformation["SPOUSE"] = "SPOUSE";
    PersonalOwnerInformation["THIRD_PARTY"] = "THIRD_PARTY";
})(PersonalOwnerInformation || (PersonalOwnerInformation = {}));
export var EmployeeStatus;
(function (EmployeeStatus) {
    EmployeeStatus["ACTIVE"] = "ACTIVE";
    EmployeeStatus["INACTIVE"] = "INACTIVE";
})(EmployeeStatus || (EmployeeStatus = {}));
export var EmployeeStatusTitle;
(function (EmployeeStatusTitle) {
    EmployeeStatusTitle["ACTIVE"] = "\u0410\u043A\u0442\u0438\u0432\u043D\u044B\u0439";
    EmployeeStatusTitle["INACTIVE"] = "\u041D\u0435\u0430\u043A\u0442\u0438\u0432\u043D\u044B\u0439";
})(EmployeeStatusTitle || (EmployeeStatusTitle = {}));
export var OrgStructureType;
(function (OrgStructureType) {
    OrgStructureType["EXTERNAL"] = "EXTERNAL";
    OrgStructureType["INTERNAL"] = "INTERNAL";
})(OrgStructureType || (OrgStructureType = {}));
export var AuthSteps;
(function (AuthSteps) {
    AuthSteps[AuthSteps["password"] = 0] = "password";
    AuthSteps[AuthSteps["code"] = 1] = "code";
})(AuthSteps || (AuthSteps = {}));
export var SUPPORT_PHONE = {
    code: '88007074882',
    title: '8-800-707-48-82',
};
export var SDO_SUPPORT_PHONE = {
    code: '88001000302',
    title: '8 (800) 100-03-02',
};
export var X_CLIENT_TYPE = 'CLIENT';
export var CustomErrorCode;
(function (CustomErrorCode) {
    CustomErrorCode[CustomErrorCode["UNKNOWN"] = 1000] = "UNKNOWN";
    CustomErrorCode[CustomErrorCode["TYPES"] = 1001] = "TYPES";
    CustomErrorCode[CustomErrorCode["TIMEOUT"] = 1002] = "TIMEOUT";
    CustomErrorCode[CustomErrorCode["CORS"] = 1003] = "CORS";
    CustomErrorCode[CustomErrorCode["UNDEFINED_FIELD"] = 1004] = "UNDEFINED_FIELD";
})(CustomErrorCode || (CustomErrorCode = {}));
export var errorText = (_a = {
        400: {
            title: 'Упс... Ошибка заполнения данных',
            subtitle: 'Пожалуйста, обратитесь в поддержку',
        },
        401: {
            title: 'Пожалуйста, авторизуйтесь',
            subtitle: '',
        },
        403: {
            title: 'Упс... У Вас недостаточно прав',
            subtitle: 'Пожалуйста, обратитесь в поддержку',
        },
        408: {
            title: 'Упс... Слишком большой объем данных',
            subtitle: 'Пожалуйста, обратитесь в поддержку',
        },
        409: {
            title: 'Упс... Конфликт данных',
            subtitle: 'Пожалуйста, обратитесь в поддержку',
        },
        417: {
            title: 'Упс... Ошибка данных',
            subtitle: 'Пожалуйста, обратитесь в поддержку',
        },
        500: {
            title: 'Упс... Ошибка сервера',
            subtitle: 'Пожалуйста, обратитесь в поддержку',
        },
        503: {
            title: 'Упс... Проводятся работы на сервере',
            subtitle: 'Пожалуйста, обратитесь в поддержку',
        }
    },
    // Все ошибки, для которых нет текста, выводятся как CustomErrorCode.UNKNOWN
    _a[CustomErrorCode.UNKNOWN] = {
        title: 'Упс... Ошибка системы',
        subtitle: 'Пожалуйста, обратитесь в поддержку',
    },
    _a[CustomErrorCode.TYPES] = {
        title: 'Упс... Ошибка отображения данных',
        subtitle: 'Пожалуйста, обратитесь в поддержку',
    },
    _a[CustomErrorCode.TIMEOUT] = {
        title: 'Упс... Слишком большой объем данных',
        subtitle: 'Пожалуйста, обратитесь в поддержку',
    },
    _a);
export var REQUESTS = 'requests';
export var REQUEST = 'request';
export var CANCEL = 'cancel';
export var APPROVE = 'approve';
export var EMPLOYEES = 'employees';
export var DEPARTMENTS = 'departments';
export var POSITIONS = 'positions';
export var ADDRESSES = 'addresses';
export var FAVORITE = 'favorite';
export var GEO = 'geo';
export var LIMITS = 'limits';
export var DEPLIMITS = 'deplimits';
export var ORGANIZATIONS = 'organizations';
export var DELEGATES = 'delegates';
export var CANDIDATES = 'candidates';
export var SUPERVISORS = 'supervisors';
export var DISPATCHER_ROOM = 'dispatcher-room';
export var GET_SELF_EMPLOYEE = "/".concat(ORGANIZATIONS, "/self");
export var CONSENT = "/".concat(ORGANIZATIONS, "/self/consent/");
export var CONSENT_DISPATCHER = "/".concat(DISPATCHER_ROOM, "/self/dispatcher/consent/");
export var GET_ALL_EMPLOYEES_BY_ORGANIZATION_PARAMS = "/".concat(ORGANIZATIONS, "/:orgId/").concat(EMPLOYEES, "/");
export var GET_ALL_EMPLOYEES_BY_DEPARTMENTS_PARAMS = "/".concat(ORGANIZATIONS, "/:orgId/").concat(DEPARTMENTS, "/:depId/").concat(EMPLOYEES, "/");
export var EMPLOYEE_PARAMS = "/".concat(ORGANIZATIONS, "/:orgId/").concat(DEPARTMENTS, "/:depId/").concat(EMPLOYEES, "/:empId");
export var EMPLOYEES_PARAMS = "/".concat(ORGANIZATIONS, "/:orgId/").concat(EMPLOYEES, "/:empIds");
export var EMPLOYEES_SEARCH_BY_ORG = "/".concat(ORGANIZATIONS, "/:orgId/employeessearch?fio=:name");
export var EMPLOYEES_SEARCH = "/".concat(ORGANIZATIONS, "/employees/search?fio=:name");
export var ORGANIZATIONS_ID = "".concat(ORGANIZATIONS, "/:orgId");
export var DEPARTMENTS_ID = "".concat(DEPARTMENTS, "/:depId");
export var POSITIONS_ID = "".concat(POSITIONS, "/:posId");
export var DELETE_DEPARTMENT = "/".concat(ORGANIZATIONS_ID, "/").concat(DEPARTMENTS_ID);
export var DEPARTMENTS_ADD_PARAMS = "/".concat(ORGANIZATIONS_ID, "/").concat(DEPARTMENTS, "/");
export var DEPARTMENT_EDIT_PARAMS = "/".concat(ORGANIZATIONS_ID, "/").concat(DEPARTMENTS_ID);
export var GET_ALL_DEPARTMENTS = "/".concat(ORGANIZATIONS_ID, "/").concat(DEPARTMENTS, "/");
export var GET_ALL_ORGANIZATIONS = "/".concat(ORGANIZATIONS, "/");
export var GET_ALL_POSITIONS = "/".concat(ORGANIZATIONS_ID, "/").concat(POSITIONS, "/");
export var GET_DEPARTMENT = "/".concat(ORGANIZATIONS_ID, "/").concat(DEPARTMENTS_ID);
export var GET_ORGANIZATION = "/".concat(ORGANIZATIONS_ID);
export var GET_POSITION = "/".concat(ORGANIZATIONS_ID, "/").concat(POSITIONS_ID);
export var SELF_ADDRESSES = "".concat(GET_SELF_EMPLOYEE, "/").concat(ADDRESSES);
export var SELF_FREQUENT_ADDRESSES = "".concat(SELF_ADDRESSES, "/frequently");
export var SELF_FREQUENT_ADDRESSES_PARAMS = "".concat(SELF_ADDRESSES, "/frequently/:addressId");
export var SELF_FAVORITE_ADDRESSES = "".concat(SELF_ADDRESSES, "/").concat(FAVORITE);
export var SELF_FAVORITE_ADDRESSES_PARAMS = "".concat(SELF_FAVORITE_ADDRESSES, "/:addressId");
export var CALC_ROUTE = "/".concat(GEO, "/route");
export var GET_ADDRESS_BY_COORDINATES = "/".concat(GEO, "/address");
export var GET_COORDINATES_BY_ADDRESS = "".concat(GET_ADDRESS_BY_COORDINATES);
export var UPLOADFILE = "/".concat(ORGANIZATIONS, "/dataimport/load/:orgId/:strategyMode/:nsi/:decSeparator/:separator");
export var PRELOADFILE = "/".concat(ORGANIZATIONS, "/dataimport/preload/:orgId/:strategyMode/:nsi/:decSeparator/:separator");
export var APPROVE_REQUEST = "/".concat(LIMITS, "/requests/approve");
export var GET_ACCOUNT_BONUSES = "/".concat(LIMITS, "/bonus/");
export var GET_ALL_REQUESTS = "/".concat(LIMITS, "/requests");
export var GET_REQUESTS_DEP = "".concat(GET_ALL_REQUESTS, "/dep");
export var GET_REQUESTS_EMP = "".concat(GET_ALL_REQUESTS, "/emp");
export var GET_SPENDINGS = "".concat(LIMITS, "/spendings");
export var GET_DEPLIMITS = "/".concat(LIMITS, "/deplimits/");
export var GET_DEPLIMITS_BY_DEP_AND_YEAR = "/".concat(LIMITS, "/deplimits/getByDepartmentAndYear/:depId/year/:year");
export var GET_EMPLIMITS = "/".concat(LIMITS, "/emplimits/");
export var GET_DEPLIMITS_BY_DEP = "/".concat(LIMITS, "/deplimits/getByDepartment");
export var GET_LIMIT_SHARING = "/".concat(LIMITS, "/limitsharing/getByLimit/full/");
export var GET_EMP_LIMIT = "/".concat(LIMITS, "/emplimits/getByEmployeeAndYear/");
export var GET_LIMITS_REQUESTS_BY_AUTHOR = "/".concat(LIMITS, "/requests/getByAuthor");
export var GET_LIMITS_REQUESTS_BY_APPROVER = "/".concat(LIMITS, "/requests/getByApprover");
export var GET_ACTIVE_LIMITS_REQUESTS_BY_APPROVER = "/".concat(LIMITS, "/requests/getActiveByApprover");
export var GET_OLD_LIMITS_REQUESTS_BY_APPROVER = "/".concat(LIMITS, "/requests/getOldByApprover");
export var LIMIT_REQUEST_CANCEL = "/".concat(LIMITS, "/").concat(REQUESTS, "/").concat(CANCEL);
export var GET_LIMIT_TRANSFER_HISTORY = "/".concat(LIMITS, "/limittransferhistory/getByLimit");
export var GET_SIBLINGS = "".concat(GET_DEPLIMITS, "siblings/");
export var LIMITREQUESTS_CANCEL_PARAMS = "/limits/".concat(DEPLIMITS, "/").concat(CANCEL, "/:reqId");
export var LIMITREQUESTS_APPROVE_PARAMS = "/limits/".concat(DEPLIMITS, "/").concat(APPROVE, "/:reqId");
export var LIMITREQUESTS_CRUD = "/limits/".concat(DEPLIMITS);
export var LIMITREQUESTS_PARAMS = "/limits/".concat(DEPLIMITS, "/:reqId");
export var GET_DELEGATES = "/".concat(ORGANIZATIONS, "/:orgId/").concat(DEPARTMENTS, "/:depId/").concat(DELEGATES, "/").concat(SUPERVISORS, "/:supId");
export var ADD_DELEGATE = "/".concat(ORGANIZATIONS, "/:orgId/").concat(DEPARTMENTS, "/:depId/").concat(DELEGATES, "/");
export var DELETE_DELEGATE = "/".concat(ORGANIZATIONS, "/:orgId/").concat(DEPARTMENTS, "/:depId/").concat(DELEGATES, "/:delId");
export var GET_SELF_CONDIDATES_TO_DELEGATES = '/organizations/self/delegates/candidates/:transportType';
export var GET_CANDIDATES_IN_DELEGATES_PARAMS = "/".concat(ORGANIZATIONS, "/:orgId/").concat(DEPARTMENTS, "/:depId/").concat(DELEGATES, "/").concat(CANDIDATES, "/:supId/:transTypeId");
export var TRANSPORTTYPES = "".concat(ORGANIZATIONS, "/transport-types");
export var AVAILABLE_TRANSPORTTYPES = "".concat(ORGANIZATIONS, "/transportorg/org/:orgId");
export var AVAILABLE_TRANSPORTTYPES_BY_SERVICE = "".concat(ORGANIZATIONS, "/transportorg/:serviceType/org/:orgId");
