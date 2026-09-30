var __spreadArray = (this && this.__spreadArray) || function (to, from, pack) {
    if (pack || arguments.length === 2) for (var i = 0, l = from.length, ar; i < l; i++) {
        if (ar || !(i in from)) {
            if (!ar) ar = Array.prototype.slice.call(from, 0, i);
            ar[i] = from[i];
        }
    }
    return to.concat(ar || Array.prototype.slice.call(from));
};
import isPlainObject from 'lodash/isPlainObject';
import moment from 'moment';
import { declOfNum, declOfNumForSymbols } from './declOfNum';
/**
 * Получение значения переменной окружения
 * @param name имя переменной окружения
 * @returns значение переменной окружения
 */
function _env(name) {
    return process.env["REACT_APP_".concat(name)] || process.env[name];
}
/**
 * Кодирование строки в base64
 * @description корректно работает с UTF-8
 */
function b64EncodeUnicode(str) {
    return btoa(encodeURIComponent(str).replace(/%([0-9A-F]{2})/g, function (match, p1) { return String.fromCharCode(parseInt(p1, 16)); }));
}
/**
 * Декодирование строки из base64
 * @description корректно работает с UTF-8
 */
function b64DecodeUnicode(str) {
    return decodeURIComponent(Array.prototype.map.call(atob(str), function (c) { return "%".concat("00".concat(c.charCodeAt(0).toString(16)).slice(-2)); }).join(''));
}
/**
 * Получение данных токена
 * @param token JWT токен авторизации
 * @returns декодированный набор данных токена
 */
function jwtDecode(token) {
    return JSON.parse(b64DecodeUnicode(token.split('.')[1]));
}
// eslint-disable-next-line @typescript-eslint/explicit-function-return-type, @typescript-eslint/no-empty-function
function noop() { }
function isFilledObject(value) {
    return isPlainObject(value) && Object.keys(value).length > 0;
}
function isEmptyObject(value) {
    return isPlainObject(value) && Object.keys(value).length === 0;
}
/**
 * @param TypeName Название модели(типа), в которую будет осуществлён каст
 * @param value Значение (массив или объект), который будет каститься
 * @param args Аргументы конструктора модели
 */
function plainToNew(TypeName, value) {
    var args = [];
    for (var _i = 2; _i < arguments.length; _i++) {
        args[_i - 2] = arguments[_i];
    }
    if (value instanceof TypeName || isEmptyObject(value) || (Array.isArray(value) && value.length === 0)) {
        return value;
    }
    // Если передаётся массив моделей, то вернём их без преобразований
    if (Array.isArray(value) && value[0] instanceof TypeName) {
        return value.map(function (x) { return x; });
    }
    if (Array.isArray(value) && isPlainObject(value[0])) {
        return value.map(function (x) { return new (TypeName.bind.apply(TypeName, __spreadArray([void 0, x], args, false)))(); });
    }
    if (isFilledObject(value)) {
        return new (TypeName.bind.apply(TypeName, __spreadArray([void 0, value], args, false)))();
    }
    return undefined;
}
function joinUrl() {
    var string = [];
    for (var _i = 0; _i < arguments.length; _i++) {
        string[_i] = arguments[_i];
    }
    return string.join('').replace(/\/\/+/g, '/');
}
/**
 * @description Удаление всех символов, кроме русского и английского языков и цифр
 */
function clearSymbols(value) {
    return value.replace(/[^0-9A-Za-zА-Яа-я.]/, '');
}
/**
 * @description Замена бесконечного числа пробелов на один
 */
function formatSpaces(value) {
    return value.replace(/ +/g, ' ').trim();
}
function includesByLowerCaseAndSpaces(first, second) {
    if (first && second) {
        var prepareSecond = clearSymbols(formatSpaces(second));
        return first.toLowerCase().includes(prepareSecond.toLowerCase());
    }
    return false;
}
var isAxiosResponse = function (response) { return 'data' in response; };
var isAxiosError = function (error) { return 'isAxiosError' in error && 'response' in error; };
export var formatPercents = function (percent) { return new Intl.NumberFormat('ru-RU', {
    style: 'percent',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
}).format(percent); };
/**
 * @value миллисекунды
 */
