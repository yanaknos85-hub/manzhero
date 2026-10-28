import { LabeledValue } from '../utils/types';
export declare const NETWORK_LOOP: string;
export declare const IS_REMOTE: boolean;
export declare const IS_PROD: boolean;
export declare const IS_DEV: boolean;
export declare const HOST: string;
export declare const IS_LOCAL: boolean;
export declare const API_URL: string;
export declare const MOCKED_API_PREFIX: string;
export declare enum routes {
    Failure = "/oauth/failure",
    SudirApi = "/api/sudir/oauth2/authorization/sudir"
}
export declare enum SYSTEM_MESSAGES {
    authGlobalError = "\u041F\u0440\u043E\u0431\u043B\u0435\u043C\u044B \u0441 \u0430\u0432\u0442\u043E\u0440\u0438\u0437\u0430\u0446\u0438\u0435\u0439. authStore \u043D\u0435 \u0438\u043D\u0438\u0446\u0438\u0430\u043B\u0438\u0437\u0438\u0440\u043E\u0432\u0430\u043D",
    employeeEditSuccess = "\u0418\u043D\u0444\u043E\u0440\u043C\u0430\u0446\u0438\u044F \u0412\u0430\u0448\u0435\u0433\u043E \u043F\u0440\u043E\u0444\u0438\u043B\u044F \u0438\u0437\u043C\u0435\u043D\u0435\u043D\u0430",
    savingWithAddressDuplicates = "\u041F\u0443\u043D\u043A\u0442 \u043E\u0442\u043F\u0440\u0430\u0432\u043B\u0435\u043D\u0438\u044F \u0438 \u043D\u0430\u0437\u043D\u0430\u0447\u0435\u043D\u0438\u044F \u043D\u0435 \u0434\u043E\u043B\u0436\u043D\u044B \u0441\u043E\u0432\u043F\u0430\u0434\u0430\u0442\u044C. \u0414\u043E\u0431\u0430\u0432\u044C\u0442\u0435 \u043F\u0440\u043E\u043C\u0435\u0436\u0443\u0442\u043E\u0447\u043D\u044B\u0439 \u0430\u0434\u0440\u0435\u0441 \u0438\u043B\u0438 \u0438\u0437\u043C\u0435\u043D\u0438\u0442\u0435 \u043F\u0443\u043D\u043A\u0442 \u043D\u0430\u0437\u043D\u0430\u0447\u0435\u043D\u0438\u044F!",
    savingWithWrongSum = "\u041A \u0441\u043E\u0436\u0430\u043B\u0435\u043D\u0438\u044E, \u043D\u0430 \u0442\u0435\u043A\u0443\u0449\u0438\u0439 \u043C\u043E\u043C\u0435\u043D\u0442, \u0440\u0435\u0437\u0435\u0440\u0432\u0438\u0440\u043E\u0432\u0430\u043D\u0438\u0435 \u0434\u043B\u044F \u0441\u043E\u0442\u0440\u0443\u0434\u043D\u0438\u043A\u0430 \u043D\u0430 \u043E\u0431\u0449\u0435\u0441\u0442\u0432\u0435\u043D\u043D\u044B\u0439 \u0432\u0438\u0434 \u0442\u0440\u0430\u043D\u0441\u043F\u043E\u0440\u0442\u0430 \u043D\u0435\u0432\u043E\u0437\u043C\u043E\u0436\u043D\u043E \u043D\u0430 \u0443\u043A\u0430\u0437\u0430\u043D\u043D\u0443\u044E \u0441\u0443\u043C\u043C\u0443",
    departmentAddSuccess = "\u041F\u043E\u0434\u0440\u0430\u0437\u0434\u0435\u043B\u0435\u043D\u0438\u0435 \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0441\u043E\u0437\u0434\u0430\u043D\u043E",
    departmentDeleteSuccess = "\u041F\u043E\u0434\u0440\u0430\u0437\u0434\u0435\u043B\u0435\u043D\u0438\u0435 \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0443\u0434\u0430\u043B\u0435\u043D\u043E",
    departmentEditSuccess = "\u0418\u043D\u0444\u043E\u0440\u043C\u0430\u0446\u0438\u044F \u043E \u043F\u043E\u0434\u0440\u0430\u0437\u0434\u0435\u043B\u0435\u043D\u0438\u0438 \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0441\u043E\u0445\u0440\u0430\u043D\u0435\u043D\u0430",
    addressLableIsNotUniq = "\u0410\u0434\u0440\u0435\u0441 \u0441 \u0442\u0430\u043A\u0438\u043C \u043D\u0430\u0437\u0432\u0430\u043D\u0438\u0435\u043C \u0443\u0436\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442",
    addressIsNotUniq = "\u0423\u043A\u0430\u0437\u0430\u043D\u043D\u044B\u0439 \u0430\u0434\u0440\u0435\u0441 \u0443\u0436\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442",
    addressDeleteSuccess = "\u0410\u0434\u0440\u0435\u0441 \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0443\u0434\u0430\u043B\u0451\u043D",
    addressAddSuccess = "\u0410\u0434\u0440\u0435\u0441 \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0434\u043E\u0431\u0430\u0432\u043B\u0435\u043D",
    limitRequestCreateSuccess = "\u0417\u0430\u044F\u0432\u043A\u0430 \u043D\u0430 \u043B\u0438\u043C\u0438\u0442 \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0441\u043E\u0437\u0434\u0430\u043D\u0430",
    limitRequestEditSuccess = "\u0417\u0430\u044F\u0432\u043A\u0430 \u043D\u0430 \u043B\u0438\u043C\u0438\u0442 \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0438\u0437\u043C\u0435\u043D\u0435\u043D\u0430",
    limitRequestCancelSuccess = "\u0417\u0430\u044F\u0432\u043A\u0430 \u043D\u0430 \u043B\u0438\u043C\u0438\u0442 \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u043E\u0442\u043C\u0435\u043D\u0435\u043D\u0430",
    limitIsApproved = "\u0417\u0430\u044F\u0432\u043A\u0430 \u043D\u0430 \u043B\u0438\u043C\u0438\u0442 \u043E\u0434\u043E\u0431\u0440\u0435\u043D\u0430",
    simulationSuccess = "\u0421\u0438\u043C\u0443\u043B\u044F\u0446\u0438\u044F \u0438\u043C\u043F\u043E\u0440\u0442\u0430 \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043D\u0430",
    fileUploadSuccess = "\u0424\u0430\u0439\u043B \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043D",
    delegateSuccessfull = "\u0414\u0435\u043B\u0435\u0433\u0430\u0442 \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u043D\u0430\u0437\u043D\u0430\u0447\u0435\u043D",
    delegateDeleteSuccess = "\u0414\u0435\u043B\u0435\u0433\u0430\u0442 \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u0443\u0434\u0430\u043B\u0435\u043D"
}
export declare enum TaxiClassEnum {
    ECONOMY = "ECONOMY",
    COMFORT = "COMFORT",
    COMFORT_PLUS = "COMFORT_PLUS",
    PERSONAL = "PERSONAL",
    BUSINESS = "BUSINESS",
    TAXI = "TAXI",
    CARSHARING = "CARSHARING",
    BICYCLE = "BICYCLE",
    WALK = "WALK",
    PUBLIC = "PUBLIC",
    SCOOTER = "SCOOTER",
    YANDEX = "YANDEX",
    CITYMOBIL = "CITYMOBIL",
    UBER = "UBER",
    VIP_BUS = "VIP_BUS",
    SMALL_BUS = "SMALL_BUS",
    MIDDLE_BUS = "MIDDLE_BUS",
    LARGE_BUS = "LARGE_BUS"
}
export declare enum TaxiClassTitlesEnum {
    TAXI = "\u0422\u0430\u043A\u0441\u0438",
    ECONOMY = "\u042D\u043A\u043E\u043D\u043E\u043C",
    COMFORT = "\u041A\u043E\u043C\u0444\u043E\u0440\u0442",
    COMFORT_PLUS = "\u041A\u043E\u043C\u0444\u043E\u0440\u0442+",
    PERSONAL = "\u041B\u0438\u0447\u043D\u044B\u0439",
    BUSINESS = "\u0411\u0438\u0437\u043D\u0435\u0441",
    CARSHARING = "\u041A\u0430\u0440\u0448\u0435\u0440\u0438\u043D\u0433",
    BICYCLE = "\u0412\u0435\u043B\u043E\u0441\u0438\u043F\u0435\u0434",
    WALK = "\u041F\u0435\u0448\u043A\u043E\u043C",
    PUBLIC = "\u041E\u0431\u0449\u0435\u0441\u0442\u0432\u0435\u043D\u043D\u044B\u0439",
    SCOOTER = "\u0421\u0430\u043C\u043E\u043A\u0430\u0442",
    YANDEX = "Yandex Go",
    CITYMOBIL = "\u0421\u0438\u0442\u0438\u043C\u043E\u0431\u0438\u043B",
    UBER = "Uber",
    VIP_BUS = "\u0410\u0432\u0442\u043E\u0431\u0443\u0441 \u0434\u043E 9 \u043C\u0435\u0441\u0442",
    SMALL_BUS = "\u0410\u0432\u0442\u043E\u0431\u0443\u0441 \u043E\u0442 10 \u0434\u043E 21 \u043C\u0435\u0441\u0442\u0430",
    MIDDLE_BUS = "\u0410\u0432\u0442\u043E\u0431\u0443\u0441 \u043E\u0442 22 \u0434\u043E 41 \u043C\u0435\u0441\u0442",
    LARGE_BUS = "\u0410\u0432\u0442\u043E\u0431\u0443\u0441 \u043E\u0442 42 \u0434\u043E 55 \u043C\u0435\u0441\u0442",
    BUS = "\u0410\u0432\u0442\u043E\u0431\u0443\u0441"
}
export declare enum TransportTypeEnum {
    TAXI = "TAXI",
    PUBLIC = "PUBLIC",
    PERSONAL = "PERSONAL",
    CARSHARING = "CARSHARING",
    BICYCLE = "BICYCLE",
    WALK = "WALK",
    SCOOTER = "SCOOTER",
    DEDICATED = "DEDICATED",
    COURIER = "COURIER",
    INTERREGIONAL = "INTERREGIONAL",
    PRIVATE = "PRIVATE"
}
export declare enum TransportTypeTitlesEnum {
    TAXI = "\u0422\u0430\u043A\u0441\u0438",
    PUBLIC = "\u041E\u0431\u0449\u0435\u0441\u0442\u0432\u0435\u043D\u043D\u044B\u0439",
    PERSONAL = "\u041B\u0438\u0447\u043D\u044B\u0439",
    CARSHARING = "\u041A\u0430\u0440\u0448\u0435\u0440\u0438\u043D\u0433",
    BICYCLE = "\u0412\u0435\u043B\u043E\u0441\u0438\u043F\u0435\u0434",
    WALK = "\u041F\u0435\u0448\u043A\u043E\u043C",
    SCOOTER = "\u0421\u0430\u043C\u043E\u043A\u0430\u0442",
    DEDICATED = "\u0414\u043E\u0441\u0442\u0430\u0432\u043A\u0430",
    COURIER = "\u041A\u0443\u0440\u044C\u0435\u0440",
    INTERREGIONAL = "\u041C\u0435\u0436\u0440\u0435\u0433\u0438\u043E\u043D\u0430\u043B\u044C\u043D\u0430\u044F",
    PRIVATE = "\u0427\u0430\u0441\u0442\u043D\u0430\u044F"
}
export declare const TransportTypeOptions: LabeledValue<TransportTypeEnum>[];
export declare enum OsagoUploadResponseParams {
    vehicleMark,
    vehicleModel,
    ownerLastName,
    ownerFirstName,
    ownerMiddleName,
    endDate,
    insurerLastName,
    insurerFirstName,
    vehicleInfoNumber,
    contractSeries,
    contractNumber,
    vehicleInfoSeries,
    plateNumber,
    type,
    vinNumber,
    driversLastName,
    driversFirstName,
    driversMiddleName,
    driversSeries,
    driversNumber
}
export declare enum PersonalOwnerInformation {
    USER = "USER",
    SPOUSE = "SPOUSE",
    THIRD_PARTY = "THIRD_PARTY"
}
export declare enum EmployeeStatus {
    ACTIVE = "ACTIVE",
    INACTIVE = "INACTIVE"
}
export declare enum EmployeeStatusTitle {
    ACTIVE = "\u0410\u043A\u0442\u0438\u0432\u043D\u044B\u0439",
    INACTIVE = "\u041D\u0435\u0430\u043A\u0442\u0438\u0432\u043D\u044B\u0439"
}
export declare enum OrgStructureType {
    EXTERNAL = "EXTERNAL",
    INTERNAL = "INTERNAL"
}
export type EmployeeStatusType = keyof typeof EmployeeStatus;
export declare enum AuthSteps {
    password = 0,
    code = 1
}
export declare const SUPPORT_PHONE: {
    code: string;
    title: string;
};
export declare const SDO_SUPPORT_PHONE: {
    code: string;
    title: string;
};
export declare const X_CLIENT_TYPE = "CLIENT";
export declare enum CustomErrorCode {
    UNKNOWN = 1000,
    TYPES = 1001,
    TIMEOUT = 1002,
    CORS = 1003,
    UNDEFINED_FIELD = 1004
}
export declare const errorText: Record<number, {
    title: string;
    subtitle: string;
}>;
export declare const REQUESTS = "requests";
export declare const REQUEST = "request";
export declare const CANCEL = "cancel";
export declare const APPROVE = "approve";
export declare const EMPLOYEES = "employees";
export declare const DEPARTMENTS = "departments";
export declare const POSITIONS = "positions";
export declare const ADDRESSES = "addresses";
export declare const FAVORITE = "favorite";
export declare const GEO = "geo";
export declare const LIMITS = "limits";
export declare const DEPLIMITS = "deplimits";
export declare const ORGANIZATIONS = "organizations";
export declare const DELEGATES = "delegates";
export declare const CANDIDATES = "candidates";
export declare const SUPERVISORS = "supervisors";
export declare const DISPATCHER_ROOM = "dispatcher-room";
export declare const GET_SELF_EMPLOYEE = "/organizations/self";
export declare const CONSENT = "/organizations/self/consent/";
export declare const CONSENT_DISPATCHER = "/dispatcher-room/self/dispatcher/consent/";
export declare const GET_ALL_EMPLOYEES_BY_ORGANIZATION_PARAMS = "/organizations/:orgId/employees/";
export declare const GET_ALL_EMPLOYEES_BY_DEPARTMENTS_PARAMS = "/organizations/:orgId/departments/:depId/employees/";
export declare const EMPLOYEE_PARAMS = "/organizations/:orgId/departments/:depId/employees/:empId";
export declare const EMPLOYEES_PARAMS = "/organizations/:orgId/employees/:empIds";
export declare const EMPLOYEES_SEARCH_BY_ORG = "/organizations/:orgId/employeessearch?fio=:name";
export declare const EMPLOYEES_SEARCH = "/organizations/employees/search?fio=:name";
export declare const ORGANIZATIONS_ID = "organizations/:orgId";
export declare const DEPARTMENTS_ID = "departments/:depId";
export declare const POSITIONS_ID = "positions/:posId";
export declare const DELETE_DEPARTMENT = "/organizations/:orgId/departments/:depId";
export declare const DEPARTMENTS_ADD_PARAMS = "/organizations/:orgId/departments/";
export declare const DEPARTMENT_EDIT_PARAMS = "/organizations/:orgId/departments/:depId";
export declare const GET_ALL_DEPARTMENTS = "/organizations/:orgId/departments/";
export declare const GET_ALL_ORGANIZATIONS = "/organizations/";
export declare const GET_ALL_POSITIONS = "/organizations/:orgId/positions/";
export declare const GET_DEPARTMENT = "/organizations/:orgId/departments/:depId";
export declare const GET_ORGANIZATION = "/organizations/:orgId";
export declare const GET_POSITION = "/organizations/:orgId/positions/:posId";
export declare const SELF_ADDRESSES = "/organizations/self/addresses";
export declare const SELF_FREQUENT_ADDRESSES = "/organizations/self/addresses/frequently";
export declare const SELF_FREQUENT_ADDRESSES_PARAMS = "/organizations/self/addresses/frequently/:addressId";
export declare const SELF_FAVORITE_ADDRESSES = "/organizations/self/addresses/favorite";
export declare const SELF_FAVORITE_ADDRESSES_PARAMS = "/organizations/self/addresses/favorite/:addressId";
export declare const CALC_ROUTE = "/geo/route";
export declare const GET_ADDRESS_BY_COORDINATES = "/geo/address";
export declare const GET_COORDINATES_BY_ADDRESS = "/geo/address";
export declare const UPLOADFILE = "/organizations/dataimport/load/:orgId/:strategyMode/:nsi/:decSeparator/:separator";
export declare const PRELOADFILE = "/organizations/dataimport/preload/:orgId/:strategyMode/:nsi/:decSeparator/:separator";
export declare const APPROVE_REQUEST = "/limits/requests/approve";
export declare const GET_ACCOUNT_BONUSES = "/limits/bonus/";
export declare const GET_ALL_REQUESTS = "/limits/requests";
export declare const GET_REQUESTS_DEP = "/limits/requests/dep";
export declare const GET_REQUESTS_EMP = "/limits/requests/emp";
export declare const GET_SPENDINGS = "limits/spendings";
export declare const GET_DEPLIMITS = "/limits/deplimits/";
export declare const GET_DEPLIMITS_BY_DEP_AND_YEAR = "/limits/deplimits/getByDepartmentAndYear/:depId/year/:year";
export declare const GET_EMPLIMITS = "/limits/emplimits/";
export declare const GET_DEPLIMITS_BY_DEP = "/limits/deplimits/getByDepartment";
export declare const GET_LIMIT_SHARING = "/limits/limitsharing/getByLimit/full/";
export declare const GET_EMP_LIMIT = "/limits/emplimits/getByEmployeeAndYear/";
export declare const GET_LIMITS_REQUESTS_BY_AUTHOR = "/limits/requests/getByAuthor";
export declare const GET_LIMITS_REQUESTS_BY_APPROVER = "/limits/requests/getByApprover";
export declare const GET_ACTIVE_LIMITS_REQUESTS_BY_APPROVER = "/limits/requests/getActiveByApprover";
export declare const GET_OLD_LIMITS_REQUESTS_BY_APPROVER = "/limits/requests/getOldByApprover";
export declare const LIMIT_REQUEST_CANCEL = "/limits/requests/cancel";
export declare const GET_LIMIT_TRANSFER_HISTORY = "/limits/limittransferhistory/getByLimit";
export declare const GET_SIBLINGS = "/limits/deplimits/siblings/";
export declare const LIMITREQUESTS_CANCEL_PARAMS = "/limits/deplimits/cancel/:reqId";
export declare const LIMITREQUESTS_APPROVE_PARAMS = "/limits/deplimits/approve/:reqId";
export declare const LIMITREQUESTS_CRUD = "/limits/deplimits";
export declare const LIMITREQUESTS_PARAMS = "/limits/deplimits/:reqId";
export declare const GET_DELEGATES = "/organizations/:orgId/departments/:depId/delegates/supervisors/:supId";
export declare const ADD_DELEGATE = "/organizations/:orgId/departments/:depId/delegates/";
export declare const DELETE_DELEGATE = "/organizations/:orgId/departments/:depId/delegates/:delId";
export declare const GET_SELF_CONDIDATES_TO_DELEGATES = "/organizations/self/delegates/candidates/:transportType";
export declare const GET_CANDIDATES_IN_DELEGATES_PARAMS = "/organizations/:orgId/departments/:depId/delegates/candidates/:supId/:transTypeId";
export declare const TRANSPORTTYPES = "organizations/transport-types";
export declare const AVAILABLE_TRANSPORTTYPES = "organizations/transportorg/org/:orgId";
export declare const AVAILABLE_TRANSPORTTYPES_BY_SERVICE = "organizations/transportorg/:serviceType/org/:orgId";
