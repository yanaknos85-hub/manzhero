import { inject, injectable } from 'inversify';

import type { IHttpService } from '../Http/http.interface';
import type { IResponseService } from '../Http/Response.service';

import { AVAILABLE_TRANSPORTTYPES, AVAILABLE_TRANSPORTTYPES_BY_SERVICE, TRANSPORTTYPES } from '../../constants/constants';

import { TYPES } from '../../ioc/ioc.types';

import { ITransportType, ITransportTypesService } from './TransportTypes.interface';

@injectable()
export class DITransportTypesService implements ITransportTypesService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  getTransportTypes(): Promise<ITransportType[]> {
    return this.http.get<ITransportType[]>(`${TRANSPORTTYPES}`).then(this.process.getResponseData);
  }

  getAvailableTransportTypes(orgId: string): Promise<ITransportType[]> {
    return this.http
      .get<ITransportType[]>(`${AVAILABLE_TRANSPORTTYPES}`, { urlParams: { orgId } })
      .then(this.process.getResponseData);
  }

  getAvailableTransportTypesByService(serviceType: string, orgId: string): Promise<ITransportType[]> {
    return this.http
      .get<ITransportType[]>(`${AVAILABLE_TRANSPORTTYPES_BY_SERVICE}`, { urlParams: { serviceType, orgId } })
      .then(this.process.getResponseData);
  }
}
