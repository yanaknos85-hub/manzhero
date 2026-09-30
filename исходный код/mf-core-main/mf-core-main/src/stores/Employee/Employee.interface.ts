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

export const IOHumanReadable = t.type({
  humanReadableId: t.string,
});

export type THumanReadable = t.TypeOf<typeof IOHumanReadable>;

export interface IEmployeeService {
  getAllEmployeesByOrganization(orgId: string): Promise<{ content: Employee[] }>;
  getAllEmployeesByDepartment(orgId: string, depId: string): Promise<Employee[]>;
  getEmployeesByIds(orgId: string, empIds: string[]): Promise<Employee[]>;
  searchEmployeesByName(name: string): Promise<{ content: Employee[] }>;
  searchEmployeesByNameByOrg(name: string): Promise<{ content: Employee[] }>;
  getEmployee(orgId: string, depId: string, empId: string): Promise<Employee>;
  addEmployee(orgId: string, depId: string, employee: Employee): Promise<Employee>;
  editEmployee(orgId: string, depId: string, empId: string, employee: Employee): Promise<number>;
  editPhone(phoneNumber: string): Promise<number>;
  deleteEmployee(orgId: string, depId: string, empId: string): Promise<Employee>;
  searchDepartmentEmployees(
    orgId: string,
    depId: string,
    params: RequestParams,
    cancelerSetter?: RequestCancelerSetter
  ): Promise<Employee[]>;
  searchOrganizationEmployees(
    orgId: string,
    params: RequestParams,
    cancelerSetter?: RequestCancelerSetter
  ): Promise<Employee[]>;
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

export const Employee = t.intersection([
  IOHumanReadable,
  t.type({
    id: t.string,
    userId: t.string,
    firstName: t.string,
    lastName: t.string,
    personnelNumber: t.string,
    departmentId: t.string,
    organizationId: t.string,
    positionId: t.string,
  }),
  t.partial({
    patronymic: t.string,
    status: t.keyof(EmployeeStatus),
    mobilePhone: t.string,
    email: t.string,
    supervisorId: t.string,
    delegatedById: t.string,
    availableTransportTypes: t.UnknownArray,
    personalCars: t.UnknownArray,
    approvals: t.number,
    positionName: t.string,
    departmentName: t.string,
  }),
]);

export type Employee = t.TypeOf<typeof Employee>;

export interface EmployeeDetails {
  delegatedBy: string;
  department: string;
  organization: string;
  position: string;
  supervisor: string;
}
// TODO:выяснить про ключ employeeID чтобы выяснить нужен ли он в сущности
export const IOPersonalCar = t.intersection([
  t.partial({
    employeeId: t.string,
    color: t.string,
  }),
  t.type({
    id: t.string,
    transportType: t.string,
    brandName: t.string,
    model: t.string,
    registrationNumber: t.string,
    registrationCertificate: t.string,
    engineVolume: t.number,
    insuranceNumber: t.string,
    passengerSeatsCount: t.number,
    ownerInfo: t.string,
    persDataAccept: t.boolean,
  }),
]);

export type OrUndefined<T> = T | undefined;

export type OsagoProps = OrUndefined<OsagoEntitie>;

export type OsagoUploadResponseFields = { [key in keyof typeof OsagoUploadResponseParams]: OsagoProps };

export type PersonalCar = t.TypeOf<typeof IOPersonalCar>;

export type personalTypeCar = 'CAR' | 'MOTORCYCLE';

export const carTypeDescriptions: Record<personalTypeCar, string> = {
  CAR: 'Автомобиль',
  MOTORCYCLE: 'Мотоцикл',
};

export const getKeyValue
  = <T extends object, U extends keyof T>(key: U) => (obj: T): any => obj[key];
