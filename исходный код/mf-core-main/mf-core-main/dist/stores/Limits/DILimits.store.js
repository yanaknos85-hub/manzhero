var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g;
    return g = { next: verb(0), "throw": verb(1), "return": verb(2) }, typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (g && (g = 0, op[0] && (_ = 0)), _) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
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
import { inject, injectable } from 'inversify';
import { mapKeys } from 'lodash';
import { action, computed, observable } from 'mobx';
import { EmployeeModel } from '../Employee/models/EmployeeModel';
import { SYSTEM_MESSAGES } from '../../constants/constants';
import { TYPES } from '../../ioc/ioc.types';
import { plainToNew } from '../../utils/utils';
import * as LimitInterface from './Limit.interface';
import { LimitEmpRequest, LimitSendRequest } from './LimitsRequest.interface';
import { LimitModel } from './Models/LimitModel';
var DILimitsStore = /** @class */ (function () {
    function DILimitsStore() {
        var _this = this;
        this.limitsIsLoaded = false;
        this.limitsIsFailed = false;
        this.currentDepartmentSharing = [];
        this.currentEmployeeSharing = [];
        this.limitsRequestsByAuthor = [];
        this.departmentLimits = [];
        this.employeeLimits = [];
        this.limitSharing = [];
        this.limitsRequestByApprover = [];
        this.activeLimitsRequestByApprover = undefined;
        this.oldLimitsRequestByApprover = undefined;
        this.allLimitRequests = [];
        this.depLimits = [];
        this.currentRequest = undefined;
        this.limitTransferHistory = [];
        this.limitCostHistory = [];
        this.getDepartmentLimitByYear = function (date) {
            if (date === void 0) { date = new Date(); }
            _this.currentLimit = _this.departmentLimits.filter(function (limit) { var _a; return ((_a = limit.department) === null || _a === void 0 ? void 0 : _a.id) === _this.selfEmployee.departmentId && limit.year === date.getFullYear(); });
            return _this.currentLimit;
        };
        this.getCurrentLimitYear = function () {
            var _a;
            if (_this.currentLimit) {
                return (_a = _this.currentLimit[0]) === null || _a === void 0 ? void 0 : _a.year;
            }
            return new Date().getFullYear();
        };
        this.getLimitsIdByDepartmentId = function (depId) {
            var currentYear = new Date().getFullYear();
            var foundDeps = _this.departmentLimits.find(function (limit) { var _a; return ((_a = limit.department) === null || _a === void 0 ? void 0 : _a.id) === depId && limit.year === currentYear; });
            return foundDeps === null || foundDeps === void 0 ? void 0 : foundDeps.id;
        };
    }
    Object.defineProperty(DILimitsStore.prototype, "selfEmployee", {
        get: function () {
            return this.selfStore.selfEmployee;
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(DILimitsStore.prototype, "listMapped", {
        get: function () {
            return mapKeys(this.limitsRequestsByAuthor, 'requestId');
        },
        enumerable: false,
        configurable: true
    });
    DILimitsStore.prototype.setCurrentRequest = function (id) {
        this.currentRequest = this.listMapped[id];
    };
    DILimitsStore.prototype.cancelLimitRequest = function (data) {
        return __awaiter(this, void 0, void 0, function () {
            var result, isCanceled;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.service.cancelLimitRequest(data)];
                    case 1:
                        result = _a.sent();
                        isCanceled = this.process.processStatus(result, SYSTEM_MESSAGES.limitRequestCancelSuccess);
                        if (!isCanceled) return [3 /*break*/, 3];
                        return [4 /*yield*/, this.getLimitsRequestsByAuthor()];
                    case 2:
                        _a.sent();
                        _a.label = 3;
                    case 3: return [2 /*return*/];
                }
            });
        });
    };
    DILimitsStore.prototype.getDepartmentLimitsByYear = function (departmentId, year) {
        var _a;
        return __awaiter(this, void 0, void 0, function () {
            var data;
            return __generator(this, function (_b) {
                switch (_b.label) {
                    case 0: return [4 /*yield*/, this.service.getDepartmentLimitsByYear(departmentId, year)];
                    case 1:
                        data = _b.sent();
                        this.departmentLimits = (_a = [plainToNew(LimitModel, data)]) !== null && _a !== void 0 ? _a : [];
                        return [2 /*return*/, this.departmentLimits];
                }
            });
        });
    };
    DILimitsStore.prototype.getDepartmentLimits = function () {
        var _a;
        return __awaiter(this, void 0, void 0, function () {
            var data;
            return __generator(this, function (_b) {
                switch (_b.label) {
                    case 0: return [4 /*yield*/, this.service.getDepartmentLimits()];
                    case 1:
                        data = _b.sent();
                        this.departmentLimits = (_a = plainToNew(LimitModel, data)) !== null && _a !== void 0 ? _a : [];
                        return [2 /*return*/, this.departmentLimits];
                }
            });
        });
    };
    DILimitsStore.prototype.getEmployeeLimits = function () {
        var _a;
        return __awaiter(this, void 0, void 0, function () {
            var data;
            return __generator(this, function (_b) {
                switch (_b.label) {
                    case 0: return [4 /*yield*/, this.service.getEmployeeLimits()];
                    case 1:
                        data = _b.sent();
                        this.employeeLimits = (_a = plainToNew(LimitModel, data)) !== null && _a !== void 0 ? _a : [];
                        return [2 /*return*/];
                }
            });
        });
    };
    DILimitsStore.prototype.getLimitSharing = function (limitId) {
        return __awaiter(this, void 0, void 0, function () {
            var data;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0:
                        data = [];
                        if (!limitId) return [3 /*break*/, 2];
                        return [4 /*yield*/, this.service.getLimitSharing(limitId)];
                    case 1:
                        data = _a.sent();
                        _a.label = 2;
                    case 2:
                        this.limitSharing = data;
                        return [2 /*return*/, data];
                }
            });
        });
    };
    DILimitsStore.prototype.setCurrentEmployeeSharing = function (limits) {
        this.currentEmployeeSharing = limits;
        return limits;
    };
    DILimitsStore.prototype.setCurrentDepartmentSharing = function () {
        return __awaiter(this, void 0, void 0, function () {
            var limits;
            var _this = this;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0:
                        limits = this.currentLimit;
                        if (!limits) return [3 /*break*/, 2];
                        return [4 /*yield*/, Promise.all(limits.map(function (limit) { return _this.getLimitSharing(limit.id); })).then(function (responses) {
                                _this.currentDepartmentSharing = [];
                                responses.forEach(function (response) {
                                    _this.currentDepartmentSharing = __spreadArray(__spreadArray([], _this.currentDepartmentSharing, true), response, true);
                                });
                            })];
                    case 1:
                        _a.sent();
                        _a.label = 2;
                    case 2: return [2 /*return*/, this.currentDepartmentSharing];
                }
            });
        });
    };
    DILimitsStore.prototype.getEmployeeLimit = function () {
        var _a;
        return __awaiter(this, void 0, void 0, function () {
            var data;
            return __generator(this, function (_b) {
                switch (_b.label) {
                    case 0: return [4 /*yield*/, this.service.getEmployeeLimit((_a = this.selfEmployee) === null || _a === void 0 ? void 0 : _a.id, this.getCurrentLimitYear())];
                    case 1:
                        data = _b.sent();
                        this.employeeLimit = data;
                        return [2 /*return*/, data];
                }
            });
        });
    };
    DILimitsStore.prototype.getLimitsRequestsByAuthor = function () {
        return __awaiter(this, void 0, void 0, function () {
            var data;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.service.getLimitsRequestsByAuthor()];
                    case 1:
                        data = _a.sent();
                        this.limitsRequestsByAuthor = data;
                        return [2 /*return*/, data];
                }
            });
        });
    };
    DILimitsStore.prototype.getLimitsRequestByApprover = function () {
        return __awaiter(this, void 0, void 0, function () {
            var data;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.service.getLimitsRequestByApprover()];
                    case 1:
                        data = _a.sent();
                        this.limitsRequestByApprover = data;
                        return [2 /*return*/, data];
                }
            });
        });
    };
    DILimitsStore.prototype.getActiveLimitsRequestByApprover = function (params) {
        return __awaiter(this, void 0, void 0, function () {
            var data;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.service.getActiveLimitsRequestByApprover(params)];
                    case 1:
                        data = _a.sent();
                        this.activeLimitsRequestByApprover = data;
                        return [2 /*return*/, data];
                }
            });
        });
    };
    DILimitsStore.prototype.getOldLimitsRequestByApprover = function (params) {
        return __awaiter(this, void 0, void 0, function () {
            var data;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.service.getOldLimitsRequestByApprover(params)];
                    case 1:
                        data = _a.sent();
                        this.oldLimitsRequestByApprover = data;
                        return [2 /*return*/, data];
                }
            });
        });
    };
    DILimitsStore.prototype.getAllLimitRequests = function () {
        return __awaiter(this, void 0, void 0, function () {
            var data;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.service.getAllLimitRequests()];
                    case 1:
                        data = _a.sent();
                        this.allLimitRequests = data;
                        return [2 /*return*/, data];
                }
            });
        });
    };
    DILimitsStore.prototype.getLimitByDepartment = function (departmentId) {
        return __awaiter(this, void 0, void 0, function () {
            var data;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.service.getLimitByDepartment(departmentId)];
                    case 1:
                        data = _a.sent();
                        this.depLimits = data;
                        this.departmentLimits = data;
                        return [2 /*return*/, data];
                }
            });
        });
    };
    DILimitsStore.prototype.getLimitTransferHistory = function (limitId, year, maxRecords) {
        return __awaiter(this, void 0, void 0, function () {
            var data;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.service.getLimitTransferHistory(limitId, year, maxRecords)];
                    case 1:
                        data = _a.sent();
                        this.limitTransferHistory = data;
                        return [2 /*return*/, data];
                }
            });
        });
    };
    DILimitsStore.prototype.getLimitCostHistory = function (limitId, maxRecords, orgId) {
        return __awaiter(this, void 0, void 0, function () {
            var data;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.service.getLimitCostHistory(limitId, maxRecords, orgId)];
                    case 1:
                        data = _a.sent();
                        this.limitCostHistory = data;
                        return [2 /*return*/, data];
                }
            });
        });
    };
    DILimitsStore.prototype.approveLimitRequest = function (data) {
        return __awaiter(this, void 0, void 0, function () {
            var res, result, isApproved, isCanceled;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0:
                        res = 0;
                        return [4 /*yield*/, this.service.approveLimitRequest(data).then(function (status) { return (res = status); })];
                    case 1:
                        result = _a.sent();
                        isApproved = data.approvalState === 'APPROVED';
                        isCanceled = isApproved
                            ? this.process.processStatus(result, SYSTEM_MESSAGES.limitIsApproved)
                            : this.process.processStatus(result, SYSTEM_MESSAGES.limitRequestCancelSuccess);
                        if (!isCanceled) return [3 /*break*/, 4];
                        return [4 /*yield*/, this.getLimitsRequestByApprover()];
                    case 2:
                        _a.sent();
                        return [4 /*yield*/, this.getAllLimitRequests()];
                    case 3:
                        _a.sent();
                        _a.label = 4;
                    case 4: return [2 /*return*/, res];
                }
            });
        });
    };
    DILimitsStore.prototype.changeDepLimitRequest = function (data, requestId) {
        return __awaiter(this, void 0, void 0, function () {
            return __generator(this, function (_a) {
                return [2 /*return*/, this.service.changeDepLimitRequest(data, requestId)];
            });
        });
    };
    DILimitsStore.prototype.changeEmpLimitRequest = function (data, requestId) {
        return __awaiter(this, void 0, void 0, function () {
            return __generator(this, function (_a) {
                return [2 /*return*/, this.service.changeEmpLimitRequest(data, requestId)];
            });
        });
    };
    DILimitsStore.prototype.getAccountBonuses = function () {
        return __awaiter(this, void 0, void 0, function () {
            var data;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.service.getAccountBonuses(this.selfEmployee.id)];
                    case 1:
                        data = _a.sent();
                        this.bonuses = data;
                        return [2 /*return*/, data];
                }
            });
        });
    };
    DILimitsStore.prototype.refreshLimits = function () {
        this.initStore();
    };
    DILimitsStore.prototype.getLimits = function () {
        var _this = this;
        this.limitsIsLoaded = false;
        this.limitsIsFailed = false;
        // Запрашиваем лимиты подзраделений /limits/deplimits/
        // this.getDepartmentLimitsByYear(this.selfStore.depId as string, new Date().getFullYear().toString())
        this.getLimitByDepartment(this.selfStore.depId)
            // .then(response => {
            //   console.log('лимиты подразделений', toJS(response))
            // })
            // Отфильтровываем лимиты подзраделений по departmentId пользователя и году
            // устанавливаем this.currentLimit (далее ПОЛЬЗОВАТЕЛЬСКИЕ ЛИМИТЫ ПОДРАЗДЕЛЕНИЙ)
            .then(function () { return _this.getDepartmentLimitByYear(); })
            // .then(response => {
            //   console.log(`departmentId пользователя ${this.selfEmployee.departmentId}`);
            //   console.log('ПОЛЬЗОВАТЕЛЬСКИЕ ЛИМИТЫ ПОДРАЗДЕЛЕНИЙ', toJS(response));
            //   return response;
            // })
            // Если лимитов у пользователя нет, то и дальше нем смысла что-то запрашивать
            .then(function (response) {
            if (!response || !response.length) {
                throw ({ name: LimitInterface.Errors.LIMIT_NOT_FOUND, message: 'пользовательские лимиты не найдены' });
            }
            return response;
        })
            // Получаем лимит пользователя /limits/emplimits/getByEmployeeAndYear/${employeeId}/year/${year}
            .then(function () { return _this.getEmployeeLimit(); }) // Тут приходит ничего от сервера!
            // .then(response => {
            //   console.log('лимит пользователя', toJS(response ||  ': - не найден / нет ответа от сервера -'))
            //   return response;
            // })
            // Получаем шэринг лимитов на основе лимита пользователя если у него есть личный лимит /limits/limitsharing/getByLimit/full/${limitId}
            .then(function (employeeLimit) { return _this.getLimitSharing(employeeLimit.id); })
            // .then(response => {
            //   console.log('шэринг лимитов', toJS(response))
            //   return response;
            // })
            // устанавливаем ЛИЧНЫЙ ЛИМИТ (this.currentEmployeeSharing)
            // вывыодим ЛИЧНЫЙ ЛИМИТ если он есть или показываем кнопку "Запросить лимит" (на странице LimitPage в прогресс баре)
            .then(function (response) { return _this.setCurrentEmployeeSharing(response); })
            // .then(response => {
            //   console.log('ЛИЧНЫЙ ЛИМИТ', toJS(response));
            //   return response;
            // })
            // Делаем кучу запросов на основе ПОЛЬЗОВАТЕЛЬСКИХ ЛИМИТОВ ПОДРАЗДЕЛЕНИЙ: currentLimit.map(limitId -> /limits/limitsharing/getByLimit/full/${limitId})
            // Конкатинируем
            // устанавливаем ЛИМИТ ПОДРАЗДЕЛЕНИЯ (this.currentDepartmentSharing)
            // вывыодим ЛИМИТ ПОДРАЗДЕЛЕНИЯ если он есть или показываем кнопку "Запросить лимит" (на странице LimitPage в прогресс баре)
            .then(function () { return _this.setCurrentDepartmentSharing(); }) // устанавливаем ЛИМИТ ПОДРАЗДЕЛЕНИЯ
            // .then(response => {
            //   console.log('ЛИМИТ ПОДРАЗДЕЛЕНИЯ', toJS(response));
            //   return response;
            // })
            .then(function () {
            _this.limitsIsLoaded = true;
        })
            .catch(function (err) {
            if ((err === null || err === void 0 ? void 0 : err.name) === LimitInterface.Errors.LIMIT_NOT_FOUND) {
                _this.limitsIsLoaded = true;
                // eslint-disable-next-line no-console
                console.log(err.message);
            }
            else {
                _this.limitsIsFailed = true;
                // eslint-disable-next-line no-console
                console.log('#### FAILED! ####\r\n', err);
            }
        });
    };
    DILimitsStore.prototype.initStore = function () {
        this.getLimits();
    };
    __decorate([
        inject(TYPES.ILimitsServiceNew),
        __metadata("design:type", Object)
    ], DILimitsStore.prototype, "service", void 0);
    __decorate([
        inject(TYPES.ISelfEmployeeStore),
        __metadata("design:type", Object)
    ], DILimitsStore.prototype, "selfStore", void 0);
    __decorate([
        inject(TYPES.IResponseService),
        __metadata("design:type", Object)
    ], DILimitsStore.prototype, "process", void 0);
    __decorate([
        computed,
        __metadata("design:type", EmployeeModel),
        __metadata("design:paramtypes", [])
    ], DILimitsStore.prototype, "selfEmployee", null);
    __decorate([
        observable,
        __metadata("design:type", Object)
    ], DILimitsStore.prototype, "limitsIsLoaded", void 0);
    __decorate([
        observable,
        __metadata("design:type", Object)
    ], DILimitsStore.prototype, "limitsIsFailed", void 0);
    __decorate([
        observable,
        __metadata("design:type", Array)
    ], DILimitsStore.prototype, "currentDepartmentSharing", void 0);
    __decorate([
        observable,
        __metadata("design:type", Array)
    ], DILimitsStore.prototype, "currentEmployeeSharing", void 0);
    __decorate([
        observable,
        __metadata("design:type", Object)
    ], DILimitsStore.prototype, "currentLimit", void 0);
    __decorate([
        observable,
        __metadata("design:type", Object)
    ], DILimitsStore.prototype, "employeeLimit", void 0);
    __decorate([
        observable,
        __metadata("design:type", Array)
    ], DILimitsStore.prototype, "limitsRequestsByAuthor", void 0);
    __decorate([
        observable,
        __metadata("design:type", Array)
    ], DILimitsStore.prototype, "departmentLimits", void 0);
    __decorate([
        observable,
        __metadata("design:type", Array)
    ], DILimitsStore.prototype, "employeeLimits", void 0);
    __decorate([
        observable,
        __metadata("design:type", Array)
    ], DILimitsStore.prototype, "limitSharing", void 0);
    __decorate([
        observable,
        __metadata("design:type", Array)
    ], DILimitsStore.prototype, "limitsRequestByApprover", void 0);
    __decorate([
        observable,
        __metadata("design:type", Object)
    ], DILimitsStore.prototype, "activeLimitsRequestByApprover", void 0);
    __decorate([
        observable,
        __metadata("design:type", Object)
    ], DILimitsStore.prototype, "oldLimitsRequestByApprover", void 0);
    __decorate([
        observable,
        __metadata("design:type", Array)
    ], DILimitsStore.prototype, "allLimitRequests", void 0);
    __decorate([
        observable,
        __metadata("design:type", Array)
    ], DILimitsStore.prototype, "depLimits", void 0);
    __decorate([
        observable,
        __metadata("design:type", Object)
    ], DILimitsStore.prototype, "currentRequest", void 0);
    __decorate([
        observable,
        __metadata("design:type", Array)
    ], DILimitsStore.prototype, "limitTransferHistory", void 0);
    __decorate([
        observable,
        __metadata("design:type", Array)
    ], DILimitsStore.prototype, "limitCostHistory", void 0);
    __decorate([
        observable,
        __metadata("design:type", Object)
    ], DILimitsStore.prototype, "bonuses", void 0);
    __decorate([
        computed,
        __metadata("design:type", Object),
        __metadata("design:paramtypes", [])
    ], DILimitsStore.prototype, "listMapped", null);
    __decorate([
        action,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String]),
        __metadata("design:returntype", void 0)
    ], DILimitsStore.prototype, "setCurrentRequest", null);
    __decorate([
        action,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [Object]),
        __metadata("design:returntype", Promise)
    ], DILimitsStore.prototype, "cancelLimitRequest", null);
    __decorate([
        action,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String, String]),
        __metadata("design:returntype", Promise)
    ], DILimitsStore.prototype, "getDepartmentLimitsByYear", null);
    __decorate([
        action,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", []),
        __metadata("design:returntype", Promise)
    ], DILimitsStore.prototype, "getDepartmentLimits", null);
    __decorate([
        action,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", []),
        __metadata("design:returntype", Promise)
    ], DILimitsStore.prototype, "getEmployeeLimits", null);
    __decorate([
        action,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String]),
        __metadata("design:returntype", Promise)
    ], DILimitsStore.prototype, "getLimitSharing", null);
    __decorate([
        action,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [Array]),
        __metadata("design:returntype", Array)
    ], DILimitsStore.prototype, "setCurrentEmployeeSharing", null);
    __decorate([
        action,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", []),
        __metadata("design:returntype", Promise)
    ], DILimitsStore.prototype, "setCurrentDepartmentSharing", null);
    __decorate([
        action,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", []),
        __metadata("design:returntype", Promise)
    ], DILimitsStore.prototype, "getEmployeeLimit", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", []),
        __metadata("design:returntype", Promise)
    ], DILimitsStore.prototype, "getLimitsRequestsByAuthor", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", []),
        __metadata("design:returntype", Promise)
    ], DILimitsStore.prototype, "getLimitsRequestByApprover", null);
    __decorate([
        action.bound
        // eslint-disable-next-line @stylistic/max-len
        ,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [Object]),
        __metadata("design:returntype", Promise)
    ], DILimitsStore.prototype, "getActiveLimitsRequestByApprover", null);
    __decorate([
        action.bound
        // eslint-disable-next-line @stylistic/max-len
        ,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [Object]),
        __metadata("design:returntype", Promise)
    ], DILimitsStore.prototype, "getOldLimitsRequestByApprover", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", []),
        __metadata("design:returntype", Promise)
    ], DILimitsStore.prototype, "getAllLimitRequests", null);
    __decorate([
        action,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String]),
        __metadata("design:returntype", Promise)
    ], DILimitsStore.prototype, "getLimitByDepartment", null);
    __decorate([
        action,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String, Number, Number]),
        __metadata("design:returntype", Promise)
    ], DILimitsStore.prototype, "getLimitTransferHistory", null);
    __decorate([
        action,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String, Number, String]),
        __metadata("design:returntype", Promise)
    ], DILimitsStore.prototype, "getLimitCostHistory", null);
    __decorate([
        action,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [Object]),
        __metadata("design:returntype", Promise)
    ], DILimitsStore.prototype, "approveLimitRequest", null);
    __decorate([
        action,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [Object, String]),
        __metadata("design:returntype", Promise)
    ], DILimitsStore.prototype, "changeDepLimitRequest", null);
    __decorate([
        action,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [Object, String]),
        __metadata("design:returntype", Promise)
    ], DILimitsStore.prototype, "changeEmpLimitRequest", null);
    __decorate([
        action,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", []),
        __metadata("design:returntype", Promise)
    ], DILimitsStore.prototype, "getAccountBonuses", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", []),
        __metadata("design:returntype", void 0)
    ], DILimitsStore.prototype, "refreshLimits", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", []),
        __metadata("design:returntype", void 0)
    ], DILimitsStore.prototype, "getLimits", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", []),
        __metadata("design:returntype", void 0)
    ], DILimitsStore.prototype, "initStore", null);
    DILimitsStore = __decorate([
        injectable()
    ], DILimitsStore);
    return DILimitsStore;
}());
export { DILimitsStore };
