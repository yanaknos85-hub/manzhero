import { Employee } from '../Employee/Employee.interface';
import type { RequestCancelerSetter, RequestParams } from '../Http/http.interface';
import { TransportTypeEnum } from '../../constants/constants';
import { Delegate, DelegateModel, IDelegatesService, getDeligatesArgs } from './Delegates.interface';
export declare class DIDelegatesService implements IDelegatesService {
    private http;
    private process;
    getCandidatesToDelegates(args: Omit<getDeligatesArgs, 'delegateId'>): Promise<Employee[]>;
    getSelfCandidatesToDelegates(transportType: TransportTypeEnum, date: string): Promise<Employee[]>;
    getDelegates(args: Omit<getDeligatesArgs, 'transportTypeId' | 'delegateId'>): Promise<Delegate[]>;
    addDelegate(args: Omit<getDeligatesArgs, 'supId' | 'transportType' | 'date' | 'delegateId'>, delegate: DelegateModel): Promise<Delegate>;
    deleteDelegate(args: Omit<getDeligatesArgs, 'supId' | 'transTypeId' | 'date'>): Promise<number>;
    searchSelfDelegateCandidates(transportType: string, params: RequestParams, cancelerSetter?: RequestCancelerSetter): Promise<Employee[]>;
}
