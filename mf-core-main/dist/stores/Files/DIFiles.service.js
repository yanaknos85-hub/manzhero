var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
import { inject, injectable } from 'inversify';
import { PRELOADFILE, UPLOADFILE } from '../../constants/constants';
import { TYPES } from '../../ioc/ioc.types';
import { IOUploadImportSummary, IOUploadParsingSummary } from './Files.interface';
var DIFilesService = /** @class */ (function () {
    function DIFilesService() {
    }
    DIFilesService.prototype.uploadFile = function (args) {
        var _this = this;
        var orgId = args.orgId, decSeparator = args.decSeparator, nsi = args.nsi, separator = args.separator, strategyMode = args.strategyMode, data = args.data;
        return this.http
            .postFormData(UPLOADFILE, data, {
            urlParams: {
                orgId: orgId,
                decSeparator: decSeparator,
                nsi: nsi,
                separator: separator,
                strategyMode: strategyMode,
            },
        })
            .then(function (result) { return _this.process.getResponseData(result, IOUploadImportSummary); });
    };
    DIFilesService.prototype.preloadFile = function (args) {
        var _this = this;
        var orgId = args.orgId, decSeparator = args.decSeparator, nsi = args.nsi, separator = args.separator, strategyMode = args.strategyMode, data = args.data;
        return this.http
            .postFormData(PRELOADFILE, data, {
            urlParams: {
                orgId: orgId,
                decSeparator: decSeparator,
                nsi: nsi,
                separator: separator,
                strategyMode: strategyMode,
            },
        })
            .then(function (result) { return _this.process.getResponseData(result, IOUploadParsingSummary); });
    };
    __decorate([
        inject(TYPES.IHttpService),
        __metadata("design:type", Object)
    ], DIFilesService.prototype, "http", void 0);
    __decorate([
        inject(TYPES.IResponseService),
        __metadata("design:type", Object)
    ], DIFilesService.prototype, "process", void 0);
    DIFilesService = __decorate([
        injectable()
    ], DIFilesService);
    return DIFilesService;
}());
export { DIFilesService };
