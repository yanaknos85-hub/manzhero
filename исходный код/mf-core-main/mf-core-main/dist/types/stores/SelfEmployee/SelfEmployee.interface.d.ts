import * as t from 'io-ts';
import { SelfEmployeeModel } from './models/SelfEmployeeModel';
import { OrgStructureType } from './../../constants/constants';
export declare enum EmployeeStatus {
    ACTIVE = "ACTIVE",
    INACTIVE = "INACTIVE"
}
export type EmployeeStatusType = keyof typeof EmployeeStatus;
export declare const IOHumanReadable: t.TypeC<{
    humanReadableId: t.StringC;
}>;
export interface ISelfEmployeeStore {
    selfEmployee: SelfEmployeeModel;
    isRequiredPhone?: boolean;
    empId: string;
    depId: string;
    posId: string;
    supId: string;
    orgId: string;
    getSelfEmployee(): Promise<SelfEmployeeModel>;
    agreeWithPrivacyPolicy(disp?: boolean): Promise<void>;
    updatePhoneStatus(value: boolean): void;
    initStore(): void;
}
export interface ISelfEmployeeService {
    getSelfEmployee(): Promise<SelfEmployeeModel>;
    agreeWithPrivacyPolicy(disp?: boolean): Promise<unknown>;
}
export declare const Employee: t.IntersectionC<[t.TypeC<{
    humanReadableId: t.StringC;
}>, t.TypeC<{
    id: t.StringC;
    userId: t.StringC;
    firstName: t.StringC;
    lastName: t.StringC;
    personnelNumber: t.StringC;
    departmentId: t.StringC;
    organizationId: t.StringC;
    positionId: t.StringC;
}>, t.PartialC<{
    patronymic: t.StringC;
    status: t.KeyofC<typeof EmployeeStatus>;
    mobilePhone: t.StringC;
    email: t.StringC;
    supervisorId: t.StringC;
    delegatedById: t.StringC;
    availableTransportTypes: t.UnknownArrayC;
    personalCars: t.UnknownArrayC;
    approvals: t.NumberC;
    positionName: t.StringC;
    departmentName: t.StringC;
}>]>;
export type Employee = t.TypeOf<typeof Employee>;
export declare const SelfEmployee: t.IntersectionC<[t.IntersectionC<[t.TypeC<{
    humanReadableId: t.StringC;
}>, t.TypeC<{
    id: t.StringC;
    userId: t.StringC;
    firstName: t.StringC;
    lastName: t.StringC;
    personnelNumber: t.StringC;
    departmentId: t.StringC;
    organizationId: t.StringC;
    positionId: t.StringC;
}>, t.PartialC<{
    patronymic: t.StringC;
    status: t.KeyofC<typeof EmployeeStatus>;
    mobilePhone: t.StringC;
    email: t.StringC;
    supervisorId: t.StringC;
    delegatedById: t.StringC;
    availableTransportTypes: t.UnknownArrayC;
    personalCars: t.UnknownArrayC;
    approvals: t.NumberC;
    positionName: t.StringC;
    departmentName: t.StringC;
}>]>, t.TypeC<{
    consent: t.BooleanC;
    orgStructureType: t.Type<OrgStructureType, OrgStructureType, unknown>;
}>, t.PartialC<{
    isDepartmentHead: t.BooleanC;
    isPhoneConfirmed: t.BooleanC;
}>]>;
export type SelfEmployee = t.TypeOf<typeof SelfEmployee>;
