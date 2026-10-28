import * as t from 'io-ts';
import { Dictionary } from 'lodash';
import { EmployeeModel } from './models/EmployeeModel';
import { OsagoUploadResponseParams, EmployeeStatus } from '../../constants/constants';
import { RequestCancelerSetter, RequestParams, Requester } from '../Http/http.interface';
export interface IEmployeeStore {
    selfEmployee: EmployeeModel;
    employeeAutocompleteSelected: EmployeeModel[];
    employeeAutocompleteList: EmployeeModel[];
    employeeListByOrg: EmployeeModel[];
    employeeListByDep: EmployeeModel[];
    employeeListByOrgMapped: Dictionary<EmployeeModel>;
    employeeListByDepMapped: Dictionary<EmployeeModel>;
    getEmployee(orgId: string, depId: string, empId: string): Promise<Employee>;
    getEmployeesByIds(orgId: string, empIds: string[]): Promise<Employee[]>;
    searchEmployees(name: string): void;
    searchEmployeesByName(name: string): Promise<EmployeeModel[]>;
    searchEmployeesByNameByOrg(name: string): Promise<EmployeeModel[]>;
    findEmployees(name: string): void;
    editEmployee(model: EmployeeModel): void;
    editPhone(phoneNumber: string): Promise<number>;
    onEmployeeSelect(value: string, index?: number, dontClear?: boolean): void;
    savedEmployee: EmployeeModel | undefined;
    initStore(): void;
    searchDepartmentEmployees: Requester<EmployeeModel[]>;
    searchOrganizationEmployees: Requester<EmployeeModel[]>;
}
export declare const IOHumanReadable: t.TypeC<{
    humanReadableId: t.StringC;
}>;
export type THumanReadable = t.TypeOf<typeof IOHumanReadable>;
export interface IEmployeeService {
    getAllEmployeesByOrganization(orgId: string): Promise<{
        content: Employee[];
    }>;
    getAllEmployeesByDepartment(orgId: string, depId: string): Promise<Employee[]>;
    getEmployeesByIds(orgId: string, empIds: string[]): Promise<Employee[]>;
    searchEmployeesByName(name: string): Promise<{
        content: Employee[];
    }>;
    searchEmployeesByNameByOrg(name: string): Promise<{
        content: Employee[];
    }>;
    getEmployee(orgId: string, depId: string, empId: string): Promise<Employee>;
    addEmployee(orgId: string, depId: string, employee: Employee): Promise<Employee>;
    editEmployee(orgId: string, depId: string, empId: string, employee: Employee): Promise<number>;
    editPhone(phoneNumber: string): Promise<number>;
    deleteEmployee(orgId: string, depId: string, empId: string): Promise<Employee>;
    searchDepartmentEmployees(orgId: string, depId: string, params: RequestParams, cancelerSetter?: RequestCancelerSetter): Promise<Employee[]>;
    searchOrganizationEmployees(orgId: string, params: RequestParams, cancelerSetter?: RequestCancelerSetter): Promise<Employee[]>;
}
export interface IEmployeeDetailed {
    id: string;
    humanReadableId: string;
    userId: string;
    fullNameString: string;
    nameWithInitials: string;
    personnelNumber: string;
    department: string;
    position: string;
    mobilePhone: string;
    email: string;
    supervisor: string;
    delegatedBy: string;
    availableTransportTypes: any[];
    organization: string;
}
export interface OsagoResponce {
    body: OsagoBodyResponce;
    success: boolean;
}
export interface OsagoBodyResponceErrors {
    code: string;
    title: string;
    text: string;
}
export interface OsagoBodyResponce {
    status: boolean;
    orientation: string;
    entities: OsagoEntitie[];
    errors?: OsagoBodyResponceErrors[];
}
export interface OsagoEntitie {
    entity_name: string;
    entity_value: string;
    confidence: number;
    entity_id?: number;
}
export interface IOsagoUploadResponseModel extends OsagoUploadResponseFields {
    status: OrUndefined<boolean>;
    orientation: OrUndefined<string>;
    collectByPersonalCar(employeeId: EmployeeModel): PersonalCar;
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
export interface EmployeeDetails {
    delegatedBy: string;
    department: string;
    organization: string;
    position: string;
    supervisor: string;
}
export declare const IOPersonalCar: t.IntersectionC<[t.PartialC<{
    employeeId: t.StringC;
    color: t.StringC;
}>, t.TypeC<{
    id: t.StringC;
    transportType: t.StringC;
    brandName: t.StringC;
    model: t.StringC;
    registrationNumber: t.StringC;
    registrationCertificate: t.StringC;
    engineVolume: t.NumberC;
    insuranceNumber: t.StringC;
    passengerSeatsCount: t.NumberC;
    ownerInfo: t.StringC;
    persDataAccept: t.BooleanC;
}>]>;
export type OrUndefined<T> = T | undefined;
export type OsagoProps = OrUndefined<OsagoEntitie>;
export type OsagoUploadResponseFields = {
    [key in keyof typeof OsagoUploadResponseParams]: OsagoProps;
};
export type PersonalCar = t.TypeOf<typeof IOPersonalCar>;
export type personalTypeCar = 'CAR' | 'MOTORCYCLE';
export declare const carTypeDescriptions: Record<personalTypeCar, string>;
export declare const getKeyValue: <T extends object, U extends keyof T>(key: U) => (obj: T) => any;
