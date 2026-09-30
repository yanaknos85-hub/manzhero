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
import * as t from 'io-ts';
import { APPROVE_REQUEST, GET_ACCOUNT_BONUSES, GET_ALL_REQUESTS, GET_DEPLIMITS, GET_DEPLIMITS_BY_DEP, GET_DEPLIMITS_BY_DEP_AND_YEAR, GET_EMPLIMITS, GET_EMP_LIMIT, GET_LIMITS_REQUESTS_BY_AUTHOR, GET_LIMIT_SHARING, GET_LIMIT_TRANSFER_HISTORY, GET_LIMITS_REQUESTS_BY_APPROVER, GET_ACTIVE_LIMITS_REQUESTS_BY_APPROVER, GET_OLD_LIMITS_REQUESTS_BY_APPROVER, GET_REQUESTS_DEP, GET_REQUESTS_EMP, GET_SPENDINGS, LIMITS, LIMIT_REQUEST_CANCEL } from '../../constants/constants';
import { TYPES } from '../../ioc/ioc.types';
import { ISpentActionsType, Limit, LimitCostHistory, LimitRequestInfo, ActiveLimitRequestInfo, OldLimitRequestInfo, LimitRequestSaving, LimitRequestSavingObject, LimitTransferHistory } from './Limit.interface';
var DILimitsService = /** @class */ (function () {
    function DILimitsService() {
    }
    DILimitsService.prototype.getDepartmentLimits = function () {
        return this.http.get(GET_DEPLIMITS).then(this.process.getResponseData);
    };
    DILimitsService.prototype.getDepartmentLimitsByYear = function (depId, year) {
        return this.http
            .get(GET_DEPLIMITS_BY_DEP_AND_YEAR, { urlParams: { depId: depId, year: year } })
            .then(this.process.getResponseData);
    };
    DILimitsService.prototype.getEmployeeLimits = function () {
        return this.http.get(GET_EMPLIMITS).then(this.process.getResponseData);
    };
    DILimitsService.prototype.getLimitSharing = function (limitId) {
        return this.http.get("".concat(GET_LIMIT_SHARING).concat(limitId)).then(this.process.getResponseData);
    };
    DILimitsService.prototype.getEmployeeLimit = function (employeeId, year) {
        return this.http.get("".concat(GET_EMP_LIMIT).concat(employeeId, "/year/").concat(year)).then(this.process.getResponseData);
    };
    DILimitsService.prototype.getLimitsRequestsByAuthor = function () {
        return this.http
            .get("".concat(GET_LIMITS_REQUESTS_BY_AUTHOR))
            .then(this.process.decodeResponseData(t.array(ISpentActionsType)));
    };
    DILimitsService.prototype.cancelLimitRequest = function (data) {
        return this.http.put(LIMIT_REQUEST_CANCEL, data).then(this.process.getResponseStatus);
    };
    DILimitsService.prototype.getLimitsRequestByApprover = function () {
        return this.http
            .get(GET_LIMITS_REQUESTS_BY_APPROVER)
            .then(this.process.decodeResponseData(t.array(LimitRequestInfo)));
    };
    DILimitsService.prototype.getActiveLimitsRequestByApprover = function (_a) {
        var page = _a.page, size = _a.size;
        return this.http
            .get(GET_ACTIVE_LIMITS_REQUESTS_BY_APPROVER, { params: { page: page, size: size } })
            .then(this.process.decodeResponseData(ActiveLimitRequestInfo));
    };
    DILimitsService.prototype.getOldLimitsRequestByApprover = function (_a) {
        var page = _a.page, size = _a.size;
        return this.http
            .get(GET_OLD_LIMITS_REQUESTS_BY_APPROVER, { params: { page: page, size: size } })
            .then(this.process.decodeResponseData(OldLimitRequestInfo));
    };
    DILimitsService.prototype.getAllLimitRequests = function () {
        return this.http
            .get(GET_ALL_REQUESTS)
            .then(this.process.decodeResponseData(t.array(LimitRequestInfo)));
    };
    // Один запрос на Approve & cancel
    DILimitsService.prototype.approveLimitRequest = function (data) {
        return this.http
            .put(APPROVE_REQUEST, LimitRequestSavingObject.encode(data), {})
            .then(this.process.getResponseStatus);
    };
    DILimitsService.prototype.changeDepLimitRequest = function (data, requestId) {
        return this.http
            .put("".concat(GET_REQUESTS_DEP, "/").concat(requestId), LimitRequestSaving.encode(data), {})
            .then(this.process.getResponseStatus);
    };
    DILimitsService.prototype.changeEmpLimitRequest = function (data, requestId) {
        return this.http
            .put("".concat(GET_REQUESTS_EMP, "/").concat(requestId), LimitRequestSaving.encode(data), {})
            .then(this.process.getResponseStatus);
    };
    DILimitsService.prototype.getLimitByDepartment = function (departmentId) {
        return this.http
            .get("".concat(GET_DEPLIMITS_BY_DEP, "/").concat(departmentId))
            .then(this.process.decodeResponseData(t.array(Limit)));
    };
    // Корректировки по лимиту (Получение движений ДС по лимиту)
    DILimitsService.prototype.getLimitTransferHistory = function (limitId, year, maxRecords) {
        return this.http
            .get("".concat(GET_LIMIT_TRANSFER_HISTORY, "/").concat(limitId, "/").concat(year, "/").concat(maxRecords))
            .then(this.process.decodeResponseData(t.array(LimitTransferHistory)));
    };
    // Расходы по лимиту
    DILimitsService.prototype.getLimitCostHistory = function (limitId, maxRecords, organizationId) {
        return this.http
            .get("/".concat(LIMITS, "/").concat(GET_SPENDINGS, "/").concat(organizationId, "/").concat(limitId, "/").concat(maxRecords))
            .then(this.process.decodeResponseData(t.array(LimitCostHistory)));
    };
    DILimitsService.prototype.getAccountBonuses = function (ownerId) {
        return this.http.get("".concat(GET_ACCOUNT_BONUSES).concat(ownerId)).then(this.process.getResponseData);
    };
    __decorate([
        inject(TYPES.IHttpService),
        __metadata("design:type", Object)
    ], DILimitsService.prototype, "http", void 0);
    __decorate([
        inject(TYPES.IResponseService),
        __metadata("design:type", Object)
    ], DILimitsService.prototype, "process", void 0);
    DILimitsService = __decorate([
        injectable()
    ], DILimitsService);
    return DILimitsService;
}());
export { DILimitsService };
