import { EmployeeStatus, EmployeeStatusType, TransportTypeEnum } from '../../../constants/constants';

import { Employee } from '../Employee.interface';

export class EmployeeModel implements Employee {
  id: string;

  humanReadableId: string;

  userId: string;

  firstName: string;

  lastName: string;

  patronymic: string;

  personnelNumber: string;

  departmentId: string;

  positionId: string;

  mobilePhone: string;

  email: string;

  supervisorId: string;

  delegatedById: string;

  availableTransportTypes: TransportTypeEnum[];

  organizationId: string;

  status: EmployeeStatusType;

  approvals: number;

  constructor(employee?: Employee) {
    // FIXME sonarjs/cognitive-complexity
    this.userId = employee?.userId ?? '';
    this.firstName = employee?.firstName ?? '';
    this.lastName = employee?.lastName ?? '';
    this.patronymic = employee?.patronymic ?? '';
    this.personnelNumber = employee?.personnelNumber ?? '';
    this.departmentId = employee?.departmentId ?? '';
    this.positionId = employee?.positionId ?? '';
    this.approvals = employee?.approvals ?? 0;
    this.mobilePhone = employee?.mobilePhone ?? '';
    this.email = employee?.email ?? '';
    this.supervisorId = employee?.supervisorId ?? '';
    this.id = employee?.id ?? '';
    this.delegatedById = employee?.delegatedById ?? '';
    this.availableTransportTypes = (employee?.availableTransportTypes as TransportTypeEnum[]) ?? [];
    this.organizationId = employee?.organizationId ?? '';
    this.status = employee?.status ?? EmployeeStatus.ACTIVE;
    this.humanReadableId = employee?.humanReadableId ?? '';
  }

  get isExisting(): boolean {
    return !!(this.organizationId && this.departmentId && this.id);
  }

  get fullNameWithCode(): string {
    return `${this.firstName} ${this.patronymic} ${this.lastName} (${this.personnelNumber})`;
  }

  get fullName(): string {
    return `${this.lastName} ${this.firstName} ${this.patronymic} `;
  }

  get shortName(): string {
    return `${this.firstName.charAt(0)}. ${this.patronymic.charAt(0)}. ${this.lastName}`;
  }

  get nameWithInitials(): string {
    return `${this.firstName.charAt(0)}. ${this.patronymic.charAt(0)}. ${this.lastName}`;
  }

  get shortNameWithNumberString(): string {
    return `${this.shortName} (${this.personnelNumber})`;
  }
}
