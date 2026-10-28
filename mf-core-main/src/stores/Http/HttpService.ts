import { AxiosRequestConfig, AxiosResponse } from 'axios';

import { Final } from '../../utils/decorators';
import type { IAxiosRequestConfigExtended } from './http.interface';
import { HttpMiddleware } from './HttpMiddleware';

export class HttpService extends HttpMiddleware {
  @Final
  public get<T>(url: string, params: IAxiosRequestConfigExtended = {}): Promise<AxiosResponse<T>> {
    return this.client.get<T>(url, params);
  }

  @Final
  public post<T = any>(
    url: string,
    data: object = {},
    params: IAxiosRequestConfigExtended = {}
  ): Promise<AxiosResponse<T>> {
    return this.client.post<T>(url, data, params);
  }

  @Final
  public postFormData<T = any>(
    url: string,
    data: object = {},
    params: IAxiosRequestConfigExtended = {}
  ): Promise<AxiosResponse<T>> {
    const paramsFormData: AxiosRequestConfig = {
      ...params,
      headers: {
        ...params.headers,
        ...(!(params.headers && 'Content-Type' in params.headers) && {
          'Content-Type': 'multipart/form-data; boundary="boundary"',
        }),
      },
    };

    return this.client.post<T>(url, data, paramsFormData);
  }

  @Final
  public put<T = any>(
    url: string,
    data: object = {},
    params: IAxiosRequestConfigExtended = {}
  ): Promise<AxiosResponse<T>> {
    return this.client.put<T>(url, data, params);
  }

  @Final
  public delete<T = any>(url: string, params: IAxiosRequestConfigExtended = {}): Promise<AxiosResponse<T>> {
    return this.client.delete<T>(url, params);
  }

  @Final
  public patch<T = any>(
    url: string,
    data: object = {},
    params: IAxiosRequestConfigExtended = {}
  ): Promise<AxiosResponse<T>> {
    return this.client.patch<T>(url, data, params);
  }

  @Final
  public request<T = any>(config: IAxiosRequestConfigExtended): Promise<AxiosResponse<T>> {
    return this.client.request(config);
  }
}
