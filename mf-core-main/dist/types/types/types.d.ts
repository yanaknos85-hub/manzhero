import { Container } from 'inversify';
import { IConfigStore } from '../stores/Config/Config.interface';
import { IEmployeeStore } from '../stores/Employee/Employee.interface';
import { IHttpService } from '../stores/Http/http.interface';
import { ResponseService } from '../stores/Http/Response.service';
import { ILogger } from '../stores/Logger/Logger.interface';
import { ISelfEmployeeStore } from '../stores/SelfEmployee/SelfEmployee.interface';
export interface IRootStore {
    authStore: any;
    configStore: IConfigStore;
    logger: ILogger;
    http: IHttpService;
    process: ResponseService;
    selfStore: ISelfEmployeeStore;
    employeeStore: IEmployeeStore;
}
export type RootCallBackProvider = <T>(container: Container) => T;
