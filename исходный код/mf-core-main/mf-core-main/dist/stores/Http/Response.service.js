var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __spreadArray = (this && this.__spreadArray) || function (to, from, pack) {
    if (pack || arguments.length === 2) for (var i = 0, l = from.length, ar; i < l; i++) {
        if (ar || !(i in from)) {
            if (!ar) ar = Array.prototype.slice.call(from, 0, i);
            ar[i] = from[i];
        }
    }
    return to.concat(ar || Array.prototype.slice.call(from));
};
import { either } from 'fp-ts';
import { pipe } from 'fp-ts/lib/function';
import { inject, injectable } from 'inversify';
import * as t from 'io-ts';
import reporter, { formatValidationErrors } from 'io-ts-reporters';
import isPlainObject from 'lodash/isPlainObject';
import { TYPES } from '../../ioc/ioc.types';
import { isAxiosResponse } from '../../utils/utils';
var ResponseService = /** @class */ (function () {
    function ResponseService() {
        var _this = this;
        this.getResponseData = function (response, typeName) {
            if (!isAxiosResponse(response)) {
                _this.logger.toConsole('warn', 'There is no data field in response object');
                return response;
            }
            if (typeName !== undefined) {
                _this.checkType(response.data, typeName).forEach(function (val) { return _this.handleTypeError(val, response.data); });
            }
            return response.data;
        };
        this.decodeResponseData = function (type) {
            if (type === void 0) { type = t.any; }
            return function (_a) {
                var data = _a.data;
                return pipe(type.decode(data), either.mapLeft(formatValidationErrors), either.fold(function (errors) {
                    errors.forEach(function (val) { return _this.handleTypeError(val, data); });
                    // в случае ошибки вернуть полученные данные как есть
                    // TODO: это должно быть фатальной ошибкой
                    return data;
                }, t.identity));
            };
        };
        this.getResponseStatus = function (response) {
            if ('status' in response) {
                return response.status;
            }
            _this.logger.toConsole('warn', 'There is no status field in response object');
            return response;
        };
        this.getResponseError = function (error) {
            var _a, _b, _c;
            if (isPlainObject(error) && 'response' in error) {
                return Promise.reject(Error("".concat((_a = error === null || error === void 0 ? void 0 : error.response) === null || _a === void 0 ? void 0 : _a.statusText, " ").concat((_b = error === null || error === void 0 ? void 0 : error.response) === null || _b === void 0 ? void 0 : _b.status) || "\u041D\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043D\u0430\u044F \u043E\u0448\u0438\u0431\u043A\u0430 ".concat((_c = error === null || error === void 0 ? void 0 : error.response) === null || _c === void 0 ? void 0 : _c.status)));
            }
            return Promise.reject(error);
        };
        this.checkType = function (value, type) {
            var report = [];
            if (Array.isArray(value)) {
                value.forEach(function (x) {
                    var validation = type.decode(x);
                    report = __spreadArray(__spreadArray([], report, true), reporter.report(validation), true);
                });
            }
            else {
                var validation = type.decode(value);
                report = reporter.report(validation);
            }
            return report;
        };
        this.handleTypeError = function (x, data) {
            _this.logger.toError(x, 'Type Error', data);
        };
    }
    ResponseService.prototype.processStatus = function (status, successMessage, failedMessage) {
        if (failedMessage === void 0) { failedMessage = 'Ошибка при выполнении запроса'; }
        var success = [200];
        var failure = [404, 403];
        if (success.includes(status)) {
            this.logger.toMessage('success', successMessage);
            return true;
        }
        if (failure.includes(status)) {
            this.logger.toMessage('error', failedMessage);
            return false;
        }
        return undefined;
    };
    __decorate([
        inject(TYPES.ILogger),
        __metadata("design:type", Object)
    ], ResponseService.prototype, "logger", void 0);
    ResponseService = __decorate([
        injectable()
    ], ResponseService);
    return ResponseService;
}());
export { ResponseService };
