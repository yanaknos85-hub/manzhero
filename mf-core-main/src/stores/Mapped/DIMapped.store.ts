import { inject, injectable } from 'inversify';
import lodash from 'lodash';
import { computed } from 'mobx';

import type { ISelfEmployeeStore } from '../SelfEmployee/SelfEmployee.interface';
import type { IEmployeeStore } from '../Employee/Employee.interface';
import { EmployeeModel } from '../Employee/models/EmployeeModel';
import { EmployeeDetailedModel } from '../Employee/models/DetailedEmployeeModel';

import { TYPES } from '../../ioc/ioc.types';

import type { ICorporateStore } from '../Corporate/Corporate.interface';
import { DepartmentModel } from '../Corporate/models/Department.model';
import { DepartmentDetailedModel } from '../Corporate/models/DepartmentDetailed.model';

@injectable()
export class MappedStore {
  @inject(TYPES.ISelfEmployeeStore)
  private selfStore!: ISelfEmployeeStore;

  @inject(TYPES.IEmployeeStore)
  private empStore!: IEmployeeStore;

  @inject(TYPES.ICorporateStore)
  private corpStore!: ICorporateStore;

  @computed
  get employeeListByOrgDetailed(): lodash.Dictionary<EmployeeDetailedModel> {
    const result = this.empStore?.employeeListByOrg.map(emp => this.mapEmployeeFields(emp)) ?? [];

    return lodash.mapKeys(result, 'id');
  }

  @computed
  get employeeListByDepMapped(): lodash.Dictionary<EmployeeDetailedModel> {
    const result = this.empStore?.employeeListByDep.map(emp => this.mapEmployeeFields(emp)) ?? [];

    return lodash.mapKeys(result, 'id');
  }

  @computed
  get selfEmployeeDetailed(): EmployeeDetailedModel {
    return this.mapEmployeeFields(this.selfStore.selfEmployee);
  }

  @computed
  get departmentsListDetailed(): lodash.Dictionary<DepartmentDetailedModel> {
    const result = this.corpStore.departments?.map(dep => this.mapDepartmentsFields(dep)) ?? [];

    return lodash.mapKeys(result, 'id');
  }

  mapEmployeeFields(employee: EmployeeModel): EmployeeDetailedModel {
    const delegatedBy
      = this.empStore?.employeeListByOrgMapped[employee.delegatedById]?.shortName || employee.delegatedById;
    const supervisor
      = this.empStore?.employeeListByOrgMapped[employee.supervisorId]?.shortName || employee.supervisorId;
    const org = this.corpStore?.organizationsMapped[employee.organizationId];
    const organization = org?.officialName ?? employee.organizationId;
    const department
      = this.corpStore?.departmentsMapped[employee.departmentId]?.departmentName ?? employee.departmentId;
    const position = this.corpStore?.positionsMapped[employee.positionId]?.positionName ?? employee.positionId;

    return new EmployeeDetailedModel({
      ...employee,
      fullName: employee.fullName,
      nameWithInitials: employee.nameWithInitials,
      delegatedBy,
      supervisor,
      organization,
      department,
      position,
    });
  }

  mapDepartmentsFields(department: DepartmentModel): DepartmentDetailedModel {
    const departmentHead = department.departmentHeadId
      ? this.empStore?.employeeListByOrgMapped[department.departmentHeadId]?.fullName || ''
      : 'Руководитель не назначен';

    const organization
      = this.corpStore.organizationsMapped[department.organizationId]?.officialName || 'Нет организации с таким uuid';
    const parent = department.parentId
      ? this.corpStore.organizationsMapped[department.parentId]?.officialName || 'Нет организации с таким uuid'
      : 'Нет родительского подразделения';

    return new DepartmentDetailedModel({
      ...department,
      departmentHead,
      organization,
      parent,
    });
  }
}
