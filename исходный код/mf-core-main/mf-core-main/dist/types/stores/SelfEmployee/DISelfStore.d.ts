import { SelfEmployeeModel } from './models/SelfEmployeeModel';
import type { ISelfEmployeeStore } from './SelfEmployee.interface';
export declare class DISelfStore implements ISelfEmployeeStore {
    private service;
    private configStore;
    selfEmployee: SelfEmployeeModel;
    isRequiredPhone: boolean;
    get empId(): string;
    get depId(): string;
    get orgId(): string;
    get posId(): string;
    get supId(): string;
    getSelfEmployee: () => Promise<SelfEmployeeModel>;
    agreeWithPrivacyPolicy: (disp?: boolean) => Promise<void>;
    updatePhoneStatus: (value: boolean) => void;
    initStore(): void;
}
