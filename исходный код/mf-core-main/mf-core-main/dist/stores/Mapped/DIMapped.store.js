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
import lodash from 'lodash';
import { computed } from 'mobx';
import { EmployeeDetailedModel } from '../Employee/models/DetailedEmployeeModel';
import { TYPES } from '../../ioc/ioc.types';
import { DepartmentDetailedModel } from '../Corporate/models/DepartmentDetailed.model';
var MappedStore = /** @class */ (function () {
    function MappedStore() {
    }
    Object.defineProperty(MappedStore.prototype, "employeeListByOrgDetailed", {
        get: function () {
            var _this = this;
            var _a, _b;
            var result = (_b = (_a = this.empStore) === null || _a === void 0 ? void 0 : _a.employeeListByOrg.map(function (emp) { return _this.mapEmployeeFields(emp); })) !== null && _b !== void 0 ? _b : [];
            return lodash.mapKeys(result, 'id');
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(MappedStore.prototype, "employeeListByDepMapped", {
        get: function () {
            var _this = this;
            var _a, _b;
            var result = (_b = (_a = this.empStore) === null || _a === void 0 ? void 0 : _a.employeeListByDep.map(function (emp) { return _this.mapEmployeeFields(emp); })) !== null && _b !== void 0 ? _b : [];
            return lodash.mapKeys(result, 'id');
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(MappedStore.prototype, "selfEmployeeDetailed", {
        get: function () {
            return this.mapEmployeeFields(this.selfStore.selfEmployee);
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(MappedStore.prototype, "departmentsListDetailed", {
        get: function () {
            var _this = this;
            var _a, _b;
            var result = (_b = (_a = this.corpStore.departments) === null || _a === void 0 ? void 0 : _a.map(function (dep) { return _this.mapDepartmentsFields(dep); })) !== null && _b !== void 0 ? _b : [];
            return lodash.mapKeys(result, 'id');
        },
        enumerable: false,
        configurable: true
    });
    MappedStore.prototype.mapEmployeeFields = function (employee) {
        var _a, _b, _c, _d, _e, _f, _g, _h, _j, _k, _l, _m;
        var delegatedBy = ((_b = (_a = this.empStore) === null || _a === void 0 ? void 0 : _a.employeeListByOrgMapped[employee.delegatedById]) === null || _b === void 0 ? void 0 : _b.shortName) || employee.delegatedById;
        var supervisor = ((_d = (_c = this.empStore) === null || _c === void 0 ? void 0 : _c.employeeListByOrgMapped[employee.supervisorId]) === null || _d === void 0 ? void 0 : _d.shortName) || employee.supervisorId;
        var org = (_e = this.corpStore) === null || _e === void 0 ? void 0 : _e.organizationsMapped[employee.organizationId];
        var organization = (_f = org === null || org === void 0 ? void 0 : org.officialName) !== null && _f !== void 0 ? _f : employee.organizationId;
        var department = (_j = (_h = (_g = this.corpStore) === null || _g === void 0 ? void 0 : _g.departmentsMapped[employee.departmentId]) === null || _h === void 0 ? void 0 : _h.departmentName) !== null && _j !== void 0 ? _j : employee.departmentId;
        var position = (_m = (_l = (_k = this.corpStore) === null || _k === void 0 ? void 0 : _k.positionsMapped[employee.positionId]) === null || _l === void 0 ? void 0 : _l.positionName) !== null && _m !== void 0 ? _m : employee.positionId;
        return new EmployeeDetailedModel(__assign(__assign({}, employee), { fullName: employee.fullName, nameWithInitials: employee.nameWithInitials, delegatedBy: delegatedBy, supervisor: supervisor, organization: organization, department: department, position: position }));
    };
    MappedStore.prototype.mapDepartmentsFields = function (department) {
        var _a, _b, _c, _d;
        var departmentHead = department.departmentHeadId
            ? ((_b = (_a = this.empStore) === null || _a === void 0 ? void 0 : _a.employeeListByOrgMapped[department.departmentHeadId]) === null || _b === void 0 ? void 0 : _b.fullName) || ''
            : 'Руководитель не назначен';
        var organization = ((_c = this.corpStore.organizationsMapped[department.organizationId]) === null || _c === void 0 ? void 0 : _c.officialName) || 'Нет организации с таким uuid';
        var parent = department.parentId
            ? ((_d = this.corpStore.organizationsMapped[department.parentId]) === null || _d === void 0 ? void 0 : _d.officialName) || 'Нет организации с таким uuid'
            : 'Нет родительского подразделения';
        return new DepartmentDetailedModel(__assign(__assign({}, department), { departmentHead: departmentHead, organization: organization, parent: parent }));
    };
    __decorate([
        inject(TYPES.ISelfEmployeeStore),
        __metadata("design:type", Object)
    ], MappedStore.prototype, "selfStore", void 0);
    __decorate([
        inject(TYPES.IEmployeeStore),
        __metadata("design:type", Object)
    ], MappedStore.prototype, "empStore", void 0);
    __decorate([
        inject(TYPES.ICorporateStore),
        __metadata("design:type", Object)
    ], MappedStore.prototype, "corpStore", void 0);
    __decorate([
        computed,
        __metadata("design:type", Object),
        __metadata("design:paramtypes", [])
    ], MappedStore.prototype, "employeeListByOrgDetailed", null);
    __decorate([
        computed,
        __metadata("design:type", Object),
        __metadata("design:paramtypes", [])
    ], MappedStore.prototype, "employeeListByDepMapped", null);
    __decorate([
        computed,
        __metadata("design:type", EmployeeDetailedModel),
        __metadata("design:paramtypes", [])
    ], MappedStore.prototype, "selfEmployeeDetailed", null);
    __decorate([
        computed,
        __metadata("design:type", Object),
        __metadata("design:paramtypes", [])
    ], MappedStore.prototype, "departmentsListDetailed", null);
    MappedStore = __decorate([
        injectable()
    ], MappedStore);
    return MappedStore;
}());
export { MappedStore };
