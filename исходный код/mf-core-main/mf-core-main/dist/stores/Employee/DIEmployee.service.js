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
import axios from 'axios';
import { inject, injectable } from 'inversify';
import { GET_SELF_EMPLOYEE, EMPLOYEES_PARAMS, EMPLOYEES_SEARCH, EMPLOYEES_SEARCH_BY_ORG, EMPLOYEE_PARAMS, GET_ALL_EMPLOYEES_BY_DEPARTMENTS_PARAMS, GET_ALL_EMPLOYEES_BY_ORGANIZATION_PARAMS, MOCKED_API_PREFIX } from '../../constants/constants';
import { TYPES } from '../../ioc/ioc.types';
var DIEmployeeService = /** @class */ (function () {
    function DIEmployeeService() {
    }
    DIEmployeeService.prototype.apiPrefix = function () {
        return this.configStore.isMockedApi ? MOCKED_API_PREFIX : '';
    };
    DIEmployeeService.prototype.getAllEmployeesByOrganization = function (orgId) {
        return this.http
            .get("".concat(this.apiPrefix()).concat(GET_ALL_EMPLOYEES_BY_ORGANIZATION_PARAMS), {
            urlParams: { orgId: orgId },
        })
            .then(this.process.getResponseData);
    };
    DIEmployeeService.prototype.getAllEmployeesByDepartment = function (orgId, depId) {
        return this.http
            .get("".concat(this.apiPrefix()).concat(GET_ALL_EMPLOYEES_BY_DEPARTMENTS_PARAMS), { urlParams: { orgId: orgId, depId: depId } })
            .then(this.process.getResponseData);
    };
    DIEmployeeService.prototype.getEmployee = function (orgId, depId, empId) {
        return this.http
            .get("".concat(this.apiPrefix()).concat(EMPLOYEE_PARAMS), {
            urlParams: {
                orgId: orgId,
                depId: depId,
                empId: empId,
            },
        })
            .then(this.process.getResponseData);
    };
    DIEmployeeService.prototype.getEmployeesByIds = function (orgId, empIds) {
        return this.http
            .get("".concat(this.apiPrefix()).concat(EMPLOYEES_PARAMS), { urlParams: { orgId: orgId, empIds: empIds.toString() } })
            .then(this.process.getResponseData);
    };
    DIEmployeeService.prototype.searchEmployeesByName = function (name) {
        return this.http
            .get("".concat(this.apiPrefix()).concat(EMPLOYEES_SEARCH), {
            urlParams: { name: name },
        })
            .then(this.process.getResponseData);
    };
    DIEmployeeService.prototype.searchEmployeesByNameByOrg = function (name) {
        return this.http
            .get("".concat(this.apiPrefix()).concat(EMPLOYEES_SEARCH_BY_ORG), {
            urlParams: { name: name },
        })
            .then(this.process.getResponseData);
    };
    DIEmployeeService.prototype.addEmployee = function (orgId, depId, employee) {
        return this.http
            .post("".concat(this.apiPrefix()).concat(EMPLOYEE_PARAMS), __assign({}, employee), { urlParams: { orgId: orgId, depId: depId } })
            .then(this.process.getResponseData);
    };
    DIEmployeeService.prototype.editEmployee = function (orgId, depId, empId, employee) {
        return this.http
            .put("".concat(this.apiPrefix()).concat(EMPLOYEE_PARAMS), __assign({}, employee), {
            urlParams: {
                orgId: orgId,
                depId: depId,
                empId: empId,
            },
        })
            .then(this.process.getResponseStatus);
    };
    DIEmployeeService.prototype.editPhone = function (phoneNumber) {
        return this.http
            .patch("".concat(this.apiPrefix()).concat(GET_SELF_EMPLOYEE), [{ field: 'mobilePhone', value: phoneNumber }], { headers: { _method: 'patch' } })
            .then(this.process.getResponseStatus);
    };
    DIEmployeeService.prototype.deleteEmployee = function (orgId, depId, empId) {
        return this.http
            .delete("".concat(this.apiPrefix()).concat(EMPLOYEE_PARAMS), {
            urlParams: {
                orgId: orgId,
                depId: depId,
                empId: empId,
            },
        })
            .then(this.process.getResponseData);
    };
    DIEmployeeService.prototype.searchDepartmentEmployees = function (orgId, depId, params, cancelerSetter) {
        var _this = this;
        return this.http
            .get("".concat(this.apiPrefix()).concat(GET_ALL_EMPLOYEES_BY_DEPARTMENTS_PARAMS), {
            params: params,
            urlParams: { orgId: orgId, depId: depId },
            cancelToken: cancelerSetter && new axios.CancelToken(cancelerSetter),
        })
            .then(function (data) { return _this.process.getResponseData(data.data.content); });
    };
    DIEmployeeService.prototype.searchOrganizationEmployees = function (orgId, params, cancelerSetter) {
        var _this = this;
        return this.http
            .get("".concat(this.apiPrefix()).concat(GET_ALL_EMPLOYEES_BY_ORGANIZATION_PARAMS), {
            params: params,
            urlParams: { orgId: orgId },
            cancelToken: cancelerSetter && new axios.CancelToken(cancelerSetter),
        })
            .then(function (data) { return _this.process.getResponseData(data.data.content); });
    };
    __decorate([
        inject(TYPES.IConfigStore),
        __metadata("design:type", Object)
    ], DIEmployeeService.prototype, "configStore", void 0);
    __decorate([
        inject(TYPES.IHttpService),
        __metadata("design:type", Object)
    ], DIEmployeeService.prototype, "http", void 0);
    __decorate([
        inject(TYPES.IResponseService),
        __metadata("design:type", Object)
    ], DIEmployeeService.prototype, "process", void 0);
    DIEmployeeService = __decorate([
        injectable()
    ], DIEmployeeService);
    return DIEmployeeService;
}());
export { DIEmployeeService };
export default DIEmployeeService;
