import { AxiosResponse } from 'axios';
import type { IAxiosRequestConfigExtended } from './http.interface';
import { HttpMiddleware } from './HttpMiddleware';
export declare class HttpService extends HttpMiddleware {
    get<T>(url: string, params?: IAxiosRequestConfigExtended): Promise<AxiosResponse<T>>;
    post<T = any>(url: string, data?: object, params?: IAxiosRequestConfigExtended): Promise<AxiosResponse<T>>;
    postFormData<T = any>(url: string, data?: object, params?: IAxiosRequestConfigExtended): Promise<AxiosResponse<T>>;
    put<T = any>(url: string, data?: object, params?: IAxiosRequestConfigExtended): Promise<AxiosResponse<T>>;
    delete<T = any>(url: string, params?: IAxiosRequestConfigExtended): Promise<AxiosResponse<T>>;
    patch<T = any>(url: string, data?: object, params?: IAxiosRequestConfigExtended): Promise<AxiosResponse<T>>;
    request<T = any>(config: IAxiosRequestConfigExtended): Promise<AxiosResponse<T>>;
}
