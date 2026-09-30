/* eslint-disable @typescript-eslint/no-explicit-any */
import { EvaluationTypes } from '../constants/constants';
import { UUID } from '../utils/io-ts';

/** Диалог с ассистентом */
export interface ChatHistory {
  question: string;
  answer?: string;
  userEvaluation: EvaluationTypes;
  comment?: string;
  id: UUID;
}

export interface AxiosResponse<T> {
  data: T;
  status: number;
  statusText: string;
  headers: any;
  config: any;
  request?: any;
}
export type Message = 'info' | 'success' | 'warning' | 'error';

export interface IHttpService {
  get<T>(url: string, params?: Record<string, unknown>): Promise<AxiosResponse<T>>;
  post<T>(url: string, data: Record<string, unknown>, params?: Record<string, unknown>): Promise<AxiosResponse<T>>;
}
