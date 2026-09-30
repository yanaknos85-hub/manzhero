import { inject, injectable } from 'inversify';
import { action, computed, observable } from 'mobx';

import { TYPES } from '../../ioc/ioc.types';

import { SelfEmployeeModel } from './models/SelfEmployeeModel';
import type { ISelfEmployeeService, ISelfEmployeeStore } from './SelfEmployee.interface';
import type { IConfigStore } from 'stores/Config/Config.interface';

@injectable()
export class DISelfStore implements ISelfEmployeeStore {
  @inject(TYPES.ISelfEmployeeService)
  private service!: ISelfEmployeeService;

  @inject(TYPES.IConfigStore)
  private configStore!: IConfigStore;

  @observable
    selfEmployee!: SelfEmployeeModel;

  @observable
    isRequiredPhone!: boolean;

  @computed
  get empId(): string {
    return this.selfEmployee.id;
  }

  @computed
  get depId(): string {
    return this.selfEmployee.departmentId;
  }

  @computed
  get orgId(): string {
    return this.selfEmployee.organizationId;
  }

  @computed
  get posId(): string {
    return this.selfEmployee.positionId;
  }

  @computed
  get supId(): string {
    return this.selfEmployee.supervisorId;
  }

  @action.bound
    getSelfEmployee = async (): Promise<SelfEmployeeModel> => {
      const result = await this.service.getSelfEmployee();
      this.selfEmployee = result;

      if (this.selfEmployee.mobilePhone.length !== 0) {
        this.updatePhoneStatus(true);
      }

      return result;
    };

  @action.bound
    agreeWithPrivacyPolicy = async (disp?: boolean): Promise<void> => {
      await this.service.agreeWithPrivacyPolicy(disp);

      if (!this.configStore.isCorp) {
        this.getSelfEmployee();
      }
    };

  @action.bound
    updatePhoneStatus = (value: boolean): void => {
      this.isRequiredPhone = value;
    };

  initStore(): void {
    this.getSelfEmployee();
  }
}
