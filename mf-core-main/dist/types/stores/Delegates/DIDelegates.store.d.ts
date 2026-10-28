import { EmployeeModel } from '../Employee/models/EmployeeModel';
import type { RequestCancelerSetter, RequestParams } from '../Http/http.interface';
import { TransportTypeEnum } from '../../constants/constants';
import * as DelegatesInterface from './Delegates.interface';
export declare class DIDelegatesStore implements DelegatesInterface.IDelegatesStore {
    private service;
    private selfStore;
    private logger;
    private process;
    private transportTypes;
    get selfEmployee(): EmployeeModel;
    delegates: DelegatesInterface.DelegateModel[];
    candidatesToDelegates: Record<string, EmployeeModel[]>;
    selfCandidatesToDelegates: EmployeeModel[];
    get namesWithInitials(): Record<string, string>;
    getDelegates(): Promise<void>;
    getCandidatesToDelegates(transType: string, date: string): Promise<void>;
    getSelfCandidatesToDelegates(transportType: TransportTypeEnum, date: string): Promise<void>;
    addDelegate(delegate: DelegatesInterface.DelegateModel): Promise<void>;
    deleteDelegate(delegateId: string): Promise<void>;
    initStore(): void;
    searchSelfDelegateCandidates({ transportType, ...params }: RequestParams, cancelerSetter?: RequestCancelerSetter): Promise<EmployeeModel[]>;
}
