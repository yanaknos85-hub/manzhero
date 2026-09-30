import lodash from 'lodash';
import { EmployeeModel } from '../Employee/models/EmployeeModel';
import { EmployeeDetailedModel } from '../Employee/models/DetailedEmployeeModel';
import { DepartmentModel } from '../Corporate/models/Department.model';
import { DepartmentDetailedModel } from '../Corporate/models/DepartmentDetailed.model';
export declare class MappedStore {
    private selfStore;
    private empStore;
    private corpStore;
    get employeeListByOrgDetailed(): lodash.Dictionary<EmployeeDetailedModel>;
    get employeeListByDepMapped(): lodash.Dictionary<EmployeeDetailedModel>;
    get selfEmployeeDetailed(): EmployeeDetailedModel;
    get departmentsListDetailed(): lodash.Dictionary<DepartmentDetailedModel>;
    mapEmployeeFields(employee: EmployeeModel): EmployeeDetailedModel;
    mapDepartmentsFields(department: DepartmentModel): DepartmentDetailedModel;
}
