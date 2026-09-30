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
var __rest = (this && this.__rest) || function (s, e) {
    var t = {};
    for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p) && e.indexOf(p) < 0)
        t[p] = s[p];
    if (s != null && typeof Object.getOwnPropertySymbols === "function")
        for (var i = 0, p = Object.getOwnPropertySymbols(s); i < p.length; i++) {
            if (e.indexOf(p[i]) < 0 && Object.prototype.propertyIsEnumerable.call(s, p[i]))
                t[p[i]] = s[p[i]];
        }
    return t;
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
import { action, computed, observable } from 'mobx';
import { EmployeeModel } from '../Employee/models/EmployeeModel';
import { SYSTEM_MESSAGES, TransportTypeEnum } from '../../constants/constants';
import { TYPES } from '../../ioc/ioc.types';
import { plainToNew } from '../../utils/utils';
import * as DelegatesInterface from './Delegates.interface';
var DIDelegatesStore = /** @class */ (function () {
    function DIDelegatesStore() {
        this.delegates = [];
        this.candidatesToDelegates = {};
        this.selfCandidatesToDelegates = [];
    }
    Object.defineProperty(DIDelegatesStore.prototype, "selfEmployee", {
        get: function () {
            return this.selfStore.selfEmployee;
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(DIDelegatesStore.prototype, "namesWithInitials", {
        get: function () {
            var getNameWithInitials = function (employee) { var _a; return "".concat(employee.firstName.charAt(0), ". ").concat((_a = employee.patronymic) === null || _a === void 0 ? void 0 : _a.charAt(0), ". ").concat(employee.lastName); };
            return this.delegates.reduce(function (names, delegate) {
                // eslint-disable-next-line no-param-reassign
                names[delegate.delegateId] = getNameWithInitials(delegate.delegateEmployee);
                // FIXME no-param-reassign
                return names;
            }, {});
        },
        enumerable: false,
        configurable: true
    });
    DIDelegatesStore.prototype.getDelegates = function () {
        var _a;
        return __awaiter(this, void 0, void 0, function () {
            var data;
            return __generator(this, function (_b) {
                switch (_b.label) {
                    case 0: return [4 /*yield*/, this.service.getDelegates({
                            orgId: this.selfEmployee.organizationId,
                            depId: this.selfEmployee.departmentId,
                            supId: this.selfEmployee.id,
                        })];
                    case 1:
                        data = _b.sent();
                        this.delegates = (_a = plainToNew(DelegatesInterface.DelegateModel, data)) !== null && _a !== void 0 ? _a : [];
                        return [2 /*return*/];
                }
            });
        });
    };
    DIDelegatesStore.prototype.getCandidatesToDelegates = function (transType, date) {
        var _a;
        return __awaiter(this, void 0, void 0, function () {
            var data;
            var _b;
            return __generator(this, function (_c) {
                switch (_c.label) {
                    case 0: return [4 /*yield*/, this.service.getCandidatesToDelegates({
                            orgId: this.selfEmployee.organizationId,
                            depId: this.selfEmployee.departmentId,
                            supId: this.selfEmployee.id,
                            transType: transType,
                            date: date,
                        })];
                    case 1:
                        data = _c.sent();
                        this.candidatesToDelegates = Object.assign(this.candidatesToDelegates, (_b = {},
                            _b[transType] = (_a = plainToNew(EmployeeModel, data)) !== null && _a !== void 0 ? _a : [],
                            _b));
                        return [2 /*return*/];
                }
            });
        });
    };
    DIDelegatesStore.prototype.getSelfCandidatesToDelegates = function (transportType, date) {
        var _a;
        return __awaiter(this, void 0, void 0, function () {
            var data;
            return __generator(this, function (_b) {
                switch (_b.label) {
                    case 0: return [4 /*yield*/, this.service.getSelfCandidatesToDelegates(transportType, date)];
                    case 1:
                        data = _b.sent();
                        this.selfCandidatesToDelegates = (_a = plainToNew(EmployeeModel, data)) !== null && _a !== void 0 ? _a : [];
                        return [2 /*return*/];
                }
            });
        });
    };
    DIDelegatesStore.prototype.addDelegate = function (delegate) {
        return __awaiter(this, void 0, void 0, function () {
            var data, result;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.service.addDelegate({ orgId: this.selfEmployee.organizationId, depId: this.selfEmployee.departmentId }, __assign({}, delegate))];
                    case 1:
                        data = _a.sent();
                        result = plainToNew(DelegatesInterface.DelegateModel, data);
                        if (result) {
                            this.delegates = __spreadArray(__spreadArray([], this.delegates, true), [result], false);
                            this.logger.toMessage('success', SYSTEM_MESSAGES.delegateSuccessfull);
                        }
                        return [2 /*return*/];
                }
            });
        });
    };
    DIDelegatesStore.prototype.deleteDelegate = function (delegateId) {
        return __awaiter(this, void 0, void 0, function () {
            var result;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.service.deleteDelegate({
                            orgId: this.selfEmployee.organizationId,
                            depId: this.selfEmployee.departmentId,
                            delegateId: delegateId,
                        })];
                    case 1:
                        result = _a.sent();
                        if (result) {
                            this.process.processStatus(result, SYSTEM_MESSAGES.delegateDeleteSuccess);
                            this.delegates = this.delegates.filter(function (x) { return x.id !== delegateId; });
                        }
                        return [2 /*return*/];
                }
            });
        });
    };
    DIDelegatesStore.prototype.initStore = function () {
        this.getDelegates();
        // TODO возможно ломает запросы
        // this.transportTypes.transportTypes.forEach(type => {
        //   const now = moment().format(DATE_FORMAT.BASE);
        //   this.getCandidatesToDelegates(type.id, now);
        // });
    };
    DIDelegatesStore.prototype.searchSelfDelegateCandidates = function (_a, cancelerSetter) {
        var _b;
        var transportType = _a.transportType, params = __rest(_a, ["transportType"]);
        return __awaiter(this, void 0, void 0, function () {
            var data;
            return __generator(this, function (_c) {
                switch (_c.label) {
                    case 0: return [4 /*yield*/, this.service.searchSelfDelegateCandidates(transportType, params, cancelerSetter)];
                    case 1:
                        data = _c.sent();
                        return [2 /*return*/, (_b = plainToNew(EmployeeModel, data)) !== null && _b !== void 0 ? _b : []];
                }
            });
        });
    };
    __decorate([
        inject(TYPES.IDelegatesService),
        __metadata("design:type", Object)
    ], DIDelegatesStore.prototype, "service", void 0);
    __decorate([
        inject(TYPES.ISelfEmployeeStore),
        __metadata("design:type", Object)
    ], DIDelegatesStore.prototype, "selfStore", void 0);
    __decorate([
        inject(TYPES.ILogger),
        __metadata("design:type", Object)
    ], DIDelegatesStore.prototype, "logger", void 0);
    __decorate([
        inject(TYPES.IResponseService),
        __metadata("design:type", Object)
    ], DIDelegatesStore.prototype, "process", void 0);
    __decorate([
        inject(TYPES.ITransportTypesStore),
        __metadata("design:type", Object)
    ], DIDelegatesStore.prototype, "transportTypes", void 0);
    __decorate([
        computed,
        __metadata("design:type", EmployeeModel),
        __metadata("design:paramtypes", [])
    ], DIDelegatesStore.prototype, "selfEmployee", null);
    __decorate([
        observable,
        __metadata("design:type", Array)
    ], DIDelegatesStore.prototype, "delegates", void 0);
    __decorate([
        observable,
        __metadata("design:type", Object)
    ], DIDelegatesStore.prototype, "candidatesToDelegates", void 0);
    __decorate([
        observable,
        __metadata("design:type", Array)
    ], DIDelegatesStore.prototype, "selfCandidatesToDelegates", void 0);
    __decorate([
        computed,
        __metadata("design:type", Object),
        __metadata("design:paramtypes", [])
    ], DIDelegatesStore.prototype, "namesWithInitials", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", []),
        __metadata("design:returntype", Promise)
    ], DIDelegatesStore.prototype, "getDelegates", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String, String]),
        __metadata("design:returntype", Promise)
    ], DIDelegatesStore.prototype, "getCandidatesToDelegates", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String, String]),
        __metadata("design:returntype", Promise)
    ], DIDelegatesStore.prototype, "getSelfCandidatesToDelegates", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [DelegatesInterface.DelegateModel]),
        __metadata("design:returntype", Promise)
    ], DIDelegatesStore.prototype, "addDelegate", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String]),
        __metadata("design:returntype", Promise)
    ], DIDelegatesStore.prototype, "deleteDelegate", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [Object, Function]),
        __metadata("design:returntype", Promise)
    ], DIDelegatesStore.prototype, "searchSelfDelegateCandidates", null);
    DIDelegatesStore = __decorate([
        injectable()
    ], DIDelegatesStore);
    return DIDelegatesStore;
}());
export { DIDelegatesStore };
