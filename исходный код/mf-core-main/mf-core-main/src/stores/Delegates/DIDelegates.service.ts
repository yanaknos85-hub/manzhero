import axios from 'axios';
import { inject, injectable } from 'inversify';

import { Employee } from '../Employee/Employee.interface';
import type { IHttpService, RequestCancelerSetter, RequestParams } from '../Http/http.interface';
import type { IResponseService } from '../Http/Response.service';

import {
  TransportTypeEnum,
  ADD_DELEGATE,
  DELETE_DELEGATE,
  GET_CANDIDATES_IN_DELEGATES_PARAMS,
  GET_DELEGATES,
  GET_SELF_CONDIDATES_TO_DELEGATES
} from '../../constants/constants';

import { TYPES } from '../../ioc/ioc.types';

import {
  Delegate, DelegateModel, IDelegatesService, getDeligatesArgs
} from './Delegates.interface';

@injectable()
export class DIDelegatesService implements IDelegatesService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  getCandidatesToDelegates(args: Omit<getDeligatesArgs, 'delegateId'>): Promise<Employee[]> {
    const {
      depId, orgId, supId, transType, date,
    } = args;

    return this.http
      .get<Employee[]>(GET_CANDIDATES_IN_DELEGATES_PARAMS, {
        params: { date },
        urlParams: {
          orgId,
          depId,
          supId,
          transType,
        },
      })
      .then(this.process.getResponseData);
  }

  getSelfCandidatesToDelegates(transportType: TransportTypeEnum, date: string): Promise<Employee[]> {
    return this.http
      .get<{ content: Employee[] }>(GET_SELF_CONDIDATES_TO_DELEGATES, {
        params: { date },
        urlParams: {
          transportType,
        },
      })
      .then(data => this.process.getResponseData(data).content);
  }

  getDelegates(args: Omit<getDeligatesArgs, 'transportTypeId' | 'delegateId'>): Promise<Delegate[]> {
    const {
      depId, orgId, supId, date,
    } = args;

    return this.http
      .get<Delegate[]>(GET_DELEGATES, {
        params: { date },
        urlParams: {
          orgId,
          depId,
          supId,
        },
      })
      .then(this.process.getResponseData);
  }

  addDelegate(
    args: Omit<getDeligatesArgs, 'supId' | 'transportType' | 'date' | 'delegateId'>,
    delegate: DelegateModel
  ): Promise<Delegate> {
    const { depId, orgId } = args;

    return this.http
      .post<Delegate>(ADD_DELEGATE, { ...delegate }, { urlParams: { orgId, depId } })
      .then(this.process.getResponseData);
  }

  deleteDelegate(args: Omit<getDeligatesArgs, 'supId' | 'transTypeId' | 'date'>): Promise<number> {
    const {
      depId, orgId, delegateId,
    } = args;

    return this.http
      .delete<number>(DELETE_DELEGATE, {
        urlParams: {
          orgId, depId, delId: delegateId,
        },
      })
      .then(this.process.getResponseStatus);
  }

  searchSelfDelegateCandidates(
    transportType: string,
    params: RequestParams,
    cancelerSetter?: RequestCancelerSetter
  ): Promise<Employee[]> {
    return this.http
      .get<{ content: Employee[] }>(GET_SELF_CONDIDATES_TO_DELEGATES, {
        params,
        urlParams: { transportType },
        cancelToken: cancelerSetter && new axios.CancelToken(cancelerSetter),
      })
      .then(data => this.process.getResponseData(data.data.content));
  }
}
