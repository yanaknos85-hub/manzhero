import axios from 'axios';
import { inject, injectable } from 'inversify';

import {
  GET_SELF_EMPLOYEE,
  EMPLOYEES_PARAMS,
  EMPLOYEES_SEARCH,
  EMPLOYEES_SEARCH_BY_ORG,
  EMPLOYEE_PARAMS,
  GET_ALL_EMPLOYEES_BY_DEPARTMENTS_PARAMS,
  GET_ALL_EMPLOYEES_BY_ORGANIZATION_PARAMS,
  MOCKED_API_PREFIX
} from '../../constants/constants';

import { TYPES } from '../../ioc/ioc.types';

import type { IHttpService, RequestParams, RequestCancelerSetter } from '../Http/http.interface';

import type { IResponseService } from '../Http/Response.service';
import { Employee } from '../SelfEmployee/SelfEmployee.interface';

import { IEmployeeService } from './Employee.interface';

import type { IConfigStore } from '../Config/Config.interface';

@injectable()
export class DIEmployeeService implements IEmployeeService {
  @inject(TYPES.IConfigStore)
  private configStore!: IConfigStore;

  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  private apiPrefix(): string {
    return this.configStore.isMockedApi ? MOCKED_API_PREFIX : '';
  }

  getAllEmployeesByOrganization(orgId: string): Promise<{ content: Employee[] }> {
    return this.http
      .get<{ content: Employee[] }>(
        `${this.apiPrefix()}${GET_ALL_EMPLOYEES_BY_ORGANIZATION_PARAMS}`,
        {
          urlParams: { orgId },
        }
      )
      .then(this.process.getResponseData);
  }

  getAllEmployeesByDepartment(orgId: string, depId: string): Promise<Employee[]> {
    return this.http
      .get<Employee[]>(`${this.apiPrefix()}${GET_ALL_EMPLOYEES_BY_DEPARTMENTS_PARAMS}`, { urlParams: { orgId, depId } })
      .then(this.process.getResponseData);
  }

  getEmployee(orgId: string, depId: string, empId: string): Promise<Employee> {
    return this.http
      .get<Employee>(`${this.apiPrefix()}${EMPLOYEE_PARAMS}`, {
        urlParams: {
          orgId, depId, empId,
        },
      })
      .then(this.process.getResponseData);
  }

  getEmployeesByIds(orgId: string, empIds: string[]): Promise<Employee[]> {
    return this.http
      .get<Employee[]>(`${this.apiPrefix()}${EMPLOYEES_PARAMS}`, { urlParams: { orgId, empIds: empIds.toString() } })
      .then(this.process.getResponseData);
  }

  searchEmployeesByName(name: string): Promise<{ content: Employee[] }> {
    return this.http
      .get<{ content: Employee[] }>(`${this.apiPrefix()}${EMPLOYEES_SEARCH}`, {
        urlParams: { name },
      })
      .then(this.process.getResponseData);
  }

  searchEmployeesByNameByOrg(name: string): Promise<{ content: Employee[] }> {
    return this.http
      .get<{ content: Employee[] }>(`${this.apiPrefix()}${EMPLOYEES_SEARCH_BY_ORG}`, {
        urlParams: { name },
      })
      .then(this.process.getResponseData);
  }

  addEmployee(orgId: string, depId: string, employee: Employee): Promise<Employee> {
    return this.http
      .post<Employee>(`${this.apiPrefix()}${EMPLOYEE_PARAMS}`, { ...employee }, { urlParams: { orgId, depId } })
      .then(this.process.getResponseData);
  }

  editEmployee(orgId: string, depId: string, empId: string, employee: Employee): Promise<number> {
    return this.http
      .put<Employee>(`${this.apiPrefix()}${EMPLOYEE_PARAMS}`, { ...employee }, {
        urlParams: {
          orgId, depId, empId,
        },
      })
      .then(this.process.getResponseStatus);
  }

  editPhone(phoneNumber: string): Promise<number> {
    return this.http
      .patch<Employee>(`${this.apiPrefix()}${GET_SELF_EMPLOYEE}`, [{ field: 'mobilePhone', value: phoneNumber }], { headers: { _method: 'patch' } })
      .then(this.process.getResponseStatus);
  }

  deleteEmployee(orgId: string, depId: string, empId: string): Promise<Employee> {
    return this.http
      .delete<Employee>(`${this.apiPrefix()}${EMPLOYEE_PARAMS}`, {
        urlParams: {
          orgId, depId, empId,
        },
      })
      .then(this.process.getResponseData);
  }

  searchDepartmentEmployees(
    orgId: string,
    depId: string,
    params: RequestParams,
    cancelerSetter?: RequestCancelerSetter
  ): Promise<Employee[]> {
    return this.http
      .get<{ content: Employee[] }>(`${this.apiPrefix()}${GET_ALL_EMPLOYEES_BY_DEPARTMENTS_PARAMS}`, {
        params,
        urlParams: { orgId, depId },
        cancelToken: cancelerSetter && new axios.CancelToken(cancelerSetter),
      })
      .then(data => this.process.getResponseData(data.data.content));
  }

  searchOrganizationEmployees(
    orgId: string,
    params: RequestParams,
    cancelerSetter?: RequestCancelerSetter
  ): Promise<Employee[]> {
    return this.http
      .get<{ content: Employee[] }>(`${this.apiPrefix()}${GET_ALL_EMPLOYEES_BY_ORGANIZATION_PARAMS}`, {
        params,
        urlParams: { orgId },
        cancelToken: cancelerSetter && new axios.CancelToken(cancelerSetter),
      })
      .then(data => this.process.getResponseData(data.data.content));
  }
}

export default DIEmployeeService;
