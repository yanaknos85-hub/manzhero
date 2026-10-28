import { RequestBaseModel } from '../../../models/RequestBase.model';
import { TransportTypeEnum } from '../../../constants/constants';
import { LimitRequestApprovalStateEnum, LimitRequestStatusEnum, TLimitRequestNew } from '../LimitsRequest.interface';
export declare class LimitRequestModel extends RequestBaseModel<LimitRequestStatusEnum, LimitRequestApprovalStateEnum> implements TLimitRequestNew {
    transportType?: TransportTypeEnum;
    description?: string;
    month?: number;
    sum: number;
    constructor(request: TLimitRequestNew & RequestBaseModel);
    get isCancellable(): boolean;
    get isFinal(): boolean;
}
