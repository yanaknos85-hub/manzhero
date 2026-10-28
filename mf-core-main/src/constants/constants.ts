import { _env } from '../utils/utils';
import { LabeledValue } from '../utils/types';

export const NETWORK_LOOP = _env('REACT_APP_NETWORK_LOOP') || '';
export const IS_REMOTE = _env('REACT_APP_REMOTE') === 'TRUE';
export const IS_PROD = _env('PROD') === 'TRUE';
export const IS_DEV = _env('NODE_ENV') === 'development';

export const HOST = window.location.host;
export const IS_LOCAL = /localhost/.test(HOST);

const API_DOMAIN = 'api';
const API_PATH = 'api';
const API_SUBDOMAIN = HOST.replace(/^[^.]+\./g, '');

const EXCLUDED_DOMENS = ['sbertransport', 'nt', 'st', 'ift', 'psi', 'hf'];
const FIRST_DOMEN = HOST.split('.')[0];
const IS_EXECEPTION = EXCLUDED_DOMENS.includes(FIRST_DOMEN);

const IS_EXTERNAL = /sbertransport\.ru/.test(HOST);

// Хост в зависимости от стенда на котором запускается - заменяется на прямой адрес api
// client.ift.sbertransport.sigma.sbrf.ru -> заменяем api.ift.sbertransport.sigma.sbrf.ru
let apiUrl = `//${API_DOMAIN}.${API_SUBDOMAIN}/${API_PATH}`;

// Хост, начинающийся с названия домена из списка EXCLUDED_DOMENS - дополняется поддоменом api.
// sbertransport.sigma.sbrf.ru -> добавляем api.sbertransport.sigma.sbrf.ru
if (IS_EXECEPTION) {
  apiUrl = `//${API_DOMAIN}.${HOST}/${API_PATH}`;
}

// Хост ака Внешний, заканчивающийся на sbertransport.ru - не меняется!
// sbertransport.ru  -> не трогаем
// Локалхост также - не меняется и подчиняется правилам прокси дев сервера (scripts/devServer.js)
if (IS_EXTERNAL || IS_LOCAL) {
  apiUrl = `/${API_PATH}`;
}

export const API_URL = apiUrl;

export const MOCKED_API_PREFIX = _env('REACT_APP_MOCKED_API_PREFIX') || 'mock/';

export enum routes {
  Failure = '/oauth/failure',
  SudirApi = '/api/sudir/oauth2/authorization/sudir',
}

export enum SYSTEM_MESSAGES {
  authGlobalError = 'Проблемы с авторизацией. authStore не инициализирован',
  employeeEditSuccess = 'Информация Вашего профиля изменена',
  savingWithAddressDuplicates = 'Пункт отправления и назначения не должны совпадать. Добавьте промежуточный адрес или измените пункт назначения!',
  savingWithWrongSum = 'К сожалению, на текущий момент, резервирование для сотрудника на общественный вид транспорта невозможно на указанную сумму',
  departmentAddSuccess = 'Подразделение успешно создано',
  departmentDeleteSuccess = 'Подразделение успешно удалено',
  departmentEditSuccess = 'Информация о подразделении успешно сохранена',
  addressLableIsNotUniq = 'Адрес с таким названием уже существует',
  addressIsNotUniq = 'Указанный адрес уже существует',
  addressDeleteSuccess = 'Адрес успешно удалён',
  addressAddSuccess = 'Адрес успешно добавлен',
  limitRequestCreateSuccess = 'Заявка на лимит успешно создана',
  limitRequestEditSuccess = 'Заявка на лимит успешно изменена',
  limitRequestCancelSuccess = 'Заявка на лимит успешно отменена',
  limitIsApproved = 'Заявка на лимит одобрена',
  simulationSuccess = 'Симуляция импорта успешно завершена',
  fileUploadSuccess = 'Файл успешно загружен',
  delegateSuccessfull = 'Делегат успешно назначен',
  delegateDeleteSuccess = 'Делегат успешно удален',
}

export enum TaxiClassEnum {
  ECONOMY = 'ECONOMY',
  COMFORT = 'COMFORT',
  COMFORT_PLUS = 'COMFORT_PLUS',
  PERSONAL = 'PERSONAL',
  BUSINESS = 'BUSINESS',
  TAXI = 'TAXI',
  CARSHARING = 'CARSHARING',
  BICYCLE = 'BICYCLE',
  WALK = 'WALK',
  PUBLIC = 'PUBLIC',
  SCOOTER = 'SCOOTER',
  YANDEX = 'YANDEX',
  CITYMOBIL = 'CITYMOBIL',
  UBER = 'UBER',

