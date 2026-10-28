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
import { action, computed, observable } from 'mobx';
import { SYSTEM_MESSAGES } from '../../constants/constants';
import { TYPES } from '../../ioc/ioc.types';
import { plainToNew } from '../../utils/utils';
import { AddressModel } from './models/Address.model';
var DIAddressStore = /** @class */ (function () {
    function DIAddressStore() {
        var _this = this;
        this.selfAddressList = [];
        this.selfFrequentList = [];
        this.selfFavoriteList = [];
        this.isAddressChanged = undefined;
        this.loadAddressList = function () { return __awaiter(_this, void 0, void 0, function () {
            var _a;
            var _b;
            return __generator(this, function (_c) {
                switch (_c.label) {
                    case 0:
                        _a = this;
                        return [4 /*yield*/, this.getAddressList()];
                    case 1:
                        _a.selfAddressList = (_b = (_c.sent())) !== null && _b !== void 0 ? _b : [];
                        return [2 /*return*/];
                }
            });
        }); };
        this.loadFavoriteList = function () { return __awaiter(_this, void 0, void 0, function () {
            var _a;
            var _b;
            return __generator(this, function (_c) {
                switch (_c.label) {
                    case 0:
                        _a = this;
                        return [4 /*yield*/, this.getFavoriteList()];
                    case 1:
                        _a.selfFavoriteList = (_b = (_c.sent())) !== null && _b !== void 0 ? _b : [];
                        return [2 /*return*/];
                }
            });
        }); };
        this.loadFrequentList = function () { return __awaiter(_this, void 0, void 0, function () {
            var _a;
            var _b;
            return __generator(this, function (_c) {
                switch (_c.label) {
                    case 0:
                        _a = this;
                        return [4 /*yield*/, this.getFrequentList()];
                    case 1:
                        _a.selfFrequentList = (_b = (_c.sent())) !== null && _b !== void 0 ? _b : [];
                        return [2 /*return*/];
                }
            });
        }); };
        this.addFavoriteAddress = function (item) { return __awaiter(_this, void 0, void 0, function () {
            var result;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0:
                        if (!this.checkLableUniqness(item.label)) {
                            this.logger.toMessage('error', SYSTEM_MESSAGES.addressLableIsNotUniq);
                            return [2 /*return*/];
                        }
                        if (this.isUnique(item)) {
                            this.logger.toMessage('error', SYSTEM_MESSAGES.addressIsNotUniq);
                            return [2 /*return*/];
                        }
                        return [4 /*yield*/, this.createFavoriteAddress(item)];
                    case 1:
                        result = _a.sent();
                        if (result) {
                            this.selfFavoriteList = __spreadArray(__spreadArray([], this.selfFavoriteList, true), [result], false);
                            this.isAddressChanged = true;
                            this.isAddressChanged = this.process.processStatus(200, SYSTEM_MESSAGES.addressAddSuccess);
                        }
                        return [2 /*return*/];
                }
            });
        }); };
        this.deleteFrequentAddress = function (addressId) { return __awaiter(_this, void 0, void 0, function () {
            var result;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.service.deleteFrequentAddress(addressId)];
                    case 1:
                        result = _a.sent();
                        this.isAddressChanged = this.process.processStatus(result, SYSTEM_MESSAGES.addressDeleteSuccess);
                        if (result) {
                            this.selfFrequentList = this.selfFrequentList.filter(function (x) { return x.id !== addressId; });
                        }
                        return [2 /*return*/];
                }
            });
        }); };
        this.deleteFavoriteAddress = function (addressId) { return __awaiter(_this, void 0, void 0, function () {
            var result;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.service.deleteFavoriteAddress(addressId)];
                    case 1:
                        result = _a.sent();
                        this.isAddressChanged = this.process.processStatus(result, SYSTEM_MESSAGES.addressDeleteSuccess);
                        if (result) {
                            this.selfFavoriteList = this.selfFavoriteList.filter(function (x) { return x.id !== addressId; });
                        }
                        return [2 /*return*/];
                }
            });
        }); };
    }
    Object.defineProperty(DIAddressStore.prototype, "addressExistLabels", {
        get: function () {
            return this.selfFavoriteList.map(function (x) { return x.label; }).filter(function (x) { return typeof x === 'string'; });
        },
        enumerable: false,
        configurable: true
    });
    DIAddressStore.prototype.checkLableUniqness = function (label) {
        return !this.addressExistLabels.includes(label);
    };
    DIAddressStore.prototype.isUnique = function (item) {
        return this.selfFavoriteList.some(function (el) { return el.country === item.country
            && el.city === item.city
            && el.house === item.house
            && el.street === item.street
            && el.building === item.building
            && el.structure === item.structure; });
    };
    DIAddressStore.prototype.getFrequentList = function () {
        return __awaiter(this, void 0, void 0, function () {
            var result;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.service.getFrequentList()];
                    case 1:
                        result = _a.sent();
                        return [2 /*return*/, plainToNew(AddressModel, result)];
                }
            });
        });
    };
    DIAddressStore.prototype.getAddressList = function () {
        return __awaiter(this, void 0, void 0, function () {
            var result;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.service.getAddressList()];
                    case 1:
                        result = _a.sent();
                        return [2 /*return*/, plainToNew(AddressModel, result)];
                }
            });
        });
    };
    DIAddressStore.prototype.getFavoriteList = function () {
        return __awaiter(this, void 0, void 0, function () {
            var result;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.service.getFavoriteList()];
                    case 1:
                        result = _a.sent();
                        return [2 /*return*/, plainToNew(AddressModel, result)];
                }
            });
        });
    };
    DIAddressStore.prototype.createFavoriteAddress = function (item) {
        return __awaiter(this, void 0, void 0, function () {
            var result;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.service.createFavoriteAddress(item)];
                    case 1:
                        result = _a.sent();
                        return [2 /*return*/, plainToNew(AddressModel, result)];
                }
            });
        });
    };
    DIAddressStore.prototype.initStore = function () {
        this.loadFavoriteList();
        this.loadFrequentList();
    };
    __decorate([
        inject(TYPES.IAddressService),
        __metadata("design:type", Object)
    ], DIAddressStore.prototype, "service", void 0);
    __decorate([
        inject(TYPES.ILogger),
        __metadata("design:type", Object)
    ], DIAddressStore.prototype, "logger", void 0);
    __decorate([
        inject(TYPES.IResponseService),
        __metadata("design:type", Object)
    ], DIAddressStore.prototype, "process", void 0);
    __decorate([
        observable,
        __metadata("design:type", Array)
    ], DIAddressStore.prototype, "selfAddressList", void 0);
    __decorate([
        observable,
        __metadata("design:type", Array)
    ], DIAddressStore.prototype, "selfFrequentList", void 0);
    __decorate([
        observable,
        __metadata("design:type", Array)
    ], DIAddressStore.prototype, "selfFavoriteList", void 0);
    __decorate([
        observable,
        __metadata("design:type", Object)
    ], DIAddressStore.prototype, "isAddressChanged", void 0);
    __decorate([
        computed,
        __metadata("design:type", Array),
        __metadata("design:paramtypes", [])
    ], DIAddressStore.prototype, "addressExistLabels", null);
    __decorate([
        action.bound,
        __metadata("design:type", Object)
    ], DIAddressStore.prototype, "loadAddressList", void 0);
    __decorate([
        action.bound,
        __metadata("design:type", Object)
    ], DIAddressStore.prototype, "loadFavoriteList", void 0);
    __decorate([
        action.bound,
        __metadata("design:type", Object)
    ], DIAddressStore.prototype, "loadFrequentList", void 0);
    __decorate([
        action.bound,
        __metadata("design:type", Object)
    ], DIAddressStore.prototype, "addFavoriteAddress", void 0);
    __decorate([
        action.bound,
        __metadata("design:type", Object)
    ], DIAddressStore.prototype, "deleteFrequentAddress", void 0);
    __decorate([
        action.bound,
        __metadata("design:type", Object)
    ], DIAddressStore.prototype, "deleteFavoriteAddress", void 0);
    DIAddressStore = __decorate([
        injectable()
    ], DIAddressStore);
    return DIAddressStore;
}());
export { DIAddressStore };
