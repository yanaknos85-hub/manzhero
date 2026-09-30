import * as t from 'io-ts';
import * as tt from '../../utils/io-ts';
import { TransportTypeEnum } from '../../constants/constants';
import { LimitEmpRequest, LimitSendRequest } from './LimitsRequest.interface';
import { LimitModel } from './Models/LimitModel';
export declare enum Errors {
    LIMIT_NOT_FOUND = "LIMIT_NOT_FOUND"
}
export interface ILimitsStore {
    limitsIsLoaded: boolean;
    limitsIsFailed: boolean;
    currentLimit: LimitModel[] | undefined;
    employeeLimit: Limit | undefined;
    limitsRequestsByAuthor: ISpentActionsType[];
    employeeLimits: LimitModel[];
    limitSharing: LimitSharing[];
    currentEmployeeSharing: LimitSharing[];
    currentDepartmentSharing: LimitSharing[];
    activeLimitsRequestByApprover: ActiveLimitRequestInfo | undefined;
    oldLimitsRequestByApprover: OldLimitRequestInfo | undefined;
    limitsRequestByApprover: LimitRequestInfo[];
    allLimitRequests: LimitRequestInfo[];
    currentRequest: ISpentActionsType | undefined;
    limitTransferHistory: LimitTransferHistory[];
    limitCostHistory: LimitCostHistory[];
    bonuses: Bonuses | undefined;
    depLimits: Limit[];
    departmentLimits: LimitModel[];
    getLimitsRequestsByAuthor(): Promise<ISpentActionsType[]>;
    getDepartmentLimits(): Promise<LimitModel[]>;
    getDepartmentLimitsByYear(depId: string, year: string): Promise<LimitModel[]>;
    getEmployeeLimits(): Promise<void>;
    getLimitsRequestByApprover(): Promise<LimitRequestInfo[]>;
    getActiveLimitsRequestByApprover(params: {
        page: number;
        size: number;
    }): Promise<ActiveLimitRequestInfo>;
    getOldLimitsRequestByApprover(params: {
        page: number;
        size: number;
    }): Promise<OldLimitRequestInfo>;
    getAllLimitRequests(): Promise<LimitRequestInfo[]>;
    getLimitByDepartment(departmentId: string): Promise<Limit[]>;
    approveLimitRequest(data: LimitRequestSavingObject): Promise<number>;
    getLimitSharing(limitId: string): Promise<LimitSharing[]>;
    getLimitsIdByDepartmentId(depId: string): string | undefined;
    getEmployeeLimit(): Promise<Limit>;
    setCurrentRequest(id: string): void;
    cancelLimitRequest(data: {
        requestId: string;
        description: string;
    }): Promise<void>;
    changeDepLimitRequest(data: LimitSendRequest, requestId: string): Promise<number>;
    changeEmpLimitRequest(data: LimitEmpRequest, requestId: string): Promise<number>;
    initStore(): void;
    getLimits(): void;
    refreshLimits(): void;
    getLimitTransferHistory(limitId: string | string, year: number, maxRecords: number): Promise<LimitTransferHistory[]>;
    getLimitCostHistory(limitId: string | string, maxRecords: number, organizationId: string | string): Promise<LimitCostHistory[]>;
    getAccountBonuses(): Promise<Bonuses>;
}
export interface ILimitsService {
    getDepartmentLimits(): Promise<Limit[]>;
    getDepartmentLimitsByYear(depId: string, year: string): Promise<Limit>;
    getEmployeeLimits(): Promise<Limit[]>;
    getLimitSharing(limitId: string): Promise<LimitSharing[]>;
    getEmployeeLimit(employeeId: string, year: number): Promise<Limit>;
    getLimitsRequestsByAuthor(): Promise<ISpentActionsType[]>;
    getLimitsRequestByApprover(): Promise<LimitRequestInfo[]>;
    getActiveLimitsRequestByApprover(params: {
        page: number;
        size: number;
    }): Promise<ActiveLimitRequestInfo>;
    getOldLimitsRequestByApprover(params: {
        page: number;
        size: number;
    }): Promise<OldLimitRequestInfo>;
    cancelLimitRequest(data: {
        requestId: string;
        description: string;
    }): Promise<number>;
    getAllLimitRequests(): Promise<LimitRequestInfo[]>;
    getLimitByDepartment(departmentId: string): Promise<Limit[]>;
    approveLimitRequest(data: LimitRequestSavingObject): Promise<number>;
    changeDepLimitRequest(data: LimitSendRequest, requestId: string): Promise<number>;
    changeEmpLimitRequest(data: LimitEmpRequest, requestId: string): Promise<number>;
    getLimitTransferHistory(limitId: string | string, year: number, maxRecords: number): Promise<LimitTransferHistory[]>;
    getLimitCostHistory(limitId: string | string, maxRecords: number, organizationId: string | string): Promise<LimitCostHistory[]>;
    getAccountBonuses(ownerId: string): Promise<Bonuses>;
}
export declare const DepSiblings: t.ExactC<t.TypeC<{
    id: t.StringC;
    humanReadableId: t.StringC;
    organizationId: t.StringC;
    code: t.StringC;
    departmentName: t.StringC;
    departmentHeadId: t.StringC;
    parentId: t.StringC;
    active: t.BooleanC;
}>>;
export type DepSiblings = t.TypeOf<typeof DepSiblings>;
export interface ILimitDetailed {
    id: string;
    value: ILimitValueDetailed;
    fromDate: string;
    toDate: string;
    transportTypeId: string;
}
export declare const IOLimitValue: t.TypeC<{
    initial: t.NumberC;
    spent: t.NumberC;
    reserved: t.NumberC;
}>;
export type TLimitValue = t.TypeOf<typeof IOLimitValue>;
export declare enum LIMIT_TYPE {
    DEPARTMENT = "DEPARTMENT",
    EMPLOYEE = "EMPLOYEE"
}
export declare const LimitTypeTitles: {
    EMPLOYEE: string;
    DEPARTMENT: string;
};
export declare enum LIMIT_STATUS {
    PLANNING = "PLANNING",
    SHARED = "SHARED",
    CLOSED = "CLOSED",
    CANCELED = "CANCELED"
}
export declare enum LIMIT_SHARING_TYPE {
    MONTHLY = "MONTHLY",
    QUARTER = "QUARTER",
    PERCENTS = "PERCENTS"
}
export declare enum LIMIT_SHARING_TYPE_TITLES {
    MONTHLY = "\u041D\u0430 \u043A\u0430\u0436\u0434\u044B\u0439 \u043C\u0435\u0441\u044F\u0446",
    QUARTER = "\u041F\u043E\u043A\u0432\u0430\u0440\u0442\u0430\u043B\u044C\u043D\u043E",
    PERCENTS = "\u041F\u0440\u043E\u0446\u0435\u043D\u0442\u044B"
}
export declare enum LIMIT_SERVICE_TYPE {
    PASSENGER = "PASSENGER",
    CARGO = "CARGO"
}
export declare const limitSharingPerPeriodDTO: t.TypeC<{
    author: t.StringC;
    balance: t.NumberC;
    creationTime: t.StringC;
    id: t.StringC;
    limitSharing: t.StringC;
    periodNumber: t.NumberC;
    sum: t.NumberC;
}>;
export declare const LimitSharing: t.IntersectionC<[t.ExactC<t.TypeC<{
    id: t.StringC;
    author: t.StringC;
    creationTime: t.StringC;
    transportType: t.StringC;
    sum: tt.MoneyC;
    balance: tt.MoneyC;
    limitId: t.StringC;
}>>, t.PartialC<{
    limitSharingPerPeriodDTO: t.TypeC<{
        author: t.StringC;
        balance: t.NumberC;
        creationTime: t.StringC;
        id: t.StringC;
        limitSharing: t.StringC;
        periodNumber: t.NumberC;
        sum: t.NumberC;
    }>;
}>]>;
export type LimitSharing = t.TypeOf<typeof LimitSharing>;
declare const department: t.ExactC<t.TypeC<{
    id: t.StringC;
    code: t.StringC;
    departmentName: t.StringC;
}>>;
export type Department = t.TypeOf<typeof department>;
export declare const Limit: t.IntersectionC<[t.ExactC<t.TypeC<{
    limitType: t.UnionC<[t.LiteralC<LIMIT_TYPE.EMPLOYEE>, t.LiteralC<LIMIT_TYPE.DEPARTMENT>]>;
    id: t.StringC;
    humanReadableId: t.StringC;
    limitOwner: t.StringC;
    reserve: tt.MoneyC;
    limitStatus: t.Type<LIMIT_STATUS, LIMIT_STATUS, unknown>;
    year: t.NumberC;
    sum: tt.MoneyC;
    limitSharingType: t.Type<LIMIT_SHARING_TYPE, LIMIT_SHARING_TYPE, unknown>;
    limitServiceType: t.Type<LIMIT_SERVICE_TYPE, LIMIT_SERVICE_TYPE, unknown>;
    finalSharing: t.BooleanC;
    useThisLimit: t.BooleanC;
}>>, t.PartialC<{
    employee: t.ExactC<t.TypeC<{
        id: t.StringC;
        humanReadableId: t.StringC;
        firstName: t.StringC;
        lastName: t.StringC;
        personnelNumber: t.StringC;
    }>>;
    department: t.ExactC<t.TypeC<{
        id: t.StringC;
        code: t.StringC;
        departmentName: t.StringC;
    }>>;
    parentLimitId: t.StringC;
}>]>;
export type Limit = t.TypeOf<typeof Limit>;
export type DepartmentLimit = Limit & {
    limitType: LIMIT_TYPE.DEPARTMENT;
};
export type EmployeeLimit = Limit & {
    limitType: LIMIT_TYPE.EMPLOYEE;
};
export interface ILimitAction {
    actionDate: string;
    sum: number;
}
export interface ILimitEmployeeAction {
    employeeId: string;
    spent: ILimitAction[];
    reserved: ILimitAction[];
}
export interface ILimitValueDetailed {
    initial: number;
    actions: ILimitEmployeeAction[];
}
export declare const LimitColorsPercent: t.ExactC<t.TypeC<{
    name: t.StringC;
    value: t.StringC;
}>>;
export type LimitColorsPercent = t.TypeOf<typeof LimitColorsPercent>;
export declare const IOLimitColorsPercent: t.TypeC<{
    name: t.StringC;
    value: t.StringC;
}>;
export type TLimitColorsPercent = t.TypeOf<typeof IOLimitColorsPercent>;
export declare const author: t.ExactC<t.TypeC<{
    id: t.StringC;
    humanReadableId: t.StringC;
    firstName: t.StringC;
    patronymic: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
    lastName: t.StringC;
    personnelNumber: t.StringC;
    positionId: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
    organizationId: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
}>>;
export declare const ApproverDTOList: t.ExactC<t.TypeC<{
    id: t.StringC;
    departmentId: t.StringC;
    employeeId: t.StringC;
    limitRequestId: t.StringC;
    sum: tt.MoneyC;
    approvalState: t.UnionC<[t.LiteralC<"AWAITING_APPROVAL">, t.LiteralC<"APPROVED">, t.LiteralC<"DECLINED">]>;
}>>;
export declare enum LIMIT_REQUEST_STATUS {
    INIT = "INIT",
    DONE_FULLY = "DONE_FULLY",
    DONE_PARTLY = "DONE_PARTLY",
    CANCELLED = "CANCELLED",
    DECLINED = "DECLINED"
}
export declare enum LimitRequestTitlesEnum {
    INIT = "\u041E\u0442\u043A\u0440\u044B\u0442\u0430",
    DONE_FULLY = "\u0412\u044B\u043F\u043E\u043B\u043D\u0435\u043D\u0430 \u043F\u043E\u043B\u043D\u043E\u0441\u0442\u044C\u044E",
    DONE_PARTLY = "\u0412\u044B\u043F\u043E\u043B\u043D\u0435\u043D\u0430 \u0447\u0430\u0441\u0442\u0438\u0447\u043D\u043E",
    CANCELLED = "\u041E\u0442\u043C\u0435\u043D\u0435\u043D\u0430",
    DECLINED = "\u0417\u0430\u043A\u0440\u044B\u0442\u0430"
}
export declare enum LIMIT_REQUEST_LEVEL {
    SIBLINGS = "SIBLINGS",
    PARENT = "PARENT"
}
export declare enum LIMIT_REQUEST_LEVEL_TITLES {
    SIBLINGS = "\u0421\u043C\u0435\u0436\u043D\u044B\u0435 \u043F\u043E\u0434\u0440\u0430\u0437\u0434\u0435\u043B\u0435\u043D\u0438\u044F",
    PARENT = "\u0412\u044B\u0448\u0435\u0441\u0442\u043E\u044F\u0449\u0435\u0435 \u043F\u043E\u0434\u0440\u0430\u0437\u0434\u0435\u043B\u0435\u043D\u0438\u0435"
}
export declare enum LimitRequestStatusesTitlesEnum {
    INIT = "\u041D\u0430 \u0441\u043E\u0433\u043B\u0430\u0441\u043E\u0432\u0430\u043D\u0438\u0438",
    DONE_FULLY = "\u0412\u044B\u043F\u043E\u043B\u043D\u0435\u043D\u0430 \u043F\u043E\u043B\u043D\u043E\u0441\u0442\u044C\u044E",
    DONE_PARTLY = "\u0412\u044B\u043F\u043E\u043B\u043D\u0435\u043D\u0430 \u0447\u0430\u0441\u0442\u0438\u0447\u043D\u043E",
    CANCELLED = "\u041E\u0442\u043C\u0435\u043D\u0435\u043D\u0430",
    DECLINED = "\u0417\u0430\u043A\u0440\u044B\u0442\u0430"
}
export declare const LimitRequestData: t.ExactC<t.TypeC<{
    id: t.StringC;
    humanReadableId: t.StringC;
    year: t.NumberC;
    period: t.NumberC;
    transportType: t.Type<TransportTypeEnum, TransportTypeEnum, unknown>;
    sum: tt.MoneyC;
    description: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
    declineReason: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
    status: t.Type<LIMIT_REQUEST_STATUS, LIMIT_REQUEST_STATUS, unknown>;
    creationTime: t.StringC;
    limitType: t.Type<LIMIT_TYPE, LIMIT_TYPE, unknown>;
}>>;
export declare const LimitRequestInfo: t.IntersectionC<[t.ExactC<t.TypeC<{
    id: t.StringC;
    humanReadableId: t.StringC;
    year: t.NumberC;
    period: t.NumberC;
    transportType: t.Type<TransportTypeEnum, TransportTypeEnum, unknown>;
    sum: tt.MoneyC;
    description: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
    declineReason: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
    status: t.Type<LIMIT_REQUEST_STATUS, LIMIT_REQUEST_STATUS, unknown>;
    creationTime: t.StringC;
    limitType: t.Type<LIMIT_TYPE, LIMIT_TYPE, unknown>;
}>>, t.TypeC<{
    approverDtoList: t.ArrayC<t.ExactC<t.TypeC<{
        id: t.StringC;
        departmentId: t.StringC;
        employeeId: t.StringC;
        limitRequestId: t.StringC;
        sum: tt.MoneyC;
        approvalState: t.UnionC<[t.LiteralC<"AWAITING_APPROVAL">, t.LiteralC<"APPROVED">, t.LiteralC<"DECLINED">]>;
    }>>>;
    author: t.ExactC<t.TypeC<{
        id: t.StringC;
        humanReadableId: t.StringC;
        firstName: t.StringC;
        patronymic: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
        lastName: t.StringC;
        personnelNumber: t.StringC;
        positionId: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
        organizationId: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
    }>>;
}>, t.PartialC<{
    limitId: t.StringC;
    limitSum: tt.MoneyC;
    limitBalance: tt.MoneyC;
    limitHumanreadableid: t.StringC;
    plannedSum: tt.MoneyC;
    askTargets: t.UnionC<[t.LiteralC<"PARENT">, t.LiteralC<"SIBLINGS">]>;
    limitSharingType: t.Type<LIMIT_SHARING_TYPE, LIMIT_SHARING_TYPE, unknown>;
}>]>;
export type LimitRequestInfo = t.TypeOf<typeof LimitRequestInfo>;
export declare const ActiveLimitRequestInfo: t.TypeC<{
    totalElements: t.NumberC;
    totalPages: t.NumberC;
    number: t.NumberC;
    sort: t.TypeC<{
        sorted: t.BooleanC;
        unsorted: t.BooleanC;
        empty: t.BooleanC;
    }>;
    size: t.NumberC;
    content: t.ArrayC<t.IntersectionC<[t.ExactC<t.TypeC<{
        id: t.StringC;
        humanReadableId: t.StringC;
        year: t.NumberC;
        period: t.NumberC;
        transportType: t.Type<TransportTypeEnum, TransportTypeEnum, unknown>;
        sum: tt.MoneyC;
        description: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
        declineReason: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
        status: t.Type<LIMIT_REQUEST_STATUS, LIMIT_REQUEST_STATUS, unknown>;
        creationTime: t.StringC;
        limitType: t.Type<LIMIT_TYPE, LIMIT_TYPE, unknown>;
    }>>, t.TypeC<{
        approverDtoList: t.ArrayC<t.ExactC<t.TypeC<{
            id: t.StringC;
            departmentId: t.StringC;
            employeeId: t.StringC;
            limitRequestId: t.StringC;
            sum: tt.MoneyC;
            approvalState: t.UnionC<[t.LiteralC<"AWAITING_APPROVAL">, t.LiteralC<"APPROVED">, t.LiteralC<"DECLINED">]>;
        }>>>;
        author: t.ExactC<t.TypeC<{
            id: t.StringC;
            humanReadableId: t.StringC;
            firstName: t.StringC;
            patronymic: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
            lastName: t.StringC;
            personnelNumber: t.StringC;
            positionId: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
            organizationId: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
        }>>;
    }>, t.PartialC<{
        limitId: t.StringC;
        limitSum: tt.MoneyC;
        limitBalance: tt.MoneyC;
        limitHumanreadableid: t.StringC;
        plannedSum: tt.MoneyC;
        askTargets: t.UnionC<[t.LiteralC<"PARENT">, t.LiteralC<"SIBLINGS">]>;
        limitSharingType: t.Type<LIMIT_SHARING_TYPE, LIMIT_SHARING_TYPE, unknown>;
    }>]>>;
    first: t.BooleanC;
    last: t.BooleanC;
    pageable: t.TypeC<{
        sort: t.TypeC<{
            sorted: t.BooleanC;
            unsorted: t.BooleanC;
            empty: t.BooleanC;
        }>;
        offset: t.NumberC;
        pageNumber: t.NumberC;
        pageSize: t.NumberC;
        paged: t.BooleanC;
        unpaged: t.BooleanC;
    }>;
    numberOfElements: t.NumberC;
    empty: t.BooleanC;
}>;
export type ActiveLimitRequestInfo = t.TypeOf<typeof ActiveLimitRequestInfo>;
export declare const OldLimitRequestInfo: t.TypeC<{
    totalElements: t.NumberC;
    totalPages: t.NumberC;
    number: t.NumberC;
    sort: t.TypeC<{
        sorted: t.BooleanC;
        unsorted: t.BooleanC;
        empty: t.BooleanC;
    }>;
    size: t.NumberC;
    content: t.ArrayC<t.IntersectionC<[t.ExactC<t.TypeC<{
        id: t.StringC;
        humanReadableId: t.StringC;
        year: t.NumberC;
        period: t.NumberC;
        transportType: t.Type<TransportTypeEnum, TransportTypeEnum, unknown>;
        sum: tt.MoneyC;
        description: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
        declineReason: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
        status: t.Type<LIMIT_REQUEST_STATUS, LIMIT_REQUEST_STATUS, unknown>;
        creationTime: t.StringC;
        limitType: t.Type<LIMIT_TYPE, LIMIT_TYPE, unknown>;
    }>>, t.TypeC<{
        approverDtoList: t.ArrayC<t.ExactC<t.TypeC<{
            id: t.StringC;
            departmentId: t.StringC;
            employeeId: t.StringC;
            limitRequestId: t.StringC;
            sum: tt.MoneyC;
            approvalState: t.UnionC<[t.LiteralC<"AWAITING_APPROVAL">, t.LiteralC<"APPROVED">, t.LiteralC<"DECLINED">]>;
        }>>>;
        author: t.ExactC<t.TypeC<{
            id: t.StringC;
            humanReadableId: t.StringC;
            firstName: t.StringC;
            patronymic: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
            lastName: t.StringC;
            personnelNumber: t.StringC;
            positionId: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
            organizationId: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
        }>>;
    }>, t.PartialC<{
        limitId: t.StringC;
        limitSum: tt.MoneyC;
        limitBalance: tt.MoneyC;
        limitHumanreadableid: t.StringC;
        plannedSum: tt.MoneyC;
        askTargets: t.UnionC<[t.LiteralC<"PARENT">, t.LiteralC<"SIBLINGS">]>;
        limitSharingType: t.Type<LIMIT_SHARING_TYPE, LIMIT_SHARING_TYPE, unknown>;
    }>]>>;
    first: t.BooleanC;
    last: t.BooleanC;
    pageable: t.TypeC<{
        sort: t.TypeC<{
            sorted: t.BooleanC;
            unsorted: t.BooleanC;
            empty: t.BooleanC;
        }>;
        offset: t.NumberC;
        pageNumber: t.NumberC;
        pageSize: t.NumberC;
        paged: t.BooleanC;
        unpaged: t.BooleanC;
    }>;
    numberOfElements: t.NumberC;
    empty: t.BooleanC;
}>;
export type OldLimitRequestInfo = t.TypeOf<typeof OldLimitRequestInfo>;
export declare const LimitRequestSavingObject: t.TypeC<{
    requestId: t.StringC;
    sum: tt.MoneyC;
    approvalState: t.UnionC<[t.LiteralC<"AWAITING_APPROVAL">, t.LiteralC<"APPROVED">, t.LiteralC<"DECLINED">]>;
}>;
export type LimitRequestSavingObject = t.TypeOf<typeof LimitRequestSavingObject>;
export declare const LimitRequestSaving: t.TypeC<{
    sum: tt.MoneyC;
    year: t.NumberC;
    description: t.StringC;
    period: t.NumberC;
    transportType: t.StringC;
}>;
export type LimitRequestSaving = t.TypeOf<typeof LimitRequestSaving>;
export declare enum ECONOMY_HISTORY_TYPE {
    FROM_ECONOMY = "FROM_ECONOMY",
    TO_ECONOMY = "TO_ECONOMY",
    GENERAL = "GENERAL"
}
export declare const LimitTransferHistory: t.ExactC<t.TypeC<{
    id: t.StringC;
    author: t.StringC;
    creationTime: t.StringC;
    sum: tt.MoneyC;
    sourceLimit: t.StringC;
    targetLimit: t.StringC;
    sourceTransportType: t.Type<TransportTypeEnum, TransportTypeEnum, unknown>;
    targetTransportType: t.Type<TransportTypeEnum, TransportTypeEnum, unknown>;
    year: t.NumberC;
    period: t.UnionC<[t.Type<number, number, unknown>, t.UndefinedC]>;
    historyType: t.Type<ECONOMY_HISTORY_TYPE, ECONOMY_HISTORY_TYPE, unknown>;
}>>;
export type LimitTransferHistory = t.TypeOf<typeof LimitTransferHistory>;
export declare const LimitCostHistory: t.ExactC<t.TypeC<{
    limitId: t.StringC;
    sumReserved: tt.MoneyC;
    sumSpent: t.UnionC<[t.Type<number, number, unknown>, t.UndefinedC]>;
}>>;
export type LimitCostHistory = t.TypeOf<typeof LimitCostHistory>;
export declare enum BonusOperation {
    DEPOSIT = "DEPOSIT",
    SPEND = "SPEND"
}
export declare enum BonusStatus {
    RESERVED = "RESERVED",
    DONE = "DONE",
    CANCELED = "CANCELED"
}
export declare const BonusesRequest: t.TypeC<{
    id: t.StringC;
    sum: t.NumberC;
    operation: t.Type<BonusOperation, BonusOperation, unknown>;
    status: t.Type<BonusStatus, BonusStatus, unknown>;
    updateTime: t.StringC;
    reason: t.StringC;
}>;
export declare const Bonuses: t.TypeC<{
    ownerId: t.StringC;
    sum: t.NumberC;
    balance: t.NumberC;
    requests: t.ArrayC<t.TypeC<{
        id: t.StringC;
        sum: t.NumberC;
        operation: t.Type<BonusOperation, BonusOperation, unknown>;
        status: t.Type<BonusStatus, BonusStatus, unknown>;
        updateTime: t.StringC;
        reason: t.StringC;
    }>>;
}>;
export type Bonuses = t.TypeOf<typeof Bonuses>;
export type BonusesRequest = t.TypeOf<typeof BonusesRequest>;
export declare const ISpentActionsType: t.ExactC<t.TypeC<{
    id: t.StringC;
    fullName: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
    transportType: t.StringC;
    period: t.NumberC;
    sum: tt.MoneyC;
    creationTime: t.StringC;
    approverDtoList: t.ArrayC<t.ExactC<t.TypeC<{
        id: t.StringC;
        departmentId: t.StringC;
        employeeId: t.StringC;
        limitRequestId: t.StringC;
        sum: tt.MoneyC;
        approvalState: t.UnionC<[t.LiteralC<"AWAITING_APPROVAL">, t.LiteralC<"APPROVED">, t.LiteralC<"DECLINED">]>;
    }>>>;
    status: t.StringC;
    limitSum: t.UnionC<[t.Type<number, number, unknown>, t.UndefinedC]>;
    humanReadableId: t.StringC;
    author: t.ExactC<t.TypeC<{
        id: t.StringC;
        humanReadableId: t.StringC;
        firstName: t.StringC;
        patronymic: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
        lastName: t.StringC;
        personnelNumber: t.StringC;
        positionId: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
        organizationId: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
    }>>;
    limitBalance: t.UnionC<[t.Type<number, number, unknown>, t.UndefinedC]>;
    limitSharingType: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
    description: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
    limitType: t.Type<LIMIT_TYPE, LIMIT_TYPE, unknown>;
    limitHumanreadableid: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
    level: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
    declineReason: t.UnionC<[t.Type<string, string, unknown>, t.UndefinedC]>;
}>>;
export type ISpentActionsType = t.TypeOf<typeof ISpentActionsType>;
export {};
