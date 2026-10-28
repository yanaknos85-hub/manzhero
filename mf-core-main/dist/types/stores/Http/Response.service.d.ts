import { AxiosError, AxiosResponse } from 'axios';
import * as t from 'io-ts';
import { Type } from 'io-ts';
export interface IResponseService {
    getResponseData<T>(response: AxiosResponse<T> | T, typeName?: Type<any>): T;
    getResponseError<T>(error: Error | AxiosError<T>): Promise<Error>;
    getResponseStatus(response: AxiosResponse): number;
    processStatus(status: number, successMessage: string, failedMessage?: string): boolean | undefined;
    decodeResponseData<T, U = T>(type: Type<U, T>): ({ data }: AxiosResponse<T>) => U;
}
export declare class ResponseService implements IResponseService {
    private logger;
    getResponseData: <T>(response: T | AxiosResponse<T>, typeName?: Type<any>) => T;
    decodeResponseData: <T, U = T>(type?: t.Type<U, T, unknown>) => ({ data }: AxiosResponse<T>) => U;
    getResponseStatus: (response: AxiosResponse) => number;
    getResponseError: <T>(error: Error | AxiosError<T>) => Promise<Error>;
    processStatus(status: number, successMessage: string, failedMessage?: string): boolean | undefined;
    private checkType;
    private handleTypeError;
}
