import { SelfEmployeeModel } from './models/SelfEmployeeModel';
import { ISelfEmployeeService } from './SelfEmployee.interface';
export declare class DISelfEmployeeService implements ISelfEmployeeService {
    private configStore;
    private http;
    private process;
    private apiPrefix;
    getSelfEmployee: () => Promise<SelfEmployeeModel>;
    agreeWithPrivacyPolicy: (disp?: boolean) => Promise<unknown>;
}
