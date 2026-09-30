import { DepSiblings } from './Limit.interface';
import { ILimitsRequestService, LimitEmpRequest, LimitSendRequest, SiblingsParams, TLimitRequestNew } from './LimitsRequest.interface';
import { LimitRequestModel } from './Models/LimitRequest.model';
export declare class DILimitsRequestService implements ILimitsRequestService {
    private http;
    private process;
    getLimitRequestList(): Promise<TLimitRequestNew[]>;
    getDepSiblings(data: SiblingsParams): Promise<DepSiblings[]>;
    addLimitRequest(data: LimitSendRequest): Promise<number>;
    addEmpLimitRequest(data: LimitEmpRequest): Promise<number>;
    getLimitRequest(reqId: string): Promise<LimitRequestModel>;
    editLimitRequest(reqId: string, data: TLimitRequestNew): Promise<number>;
    approveLimitRequest(reqId: string): Promise<number>;
    cancelLimitRequest(reqId: string): Promise<number>;
    deleteLimitRequest(reqId: string): Promise<number>;
}
