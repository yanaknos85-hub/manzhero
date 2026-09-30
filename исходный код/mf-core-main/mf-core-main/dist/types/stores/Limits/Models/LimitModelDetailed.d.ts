import { ILimitDetailed, ILimitValueDetailed } from '../Limit.interface';
export declare class LimitModelDetailed implements ILimitDetailed {
    id: string;
    value: ILimitValueDetailed;
    fromDate: string;
    toDate: string;
    transportTypeId: string;
    constructor(limit: ILimitDetailed);
}
