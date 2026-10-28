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
import { inject, injectable } from 'inversify';
import { DELETE_DEPARTMENT, DEPARTMENTS_ADD_PARAMS, DEPARTMENT_EDIT_PARAMS, GET_ALL_DEPARTMENTS, GET_ALL_ORGANIZATIONS, GET_ALL_POSITIONS, GET_DEPARTMENT, GET_ORGANIZATION, GET_POSITION } from '../../constants/constants';
import { TYPES } from '../../ioc/ioc.types';
var DICorporateService = /** @class */ (function () {
    function DICorporateService() {
    }
    DICorporateService.prototype.getAllOrganizations = function () {
        return this.http.get("".concat(GET_ALL_ORGANIZATIONS)).then(this.process.getResponseData);
    };
    DICorporateService.prototype.getAllDepartments = function (orgId) {
        return this.http
            .get("".concat(GET_ALL_DEPARTMENTS), { urlParams: { orgId: orgId } })
            .then(this.process.getResponseData);
    };
    DICorporateService.prototype.getAllPositions = function (orgId) {
        return this.http
            .get("".concat(GET_ALL_POSITIONS), { urlParams: { orgId: orgId } })
            .then(this.process.getResponseData);
    };
    DICorporateService.prototype.getOrganization = function (orgId) {
        return this.http
            .get("".concat(GET_ORGANIZATION), { urlParams: { orgId: orgId } })
            .then(this.process.getResponseData);
    };
    DICorporateService.prototype.getDepartment = function (orgId, depId) {
        return this.http
            .get("".concat(GET_DEPARTMENT), { urlParams: { orgId: orgId, depId: depId } })
            .then(this.process.getResponseData);
    };
    DICorporateService.prototype.addDepartment = function (orgId, model) {
        return this.http
            .post("".concat(DEPARTMENTS_ADD_PARAMS), __assign({}, model), { urlParams: { orgId: orgId } })
            .then(this.process.getResponseData);
    };
    DICorporateService.prototype.editDepartment = function (orgId, depId, model) {
        return this.http
            .put("".concat(DEPARTMENT_EDIT_PARAMS), __assign({}, model), { urlParams: { orgId: orgId, depId: depId } })
            .then(this.process.getResponseStatus);
    };
    DICorporateService.prototype.deleteDepartment = function (orgId, depId) {
        return this.http
            .delete("".concat(DELETE_DEPARTMENT), { urlParams: { orgId: orgId, depId: depId } })
            .then(this.process.getResponseStatus);
    };
    DICorporateService.prototype.getPosition = function (orgId, posId) {
        return this.http
            .get("".concat(GET_POSITION), { urlParams: { orgId: orgId, posId: posId } })
            .then(this.process.getResponseData);
    };
    __decorate([
        inject(TYPES.IHttpService),
        __metadata("design:type", Object)
    ], DICorporateService.prototype, "http", void 0);
    __decorate([
        inject(TYPES.IResponseService),
        __metadata("design:type", Object)
    ], DICorporateService.prototype, "process", void 0);
    DICorporateService = __decorate([
        injectable()
    ], DICorporateService);
    return DICorporateService;
}());
export { DICorporateService };
