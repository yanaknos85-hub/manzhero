import { EmployeeModel } from './EmployeeModel';
import { IEmployeeDetailed } from '../Employee.interface';
export declare class EmployeeDetailedModel implements IEmployeeDetailed {
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
    constructor(employee: Partial<IEmployeeDetailed & EmployeeModel>);
}