  VIP_BUS = 'VIP_BUS',
  SMALL_BUS = 'SMALL_BUS',
  MIDDLE_BUS = 'MIDDLE_BUS',
  LARGE_BUS = 'LARGE_BUS',
}

export enum TaxiClassTitlesEnum {
  TAXI = 'Такси',
  ECONOMY = 'Эконом',
  COMFORT = 'Комфорт',
  COMFORT_PLUS = 'Комфорт+',
  PERSONAL = 'Личный',
  BUSINESS = 'Бизнес',
  CARSHARING = 'Каршеринг',
  BICYCLE = 'Велосипед',
  WALK = 'Пешком',
  PUBLIC = 'Общественный',
  SCOOTER = 'Самокат',
  YANDEX = 'Yandex Go',
  CITYMOBIL = 'Ситимобил',
  UBER = 'Uber',

  VIP_BUS = 'Автобус до 9 мест',
  SMALL_BUS = 'Автобус от 10 до 21 места',
  MIDDLE_BUS = 'Автобус от 22 до 41 мест',
  LARGE_BUS = 'Автобус от 42 до 55 мест',
  BUS = 'Автобус', // stub for all buses
}

export enum TransportTypeEnum {
  TAXI = 'TAXI',
  PUBLIC = 'PUBLIC',
  PERSONAL = 'PERSONAL',
  CARSHARING = 'CARSHARING',
  BICYCLE = 'BICYCLE',
  WALK = 'WALK',
  SCOOTER = 'SCOOTER',
  DEDICATED = 'DEDICATED',
  COURIER = 'COURIER',
  INTERREGIONAL = 'INTERREGIONAL',
  PRIVATE = 'PRIVATE',
}

export enum TransportTypeTitlesEnum {
  TAXI = 'Такси',
  PUBLIC = 'Общественный',
  PERSONAL = 'Личный',
  CARSHARING = 'Каршеринг',
  BICYCLE = 'Велосипед',
  WALK = 'Пешком',
  SCOOTER = 'Самокат',
  DEDICATED = 'Доставка',
  COURIER = 'Курьер',
  INTERREGIONAL = 'Межрегиональная',
  PRIVATE = 'Частная',
}

export const TransportTypeOptions: LabeledValue<TransportTypeEnum>[] = [
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

export enum OsagoUploadResponseParams {
  vehicleMark = 'vehicleInfo.vehicleMark' as any,
  vehicleModel = 'vehicleInfo.vehicleModel' as any,
  ownerLastName = 'owner.personalData.lastName' as any,
  ownerFirstName = 'owner.personalData.firstName' as any,
  ownerMiddleName = 'owner.personalData.middleName' as any,
  endDate = 'contractConditions.endDate' as any,
  insurerLastName = 'insurer.personalData.lastName' as any,
  insurerFirstName = 'insurer.personalData.firstName' as any,
  vehicleInfoNumber = 'vehicleInfo.vehicleDocuments.number' as any,
  contractSeries = 'contractSeries' as any,
  contractNumber = 'contractNumber' as any,
  vehicleInfoSeries = 'vehicleInfo.vehicleDocuments.series' as any,
  plateNumber = 'vehicleInfo.plateNumber' as any,
  type = 'vehicleInfo.vehicleDocuments.type' as any,
  vinNumber = 'vehicleInfo.vinNumber' as any,
  driversLastName = 'drivers.personalData.lastName' as any,
  driversFirstName = 'drivers.personalData.firstName' as any,
  driversMiddleName = 'drivers.personalData.middleName' as any,
  driversSeries = 'drivers.driverLicenses.series' as any,
  driversNumber = 'drivers.driverLicenses.number' as any,
}

export enum PersonalOwnerInformation {
  USER = 'USER',
  SPOUSE = 'SPOUSE',
  THIRD_PARTY = 'THIRD_PARTY',
}

export enum EmployeeStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
}

export enum EmployeeStatusTitle {
  ACTIVE = 'Активный',
  INACTIVE = 'Неактивный',
}

export enum OrgStructureType {
  EXTERNAL = 'EXTERNAL',
  INTERNAL = 'INTERNAL',
}

export type EmployeeStatusType = keyof typeof EmployeeStatus;

export enum AuthSteps {
  password,
  code,
}

export const SUPPORT_PHONE = {
  code: '88007074882',
  title: '8-800-707-48-82',
};

