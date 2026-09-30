import { ISpentActionsType, Bonuses, ILimitsService, Limit, LimitCostHistory, LimitRequestInfo, ActiveLimitRequestInfo, OldLimitRequestInfo, LimitRequestSavingObject, LimitSharing, LimitTransferHistory } from './Limit.interface';
import { LimitEmpRequest, LimitSendRequest } from './LimitsRequest.interface';
export declare class DILimitsService implements ILimitsService {
    private http;
    private process;
    getDepartmentLimits(): Promise<Limit[]>;
    getDepartmentLimitsByYear(depId: string, year: string): Promise<Limit>;
    getEmployeeLimits(): Promise<Limit[]>;
    getLimitSharing(limitId: string): Promise<LimitSharing[]>;
    getEmployeeLimit(employeeId: string, year: number): Promise<Limit>;
    getLimitsRequestsByAuthor(): Promise<ISpentActionsType[]>;
    cancelLimitRequest(data: {
        requestId: string;
        description: string;
    }): Promise<number>;
    getLimitsRequestByApprover(): Promise<LimitRequestInfo[]>;
    getActiveLimitsRequestByApprover({ page, size }: {
        page: number;
        size: number;
    }): Promise<ActiveLimitRequestInfo>;
    getOldLimitsRequestByApprover({ page, size }: {
        page: number;
        size: number;
    }): Promise<OldLimitRequestInfo>;
    getAllLimitRequests(): Promise<LimitRequestInfo[]>;
    approveLimitRequest(data: LimitRequestSavingObject): Promise<number>;
    changeDepLimitRequest(data: LimitSendRequest, requestId: string): Promise<number>;
    changeEmpLimitRequest(data: LimitEmpRequest, requestId: string): Promise<number>;
    getLimitByDepartment(departmentId: string): Promise<Limit[]>;
    getLimitTransferHistory(limitId: string | string, year: number, maxRecords: number): Promise<LimitTransferHistory[]>;
    getLimitCostHistory(limitId: string | string, maxRecords: number, organizationId: string | string): Promise<LimitCostHistory[]>;
    getAccountBonuses(ownerId: string): Promise<Bonuses>;
}
