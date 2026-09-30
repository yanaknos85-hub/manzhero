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
import { inject, injectable } from 'inversify';
import lodash from 'lodash';
import { action, computed, observable } from 'mobx';
import { SYSTEM_MESSAGES } from '../../constants/constants';
import { TYPES } from '../../ioc/ioc.types';
import { plainToNew } from '../../utils/utils';
import * as LimitsRequestInterface from './LimitsRequest.interface';
import { LimitRequestModel } from './Models/LimitRequest.model';
var DILimitsRequestStore = /** @class */ (function () {
    function DILimitsRequestStore() {
        var _this = this;
        this.list = [];
        this.siblings = [];
        this.currentRequest = undefined;
        this.getList = function () { return __awaiter(_this, void 0, void 0, function () {
            var result;
            var _a;
            return __generator(this, function (_b) {
                switch (_b.label) {
                    case 0: return [4 /*yield*/, this.service.getLimitRequestList()];
                    case 1:
                        result = _b.sent();
                        this.list = (_a = plainToNew(LimitRequestModel, result)) !== null && _a !== void 0 ? _a : [];
                        return [2 /*return*/];
                }
            });
        }); };
        this.getDepSiblings = function (data) { return __awaiter(_this, void 0, void 0, function () {
            var result;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.service.getDepSiblings(LimitsRequestInterface.SiblingsParams.encode(data))];
                    case 1:
                        result = _a.sent();
                        this.siblings = result;
                        return [2 /*return*/, result];
                }
            });
        }); };
        this.addLimitRequest = function (data) { return __awaiter(_this, void 0, void 0, function () { return __generator(this, function (_a) {
            switch (_a.label) {
                case 0: return [4 /*yield*/, this.service.addLimitRequest(data)];
                case 1: return [2 /*return*/, _a.sent()];
            }
        }); }); };
        this.addEmpLimitRequest = function (data) { return __awaiter(_this, void 0, void 0, function () { return __generator(this, function (_a) {
            switch (_a.label) {
                case 0: return [4 /*yield*/, this.service.addEmpLimitRequest(data)];
                case 1: return [2 /*return*/, _a.sent()];
            }
        }); }); };
        this.getRequest = function (reqId) { return __awaiter(_this, void 0, void 0, function () {
            var result;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.service.getLimitRequest(reqId)];
                    case 1:
                        result = _a.sent();
                        return [2 /*return*/, new LimitRequestModel(result)];
                }
            });
        }); };
    }
    Object.defineProperty(DILimitsRequestStore.prototype, "listMapped", {
        get: function () {
            return lodash.mapKeys(this.list, 'id');
        },
        enumerable: false,
        configurable: true
    });
    DILimitsRequestStore.prototype.createRequest = function (data) {
        this.service.addLimitRequest(data);
    };
    DILimitsRequestStore.prototype.editLimitRequest = function (reqId, data) {
        return __awaiter(this, void 0, void 0, function () {
            var result, isEdited, _a;
            return __generator(this, function (_b) {
                switch (_b.label) {
                    case 0:
                        this.currentRequest = undefined;
                        return [4 /*yield*/, this.service.editLimitRequest(reqId, data)];
                    case 1:
                        result = _b.sent();
                        isEdited = this.process.processStatus(result, SYSTEM_MESSAGES.limitRequestEditSuccess);
                        if (!isEdited) return [3 /*break*/, 3];
                        _a = this;
                        return [4 /*yield*/, this.getRequest(reqId)];
                    case 2:
                        _a.currentRequest = _b.sent();
                        this.getList();
                        _b.label = 3;
                    case 3: return [2 /*return*/];
                }
            });
        });
    };
    DILimitsRequestStore.prototype.setCurrentRequest = function (id) {
        this.currentRequest = this.listMapped[id];
    };
    DILimitsRequestStore.prototype.cancelRequest = function (id, reason) {
        return __awaiter(this, void 0, void 0, function () {
            var result, isCanceled;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.service.cancelLimitRequest(id, reason)];
                    case 1:
                        result = _a.sent();
                        isCanceled = this.process.processStatus(result, SYSTEM_MESSAGES.limitRequestCancelSuccess);
                        if (isCanceled) {
                            this.getList();
                        }
                        return [2 /*return*/];
                }
            });
        });
    };
    DILimitsRequestStore.prototype.initStore = function () {
        this.getList();
    };
    __decorate([
        inject(TYPES.ILimitsRequestService),
        __metadata("design:type", Object)
    ], DILimitsRequestStore.prototype, "service", void 0);
    __decorate([
        inject(TYPES.IResponseService),
        __metadata("design:type", Object)
    ], DILimitsRequestStore.prototype, "process", void 0);
    __decorate([
        observable,
        __metadata("design:type", Array)
    ], DILimitsRequestStore.prototype, "list", void 0);
    __decorate([
        observable,
        __metadata("design:type", Array)
    ], DILimitsRequestStore.prototype, "siblings", void 0);
    __decorate([
        computed,
        __metadata("design:type", Object),
        __metadata("design:paramtypes", [])
    ], DILimitsRequestStore.prototype, "listMapped", null);
    __decorate([
        observable,
        __metadata("design:type", Object)
    ], DILimitsRequestStore.prototype, "currentRequest", void 0);
    __decorate([
        action,
        __metadata("design:type", Object)
    ], DILimitsRequestStore.prototype, "getList", void 0);
    __decorate([
        action,
        __metadata("design:type", Object)
    ], DILimitsRequestStore.prototype, "getDepSiblings", void 0);
    __decorate([
        action,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [Object]),
        __metadata("design:returntype", void 0)
    ], DILimitsRequestStore.prototype, "createRequest", null);
    __decorate([
        action
        // eslint-disable-next-line no-return-await, @stylistic/max-len
        ,
        __metadata("design:type", Object)
    ], DILimitsRequestStore.prototype, "addLimitRequest", void 0);
    __decorate([
        action
        // eslint-disable-next-line no-return-await, @stylistic/max-len
        ,
        __metadata("design:type", Object)
    ], DILimitsRequestStore.prototype, "addEmpLimitRequest", void 0);
    __decorate([
        action,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String, Object]),
        __metadata("design:returntype", Promise)
    ], DILimitsRequestStore.prototype, "editLimitRequest", null);
    __decorate([
        action,
        __metadata("design:type", Object)
    ], DILimitsRequestStore.prototype, "getRequest", void 0);
    __decorate([
        action,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String]),
        __metadata("design:returntype", void 0)
    ], DILimitsRequestStore.prototype, "setCurrentRequest", null);
    __decorate([
        action,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String, String]),
        __metadata("design:returntype", Promise)
    ], DILimitsRequestStore.prototype, "cancelRequest", null);
    DILimitsRequestStore = __decorate([
        injectable()
    ], DILimitsRequestStore);
    return DILimitsRequestStore;
}());
export { DILimitsRequestStore };
