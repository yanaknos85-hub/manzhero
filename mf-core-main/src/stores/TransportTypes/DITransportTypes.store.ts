import { inject, injectable } from 'inversify';
import {
  action, computed, observable, reaction
} from 'mobx';

import type { ISelfEmployeeStore } from '../SelfEmployee/SelfEmployee.interface';

import { TYPES } from '../../ioc/ioc.types';

import { plainToNew } from '../../utils/utils';

import type {
  ITransportType,
  ITransportTypesService,
  ITransportTypesStore,
  TransportType
} from './TransportTypes.interface';

import {
  TransportTypesModel
} from './TransportTypes.interface';

@injectable()
export class DITransportTypesStore implements ITransportTypesStore {
  @inject(TYPES.ITransportTypesService)
  private service!: ITransportTypesService;

  @inject(TYPES.ISelfEmployeeStore)
  private selfStore!: ISelfEmployeeStore;

  @observable
    transportTypes: TransportTypesModel[] = [];

  @observable
    activeTransportType?: TransportType;

  @observable
    availableTransportTypes: ITransportType[] = [];

  @observable
    availableTransportTypesByService: ITransportType[] = [];

  @computed
  get rusNamesByTransportType(): Record<string, string> {
    return this.availableTransportTypes.reduce((rusNames: Record<string, string>, transportType) => {
      // eslint-disable-next-line no-param-reassign
      rusNames[transportType.name] = transportType.rusName;
      // FIXME no-param-reassign
      return rusNames;
    }, {});
  }

  @action.bound
  setActiveTransportType(type: TransportType): void {
    this.activeTransportType = type;
  }

  @action.bound
  clearActiveTransportType(): void {
    this.activeTransportType = undefined;
  }

  @action
  async getTransportTypes(): Promise<void> {
    const data = await this.service.getTransportTypes();
    const result = plainToNew<TransportTypesModel[]>(TransportTypesModel, data) ?? [];
    this.transportTypes = result;
  }

  @action.bound
  async getAvailableTransportTypes() {
    this.availableTransportTypes = await this.service.getAvailableTransportTypes(
      this.selfStore.selfEmployee.organizationId
    );
  }

  @action.bound
  async getAvailableTransportTypesByService(serviceType: string) {
    this.availableTransportTypesByService = await this.service.getAvailableTransportTypesByService(
      serviceType,
      this.selfStore.selfEmployee.organizationId
    );
  }

  initStore(): void {
    this.getTransportTypes();

    reaction(
      () => this.selfStore.selfEmployee,
      selfEmployee => {
        if (selfEmployee) {
          this.getAvailableTransportTypes();
        }
      },
      { fireImmediately: true }
    );
  }
}
