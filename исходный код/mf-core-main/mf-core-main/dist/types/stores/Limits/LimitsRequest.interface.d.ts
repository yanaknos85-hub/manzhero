import * as t from 'io-ts';
import * as tt from '../../utils/io-ts';
import { TransportTypeEnum } from '../../constants/constants';
import { DepSiblings } from './Limit.interface';
import { LimitRequestModel } from './Models/LimitRequest.model';
export interface ILimitsRequestService {
    getLimitRequestList(): Promise<TLimitRequestNew[]>;
    getLimitRequest(reqId: string): Promise<LimitRequestModel>;
    editLimitRequest(reqId: string, data: TLimitRequestNew): Promise<number>;
    approveLimitRequest(reqId: string): Promise<number>;
    cancelLimitRequest(reqId: string, reason: string): Promise<number>;
    deleteLimitRequest(reqId: string): Promise<number>;
    getDepSiblings(data: SiblingsParams): Promise<DepSiblings[]>;
    addLimitRequest(data: LimitSendRequest): Promise<number>;
    addEmpLimitRequest(data: LimitEmpRequest): Promise<number>;
}
export interface ILimitsRequestStore {
    list: LimitRequestModel[];
    currentRequest: LimitRequestModel | undefined;
    siblings: DepSiblings[];
    setCurrentRequest(id: string): void;
    cancelRequest(id: string, reason: string): void;
    getList(): Promise<void>;
    createRequest(data: LimitSendRequest): void;
    addLimitRequest(data: LimitSendRequest): Promise<number>;
    addEmpLimitRequest(data: LimitEmpRequest): Promise<number>;
    editLimitRequest(reqId: string, data: TLimitRequestNew): void;
    getRequest(reqId: string): Promise<LimitRequestModel>;
    initStore(): void;
    getDepSiblings(data: SiblingsParams): Promise<DepSiblings[]>;
}
export declare const SiblingsParams: t.ExactC<t.TypeC<{
    departmentId: t.StringC;
    percent: t.NumberC;
    transportType: t.StringC;
    year: t.NumberC;
    sum: tt.MoneyC;
}>>;
export type SiblingsParams = t.TypeOf<typeof SiblingsParams>;
export declare enum LimitRequestStatusEnum {
    AWAITING_APPROVAL = "AWAITING_APPROVAL",
    APPROVED = "APPROVED",
    DONE = "DONE",
    CANCELLED = "CANCELLED"
}
export declare enum LimitRequestStatusTitlesEnum {
    AWAITING_APPROVAL = "\u041E\u0436\u0438\u0434\u0430\u0435\u0442 \u0441\u043E\u0433\u043B\u0430\u0441\u043E\u0432\u0430\u043D\u0438\u044F",
    APPROVED = "\u0421\u043E\u0433\u043B\u0430\u0441\u043E\u0432\u0430\u043D\u0430",
    DONE = "\u0412\u044B\u043F\u043E\u043B\u043D\u0435\u043D\u0430",
    CANCELLED = "\u041E\u0442\u043C\u0435\u043D\u0435\u043D\u0430"
}
export declare enum LimitRequestApprovalStateEnum {
    AWAITING_APPROVAL = "AWAITING_APPROVAL",
    APPROVED = "APPROVED",
    DECLINED = "DECLINED"
}
export declare enum LimitRequestApprovalStateTitlesEnum {
    AWAITING_APPROVAL = "\u041E\u0436\u0438\u0434\u0430\u0435\u0442 \u0441\u043E\u0433\u043B\u0430\u0441\u043E\u0432\u0430\u043D\u0438\u044F",
    APPROVED = "\u0421\u043E\u0433\u043B\u0430\u0441\u043E\u0432\u0430\u043D\u0430",
    DECLINED = "\u041E\u0442\u043A\u043B\u043E\u043D\u0435\u043D\u0430"
}
export declare const LimitRequestStatusesCancellable: LimitRequestStatusEnum[];
export declare const LimitRequestStatusesFinal: LimitRequestStatusEnum[];
export declare const IOLimitRequestNew: t.IntersectionC<[t.TypeC<{
    sum: t.NumberC;
    id: t.StringC;
    humanReadableId: t.StringC;
    creationTime: t.UnionC<[t.StringC, t.NumberC]>;
}>, t.PartialC<{
    month: t.NumberC;
    transportType: t.Type<TransportTypeEnum, TransportTypeEnum, unknown>;
    status: t.StringC;
    authorId: t.StringC;
    approvalState: t.StringC;
    description: t.StringC;
    askTargets: t.StringC;
    departments: t.ArrayC<t.StringC>;
}>]>;
export type TLimitRequestNew = t.TypeOf<typeof IOLimitRequestNew>;
export declare const LimitGeneralRequest: t.ExactC<t.TypeC<{
    year: t.NumberC;
    period: t.NumberC;
    transportType: t.StringC;
    askTargets: t.StringC;
    sum: tt.MoneyC;
    description: t.StringC;
}>>;
export declare const LimitSendRequest: t.IntersectionC<[t.ExactC<t.TypeC<{
    year: t.NumberC;
    period: t.NumberC;
    transportType: t.StringC;
    askTargets: t.StringC;
    sum: tt.MoneyC;
    description: t.StringC;
}>>, t.PartialC<{
    departments: t.ArrayC<t.StringC>;
}>]>;
export type LimitSendRequest = t.TypeOf<typeof LimitSendRequest>;
export declare const LimitEmpRequest: t.ExactC<t.TypeC<{
    year: t.NumberC;
    period: t.NumberC;
    transportType: t.StringC;
    sum: tt.MoneyC;
    description: t.StringC;
}>>;
export type LimitEmpRequest = t.TypeOf<typeof LimitEmpRequest>;