var getDaysTime = function (value) {
    if (value === void 0) { value = 0; }
    var days = Math.floor(value / (24 * 60 * 60 * 1000));
    var daysms = value % (24 * 60 * 60 * 1000);
    var hours = Math.floor(daysms / (60 * 60 * 1000));
    var hoursms = value % (60 * 60 * 1000);
    var minutes = Math.floor(hoursms / (60 * 1000));
    var minutesms = value % (60 * 1000);
    var sec = Math.floor(minutesms / 1000);
    return {
        days: days || 0,
        hours: hours || 0,
        minutes: minutes || 0,
        sec: sec || 0,
    };
};
/**
 * @value миллисекунды
 */
var getTime = function (value, showHours, showMinutes) {
    if (value === void 0) { value = 0; }
    var daysTime = getDaysTime(value);
    return [
        daysTime.days && "".concat(daysTime.days, " \u0434"),
        daysTime.hours && (showHours || !daysTime.days) && "".concat(daysTime.hours, " \u0447"),
        daysTime.minutes && (showMinutes || !daysTime.hours) && "".concat(daysTime.minutes, " \u043C\u0438\u043D"),
    ]
        .filter(Boolean)
        .join('. ');
};
/**
 * @value километры
 */
var getDistance = function (value) {
    if (value === void 0) { value = 0; }
    return "".concat(Math.round(value), " \u043A\u043C");
};
/**
 * @value килограммы
 */
var getWeight = function (value) {
    if (value === void 0) { value = 0; }
    return "".concat(Math.round(value), " \u043A\u0433");
};
/**
 * @value сантиметры
 */
var getVolume = function (value) {
    var _a;
    if (value === void 0) { value = 0; }
    var val = value / (100 * 100 * 100);
    return "".concat(((_a = val.toString().split('.')[1]) === null || _a === void 0 ? void 0 : _a.length) > 2 ? val.toFixed(2) : val, "  \u043C\u00B3");
};
var getTimeString = function (timeValue) {
    var time = timeValue / 60000;
    if (time < 60) {
        return "".concat(Math.round(time), " \u043C\u0438\u043D");
    }
    return "".concat(Math.trunc(time / 60), " \u0447 ").concat(Math.round(time % 60), " \u043C\u0438\u043D");
};
export var getYear = function (selectedMonth) {
    var isNewYear = selectedMonth === 0 && new Date().getMonth() !== 0;
    return isNewYear ? new Date().getFullYear() + 1 : new Date().getFullYear();
};
// eslint-disable-next-line @stylistic/max-len
export var sortByTime = function (array) { return array.sort(function (start, end) { return moment(end.creationTime).valueOf() - moment(start.creationTime).valueOf(); }); };
var getDistanceString = function (distance) { return "".concat(distance.toFixed(2), " \u043A\u043C"); };
var ignore = function () { return undefined; };
var getErrorMessage = function (error) {
    var response = JSON.parse(error === null || error === void 0 ? void 0 : error.request.response);
    return response.message;
};
export var toMillimeters = function (size) { return size * 10; };
var _cn = function (classNames) { return "".concat(classNames).replace(/,/g, ' ').trim(); };
var rootElement = function () { return document.querySelector('#root'); };
export { _env, jwtDecode, b64DecodeUnicode, b64EncodeUnicode, noop, plainToNew, isFilledObject, isEmptyObject, joinUrl, isAxiosResponse, isAxiosError, clearSymbols, formatSpaces, includesByLowerCaseAndSpaces, getTimeString, getDaysTime, getTime, getDistance, getWeight, getVolume, getDistanceString, ignore, getErrorMessage, declOfNum, declOfNumForSymbols, _cn, rootElement };
