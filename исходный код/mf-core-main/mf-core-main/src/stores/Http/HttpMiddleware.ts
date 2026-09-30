import axios, { AxiosError, AxiosInstance, AxiosResponse } from 'axios';
import { inject, injectable } from 'inversify';
import { IStringifyOptions, stringify } from 'qs';

import {
  routes, API_URL, SYSTEM_MESSAGES, X_CLIENT_TYPE, errorText, CustomErrorCode
} from '../../constants/constants';
import { TYPES } from '../../ioc/ioc.types';
import type { INavigator } from '../../utils/navigator';
import { Token } from '../../utils/token';
import type { IConfigStore } from '../../stores/Config/Config.interface';
import { isAxiosError } from '../../utils/utils';
import type { ILogger } from '../Logger/Logger.interface';
import { IAxiosRequestConfigExtended, IHttpService, IServerErrorResponseData } from './http.interface';
import { getErrorCode } from '../../utils/getErrorCode';

@injectable()
export abstract class HttpMiddleware implements IHttpService {
  private readonly _promise: PromiseConstructor;

  protected readonly client: AxiosInstance;

  // @inject(TYPES.IConfigStore)
  // private configStore!: IConfigStore;

  // @inject(TYPES.ILogger)
  // private _logger!: ILogger;

  // @inject(TYPES.INavigator)
  // private _navigator!: INavigator;

  // @inject(TYPES.Token)
  // private _token!: Token;

  constructor(
    // @ts-ignore
    @inject(TYPES.IConfigStore) private configStore: IConfigStore,
    // @ts-ignore
    @inject(TYPES.ILogger) private _logger: ILogger,
    // @ts-ignore
    @inject(TYPES.INavigator) private _navigator: INavigator,
    // @ts-ignore
    @inject(TYPES.Token) private _token: Token
  ) {
    this._promise = Promise;
    this.client = axios.create({
      baseURL: API_URL, timeout: 10000, headers: { 'x-client-type': X_CLIENT_TYPE },
    });

    const errorMiddleware = (error: Error | AxiosError<IServerErrorResponseData>): any => {
      // Логика обработки ошибок при авторизации находится в authStore и компонентах, поэтому тут пропускаем
      if (window.location.pathname === '/oauth') {
        return this._promise.reject(error);
      }

      if (this.configStore.isCorp) {
        const code = getErrorCode(error);

        // Если выелезла ошибка авторизации, когда мы находились внутри приложения
        if (code === 401) {
          (history as any).push('/');
          this._logger.toMessage('error', errorText[code]?.title);
        }

        const isLogToConsole = 'isAxiosError' in error && !error.config?.hush?.includes(code);

        if (isLogToConsole) {
          // Вывод ошибки в консоль в старом виде
          const data = error.response?.data;
          const title = `${data?.status || error.name || 'Server Error'}: ${data?.reason || data?.error || error.message}`;
          const description = (data?.reason ? data?.error : data?.message) || error.message;

          this._logger.toConsoleGroup('error', description, title, { ...error });
        }

        // Выводим уведомление об ошибке только для post, put, patch, delete, т.к. get 100% будет отловлен errorBoundary
        // и нет смысла дублировать ошибку в уведомлении
        const methodsForNotifications = ['POST', 'post', 'PUT', 'put', 'PATCH', 'patch', 'DELETE', 'delete'];
        let notifyOnError = isLogToConsole && error.config?.notifyOnError && methodsForNotifications.includes(error.config?.method ?? '');

        // Оставлены старые условия, желательно пересмотреть
        if ('isAxiosError' in error) {
          if (window.location.pathname === '/directories/departments/adding' && error.response?.status === 409) {
            notifyOnError = false;
          } else if (window.location.pathname === '/rules/cargoTypes/adding' && error.response?.status === 409) {
            notifyOnError = false;
          } else if (
            window.location.pathname === '/directories/employees/create-employee'
            && error.response?.status === 409
          ) {
            notifyOnError = false;
          } else if (window.location.pathname === '/settings/notifications' && error.response?.status === 409) {
            notifyOnError = false;
          } else if (window.location.pathname.includes('/reports/taxiRegistry/') && error.response?.status === 404) {
            notifyOnError = false;
          } else if (window.location.pathname.includes('/directories/departments/') && error.response?.status === 409) {
            notifyOnError = false;
          } else if (error.response?.status === 412) {
            notifyOnError = false;
          } else if (window.location.pathname.includes('/multi-logistics') && error.response?.status === 409) {
            notifyOnError = false;
          } else if (error.response?.config?.url?.includes('/reports/files/trip-requests-') && error.response?.status === 409) {
            notifyOnError = false;
          } else if (window.location.pathname.includes('fleet-management/telemechanic/') && error.response?.status === 500) {
            notifyOnError = false;
          } else if (window.location.pathname.includes('analytics') && error.response?.config?.url?.includes('/user-agent/')) {
            // По просьбе ВП не выводить ошибку в отчетности
            notifyOnError = false;
          }
        }

        if (notifyOnError) {
          this._logger.toNotify(
            'error',
            errorText[code]?.subtitle ?? errorText[CustomErrorCode.UNKNOWN]?.subtitle,
            errorText[code]?.title ?? errorText[CustomErrorCode.UNKNOWN]?.title,
            5,
            code
          );
        }
      } else {
        if (isAxiosError(error) && error.isAxiosError === true) {
          const reAuth = () => new Promise((): void => this._navigator.navSudirReAuth({
            reAuth: routes.SudirApi,
            fail: routes.Failure,
          })
          );

          const defaultErr: () => void = (): void => {
            this._navigator.navRoot();
            // @ts-ignore
            this._logger.toMessage('error', error.response?.statusText);
          };

          if (error.response?.status === 408 || error.code === 'ECONNABORTED') {
            // eslint-disable-next-line no-console
            console.error(error.config.url ?? '', 'Timeout exceeded');
            // this.logger.toError(error.config.url ?? '', 'Timeout exceeded');
          } else if (error.response?.status === 401) {
            // не проверять авторизацию по этому урлу

            if (/\/print\/ttn/.test(error.config.url || '')) {
              return this._promise.reject(error);
            }

            if (this.configStore.isMockedAuth) {
              return this._promise.reject(error);
            }

            if (!this.configStore.isBasicAuth) {
              return reAuth();
            }

            defaultErr();
          } else if (error.response?.status === 504) {
            if (!this.configStore.isBasicAuth) {
              return reAuth();
            }

            defaultErr();
          } else if (
            error.response?.status === 409
            && error.response?.data.message === 'The address by position 1 duplicates the previous one' /// что это такое ?!
          ) {
            this._logger.toMessage('error', SYSTEM_MESSAGES.savingWithAddressDuplicates);
          } else if (error.response?.status === 404 && error.config.url === '/requests/public/compensation') {
            if (error.response?.data.message.includes('LIMIT_NOT_FOUND: Резервирование:')) {
              /// что это такое2 ?!
              this._logger.toMessage('error', SYSTEM_MESSAGES.savingWithWrongSum);
            }
          } else if (error.response?.status === 417 && error.config.url === '/geo/address') {
            // eslint-disable-next-line no-console
            console.info(`[${error.config.url ?? ''}] address not found`);
          } else {
            // this.logger.toError(description, title, { ...error });
            /// что это такое? что это за эррор? нужно ли нам такое в таком виде?
            // eslint-disable-next-line no-console
            console.error(
              `
              [${error.config.url ?? ''}]
              ${error.response?.data.status || error.name || 'Server Error'}: ${error.response?.data.reason || error.response?.data.error || error.message}`,
              {
                description:
                  (error.response?.data.reason ? error.response?.data.error : error.response?.data.message)
                  || error.message,
                error: { ...error },
              }
            );
          }
        } else {
          // this.logger.toNotify('error', error.message, error.name);
          // eslint-disable-next-line no-console
          console.error(error);
        }
      }

      return this._promise.reject(error);
    };

    this.client.interceptors.response.use(this.responseMiddlware.bind(this), errorMiddleware);
  }

