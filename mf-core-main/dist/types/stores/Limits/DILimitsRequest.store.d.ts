import lodash from 'lodash';
import { DepSiblings } from './Limit.interface';
import * as LimitsRequestInterface from './LimitsRequest.interface';
import { LimitRequestModel } from './Models/LimitRequest.model';
export declare class DILimitsRequestStore implements LimitsRequestInterface.ILimitsRequestStore {
    private service;
    private process;
    list: LimitRequestModel[];
    siblings: DepSiblings[];
    get listMapped(): lodash.Dictionary<LimitRequestModel>;
    currentRequest: LimitRequestModel | undefined;
    getList: () => Promise<void>;
    getDepSiblings: (data: LimitsRequestInterface.SiblingsParams) => Promise<DepSiblings[]>;
    createRequest(data: LimitsRequestInterface.LimitSendRequest): void;
    addLimitRequest: (data: LimitsRequestInterface.LimitSendRequest) => Promise<number>;
    addEmpLimitRequest: (data: LimitsRequestInterface.LimitEmpRequest) => Promise<number>;
    editLimitRequest(reqId: string, data: LimitsRequestInterface.TLimitRequestNew): Promise<void>;
    getRequest: (reqId: string) => Promise<LimitRequestModel>;
    setCurrentRequest(id: string): void;
    cancelRequest(id: string, reason: string): Promise<void>;
    initStore(): void;
}
