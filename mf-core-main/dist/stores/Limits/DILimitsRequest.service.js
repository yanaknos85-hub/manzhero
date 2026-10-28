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
import { GET_REQUESTS_DEP, GET_REQUESTS_EMP, GET_SIBLINGS, LIMITREQUESTS_APPROVE_PARAMS, LIMITREQUESTS_CANCEL_PARAMS, LIMITREQUESTS_CRUD, LIMITREQUESTS_PARAMS } from '../../constants/constants';
import { TYPES } from '../../ioc/ioc.types';
import { IOLimitRequestNew, LimitEmpRequest, LimitSendRequest } from './LimitsRequest.interface';
var DILimitsRequestService = /** @class */ (function () {
    function DILimitsRequestService() {
    }
    DILimitsRequestService.prototype.getLimitRequestList = function () {
        var _this = this;
        return this.http
            .get(LIMITREQUESTS_CRUD)
            .then(function (r) { return _this.process.getResponseData(r, IOLimitRequestNew); });
    };
    // Получение смежников подразделения
    DILimitsRequestService.prototype.getDepSiblings = function (data) {
        var _this = this;
        return this.http
            .get("".concat(GET_SIBLINGS).concat(data.departmentId, "/").concat(data.percent, "/").concat(data.transportType, "/").concat(data.year, "/").concat(data.sum))
            .then(function (r) { return _this.process.getResponseData(r); });
    };
    DILimitsRequestService.prototype.addLimitRequest = function (data) {
        return this.http.post(GET_REQUESTS_DEP, LimitSendRequest.encode(data)).then(this.process.getResponseStatus);
    };
    DILimitsRequestService.prototype.addEmpLimitRequest = function (data) {
        return this.http.post(GET_REQUESTS_EMP, LimitEmpRequest.encode(data)).then(this.process.getResponseStatus);
    };
    DILimitsRequestService.prototype.getLimitRequest = function (reqId) {
        var _this = this;
        return this.http
            .get(LIMITREQUESTS_PARAMS, { urlParams: { reqId: reqId } })
            .then(function (r) { return _this.process.getResponseData(r, IOLimitRequestNew); });
    };
    DILimitsRequestService.prototype.editLimitRequest = function (reqId, data) {
        return this.http
            .put(LIMITREQUESTS_PARAMS, data, { urlParams: { reqId: reqId } })
            .then(this.process.getResponseStatus);
    };
    DILimitsRequestService.prototype.approveLimitRequest = function (reqId) {
        return this.http
            .put(LIMITREQUESTS_APPROVE_PARAMS, {}, { urlParams: { reqId: reqId } })
            .then(this.process.getResponseStatus);
    };
    DILimitsRequestService.prototype.cancelLimitRequest = function (reqId) {
        return this.http
            .put(LIMITREQUESTS_CANCEL_PARAMS, {}, { urlParams: { reqId: reqId } })
            .then(this.process.getResponseStatus);
    };
    DILimitsRequestService.prototype.deleteLimitRequest = function (reqId) {
        return this.http
            .delete(LIMITREQUESTS_PARAMS, { urlParams: { reqId: reqId } })
            .then(this.process.getResponseStatus);
    };
    __decorate([
        inject(TYPES.IHttpService),
        __metadata("design:type", Object)
    ], DILimitsRequestService.prototype, "http", void 0);
    __decorate([
        inject(TYPES.IResponseService),
        __metadata("design:type", Object)
    ], DILimitsRequestService.prototype, "process", void 0);
    DILimitsRequestService = __decorate([
        injectable()
    ], DILimitsRequestService);
    return DILimitsRequestService;
}());
export { DILimitsRequestService };
