import { AxiosError, AxiosResponse } from 'axios';
import { either } from 'fp-ts';
import { Either } from 'fp-ts/lib/Either';
import { pipe } from 'fp-ts/lib/function';
import { inject, injectable } from 'inversify';
import * as t from 'io-ts';
import { Type } from 'io-ts';
import reporter, { formatValidationErrors } from 'io-ts-reporters';
import isPlainObject from 'lodash/isPlainObject';

import { TYPES } from '../../ioc/ioc.types';
import { isAxiosResponse } from '../../utils/utils';
import type { ILogger } from '../Logger/Logger.interface';

export interface IResponseService {
  getResponseData<T>(response: AxiosResponse<T> | T, typeName?: Type<any>): T;
  getResponseError<T>(error: Error | AxiosError<T>): Promise<Error>;
  getResponseStatus(response: AxiosResponse): number;
  processStatus(status: number, successMessage: string, failedMessage?: string): boolean | undefined;
  decodeResponseData<T, U = T>(type: Type<U, T>): ({ data }: AxiosResponse<T>) => U;
}

@injectable()
export class ResponseService implements IResponseService {
  @inject(TYPES.ILogger)
  private logger!: ILogger;

  getResponseData = <T>(response: AxiosResponse<T> | T, typeName?: Type<any>): T => {
    if (!isAxiosResponse(response)) {
      this.logger.toConsole('warn', 'There is no data field in response object');
      return response;
    }

    if (typeName !== undefined) {
      this.checkType(response.data, typeName).forEach(val => this.handleTypeError(val, response.data));
    }

    return response.data;
  };

  decodeResponseData
    = <T, U = T>(type: Type<U, T> = t.any) => ({ data }: AxiosResponse<T>): U => pipe(
    type.decode(data) as Either<t.Errors, U>,
    either.mapLeft(formatValidationErrors),
    either.fold(errors => {
      errors.forEach(val => this.handleTypeError(val, data));

      // в случае ошибки вернуть полученные данные как есть
      // TODO: это должно быть фатальной ошибкой
      return data as any as U;
    }, t.identity)
  );

  getResponseStatus = (response: AxiosResponse): number => {
    if ('status' in response) {
      return response.status;
    }
    this.logger.toConsole('warn', 'There is no status field in response object');
    return response;
  };

  getResponseError = <T>(error: Error | AxiosError<T>): Promise<Error> => {
    if (isPlainObject(error) && 'response' in error) {
      return Promise.reject(
        Error(
          `${error?.response?.statusText} ${error?.response?.status}` || `Неизвестная ошибка ${error?.response?.status}`
        )
      );
    }
    return Promise.reject(error);
  };

  processStatus(
    status: number,
    successMessage: string,
    failedMessage = 'Ошибка при выполнении запроса'
  ): boolean | undefined {
    const success = [200];
    const failure = [404, 403];

    if (success.includes(status)) {
      this.logger.toMessage('success', successMessage);
      return true;
    }

    if (failure.includes(status)) {
      this.logger.toMessage('error', failedMessage);
      return false;
    }

    return undefined;
  }

  private checkType = <T>(value: T, type: Type<any>): string[] => {
    let report: string[] = [];

    if (Array.isArray(value)) {
      value.forEach(x => {
        const validation = type.decode(x);
        report = [...report, ...reporter.report(validation)];
      });
    } else {
      const validation = type.decode(value);
      report = reporter.report(validation);
    }

    return report;
  };

  private handleTypeError = (x: string, data: unknown): void => {
    this.logger.toError(x, 'Type Error', data);
  };
}
