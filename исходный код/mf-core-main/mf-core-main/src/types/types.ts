/* eslint-disable @typescript-eslint/no-unused-vars */

import { Container } from 'inversify';

import { IConfigStore } from '../stores/Config/Config.interface';
import { IAuthStore } from '../stores/Auth/Auth.interface';
import { IEmployeeStore } from '../stores/Employee/Employee.interface';
import { ICorporateStore } from '../stores/Corporate/Corporate.interface';
import { MappedStore } from '../stores/Mapped/DIMapped.store';
import { IHttpService } from '../stores/Http/http.interface';
import { ResponseService } from '../stores/Http/Response.service';
import { ILogger } from '../stores/Logger/Logger.interface';
import { ISelfEmployeeStore } from '../stores/SelfEmployee/SelfEmployee.interface';
import { IAddressStore } from '../stores/Address/Address.interface';
import { IGeoStore } from '../stores/Geo/Geo.interface';
import { IFilesStore } from '../stores/Files/Files.interface';
import { ILimitsStore } from '../stores/Limits/Limit.interface';
import { ILimitsRequestStore } from '../stores/Limits/LimitsRequest.interface';
import { IDelegatesStore } from '../stores/Delegates/Delegates.interface';
import { ITransportTypesStore } from '../stores/TransportTypes/TransportTypes.interface';

export interface IRootStore {
  authStore: any;
  configStore: IConfigStore;
  logger: ILogger;
  http: IHttpService;
  process: ResponseService;
  selfStore: ISelfEmployeeStore;
  employeeStore: IEmployeeStore;
  // corporateStore: ICorporateStore;
  // mappedStore: MappedStore;
  // addressStore: IAddressStore;
  // geoStore: IGeoStore;
  // filesStore: IFilesStore;
  // limitsStore: ILimitsStore;
  // limitsRequestStore: ILimitsRequestStore;
  // delegatesStore: IDelegatesStore;
  // transportTypesStore: ITransportTypesStore;
}

export type RootCallBackProvider = <T>(container: Container) => T;