export const SDO_SUPPORT_PHONE = {
  code: '88001000302',
  title: '8 (800) 100-03-02',
};

export const X_CLIENT_TYPE = 'CLIENT';

export enum CustomErrorCode {
  UNKNOWN = 1000,
  TYPES = 1001,
  TIMEOUT = 1002,
  CORS = 1003,
  UNDEFINED_FIELD = 1004,
}

export const errorText: Record<number, { title: string; subtitle: string }> = {
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
  },

  // Все ошибки, для которых нет текста, выводятся как CustomErrorCode.UNKNOWN
  [CustomErrorCode.UNKNOWN]: {
    title: 'Упс... Ошибка системы',
    subtitle: 'Пожалуйста, обратитесь в поддержку',
  },
  [CustomErrorCode.TYPES]: {
    title: 'Упс... Ошибка отображения данных',
    subtitle: 'Пожалуйста, обратитесь в поддержку',
  },
  [CustomErrorCode.TIMEOUT]: {
    title: 'Упс... Слишком большой объем данных',
    subtitle: 'Пожалуйста, обратитесь в поддержку',
  },
};

export const REQUESTS = 'requests';
export const REQUEST = 'request';
export const CANCEL = 'cancel';
export const APPROVE = 'approve';
export const EMPLOYEES = 'employees';
export const DEPARTMENTS = 'departments';
export const POSITIONS = 'positions';
export const ADDRESSES = 'addresses';
export const FAVORITE = 'favorite';
export const GEO = 'geo';
export const LIMITS = 'limits';
export const DEPLIMITS = 'deplimits';
export const ORGANIZATIONS = 'organizations';
export const DELEGATES = 'delegates';
export const CANDIDATES = 'candidates';
export const SUPERVISORS = 'supervisors';
export const DISPATCHER_ROOM = 'dispatcher-room';

export const GET_SELF_EMPLOYEE = `/${ORGANIZATIONS}/self`;
export const CONSENT = `/${ORGANIZATIONS}/self/consent/`;
export const CONSENT_DISPATCHER = `/${DISPATCHER_ROOM}/self/dispatcher/consent/`;

export const GET_ALL_EMPLOYEES_BY_ORGANIZATION_PARAMS = `/${ORGANIZATIONS}/:orgId/${EMPLOYEES}/`;
export const GET_ALL_EMPLOYEES_BY_DEPARTMENTS_PARAMS = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${EMPLOYEES}/`;
export const EMPLOYEE_PARAMS = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${EMPLOYEES}/:empId`;
export const EMPLOYEES_PARAMS = `/${ORGANIZATIONS}/:orgId/${EMPLOYEES}/:empIds`;
export const EMPLOYEES_SEARCH_BY_ORG = `/${ORGANIZATIONS}/:orgId/employeessearch?fio=:name`;
export const EMPLOYEES_SEARCH = `/${ORGANIZATIONS}/employees/search?fio=:name`;

export const ORGANIZATIONS_ID = `${ORGANIZATIONS}/:orgId`;
export const DEPARTMENTS_ID = `${DEPARTMENTS}/:depId`;
export const POSITIONS_ID = `${POSITIONS}/:posId`;

export const DELETE_DEPARTMENT = `/${ORGANIZATIONS_ID}/${DEPARTMENTS_ID}`;
export const DEPARTMENTS_ADD_PARAMS = `/${ORGANIZATIONS_ID}/${DEPARTMENTS}/`;
export const DEPARTMENT_EDIT_PARAMS = `/${ORGANIZATIONS_ID}/${DEPARTMENTS_ID}`;
export const GET_ALL_DEPARTMENTS = `/${ORGANIZATIONS_ID}/${DEPARTMENTS}/`;

export const GET_ALL_ORGANIZATIONS = `/${ORGANIZATIONS}/`;
export const GET_ALL_POSITIONS = `/${ORGANIZATIONS_ID}/${POSITIONS}/`;
export const GET_DEPARTMENT = `/${ORGANIZATIONS_ID}/${DEPARTMENTS_ID}`;
export const GET_ORGANIZATION = `/${ORGANIZATIONS_ID}`;
export const GET_POSITION = `/${ORGANIZATIONS_ID}/${POSITIONS_ID}`;

export const SELF_ADDRESSES = `${GET_SELF_EMPLOYEE}/${ADDRESSES}`;

