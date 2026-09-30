import { inject, injectable } from 'inversify';
import * as t from 'io-ts';

import type { IHttpService } from '../Http/http.interface';
import type { IResponseService } from '../Http/Response.service';

import {
  APPROVE_REQUEST,
  GET_ACCOUNT_BONUSES,
  GET_ALL_REQUESTS,
  GET_DEPLIMITS,
  GET_DEPLIMITS_BY_DEP,
  GET_DEPLIMITS_BY_DEP_AND_YEAR,
  GET_EMPLIMITS,
  GET_EMP_LIMIT,
  GET_LIMITS_REQUESTS_BY_AUTHOR,
  GET_LIMIT_SHARING,
  GET_LIMIT_TRANSFER_HISTORY,
  GET_LIMITS_REQUESTS_BY_APPROVER,
  GET_ACTIVE_LIMITS_REQUESTS_BY_APPROVER,
  GET_OLD_LIMITS_REQUESTS_BY_APPROVER,
  GET_REQUESTS_DEP,
  GET_REQUESTS_EMP,
  GET_SPENDINGS,
  LIMITS,
  LIMIT_REQUEST_CANCEL
} from '../../constants/constants';

import { TYPES } from '../../ioc/ioc.types';

import {
  ISpentActionsType,
  Bonuses,
  ILimitsService,
  Limit,
  LimitCostHistory,
  LimitRequestInfo,
  ActiveLimitRequestInfo,
  OldLimitRequestInfo,
  LimitRequestSaving,
  LimitRequestSavingObject,
  LimitSharing,
  LimitTransferHistory
} from './Limit.interface';

import { LimitEmpRequest, LimitSendRequest } from './LimitsRequest.interface';

@injectable()
export class DILimitsService implements ILimitsService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  getDepartmentLimits(): Promise<Limit[]> {
    return this.http.get<Limit[]>(GET_DEPLIMITS).then(this.process.getResponseData);
  }

  getDepartmentLimitsByYear(depId: string, year: string): Promise<Limit> {
    return this.http
      .get<Limit>(GET_DEPLIMITS_BY_DEP_AND_YEAR, { urlParams: { depId, year } })
      .then(this.process.getResponseData);
  }

  getEmployeeLimits(): Promise<Limit[]> {
    return this.http.get<Limit[]>(GET_EMPLIMITS).then(this.process.getResponseData);
  }

  getLimitSharing(limitId: string): Promise<LimitSharing[]> {
    return this.http.get<LimitSharing[]>(`${GET_LIMIT_SHARING}${limitId}`).then(this.process.getResponseData);
  }

  getEmployeeLimit(employeeId: string, year: number): Promise<Limit> {
    return this.http.get<Limit>(`${GET_EMP_LIMIT}${employeeId}/year/${year}`).then(this.process.getResponseData);
  }

  getLimitsRequestsByAuthor(): Promise<ISpentActionsType[]> {
    return this.http
      .get<ISpentActionsType[]>(`${GET_LIMITS_REQUESTS_BY_AUTHOR}`)
      .then(this.process.decodeResponseData(t.array(ISpentActionsType)));
  }

  cancelLimitRequest(data: { requestId: string; description: string }): Promise<number> {
    return this.http.put<ISpentActionsType>(LIMIT_REQUEST_CANCEL, data).then(this.process.getResponseStatus);
  }

  getLimitsRequestByApprover(): Promise<LimitRequestInfo[]> {
    return this.http
      .get<LimitRequestInfo[]>(GET_LIMITS_REQUESTS_BY_APPROVER)
      .then(this.process.decodeResponseData(t.array(LimitRequestInfo)));
  }

  getActiveLimitsRequestByApprover({ page, size }: { page: number; size: number }): Promise<ActiveLimitRequestInfo> {
    return this.http
      .get<ActiveLimitRequestInfo>(GET_ACTIVE_LIMITS_REQUESTS_BY_APPROVER, { params: { page, size } })
      .then(this.process.decodeResponseData(ActiveLimitRequestInfo));
  }

  getOldLimitsRequestByApprover({ page, size }: { page: number; size: number }): Promise<OldLimitRequestInfo> {
    return this.http
      .get<OldLimitRequestInfo>(GET_OLD_LIMITS_REQUESTS_BY_APPROVER, { params: { page, size } })
      .then(this.process.decodeResponseData(OldLimitRequestInfo));
  }

  getAllLimitRequests(): Promise<LimitRequestInfo[]> {
    return this.http
      .get<LimitRequestInfo[]>(GET_ALL_REQUESTS)
      .then(this.process.decodeResponseData(t.array(LimitRequestInfo)));
  }

  // Один запрос на Approve & cancel
  approveLimitRequest(data: LimitRequestSavingObject): Promise<number> {
    return this.http
      .put<LimitRequestInfo>(APPROVE_REQUEST, LimitRequestSavingObject.encode(data), {})
      .then(this.process.getResponseStatus);
  }

  changeDepLimitRequest(data: LimitSendRequest, requestId: string): Promise<number> {
    return this.http
      .put<LimitRequestSaving>(`${GET_REQUESTS_DEP}/${requestId}`, LimitRequestSaving.encode(data), {})
      .then(this.process.getResponseStatus);
  }

  changeEmpLimitRequest(data: LimitEmpRequest, requestId: string): Promise<number> {
    return this.http
      .put<LimitRequestSaving>(`${GET_REQUESTS_EMP}/${requestId}`, LimitRequestSaving.encode(data), {})
      .then(this.process.getResponseStatus);
  }

  getLimitByDepartment(departmentId: string): Promise<Limit[]> {
    return this.http
      .get<Limit[]>(`${GET_DEPLIMITS_BY_DEP}/${departmentId}`)
      .then(this.process.decodeResponseData(t.array(Limit)));
  }

  // Корректировки по лимиту (Получение движений ДС по лимиту)
  getLimitTransferHistory(limitId: string | string, year: number, maxRecords: number): Promise<LimitTransferHistory[]> {
    return this.http
      .get<LimitTransferHistory[]>(`${GET_LIMIT_TRANSFER_HISTORY}/${limitId}/${year}/${maxRecords}`)
      .then(this.process.decodeResponseData(t.array(LimitTransferHistory)));
  }

  // Расходы по лимиту
  getLimitCostHistory(
    limitId: string | string,
    maxRecords: number,
    organizationId: string | string
  ): Promise<LimitCostHistory[]> {
    return this.http
      .get<LimitCostHistory[]>(`/${LIMITS}/${GET_SPENDINGS}/${organizationId}/${limitId}/${maxRecords}`)
      .then(this.process.decodeResponseData(t.array(LimitCostHistory)));
  }

  getAccountBonuses(ownerId: string): Promise<Bonuses> {
    return this.http.get<Bonuses>(`${GET_ACCOUNT_BONUSES}${ownerId}`).then(this.process.getResponseData);
  }
}
