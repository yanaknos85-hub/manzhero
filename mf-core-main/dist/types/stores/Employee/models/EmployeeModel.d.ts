import { EmployeeStatusType, TransportTypeEnum } from '../../../constants/constants';
import { Employee } from '../Employee.interface';
export declare class EmployeeModel implements Employee {
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
    constructor(employee?: Employee);
    get isExisting(): boolean;
    get fullNameWithCode(): string;
    get fullName(): string;
    get shortName(): string;
    get nameWithInitials(): string;
    get shortNameWithNumberString(): string;
}
