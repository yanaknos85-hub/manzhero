import { inject, injectable } from 'inversify';
import { action, computed, observable } from 'mobx';

import type { ISelfEmployeeStore } from '../SelfEmployee/SelfEmployee.interface';
import { Employee } from '../Employee/Employee.interface';
import { EmployeeModel } from '../Employee/models/EmployeeModel';
import type { RequestCancelerSetter, RequestParams } from '../Http/http.interface';
import type { IResponseService } from '../Http/Response.service';
import type { ILogger } from '../Logger/Logger.interface';

import { SYSTEM_MESSAGES, TransportTypeEnum } from '../../constants/constants';

import { TYPES } from '../../ioc/ioc.types';

import type { ITransportTypesStore } from '../TransportTypes/TransportTypes.interface';

import { plainToNew } from '../../utils/utils';

import * as DelegatesInterface from './Delegates.interface';

@injectable()
export class DIDelegatesStore implements DelegatesInterface.IDelegatesStore {
  @inject(TYPES.IDelegatesService)
  private service!: DelegatesInterface.IDelegatesService;

  @inject(TYPES.ISelfEmployeeStore)
  private selfStore!: ISelfEmployeeStore;

  @inject(TYPES.ILogger)
  private logger!: ILogger;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  @inject(TYPES.ITransportTypesStore)
  private transportTypes!: ITransportTypesStore;

  @computed
  get selfEmployee(): EmployeeModel {
    return this.selfStore.selfEmployee;
  }

  @observable
    delegates: DelegatesInterface.DelegateModel[] = [];

  @observable
    candidatesToDelegates: Record<string, EmployeeModel[]> = {};

  @observable
    selfCandidatesToDelegates: EmployeeModel[] = [];

  @computed
  get namesWithInitials(): Record<string, string> {
    const getNameWithInitials = (employee: Employee): string => `${employee.firstName.charAt(0)}. ${employee.patronymic?.charAt(0)}. ${employee.lastName}`;

    return this.delegates.reduce((names: Record<string, string>, delegate) => {
      // eslint-disable-next-line no-param-reassign
      names[delegate.delegateId] = getNameWithInitials(delegate.delegateEmployee);
      // FIXME no-param-reassign
      return names;
    }, {});
  }

  @action.bound
  async getDelegates(): Promise<void> {
    const data = await this.service.getDelegates({
      orgId: this.selfEmployee.organizationId,
      depId: this.selfEmployee.departmentId,
      supId: this.selfEmployee.id,
    });
    this.delegates = plainToNew<DelegatesInterface.DelegateModel[]>(DelegatesInterface.DelegateModel, data) ?? [];
  }

  @action.bound
  async getCandidatesToDelegates(transType: string, date: string): Promise<void> {
    const data = await this.service.getCandidatesToDelegates({
      orgId: this.selfEmployee.organizationId,
      depId: this.selfEmployee.departmentId,
      supId: this.selfEmployee.id,
      transType,
      date,
    });

    this.candidatesToDelegates = Object.assign(this.candidatesToDelegates, {
      [transType]: plainToNew<EmployeeModel[]>(EmployeeModel, data) ?? [],
    });
  }

  @action.bound
  async getSelfCandidatesToDelegates(transportType: TransportTypeEnum, date: string): Promise<void> {
    const data = await this.service.getSelfCandidatesToDelegates(transportType, date);

    this.selfCandidatesToDelegates = plainToNew<EmployeeModel[]>(EmployeeModel, data) ?? [];
  }

  @action.bound
  async addDelegate(delegate: DelegatesInterface.DelegateModel): Promise<void> {
    const data = await this.service.addDelegate(
      { orgId: this.selfEmployee.organizationId, depId: this.selfEmployee.departmentId },
      { ...delegate }
    );

    const result = plainToNew<DelegatesInterface.DelegateModel>(DelegatesInterface.DelegateModel, data);

    if (result) {
      this.delegates = [...this.delegates, result];
      this.logger.toMessage('success', SYSTEM_MESSAGES.delegateSuccessfull);
    }
  }

  @action.bound
  async deleteDelegate(delegateId: string): Promise<void> {
    const result = await this.service.deleteDelegate({
      orgId: this.selfEmployee.organizationId,
      depId: this.selfEmployee.departmentId,
      delegateId,
    });

    if (result) {
      this.process.processStatus(result, SYSTEM_MESSAGES.delegateDeleteSuccess);
      this.delegates = this.delegates.filter(x => x.id !== delegateId);
    }
  }

  initStore(): void {
    this.getDelegates();
    // TODO возможно ломает запросы
    // this.transportTypes.transportTypes.forEach(type => {
    //   const now = moment().format(DATE_FORMAT.BASE);
    //   this.getCandidatesToDelegates(type.id, now);
    // });
  }

  @action.bound
  async searchSelfDelegateCandidates(
    { transportType, ...params }: RequestParams,
    cancelerSetter?: RequestCancelerSetter
  ): Promise<EmployeeModel[]> {
    const data = await this.service.searchSelfDelegateCandidates(transportType, params, cancelerSetter);
    return plainToNew<EmployeeModel[]>(EmployeeModel, data) ?? [];
  }
}
