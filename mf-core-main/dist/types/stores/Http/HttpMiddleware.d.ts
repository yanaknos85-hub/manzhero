import { AxiosInstance, AxiosResponse } from 'axios';
import { IStringifyOptions } from 'qs';
import type { INavigator } from '../../utils/navigator';
import { Token } from '../../utils/token';
import type { IConfigStore } from '../../stores/Config/Config.interface';
import type { ILogger } from '../Logger/Logger.interface';
import { IAxiosRequestConfigExtended, IHttpService } from './http.interface';
export declare abstract class HttpMiddleware implements IHttpService {
    private configStore;
    private _logger;
    private _navigator;
    private _token;
    private readonly _promise;
    protected readonly client: AxiosInstance;
    constructor(configStore: IConfigStore, _logger: ILogger, _navigator: INavigator, _token: Token);
    changeClientType(clientType: string): void;
    setParamsSerializer(options?: IStringifyOptions): void;
    setRequestInterceptor(middleware: (config: IAxiosRequestConfigExtended) => Promise<IAxiosRequestConfigExtended>): void;
    private responseMiddlware;
    abstract get<T>(url: string, params?: IAxiosRequestConfigExtended): Promise<AxiosResponse<T>>;
    abstract post<T>(url: string, data: Record<string, any>, params?: IAxiosRequestConfigExtended): Promise<AxiosResponse<T>>;
    abstract postFormData<T>(url: string, data: Record<string, any>, params?: IAxiosRequestConfigExtended): Promise<AxiosResponse<T>>;
    abstract put<T>(url: string, data: Record<string, any>, params?: IAxiosRequestConfigExtended): Promise<AxiosResponse<T>>;
    abstract delete<T>(url: string, params?: IAxiosRequestConfigExtended): Promise<AxiosResponse<T>>;
    abstract patch<T>(url: string, data: Record<string, any>, params?: IAxiosRequestConfigExtended): Promise<AxiosResponse<T>>;
    abstract request<T = any>(config: IAxiosRequestConfigExtended): Promise<AxiosResponse<T>>;
}
