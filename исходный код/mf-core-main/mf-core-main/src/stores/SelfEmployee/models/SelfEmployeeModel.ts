import { OrgStructureType, TransportTypeEnum } from '../../../constants/constants';
import {
  EmployeeStatus, EmployeeStatusType, SelfEmployee
} from '../SelfEmployee.interface';

export class SelfEmployeeModel implements SelfEmployee {
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

  consent: boolean;

  orgStructureType: OrgStructureType;

  isDepartmentHead: boolean;

  isPhoneConfirmed: boolean;

  constructor(employee?: SelfEmployee) {
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
    this.consent = employee?.consent ?? false;
    // eslint-disable-next-line @typescript-eslint/no-non-null-asserted-optional-chain
    this.orgStructureType = employee?.orgStructureType!;
    this.isDepartmentHead = employee?.isDepartmentHead ?? false;
    this.isPhoneConfirmed = employee?.isPhoneConfirmed ?? false;
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
