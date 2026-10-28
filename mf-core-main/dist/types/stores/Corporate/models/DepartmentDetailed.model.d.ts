import { EmployeeStatus, EmployeeStatusTitle } from '../../../constants/constants';
import { TDepartmentDetailed, TDepartmentEmployee } from '../Corporate.interface';
export declare class DepartmentDetailedModel implements TDepartmentDetailed {
    id?: string;
    humanReadableId?: string;
    statusTitle?: EmployeeStatusTitle;
    organization: string;
    organizationId: string;
    code: string;
    departmentName: string;
    fullStructurePath: string;
    departmentHeadId?: string;
    departmentHead?: string;
    parent?: string;
    location?: string;
    children: any[];
    employees: TDepartmentEmployee[];
    status: EmployeeStatus;
    constructor(department: TDepartmentDetailed);
    get isExisting(): boolean;
    set editStatusTitle(statusTitle: EmployeeStatusTitle);
}
