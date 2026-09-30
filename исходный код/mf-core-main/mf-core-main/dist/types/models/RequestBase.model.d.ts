import { TRangePickerArg } from '../utils/types';
import { IEntityBase, IEntityStatused } from './Entity.interface';
import { IRequestBase } from './Request.interface';
export declare class RequestBaseModel<S extends string = any, A extends string = any> implements IRequestBase, IEntityBase, IEntityStatused {
    authorId?: string;
    approvalState?: A;
    status?: S;
    id: string;
    humanReadableId: string;
    creationTime: string | number;
    constructor(request: IRequestBase);
    get isExisting(): boolean;
    isMatched(statuses?: string[]): boolean;
    inDateRange: (range: TRangePickerArg) => boolean;
}
