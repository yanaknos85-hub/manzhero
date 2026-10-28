import { Department, LIMIT_SERVICE_TYPE, LIMIT_SHARING_TYPE, LIMIT_STATUS, LIMIT_TYPE, Limit } from '../Limit.interface';
export declare class LimitModel implements Limit {
    department?: Department;
    parentLimitId?: string;
    id: string;
    limitOwner: string;
    year: number;
    sum: number;
    reserve: number;
    limitType: LIMIT_TYPE;
    limitStatus: LIMIT_STATUS;
    limitSharingType: LIMIT_SHARING_TYPE;
    limitServiceType: LIMIT_SERVICE_TYPE;
    finalSharing: boolean;
    useThisLimit: boolean;
    humanReadableId: string;
    constructor(limit: Limit);
    get availablePersentage(): number;
}
