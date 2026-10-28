import * as t from 'io-ts';

import { SelfEmployeeModel } from './models/SelfEmployeeModel';
import { ioTypeFromEnum } from '../../utils/ioTypeFromEnum';
import { OrgStructureType } from './../../constants/constants';

export enum EmployeeStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
}

export type EmployeeStatusType = keyof typeof EmployeeStatus;

export const IOHumanReadable = t.type({
  humanReadableId: t.string,
});

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

export const SelfEmployee = t.intersection([
  Employee,
  t.type({
    consent: t.boolean,
    orgStructureType: ioTypeFromEnum<OrgStructureType>('OrgStructureType', OrgStructureType),
  }),
  t.partial({
    isDepartmentHead: t.boolean,
    isPhoneConfirmed: t.boolean,
  }),
]);

export type SelfEmployee = t.TypeOf<typeof SelfEmployee>;
