import * as t from 'io-ts';
import { EmployeeModel } from '../Employee/models/EmployeeModel';
import { Employee } from '../Employee/Employee.interface';
import { RequestCancelerSetter, RequestParams } from '../Http/http.interface';
import { TransportTypeEnum } from '../../constants/constants';
export interface IDelegatesStore {
    delegates: DelegateModel[];
    candidatesToDelegates?: Record<string, EmployeeModel[]>;
    selfCandidatesToDelegates: EmployeeModel[];
    namesWithInitials: Record<string, string>;
    addDelegate(delegate: Partial<DelegateModel>): void;
    getDelegates(): Promise<void>;
    getSelfCandidatesToDelegates(transportType: TransportTypeEnum, date: string): Promise<void>;
    getCandidatesToDelegates(transTypeId: string, date: string): Promise<void>;
    deleteDelegate(delegateId: string): void;
    initStore(): void;
    searchSelfDelegateCandidates(params: RequestParams, cancelerSetter?: RequestCancelerSetter): Promise<EmployeeModel[]>;
}
export interface IDelegatesService {
    getCandidatesToDelegates(args: Omit<getDeligatesArgs, 'delegateId'>): Promise<Employee[]>;
    getSelfCandidatesToDelegates(transportType: TransportTypeEnum, date: string): Promise<Employee[]>;
    getDelegates(args: Omit<getDeligatesArgs, 'transType' | 'delegateId'>): Promise<Delegate[]>;
    addDelegate(args: Omit<getDeligatesArgs, 'supId' | 'transType' | 'date' | 'delegateId'>, delegate: DelegateModel): Promise<Delegate>;
    deleteDelegate(args: Omit<getDeligatesArgs, 'supId' | 'transType' | 'date'>): Promise<number>;
    searchSelfDelegateCandidates(transportType: string, params: RequestParams, cancelerSetter?: RequestCancelerSetter): Promise<Employee[]>;
}
export interface getDeligatesArgs {
    orgId: string;
    depId: string;
    supId: string;
    transType: string;
    delegateId: string;
    date?: string;
}
export declare const Delegate: t.ExactC<t.TypeC<{
    id: t.StringC;
    supervisorId: t.StringC;
    delegateId: t.StringC;
    startDate: t.StringC;
    endDate: t.StringC;
    transportType: t.StringC;
    delegateEmployee: t.IntersectionC<[t.TypeC<{
        humanReadableId: t.StringC;
    }>, t.TypeC<{
        id: t.StringC;
        userId: t.StringC;
        firstName: t.StringC;
        lastName: t.StringC;
        personnelNumber: t.StringC;
        departmentId: t.StringC;
        organizationId: t.StringC;
        positionId: t.StringC;
    }>, t.PartialC<{
        patronymic: t.StringC;
        status: t.KeyofC<typeof import("../../constants/constants").EmployeeStatus>;
        mobilePhone: t.StringC;
        email: t.StringC;
        supervisorId: t.StringC;
        delegatedById: t.StringC;
        availableTransportTypes: t.UnknownArrayC;
        personalCars: t.UnknownArrayC;
        approvals: t.NumberC;
        positionName: t.StringC;
        departmentName: t.StringC;
    }>]>;
}>>;
export type Delegate = t.TypeOf<typeof Delegate>;
export declare class DelegateModel implements Delegate {
    id: string;
    supervisorId: string;
    delegateId: string;
    startDate: string;
    endDate: string;
    transportType: string;
    delegateEmployee: Employee;
    constructor(delegate: Delegate);
}
