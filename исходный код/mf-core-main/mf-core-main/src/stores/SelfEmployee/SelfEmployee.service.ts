import { inject, injectable } from 'inversify';

import {
  CONSENT, CONSENT_DISPATCHER, GET_SELF_EMPLOYEE, MOCKED_API_PREFIX
} from '../../constants/constants';

import { TYPES } from '../../ioc/ioc.types';

import type { IHttpService } from '../Http/http.interface';

import type { IResponseService } from '../Http/Response.service';
import { SelfEmployeeModel } from './models/SelfEmployeeModel';
import { Employee, ISelfEmployeeService } from './SelfEmployee.interface';

import type { IConfigStore } from '../Config/Config.interface';

@injectable()
export class DISelfEmployeeService implements ISelfEmployeeService {
  @inject(TYPES.IConfigStore)
  private configStore!: IConfigStore;

  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  private apiPrefix(): string {
    return this.configStore.isMockedAuth ? MOCKED_API_PREFIX : '';
  }

  getSelfEmployee = (): Promise<SelfEmployeeModel> => {
    return this.http
      .get<SelfEmployeeModel>(`${this.apiPrefix()}${GET_SELF_EMPLOYEE}`)
      .then(x => this.process.getResponseData(x, Employee))
      .then(x => new SelfEmployeeModel(x));
  };

  agreeWithPrivacyPolicy = (disp?: boolean): Promise<unknown> => {
    return this.http
      .patch(`${this.apiPrefix()}${disp ? CONSENT_DISPATCHER : CONSENT}`, {});
  };
}
