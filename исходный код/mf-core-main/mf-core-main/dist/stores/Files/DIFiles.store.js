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
import { inject, injectable } from 'inversify';
import { action, observable } from 'mobx';
import { SYSTEM_MESSAGES } from '../../constants/constants';
import { TYPES } from '../../ioc/ioc.types';
var DIFilesStore = /** @class */ (function () {
    function DIFilesStore() {
        var _this = this;
        this.parsingSummary = undefined;
        this.loadingSummary = undefined;
        this.isUploading = false;
        this.getDataAppended = function (values) {
            var _a;
            var file = (_a = values === null || values === void 0 ? void 0 : values.upload[0]) === null || _a === void 0 ? void 0 : _a.originFileObj;
            var data = new FormData();
            data.append('file', file);
            return data;
        };
        this.preloadHandbook = function (values) { return __awaiter(_this, void 0, void 0, function () {
            var data;
            var _this = this;
            return __generator(this, function (_a) {
                data = this.getDataAppended(values);
                this.isUploading = true;
                this.service.preloadFile(__assign(__assign({}, values), { orgId: this.self.orgId, data: data })).then(function (result) {
                    _this.isUploading = false;
                    _this.parsingSummary = result;
                    if (result.parsingResult.parseStatus === 'OK') {
                        _this.logger.toMessage('success', SYSTEM_MESSAGES.simulationSuccess);
                    }
                }, function (error) {
                    _this.isUploading = false;
                    return error;
                });
                return [2 /*return*/];
            });
        }); };
        this.loadHandbook = function (values) { return __awaiter(_this, void 0, void 0, function () {
            var data;
            var _this = this;
            return __generator(this, function (_a) {
                data = this.getDataAppended(values);
                this.isUploading = true;
                this.service.uploadFile(__assign(__assign({}, values), { orgId: this.self.orgId, data: data })).then(function (result) {
                    _this.isUploading = false;
                    _this.loadingSummary = result;
                    // if (result.parsingResult.parseStatus === 'OK') {
                    _this.logger.toMessage('success', SYSTEM_MESSAGES.fileUploadSuccess);
                    // }
                }, function (error) {
                    _this.isUploading = false;
                    return error;
                });
                return [2 /*return*/];
            });
        }); };
        // eslint-disable-next-line @typescript-eslint/no-empty-function
        this.initStore = function () { };
    }
    DIFilesStore.prototype.clearSummary = function () {
        this.parsingSummary = undefined;
        this.loadingSummary = undefined;
    };
    __decorate([
        inject(TYPES.ISelfEmployeeStore),
        __metadata("design:type", Object)
    ], DIFilesStore.prototype, "self", void 0);
    __decorate([
        inject(TYPES.IFilesService),
        __metadata("design:type", Object)
    ], DIFilesStore.prototype, "service", void 0);
    __decorate([
        inject(TYPES.ILogger),
        __metadata("design:type", Object)
    ], DIFilesStore.prototype, "logger", void 0);
    __decorate([
        observable,
        __metadata("design:type", Object)
    ], DIFilesStore.prototype, "parsingSummary", void 0);
    __decorate([
        observable,
        __metadata("design:type", Object)
    ], DIFilesStore.prototype, "loadingSummary", void 0);
    __decorate([
        observable,
        __metadata("design:type", Object)
    ], DIFilesStore.prototype, "isUploading", void 0);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", []),
        __metadata("design:returntype", void 0)
    ], DIFilesStore.prototype, "clearSummary", null);
    DIFilesStore = __decorate([
        injectable()
    ], DIFilesStore);
    return DIFilesStore;
}());
export { DIFilesStore };