  public changeClientType(clientType: string): void {
    this.client.defaults.headers['x-client-type'] = clientType;
  }

  public setParamsSerializer(options?: IStringifyOptions): void {
    this.client.defaults.paramsSerializer = params => stringify(params, options);
  }

  // eslint-disable-next-line @stylistic/max-len
  public setRequestInterceptor(middleware: (config: IAxiosRequestConfigExtended) => Promise<IAxiosRequestConfigExtended>) {
    this.client.interceptors.request.use(middleware);
  }

  private responseMiddlware(response: AxiosResponse): Promise<AxiosResponse> {
    return this._promise.resolve(response);
  }

  public abstract get<T>(url: string, params?: IAxiosRequestConfigExtended): Promise<AxiosResponse<T>>;

  public abstract post<T>(
    url: string,
    data: Record<string, any>,
    params?: IAxiosRequestConfigExtended
  ): Promise<AxiosResponse<T>>;

  public abstract postFormData<T>(
    url: string,
    data: Record<string, any>,
    params?: IAxiosRequestConfigExtended
  ): Promise<AxiosResponse<T>>;

  public abstract put<T>(
    url: string,
    data: Record<string, any>,
    params?: IAxiosRequestConfigExtended
  ): Promise<AxiosResponse<T>>;

  public abstract delete<T>(url: string, params?: IAxiosRequestConfigExtended): Promise<AxiosResponse<T>>;

  public abstract patch<T>(
    url: string,
    data: Record<string, any>,
    params?: IAxiosRequestConfigExtended
  ): Promise<AxiosResponse<T>>;

  public abstract request<T = any>(config: IAxiosRequestConfigExtended): Promise<AxiosResponse<T>>;
}
