import { EmployeeStatus } from '../../../constants/constants';
import { TDepartment, TDepartmentEmployee } from '../Corporate.interface';
export declare class DepartmentModel implements TDepartment {
    id?: string;
    humanReadableId?: string;
    organizationId: string;
    code: string;
    departmentName: string;
    fullStructurePath: string;
    departmentHeadId?: string;
    parentId?: string;
    location?: string;
    children: TDepartment[];
    employees: TDepartmentEmployee[];
    status: EmployeeStatus;
    constructor(department: TDepartment);
    get isExisting(): boolean;
}
