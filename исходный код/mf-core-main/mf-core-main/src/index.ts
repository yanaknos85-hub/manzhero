// _____ APP ______

import 'reflect-metadata';

// Utils

export { logFatalError } from './utils/logError';

// Hooks
export { useHistory } from './hooks/useHistory';

// IoC

export type { IRootStore, RootCallBackProvider } from './types/types';

export { TYPES } from './ioc/ioc.types';
export {
  AppStore,
  useAppStoreContext,
  useMfContext,
  initProviders
} from './ioc/ioc.context';

// Stores

export type { IConfigStore } from './stores/Config/Config.interface';

export { ResponseService } from './stores/Http/Response.service';
export type { IResponseService } from './stores/Http/Response.service';

export { HttpService } from './stores/Http/HttpService';
export type {
  IHttpService,
  IServerErrorResponseData,
  RequestParams,
  RequestCanceler,
  RequestCancelerSetter,
  Requester,
  IAxiosRequestConfigExtended
} from './stores/Http/http.interface';

export type { INavigator } from './utils/navigator';
export type { ILogger } from './stores/Logger/Logger.interface';

export { DIEmployeeStore } from './stores/Employee/DIEmployee.store';
export { DIEmployeeService } from './stores/Employee/DIEmployee.service';
export { EmployeeModel } from './stores/Employee/models/EmployeeModel';
export { EmployeeDetailedModel } from './stores/Employee/models/DetailedEmployeeModel';
export { OsagoUploadResponseModel } from './stores/Employee/models/OsagoUploadResponseModel';
export {
  Employee, IOPersonalCar, IOHumanReadable, carTypeDescriptions
} from './stores/Employee/Employee.interface';
export type {
  IEmployeeStore,
  OrUndefined,
  PersonalCar,
  IOsagoUploadResponseModel,
  OsagoResponce,
  OsagoBodyResponce,
  OsagoBodyResponceErrors
} from './stores/Employee/Employee.interface';

export { DISelfStore } from './stores/SelfEmployee/DISelfStore';
export { DISelfEmployeeService } from './stores/SelfEmployee/SelfEmployee.service';
export { SelfEmployeeModel } from './stores/SelfEmployee/models/SelfEmployeeModel';
export { SelfEmployee } from './stores/SelfEmployee/SelfEmployee.interface';
export type { ISelfEmployeeService, ISelfEmployeeStore } from './stores/SelfEmployee/SelfEmployee.interface';

export { DICorporateStore } from './stores/Corporate/DICorporate.store';
export { DICorporateService } from './stores/Corporate/DICorporate.service';
export { DepartmentModel } from './stores/Corporate/models/Department.model';

export { DepartmentDetailedModel } from './stores/Corporate/models/DepartmentDetailed.model';
export type {
  ICorporateService, ICorporateStore, IDepartment, TDepartment
} from './stores/Corporate/Corporate.interface';

export { MappedStore } from './stores/Mapped/DIMapped.store';

export { DIAddressService } from './stores/Address/DIAddress.service';
export { DIAddressStore } from './stores/Address/DIAddress.store';
export type { IAddressService, IAddressStore } from './stores/Address/Address.interface';

export { WaypointModel } from './models/geo/Waypoint.model';
export { RouteModel } from './models/geo/Route.model';
export { DIGeoService } from './stores/Geo/DIGeo.service';
export { DIGeoStore } from './stores/Geo/DIGeo.store';
export { IOWaypoint } from './models/geo/types';
export type { TWaypoint, LatLngTuple, Segment } from './models/geo/types';
export type { IGeoService, IGeoStore } from './stores/Geo/Geo.interface';

export { DIFilesService } from './stores/Files/DIFiles.service';
export { DIFilesStore } from './stores/Files/DIFiles.store';
export type { IFilesService, IFilesStore } from './stores/Files/Files.interface';

export { DILimitsStore } from './stores/Limits/DILimits.store';
export { DILimitsService } from './stores/Limits/DILimits.service';
export { LimitModel } from './stores/Limits/Models/LimitModel';
export { LimitModelDetailed } from './stores/Limits/Models/LimitModelDetailed';
export { LimitColorsPercentModel } from './stores/Limits/Models/LimitColorsPercent.model';
export { LIMIT_REQUEST_STATUS, LIMIT_SERVICE_TYPE, LIMIT_TYPE } from './stores/Limits/Limit.interface';
export type {
  ILimitsService, ILimitsStore, LimitSharing, Limit, BonusesRequest
} from './stores/Limits/Limit.interface';

export { DILimitsRequestService } from './stores/Limits/DILimitsRequest.service';
export { DILimitsRequestStore } from './stores/Limits/DILimitsRequest.store';
export type {
  ILimitsRequestService, ILimitsRequestStore, LimitEmpRequest, LimitSendRequest
} from './stores/Limits/LimitsRequest.interface';

export { DITransportTypesService } from './stores/TransportTypes/DITransportTypes.service';
export { DITransportTypesStore } from './stores/TransportTypes/DITransportTypes.store';
export type { ITransportTypesService, ITransportTypesStore } from './stores/TransportTypes/TransportTypes.interface';

export { DIDelegatesService } from './stores/Delegates/DIDelegates.service';
export { DIDelegatesStore } from './stores/Delegates/DIDelegates.store';
export type { IDelegatesService, IDelegatesStore } from './stores/Delegates/Delegates.interface';

// Test

export { CustomSuspense } from './Test';
