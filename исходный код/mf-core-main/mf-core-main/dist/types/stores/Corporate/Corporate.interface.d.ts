import { EmployeeModel } from '../Employee/models/EmployeeModel';
import * as t from 'io-ts';
import { Dictionary } from 'lodash';
import { EmployeeStatus, EmployeeStatusTitle, TaxiClassEnum } from '../../constants/constants';
import { DepartmentModel } from './models/Department.model';
import { DepartmentDetailedModel } from './models/DepartmentDetailed.model';
import { OrganizationNormalizedModel } from './models/OrganizationNormalized.model';
export interface ICorporateStore {
    selfEmployee: EmployeeModel | undefined;
    organizations: IOrganization[];
    departments: DepartmentModel[];
    positions: IPosition[];
    organizationsMapped: Dictionary<OrganizationNormalizedModel>;
    departmentsMapped: Dictionary<TDepartment>;
    positionsMapped: Dictionary<IPosition>;
    savedDepartment?: DepartmentDetailedModel;
    initStore(): void;
    clearSavedDepartment(): void;
    editSavedDepartment(model: DepartmentDetailedModel): void;
    refreshDepartments(): void;
    getDepartment(depId: string): TDepartment;
    editDepartment(model: DepartmentDetailedModel): void;
    deleteDepartment(depId: string): void;
}
export interface ICorporateService {
    getAllOrganizations(): Promise<IOrganization[]>;
    getAllDepartments(orgId: string): Promise<{
        content: TDepartment[];
    }>;
    getAllPositions(orgId: string): Promise<IPosition[]>;
    getOrganization(orgId: string): Promise<IOrganization>;
    getPosition(orgId: string, depId: string, posId: string): Promise<IPosition>;
    getDepartment(orgId: string, depId: string): Promise<TDepartment>;
    addDepartment(orgId: string, model: DepartmentDetailedModel): Promise<TDepartment>;
    editDepartment(orgId: string, depId: string, model: DepartmentDetailedModel): Promise<number>;
    deleteDepartment(orgId: string, depId: string): Promise<number>;
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
export interface IOrganizationPosition {
    id: string;
    positionName: string;
    organizationId: string;
}
export interface IOrganization {
    id: string;
    officialName: string;
    address: string;
    positions: IShortPosition[];
    departments: IShortDepartment[];
}
export interface IShortDepartment {
    id: string;
    departmentName: string;
}
export declare const IODepartmentEmployee: t.TypeC<{
    id: t.StringC;
    firstName: t.StringC;
    lastName: t.StringC;
    personnelNumber: t.StringC;
}>;
export type TDepartmentEmployee = t.TypeOf<typeof IODepartmentEmployee>;
export type IDepartments = Record<string, IDepartment[]>;
export interface IDepartment {
    id?: string;
    humanReadableId?: string;
    departmentHeadId?: string;
    parentId?: string;
    location?: string;
    code: string;
    organizationId: string;
    departmentName: string;
    fullStructurePath: string;
    children: IDepartment[];
    employees: TDepartmentEmployee[];
    status: EmployeeStatus;
}
export declare const IODepartment: t.Type<IDepartment>;
export type TDepartment = t.TypeOf<typeof IODepartment>;
export interface TDepartmentDetailed {
    id?: string;
    humanReadableId?: string;
    statusTitle?: EmployeeStatusTitle;
    departmentHead?: string;
    departmentHeadId?: string;
    parent?: string;
    location?: string;
    organization: string;
    organizationId: string;
    code: string;
    departmentName: string;
    fullStructurePath: string;
    children: TDepartment[];
    employees: TDepartmentEmployee[];
    status: EmployeeStatus;
}
export interface TDepartmentFilters {
    id?: string;
    organization?: string;
    departmentName?: string;
    departmentHead?: string;
    statusTitle?: EmployeeStatus;
    code?: string;
    parent?: string;
    location?: string;
}
export type TDepartmentFiltersTitles = keyof TDepartmentFilters;
export interface IPosition {
    id: string;
    organizationId: string;
    positionName: string;
    selfApproved: boolean;
    availableClasses: TaxiClassEnum[];
}
export interface IShortPosition {
    id: string;
    organizationId: string;
    positionName: string;
}
