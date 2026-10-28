import { AxiosRequestConfig, AxiosResponse, Canceler } from 'axios';
import { IStringifyOptions } from 'qs';

declare module 'axios' {
  interface AxiosRequestConfig {
    urlParams?: Record<string, string>;
    unstoppable?: boolean;
    hush?: number[];
    notifyOnError?: boolean;
  }
}

export interface IAxiosRequestConfigExtended extends AxiosRequestConfig {
  urlParams?: Record<string, string>;
  hush?: number[];
  notifyOnError?: boolean;
}

export interface IServerErrorResponseData {
  error: string;
  reason: string;
  message: string;
  status: number;
  timestamp: string;
}

export interface IHttpService {
  changeClientType(clientType: string): void;
  setParamsSerializer(options?: IStringifyOptions): void;
  // eslint-disable-next-line @stylistic/max-len
  setRequestInterceptor: (middleware: (config: IAxiosRequestConfigExtended) => Promise<IAxiosRequestConfigExtended>) => void;
  get<T>(url: string, params?: IAxiosRequestConfigExtended): Promise<AxiosResponse<T>>;
  post<T>(url: string, data: Record<string, any>, params?: IAxiosRequestConfigExtended): Promise<AxiosResponse<T>>;
  postFormData<T>(
    url: string,
    data: Record<string, any>,
    params?: IAxiosRequestConfigExtended
  ): Promise<AxiosResponse<T>>;
  put<T>(url: string, data: Record<string, any>, params?: IAxiosRequestConfigExtended): Promise<AxiosResponse<T>>;
  delete<T>(url: string, params?: IAxiosRequestConfigExtended): Promise<AxiosResponse<T>>;
  patch<T>(url: string, data: Record<string, any>, params?: IAxiosRequestConfigExtended): Promise<AxiosResponse<T>>;
  request<T = any>(config: IAxiosRequestConfigExtended): Promise<AxiosResponse<T>>;
}

export type RequestCanceler = Canceler;
export type RequestCancelerSetter = (cancel: RequestCanceler) => void;
export type RequestParams = Record<string, any>;
export type Requester<T> = (params: RequestParams, cancelerSetter?: RequestCancelerSetter) => Promise<T>;
