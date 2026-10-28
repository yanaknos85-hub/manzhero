import { Container } from 'inversify';
import { TYPES } from './ioc.types';
import { IS_DEV } from '../constants/constants';
import { DIEmployeeService } from '../stores/Employee/DIEmployee.service';
import { HttpService } from '../stores/Http/HttpService';
import { ResponseService } from '../stores/Http/Response.service';
import { CommonLogger } from '../stores/Logger/CommonLogger';
import { ProductionLogger } from '../stores/Logger/ProductionLogger';
import { ProductionDebugLogger } from '../stores/Logger/ProductionDebugLogger';
import { DIConfigStore } from '../stores/Config/DIConfigStore';
import { DISelfEmployeeService } from '../stores/SelfEmployee/SelfEmployee.service';
import { DISelfStore } from '../stores/SelfEmployee/DISelfStore';
import { DIEmployeeStore } from '../stores/Employee/DIEmployee.store';
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
import { Navigator } from '../utils/navigator';
import { Token } from '../utils/token';
var createParentContainer = function () {
    var container = new Container();
    if (IS_DEV) {
        container.bind(TYPES.ILogger).to(CommonLogger);
    }
    else {
        var location_1 = window.location;
        var isDebugMode_1 = Boolean(new URLSearchParams(location_1.search).get('DEBUG'));
        container
            .bind(TYPES.ILogger)
            .to(ProductionLogger)
            .when(function () { return !isDebugMode_1; });
        container
            .bind(TYPES.ILogger)
            .to(ProductionDebugLogger)
            .when(function () { return isDebugMode_1; });
    }
    container.bind(TYPES.IResponseService).to(ResponseService);
    container.bind(TYPES.INavigator).to(Navigator);
    container.bind(TYPES.Token).toConstantValue(new Token());
    container.bind(TYPES.IConfigStore).to(DIConfigStore).inSingletonScope();
    container.bind(TYPES.ISelfEmployeeService).to(DISelfEmployeeService);
    container.bind(TYPES.ISelfEmployeeStore).to(DISelfStore).inSingletonScope();
    container.bind(TYPES.IEmployeeService).to(DIEmployeeService);
    container.bind(TYPES.IEmployeeStore).to(DIEmployeeStore).inSingletonScope();
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
        .bind(TYPES.IHttpService)
        .toDynamicValue(function (context) { return new HttpService(context.container.get(TYPES.IConfigStore), context.container.get(TYPES.ILogger), context.container.get(TYPES.INavigator), context.container.get(TYPES.Token)); })
        .inSingletonScope();
    return container;
};
var сreateRootContainer = function () {
    var container = createParentContainer();
    container.parent = new Container();
    return container;
};
export var rootContainer = сreateRootContainer();
export var initRootStore = function (rootContainer) {
    return {
        authStore: {},
        configStore: rootContainer.get(TYPES.IConfigStore),
        logger: rootContainer.get(TYPES.ILogger),
        http: rootContainer.get(TYPES.IHttpService),
        process: rootContainer.get(TYPES.IResponseService),
        selfStore: rootContainer.get(TYPES.ISelfEmployeeStore),
        employeeStore: rootContainer.get(TYPES.IEmployeeStore),
        // this.corporateStore = this.rootContainer.get<ICorporateStore>(TYPES.ICorporateStore);
        // this.mappedStore = this.rootContainer.get<MappedStore>(TYPES.MappedStore);
        // this.addressStore = this.rootContainer.get<IAddressStore>(TYPES.MappedStore);
        // this.geoStore = this.rootContainer.get<IGeoStore>(TYPES.IGeoStore);
        // this.filesStore = this.rootContainer.get<IFilesStore>(TYPES.IFilesStore);
        // this.limitsStore = this.rootContainer.get<ILimitsStore>(TYPES.ILimitsStore);
        // this.limitsRequestStore = this.rootContainer.get<ILimitsRequestStore>(TYPES.ILimitsRequestStore);
        // this.delegatesStore = this.rootContainer.get<IDelegatesStore>(TYPES.IDelegatesStore);
        // this.transportTypesStore = this.rootContainer.get<ITransportTypesStore>(TYPES.ITransportTypesStore);
    };
};
