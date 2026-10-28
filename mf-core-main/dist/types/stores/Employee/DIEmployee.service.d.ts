import type { RequestParams, RequestCancelerSetter } from '../Http/http.interface';
import { Employee } from '../SelfEmployee/SelfEmployee.interface';
import { IEmployeeService } from './Employee.interface';
export declare class DIEmployeeService implements IEmployeeService {
    private configStore;
    private http;
    private process;
    private apiPrefix;
    getAllEmployeesByOrganization(orgId: string): Promise<{
        content: Employee[];
    }>;
    getAllEmployeesByDepartment(orgId: string, depId: string): Promise<Employee[]>;
    getEmployee(orgId: string, depId: string, empId: string): Promise<Employee>;
    getEmployeesByIds(orgId: string, empIds: string[]): Promise<Employee[]>;
    searchEmployeesByName(name: string): Promise<{
        content: Employee[];
    }>;
    searchEmployeesByNameByOrg(name: string): Promise<{
        content: Employee[];
    }>;
    addEmployee(orgId: string, depId: string, employee: Employee): Promise<Employee>;
    editEmployee(orgId: string, depId: string, empId: string, employee: Employee): Promise<number>;
    editPhone(phoneNumber: string): Promise<number>;
    deleteEmployee(orgId: string, depId: string, empId: string): Promise<Employee>;
    searchDepartmentEmployees(orgId: string, depId: string, params: RequestParams, cancelerSetter?: RequestCancelerSetter): Promise<Employee[]>;
    searchOrganizationEmployees(orgId: string, params: RequestParams, cancelerSetter?: RequestCancelerSetter): Promise<Employee[]>;
}
export default DIEmployeeService;
