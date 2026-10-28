import { AxiosError, AxiosResponse } from 'axios';
import { declOfNum, declOfNumForSymbols } from './declOfNum';
/**
 * Получение значения переменной окружения
 * @param name имя переменной окружения
 * @returns значение переменной окружения
 */
declare function _env(name: string): string | undefined;
/**
 * Кодирование строки в base64
 * @description корректно работает с UTF-8
 */
declare function b64EncodeUnicode(str: string): string;
/**
 * Декодирование строки из base64
 * @description корректно работает с UTF-8
 */
declare function b64DecodeUnicode(str: string): string;
/**
 * Получение данных токена
 * @param token JWT токен авторизации
 * @returns декодированный набор данных токена
 */
declare function jwtDecode<T>(token: string): T;
declare function noop(): void;
declare function isFilledObject(value: any): boolean;
declare function isEmptyObject(value: any): boolean;
type ConstructorType = new (value: any, ...args: any) => any;
/**
 * @param TypeName Название модели(типа), в которую будет осуществлён каст
 * @param value Значение (массив или объект), который будет каститься
 * @param args Аргументы конструктора модели
 */
declare function plainToNew<R>(TypeName: ConstructorType, value: object | object[] | undefined, ...args: any): typeof value extends undefined ? undefined : R;
declare function joinUrl(...string: string[]): string;
/**
 * @description Удаление всех символов, кроме русского и английского языков и цифр
 */
declare function clearSymbols(value: string): string;
/**
 * @description Замена бесконечного числа пробелов на один
 */
declare function formatSpaces(value: string): string;
declare function includesByLowerCaseAndSpaces(first?: string, second?: string): boolean;
declare const isAxiosResponse: (response: any) => response is AxiosResponse<any>;
declare const isAxiosError: (error: any) => error is AxiosError<any>;
export declare const formatPercents: (percent: number) => string;
/**
 * @value миллисекунды
 */
declare const getDaysTime: (value?: number) => {
    days: number;
    hours: number;
    minutes: number;
    sec: number;
};
/**
 * @value миллисекунды
 */
declare const getTime: (value?: number, showHours?: boolean, showMinutes?: boolean) => string;
/**
 * @value километры
 */
declare const getDistance: (value?: number) => string;
/**
 * @value килограммы
 */
declare const getWeight: (value?: number) => string;
/**
 * @value сантиметры
 */
declare const getVolume: (value?: number) => string;
declare const getTimeString: (timeValue: number) => string;
export declare const getYear: (selectedMonth: number) => number;
export declare const sortByTime: (array: any[]) => any[];
declare const getDistanceString: (distance: number) => string;
declare const ignore: () => void;
declare const getErrorMessage: (error?: AxiosError) => string;
export declare const toMillimeters: (size: number) => number;
declare const _cn: (classNames: (string | boolean | number | null | undefined)[]) => string;
declare const rootElement: () => Element;
export { _env, jwtDecode, b64DecodeUnicode, b64EncodeUnicode, noop, plainToNew, isFilledObject, isEmptyObject, joinUrl, isAxiosResponse, isAxiosError, clearSymbols, formatSpaces, includesByLowerCaseAndSpaces, getTimeString, getDaysTime, getTime, getDistance, getWeight, getVolume, getDistanceString, ignore, getErrorMessage, declOfNum, declOfNumForSymbols, _cn, rootElement };