export const SELF_FREQUENT_ADDRESSES = `${SELF_ADDRESSES}/frequently`;
export const SELF_FREQUENT_ADDRESSES_PARAMS = `${SELF_ADDRESSES}/frequently/:addressId`;

export const SELF_FAVORITE_ADDRESSES = `${SELF_ADDRESSES}/${FAVORITE}`;
export const SELF_FAVORITE_ADDRESSES_PARAMS = `${SELF_FAVORITE_ADDRESSES}/:addressId`;

export const CALC_ROUTE = `/${GEO}/route`;
export const GET_ADDRESS_BY_COORDINATES = `/${GEO}/address`;
export const GET_COORDINATES_BY_ADDRESS = `${GET_ADDRESS_BY_COORDINATES}`;

export const UPLOADFILE = `/${ORGANIZATIONS}/dataimport/load/:orgId/:strategyMode/:nsi/:decSeparator/:separator`;
export const PRELOADFILE = `/${ORGANIZATIONS}/dataimport/preload/:orgId/:strategyMode/:nsi/:decSeparator/:separator`;

export const APPROVE_REQUEST = `/${LIMITS}/requests/approve`;
export const GET_ACCOUNT_BONUSES = `/${LIMITS}/bonus/`;
export const GET_ALL_REQUESTS = `/${LIMITS}/requests`;
export const GET_REQUESTS_DEP = `${GET_ALL_REQUESTS}/dep`;
export const GET_REQUESTS_EMP = `${GET_ALL_REQUESTS}/emp`;

export const GET_SPENDINGS = `${LIMITS}/spendings`;
export const GET_DEPLIMITS = `/${LIMITS}/deplimits/`;
export const GET_DEPLIMITS_BY_DEP_AND_YEAR = `/${LIMITS}/deplimits/getByDepartmentAndYear/:depId/year/:year`;
export const GET_EMPLIMITS = `/${LIMITS}/emplimits/`;
export const GET_DEPLIMITS_BY_DEP = `/${LIMITS}/deplimits/getByDepartment`;
export const GET_LIMIT_SHARING = `/${LIMITS}/limitsharing/getByLimit/full/`;
export const GET_EMP_LIMIT = `/${LIMITS}/emplimits/getByEmployeeAndYear/`;
export const GET_LIMITS_REQUESTS_BY_AUTHOR = `/${LIMITS}/requests/getByAuthor`;
export const GET_LIMITS_REQUESTS_BY_APPROVER = `/${LIMITS}/requests/getByApprover`;
export const GET_ACTIVE_LIMITS_REQUESTS_BY_APPROVER = `/${LIMITS}/requests/getActiveByApprover`;
export const GET_OLD_LIMITS_REQUESTS_BY_APPROVER = `/${LIMITS}/requests/getOldByApprover`;
export const LIMIT_REQUEST_CANCEL = `/${LIMITS}/${REQUESTS}/${CANCEL}`;
export const GET_LIMIT_TRANSFER_HISTORY = `/${LIMITS}/limittransferhistory/getByLimit`;

export const GET_SIBLINGS = `${GET_DEPLIMITS}siblings/`;
export const LIMITREQUESTS_CANCEL_PARAMS = `/limits/${DEPLIMITS}/${CANCEL}/:reqId`;
export const LIMITREQUESTS_APPROVE_PARAMS = `/limits/${DEPLIMITS}/${APPROVE}/:reqId`;

export const LIMITREQUESTS_CRUD = `/limits/${DEPLIMITS}`;
export const LIMITREQUESTS_PARAMS = `/limits/${DEPLIMITS}/:reqId`;

export const GET_DELEGATES = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${DELEGATES}/${SUPERVISORS}/:supId`;
export const ADD_DELEGATE = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${DELEGATES}/`;
export const DELETE_DELEGATE = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${DELEGATES}/:delId`;
export const GET_SELF_CONDIDATES_TO_DELEGATES = '/organizations/self/delegates/candidates/:transportType';
export const GET_CANDIDATES_IN_DELEGATES_PARAMS = `/${ORGANIZATIONS}/:orgId/${DEPARTMENTS}/:depId/${DELEGATES}/${CANDIDATES}/:supId/:transTypeId`;

export const TRANSPORTTYPES = `${ORGANIZATIONS}/transport-types`;
export const AVAILABLE_TRANSPORTTYPES = `${ORGANIZATIONS}/transportorg/org/:orgId`;
export const AVAILABLE_TRANSPORTTYPES_BY_SERVICE = `${ORGANIZATIONS}/transportorg/:serviceType/org/:orgId`;
