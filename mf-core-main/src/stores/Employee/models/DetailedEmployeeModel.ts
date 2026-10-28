import { EmployeeModel } from './EmployeeModel';
import { IEmployeeDetailed } from '../Employee.interface';

export class EmployeeDetailedModel implements IEmployeeDetailed {
  id: string;

  humanReadableId: string;

  userId: string;

  nameWithInitials: string;

  fullNameString: string;

  personnelNumber: string;

  department: string;

  position: string;

  mobilePhone: string;

  email: string;

  supervisor: string;

  delegatedBy: string;

  availableTransportTypes: any[];

  organization: string;

  constructor(employee: Partial<IEmployeeDetailed & EmployeeModel>) {
    this.id = employee.id ?? '';
    this.humanReadableId = employee.humanReadableId ?? '';
    this.userId = employee.userId ?? '';
    this.nameWithInitials = employee.nameWithInitials ?? '';
    this.fullNameString = employee.fullName ?? '';
    this.department = employee.department ?? '';
    this.position = employee.position ?? '';
    this.personnelNumber = employee.personnelNumber ?? '';
    this.supervisor = employee.supervisor ?? '';
    this.delegatedBy = employee.delegatedBy ?? '';
    this.mobilePhone = employee.mobilePhone ?? '';
    this.email = employee.email ?? '';
    this.organization = employee.organization ?? '';
    this.availableTransportTypes = employee.availableTransportTypes ?? [];
  }
}
