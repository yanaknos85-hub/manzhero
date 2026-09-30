var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __param = (this && this.__param) || function (paramIndex, decorator) {
    return function (target, key) { decorator(target, key, paramIndex); }
};
import axios from 'axios';
import { inject, injectable } from 'inversify';
import { stringify } from 'qs';
import { routes, API_URL, SYSTEM_MESSAGES, X_CLIENT_TYPE, errorText, CustomErrorCode } from '../../constants/constants';
import { TYPES } from '../../ioc/ioc.types';
import { Token } from '../../utils/token';
import { isAxiosError } from '../../utils/utils';
import { getErrorCode } from '../../utils/getErrorCode';
var HttpMiddleware = /** @class */ (function () {
    // @inject(TYPES.IConfigStore)
    // private configStore!: IConfigStore;
    // @inject(TYPES.ILogger)
    // private _logger!: ILogger;
    // @inject(TYPES.INavigator)
    // private _navigator!: INavigator;
    // @inject(TYPES.Token)
    // private _token!: Token;
    function HttpMiddleware(configStore, _logger, _navigator, _token) {
        var _this = this;
        this.configStore = configStore;
        this._logger = _logger;
        this._navigator = _navigator;
        this._token = _token;
        this._promise = Promise;
        this.client = axios.create({
            baseURL: API_URL, timeout: 10000, headers: { 'x-client-type': X_CLIENT_TYPE },
        });
        var errorMiddleware = function (error) {
            var _a, _b, _c, _d, _e, _f, _g, _h, _j, _k, _l, _m, _o, _p, _q, _r, _s, _t, _u, _v, _w, _x, _y, _z, _0, _1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15, _16, _17, _18, _19, _20, _21;
            // Логика обработки ошибок при авторизации находится в authStore и компонентах, поэтому тут пропускаем
            if (window.location.pathname === '/oauth') {
                return _this._promise.reject(error);
            }
            if (_this.configStore.isCorp) {
                var code = getErrorCode(error);
                // Если выелезла ошибка авторизации, когда мы находились внутри приложения
                if (code === 401) {
                    history.push('/');
                    _this._logger.toMessage('error', (_a = errorText[code]) === null || _a === void 0 ? void 0 : _a.title);
                }
                var isLogToConsole = 'isAxiosError' in error && !((_c = (_b = error.config) === null || _b === void 0 ? void 0 : _b.hush) === null || _c === void 0 ? void 0 : _c.includes(code));
                if (isLogToConsole) {
                    // Вывод ошибки в консоль в старом виде
                    var data = (_d = error.response) === null || _d === void 0 ? void 0 : _d.data;
                    var title = "".concat((data === null || data === void 0 ? void 0 : data.status) || error.name || 'Server Error', ": ").concat((data === null || data === void 0 ? void 0 : data.reason) || (data === null || data === void 0 ? void 0 : data.error) || error.message);
                    var description = ((data === null || data === void 0 ? void 0 : data.reason) ? data === null || data === void 0 ? void 0 : data.error : data === null || data === void 0 ? void 0 : data.message) || error.message;
                    _this._logger.toConsoleGroup('error', description, title, __assign({}, error));
                }
                // Выводим уведомление об ошибке только для post, put, patch, delete, т.к. get 100% будет отловлен errorBoundary
                // и нет смысла дублировать ошибку в уведомлении
                var methodsForNotifications = ['POST', 'post', 'PUT', 'put', 'PATCH', 'patch', 'DELETE', 'delete'];
                var notifyOnError = isLogToConsole && ((_e = error.config) === null || _e === void 0 ? void 0 : _e.notifyOnError) && methodsForNotifications.includes((_g = (_f = error.config) === null || _f === void 0 ? void 0 : _f.method) !== null && _g !== void 0 ? _g : '');
                // Оставлены старые условия, желательно пересмотреть
                if ('isAxiosError' in error) {
                    if (window.location.pathname === '/directories/departments/adding' && ((_h = error.response) === null || _h === void 0 ? void 0 : _h.status) === 409) {
                        notifyOnError = false;
                    }
                    else if (window.location.pathname === '/rules/cargoTypes/adding' && ((_j = error.response) === null || _j === void 0 ? void 0 : _j.status) === 409) {
                        notifyOnError = false;
                    }
                    else if (window.location.pathname === '/directories/employees/create-employee'
                        && ((_k = error.response) === null || _k === void 0 ? void 0 : _k.status) === 409) {
                        notifyOnError = false;
                    }
                    else if (window.location.pathname === '/settings/notifications' && ((_l = error.response) === null || _l === void 0 ? void 0 : _l.status) === 409) {
                        notifyOnError = false;
                    }
                    else if (window.location.pathname.includes('/reports/taxiRegistry/') && ((_m = error.response) === null || _m === void 0 ? void 0 : _m.status) === 404) {
                        notifyOnError = false;
                    }
                    else if (window.location.pathname.includes('/directories/departments/') && ((_o = error.response) === null || _o === void 0 ? void 0 : _o.status) === 409) {
                        notifyOnError = false;
                    }
                    else if (((_p = error.response) === null || _p === void 0 ? void 0 : _p.status) === 412) {
                        notifyOnError = false;
                    }
                    else if (window.location.pathname.includes('/multi-logistics') && ((_q = error.response) === null || _q === void 0 ? void 0 : _q.status) === 409) {
                        notifyOnError = false;
                    }
                    else if (((_t = (_s = (_r = error.response) === null || _r === void 0 ? void 0 : _r.config) === null || _s === void 0 ? void 0 : _s.url) === null || _t === void 0 ? void 0 : _t.includes('/reports/files/trip-requests-')) && ((_u = error.response) === null || _u === void 0 ? void 0 : _u.status) === 409) {
                        notifyOnError = false;
                    }
                    else if (window.location.pathname.includes('fleet-management/telemechanic/') && ((_v = error.response) === null || _v === void 0 ? void 0 : _v.status) === 500) {
                        notifyOnError = false;
                    }
                    else if (window.location.pathname.includes('analytics') && ((_y = (_x = (_w = error.response) === null || _w === void 0 ? void 0 : _w.config) === null || _x === void 0 ? void 0 : _x.url) === null || _y === void 0 ? void 0 : _y.includes('/user-agent/'))) {
                        // По просьбе ВП не выводить ошибку в отчетности
                        notifyOnError = false;
                    }
                }
                if (notifyOnError) {
                    _this._logger.toNotify('error', (_0 = (_z = errorText[code]) === null || _z === void 0 ? void 0 : _z.subtitle) !== null && _0 !== void 0 ? _0 : (_1 = errorText[CustomErrorCode.UNKNOWN]) === null || _1 === void 0 ? void 0 : _1.subtitle, (_3 = (_2 = errorText[code]) === null || _2 === void 0 ? void 0 : _2.title) !== null && _3 !== void 0 ? _3 : (_4 = errorText[CustomErrorCode.UNKNOWN]) === null || _4 === void 0 ? void 0 : _4.title, 5, code);
                }
            }
            else {
                if (isAxiosError(error) && error.isAxiosError === true) {
                    var reAuth = function () { return new Promise(function () { return _this._navigator.navSudirReAuth({
                        reAuth: routes.SudirApi,
                        fail: routes.Failure,
                    }); }); };
                    var defaultErr = function () {
                        var _a;
                        _this._navigator.navRoot();
                        // @ts-ignore
                        _this._logger.toMessage('error', (_a = error.response) === null || _a === void 0 ? void 0 : _a.statusText);
                    };
                    if (((_5 = error.response) === null || _5 === void 0 ? void 0 : _5.status) === 408 || error.code === 'ECONNABORTED') {
                        // eslint-disable-next-line no-console
                        console.error((_6 = error.config.url) !== null && _6 !== void 0 ? _6 : '', 'Timeout exceeded');
                        // this.logger.toError(error.config.url ?? '', 'Timeout exceeded');
                    }
                    else if (((_7 = error.response) === null || _7 === void 0 ? void 0 : _7.status) === 401) {
                        // не проверять авторизацию по этому урлу
                        if (/\/print\/ttn/.test(error.config.url || '')) {
                            return _this._promise.reject(error);
                        }
                        if (_this.configStore.isMockedAuth) {
                            return _this._promise.reject(error);
                        }
                        if (!_this.configStore.isBasicAuth) {
                            return reAuth();
                        }
                        defaultErr();
                    }
                    else if (((_8 = error.response) === null || _8 === void 0 ? void 0 : _8.status) === 504) {
                        if (!_this.configStore.isBasicAuth) {
                            return reAuth();
                        }
                        defaultErr();
                    }
                    else if (((_9 = error.response) === null || _9 === void 0 ? void 0 : _9.status) === 409
                        && ((_10 = error.response) === null || _10 === void 0 ? void 0 : _10.data.message) === 'The address by position 1 duplicates the previous one' /// что это такое ?!
                    ) {
                        _this._logger.toMessage('error', SYSTEM_MESSAGES.savingWithAddressDuplicates);
                    }
                    else if (((_11 = error.response) === null || _11 === void 0 ? void 0 : _11.status) === 404 && error.config.url === '/requests/public/compensation') {
                        if ((_12 = error.response) === null || _12 === void 0 ? void 0 : _12.data.message.includes('LIMIT_NOT_FOUND: Резервирование:')) {
                            /// что это такое2 ?!
                            _this._logger.toMessage('error', SYSTEM_MESSAGES.savingWithWrongSum);
                        }
                    }
                    else if (((_13 = error.response) === null || _13 === void 0 ? void 0 : _13.status) === 417 && error.config.url === '/geo/address') {
                        // eslint-disable-next-line no-console
                        console.info("[".concat((_14 = error.config.url) !== null && _14 !== void 0 ? _14 : '', "] address not found"));
                    }
                    else {
                        // this.logger.toError(description, title, { ...error });
                        /// что это такое? что это за эррор? нужно ли нам такое в таком виде?
                        // eslint-disable-next-line no-console
                        console.error("\n              [".concat((_15 = error.config.url) !== null && _15 !== void 0 ? _15 : '', "]\n              ").concat(((_16 = error.response) === null || _16 === void 0 ? void 0 : _16.data.status) || error.name || 'Server Error', ": ").concat(((_17 = error.response) === null || _17 === void 0 ? void 0 : _17.data.reason) || ((_18 = error.response) === null || _18 === void 0 ? void 0 : _18.data.error) || error.message), {
                            description: (((_19 = error.response) === null || _19 === void 0 ? void 0 : _19.data.reason) ? (_20 = error.response) === null || _20 === void 0 ? void 0 : _20.data.error : (_21 = error.response) === null || _21 === void 0 ? void 0 : _21.data.message)
                                || error.message,
                            error: __assign({}, error),
                        });
                    }
                }
                else {
                    // this.logger.toNotify('error', error.message, error.name);
                    // eslint-disable-next-line no-console
                    console.error(error);
                }
            }
            return _this._promise.reject(error);
        };
        this.client.interceptors.response.use(this.responseMiddlware.bind(this), errorMiddleware);
    }
    HttpMiddleware.prototype.changeClientType = function (clientType) {
        this.client.defaults.headers['x-client-type'] = clientType;
    };
    HttpMiddleware.prototype.setParamsSerializer = function (options) {
        this.client.defaults.paramsSerializer = function (params) { return stringify(params, options); };
    };
    // eslint-disable-next-line @stylistic/max-len
    HttpMiddleware.prototype.setRequestInterceptor = function (middleware) {
        this.client.interceptors.request.use(middleware);
    };
    HttpMiddleware.prototype.responseMiddlware = function (response) {
        return this._promise.resolve(response);
    };
    HttpMiddleware = __decorate([
        injectable(),
        __param(0, inject(TYPES.IConfigStore)),
        __param(1, inject(TYPES.ILogger)),
        __param(2, inject(TYPES.INavigator)),
        __param(3, inject(TYPES.Token)),
        __metadata("design:paramtypes", [Object, Object, Object, Token])
    ], HttpMiddleware);
    return HttpMiddleware;
}());
export { HttpMiddleware };
