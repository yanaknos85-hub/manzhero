import { Container, interfaces } from 'inversify';
import { TYPES } from './ioc.types';
import { IS_DEV } from '../constants/constants';
import { IRootStore } from '../types/types';

import { IEmployeeService, IEmployeeStore } from '../stores/Employee/Employee.interface';
import { DIEmployeeService } from '../stores/Employee/DIEmployee.service';
import { HttpService } from '../stores/Http/HttpService';
import { IHttpService } from '../stores/Http/http.interface';
import { IResponseService, ResponseService } from '../stores/Http/Response.service';
import { IConfigStore } from '../stores/Config/Config.interface';
import { ILogger } from '../stores/Logger/Logger.interface';
import { CommonLogger } from '../stores/Logger/CommonLogger';
import { ProductionLogger } from '../stores/Logger/ProductionLogger';
import { ProductionDebugLogger } from '../stores/Logger/ProductionDebugLogger';
import { DIConfigStore } from '../stores/Config/DIConfigStore';
import { DISelfEmployeeService } from '../stores/SelfEmployee/SelfEmployee.service';
import { DISelfStore } from '../stores/SelfEmployee/DISelfStore';
import { DIEmployeeStore } from '../stores/Employee/DIEmployee.store';
import { ISelfEmployeeService, ISelfEmployeeStore } from '../stores/SelfEmployee/SelfEmployee.interface';

// import { MappedStore } from '../stores/Mapped/DIMapped.store';
// import { IAddressService, IAddressStore } from '../stores/Address/Address.interface';
// import { DIAddressService } from '../stores/Address/DIAddress.service';
// import { DIAddressStore } from '../stores/Address/DIAddress.store';
// import { DIGeoService } from '../stores/Geo/DIGeo.service';
// import { DIGeoStore } from '../stores/Geo/DIGeo.store';
// import { IGeoService, IGeoStore } from '../stores/Geo/Geo.interface';
// import { IFilesService, IFilesStore } from '../stores/Files/Files.interface';
// import { DIFilesService } from '../stores/Files/DIFiles.service';
// import { DIFilesStore } from '../stores/Files/DIFiles.store';
// import { DILimitsStore } from '../stores/Limits/DILimits.store';
// import { DILimitsService } from '../stores/Limits/DILimits.service';
// import { ILimitsService, ILimitsStore } from '../stores/Limits/Limit.interface';
// import { ILimitsRequestService, ILimitsRequestStore } from '../stores/Limits/LimitsRequest.interface';
// import { DILimitsRequestService } from '../stores/Limits/DILimitsRequest.service';
// import { DILimitsRequestStore } from '../stores/Limits/DILimitsRequest.store';
// import { ITransportTypesService, ITransportTypesStore } from '../stores/TransportTypes/TransportTypes.interface';
// import { DITransportTypesService } from '../stores/TransportTypes/DITransportTypes.service';
// import { DITransportTypesStore } from '../stores/TransportTypes/DITransportTypes.store';
// import { IDelegatesService, IDelegatesStore } from '../stores/Delegates/Delegates.interface';
// import { DIDelegatesService } from '../stores/Delegates/DIDelegates.service';
// import { DIDelegatesStore } from '../stores/Delegates/DIDelegates.store';
// import { ICorporateService, ICorporateStore } from '../stores/Corporate/Corporate.interface';
// import { DICorporateService } from '../stores/Corporate/DICorporate.service';
// import { DICorporateStore } from '../stores/Corporate/DICorporate.store';

import { INavigator, Navigator } from '../utils/navigator';
import { Token } from '../utils/token';

