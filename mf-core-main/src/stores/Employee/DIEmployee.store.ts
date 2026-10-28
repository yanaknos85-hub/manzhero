import { inject, injectable } from 'inversify';
import lodash from 'lodash';
import mapKeys from 'lodash/mapKeys';
import { action, computed, observable } from 'mobx';

import { SYSTEM_MESSAGES } from '../../constants/constants';
import { TYPES } from '../../ioc/ioc.types';
import { plainToNew } from '../../utils/utils';

import type { RequestCancelerSetter, RequestParams } from '../Http/http.interface';
import type { IResponseService } from '../Http/Response.service';

import { EmployeeModel } from './models/EmployeeModel';
import * as SelfEmployeeInterface from '../SelfEmployee/SelfEmployee.interface';
import type { IEmployeeService, IEmployeeStore } from './Employee.interface';
import { SelfEmployeeModel } from '../SelfEmployee/models/SelfEmployeeModel';

@injectable()
export class DIEmployeeStore implements IEmployeeStore {
  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  @inject(TYPES.IEmployeeService)
  private service!: IEmployeeService;

  @inject(TYPES.ISelfEmployeeStore)
  private selfStore!: SelfEmployeeInterface.ISelfEmployeeStore;

  @computed
  get selfEmployee(): EmployeeModel {
    return this.selfStore.selfEmployee;
  }

  @observable
    employeeAutocompleteSelected: EmployeeModel[] = [new EmployeeModel()];

  @observable
    employeeAutocompleteList: EmployeeModel[] = [];

  @observable
    employeeListByOrg: EmployeeModel[] = [];

  @observable
    employeeListByDep: EmployeeModel[] = [];

  @computed
  get employeeListByDepMapped(): lodash.Dictionary<EmployeeModel> {
    return mapKeys(this.employeeListByDep, 'id');
  }

  @computed
  get employeeListByOrgMapped(): lodash.Dictionary<EmployeeModel> {
    return mapKeys(this.employeeListByOrg, 'id');
  }

  @observable
    savedEmployee: EmployeeModel | undefined = undefined;

  @action
    getAllEmployeesByOrganization = async (): Promise<void> => {
      const data = await this.service.getAllEmployeesByOrganization(this.selfEmployee.organizationId);
      this.employeeListByOrg = plainToNew<EmployeeModel[]>(EmployeeModel, data.content) ?? [];
    };

  @action
    getAllEmployeesByDepartment = async (): Promise<void> => {
      const data = await this.service.getAllEmployeesByDepartment(
        this.selfEmployee.organizationId,
        this.selfEmployee.delegatedById
      );

      this.employeeListByDep = plainToNew<EmployeeModel[]>(EmployeeModel, data) ?? [];
    };

  @action
  // eslint-disable-next-line @stylistic/max-len
    getEmployee = async (orgId: string, depId: string, empId: string): Promise<SelfEmployeeInterface.Employee> => this.service.getEmployee(orgId, depId, empId);

  @action.bound
  getEmployeesByIds(orgId: string, empIds: string[]): Promise<SelfEmployeeInterface.Employee[]> {
    return this.service.getEmployeesByIds(orgId, empIds);
  }

  @action.bound
    searchEmployees = lodash.debounce(async (name: string) => {
      this.employeeAutocompleteList = observable(await this.searchEmployeesByName(name));
    }, 1500);

  @action.bound
    searchEmployeesByOrg = lodash.debounce(async (name: string) => {
      this.employeeAutocompleteList = observable(await this.searchEmployeesByNameByOrg(name));
    }, 1500);

  @action.bound
  findEmployees(value: string): void {
    if (value.length > 3 && !this.employeeAutocompleteList.some(x => x.fullNameWithCode === value)) {
      this.clearAutocompleteList();
      this.searchEmployees(value);
    }
  }

  @action.bound
  async onEmployeeSelect(value: string, index = 0, dontClear?: boolean): Promise<void> {
    const employee = this.employeeAutocompleteList.find(x => x.fullNameWithCode === value);
    if (employee) {
      try {
        this.employeeAutocompleteSelected[index] = employee;
      } catch (error) {
        throw new Error(`
          Попытка применить index: ${index} в массиве из ${this.employeeAutocompleteSelected.length} элементов!
          Примените onEmployeeSelect в компоненте, если один из автокомплитов был заполнен сразу
        `);
      }
    }
    if (!dontClear) {
      this.clearAutocompleteList();
    }
  }

  private clearAutocompleteList(): void {
    this.employeeAutocompleteList = [];
  }

  @action.bound
  async searchEmployeesByName(name: string): Promise<EmployeeModel[]> {
    const { content } = await this.service.searchEmployeesByName(name);
    const models = plainToNew<EmployeeModel[]>(EmployeeModel, content) ?? [];
    const noBlank = models.filter(x => x.fullNameWithCode !== '');
    return Object.values(mapKeys(noBlank, 'fullNameWithCode')); // removes duplicates
  }

  @action.bound
  async searchEmployeesByNameByOrg(name: string): Promise<EmployeeModel[]> {
    const { content } = await this.service.searchEmployeesByNameByOrg(name);
    const models = plainToNew<EmployeeModel[]>(EmployeeModel, content) ?? [];
    const noBlank = models.filter(x => x.fullNameWithCode !== '');
    return Object.values(mapKeys(noBlank, 'fullNameWithCode')); // removes duplicates
  }

  private updateEmployee(orgId: string, depId: string, empId: string, model: EmployeeModel): Promise<number> {
    return this.service.editEmployee(orgId, depId, empId, model);
  }

  @action.bound
  async editEmployee(model: EmployeeModel): Promise<void> {
    if (!this.selfEmployee) {
      return;
    }

    const {
      organizationId: orgId, departmentId: depId, id: empId,
    } = this.selfEmployee;

    this.updateEmployee(orgId, depId, empId, model).then(response => {
      const status = this.process.processStatus(response, SYSTEM_MESSAGES.employeeEditSuccess);

      if (status) {
        this.savedEmployee = model;
        this.selfStore.getSelfEmployee();
      }
    });
  }

  @action.bound
  async editPhone(phoneNumber: string): Promise<number> {
    return this.service.editPhone(phoneNumber).then(() => {
      this.selfStore.selfEmployee = new SelfEmployeeModel({
        ...this.selfStore.selfEmployee,
        mobilePhone: phoneNumber,
      });
      return 200;
    });
  }

  @action.bound
  clearSavedEmployee(): void {
    this.savedEmployee = undefined;
  }

  initStore(): void {
    this.getAllEmployeesByOrganization();
    this.clearAutocompleteList();
  }

  @action.bound
  async searchDepartmentEmployees(
    params: RequestParams,
    cancelerSetter?: RequestCancelerSetter
  ): Promise<EmployeeModel[]> {
    const { organizationId: orgId, departmentId: depId } = this.selfEmployee;
    const employees = await this.service.searchDepartmentEmployees(orgId, depId, params, cancelerSetter);
    return plainToNew<EmployeeModel[]>(EmployeeModel, employees) ?? [];
  }

  @action.bound
  async searchOrganizationEmployees(
    params: RequestParams,
    cancelerSetter?: RequestCancelerSetter
  ): Promise<EmployeeModel[]> {
    const { organizationId: orgId } = this.selfEmployee;
    const employees = await this.service.searchOrganizationEmployees(orgId, params, cancelerSetter);
    return plainToNew<EmployeeModel[]>(EmployeeModel, employees) ?? [];
  }
}