const createParentContainer = () => {
  const container: Container = new Container();

  if (IS_DEV) {
    container.bind<ILogger>(TYPES.ILogger).to(CommonLogger);
  } else {
    const { location } = window as Window;
    const isDebugMode = Boolean(new URLSearchParams(location.search).get('DEBUG'));

    container
      .bind<ILogger>(TYPES.ILogger)
      .to(ProductionLogger)
      .when(() => !isDebugMode);
    container
      .bind<ILogger>(TYPES.ILogger)
      .to(ProductionDebugLogger)
      .when(() => isDebugMode);
  }

  container.bind<IResponseService>(TYPES.IResponseService).to(ResponseService);
  container.bind<INavigator>(TYPES.INavigator).to(Navigator);
  container.bind<Token>(TYPES.Token).toConstantValue(new Token());

  container.bind<IConfigStore>(TYPES.IConfigStore).to(DIConfigStore).inSingletonScope();

  container.bind<ISelfEmployeeService>(TYPES.ISelfEmployeeService).to(DISelfEmployeeService);
  container.bind<ISelfEmployeeStore>(TYPES.ISelfEmployeeStore).to(DISelfStore).inSingletonScope();

  container.bind<IEmployeeService>(TYPES.IEmployeeService).to(DIEmployeeService);
  container.bind<IEmployeeStore>(TYPES.IEmployeeStore).to(DIEmployeeStore).inSingletonScope();

  // container.bind<ICorporateService>(TYPES.ICorporateService).to(DICorporateService);
  // container.bind<ICorporateStore>(TYPES.ICorporateStore).to(DICorporateStore).inSingletonScope();

  // container.bind<MappedStore>(TYPES.MappedStore).to(MappedStore).inSingletonScope();

  // container.bind<IAddressService>(TYPES.IAddressService).to(DIAddressService);
  // container.bind<IAddressStore>(TYPES.IAddressStore).to(DIAddressStore).inSingletonScope();

  // container.bind<IGeoService>(TYPES.IGeoService).to(DIGeoService);
  // container.bind<IGeoStore>(TYPES.IGeoStore).to(DIGeoStore).inSingletonScope();

  // container.bind<IFilesService>(TYPES.IFilesService).to(DIFilesService);
  // container.bind<IFilesStore>(TYPES.IFilesStore).to(DIFilesStore).inSingletonScope();

  // container.bind<ILimitsService>(TYPES.ILimitsServiceNew).to(DILimitsService);
  // container.bind<ILimitsStore>(TYPES.ILimitsStore).to(DILimitsStore).inSingletonScope();

  // container.bind<ILimitsRequestService>(TYPES.ILimitsRequestService).to(DILimitsRequestService);
  // container.bind<ILimitsRequestStore>(TYPES.ILimitsRequestStore).to(DILimitsRequestStore).inSingletonScope();

  // container.bind<ITransportTypesService>(TYPES.ITransportTypesService).to(DITransportTypesService);
  // container.bind<ITransportTypesStore>(TYPES.ITransportTypesStore).to(DITransportTypesStore).inSingletonScope();

  // container.bind<IDelegatesService>(TYPES.IDelegatesService).to(DIDelegatesService);
  // container.bind<IDelegatesStore>(TYPES.IDelegatesStore).to(DIDelegatesStore).inSingletonScope();

  container
    .bind<IHttpService>(TYPES.IHttpService)
    .toDynamicValue(
      (context: interfaces.Context) => new HttpService(
        context.container.get<IConfigStore>(TYPES.IConfigStore),
        context.container.get<ILogger>(TYPES.ILogger),
        context.container.get<INavigator>(TYPES.INavigator),
        context.container.get<Token>(TYPES.Token)
      )
    )
    .inSingletonScope();

  return container;
};

const сreateRootContainer = (): Container => {
  const container: Container = createParentContainer();
  container.parent = new Container();
  return container;
};

export const rootContainer = сreateRootContainer();

export const initRootStore = (rootContainer: Container) => {
  return {
    authStore: {},
    configStore: rootContainer.get<IConfigStore>(TYPES.IConfigStore),
    logger: rootContainer.get<ILogger>(TYPES.ILogger),
    http: rootContainer.get<IHttpService>(TYPES.IHttpService),
    process: rootContainer.get<ResponseService>(TYPES.IResponseService),
    selfStore: rootContainer.get<ISelfEmployeeStore>(TYPES.ISelfEmployeeStore),
    employeeStore: rootContainer.get<IEmployeeStore>(TYPES.IEmployeeStore),

    // this.corporateStore = this.rootContainer.get<ICorporateStore>(TYPES.ICorporateStore);
    // this.mappedStore = this.rootContainer.get<MappedStore>(TYPES.MappedStore);
    // this.addressStore = this.rootContainer.get<IAddressStore>(TYPES.MappedStore);
    // this.geoStore = this.rootContainer.get<IGeoStore>(TYPES.IGeoStore);
    // this.filesStore = this.rootContainer.get<IFilesStore>(TYPES.IFilesStore);
    // this.limitsStore = this.rootContainer.get<ILimitsStore>(TYPES.ILimitsStore);
    // this.limitsRequestStore = this.rootContainer.get<ILimitsRequestStore>(TYPES.ILimitsRequestStore);
    // this.delegatesStore = this.rootContainer.get<IDelegatesStore>(TYPES.IDelegatesStore);
    // this.transportTypesStore = this.rootContainer.get<ITransportTypesStore>(TYPES.ITransportTypesStore);
  } as IRootStore;
};
