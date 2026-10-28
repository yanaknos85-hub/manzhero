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
import lodash from 'lodash';
import mapKeys from 'lodash/mapKeys';
import { action, computed, observable } from 'mobx';
import { SYSTEM_MESSAGES } from '../../constants/constants';
import { TYPES } from '../../ioc/ioc.types';
import { plainToNew } from '../../utils/utils';
import { EmployeeModel } from './models/EmployeeModel';
import * as SelfEmployeeInterface from '../SelfEmployee/SelfEmployee.interface';
import { SelfEmployeeModel } from '../SelfEmployee/models/SelfEmployeeModel';
var DIEmployeeStore = /** @class */ (function () {
    function DIEmployeeStore() {
        var _this = this;
        this.employeeAutocompleteSelected = [new EmployeeModel()];
        this.employeeAutocompleteList = [];
        this.employeeListByOrg = [];
        this.employeeListByDep = [];
        this.savedEmployee = undefined;
        this.getAllEmployeesByOrganization = function () { return __awaiter(_this, void 0, void 0, function () {
            var data;
            var _a;
            return __generator(this, function (_b) {
                switch (_b.label) {
                    case 0: return [4 /*yield*/, this.service.getAllEmployeesByOrganization(this.selfEmployee.organizationId)];
                    case 1:
                        data = _b.sent();
                        this.employeeListByOrg = (_a = plainToNew(EmployeeModel, data.content)) !== null && _a !== void 0 ? _a : [];
                        return [2 /*return*/];
                }
            });
        }); };
        this.getAllEmployeesByDepartment = function () { return __awaiter(_this, void 0, void 0, function () {
            var data;
            var _a;
            return __generator(this, function (_b) {
                switch (_b.label) {
                    case 0: return [4 /*yield*/, this.service.getAllEmployeesByDepartment(this.selfEmployee.organizationId, this.selfEmployee.delegatedById)];
                    case 1:
                        data = _b.sent();
                        this.employeeListByDep = (_a = plainToNew(EmployeeModel, data)) !== null && _a !== void 0 ? _a : [];
                        return [2 /*return*/];
                }
            });
        }); };
        this.getEmployee = function (orgId, depId, empId) { return __awaiter(_this, void 0, void 0, function () { return __generator(this, function (_a) {
            return [2 /*return*/, this.service.getEmployee(orgId, depId, empId)];
        }); }); };
        this.searchEmployees = lodash.debounce(function (name) { return __awaiter(_this, void 0, void 0, function () {
            var _a, _b;
            return __generator(this, function (_c) {
                switch (_c.label) {
                    case 0:
                        _a = this;
                        _b = observable;
                        return [4 /*yield*/, this.searchEmployeesByName(name)];
                    case 1:
                        _a.employeeAutocompleteList = _b.apply(void 0, [_c.sent()]);
                        return [2 /*return*/];
                }
            });
        }); }, 1500);
        this.searchEmployeesByOrg = lodash.debounce(function (name) { return __awaiter(_this, void 0, void 0, function () {
            var _a, _b;
            return __generator(this, function (_c) {
                switch (_c.label) {
                    case 0:
                        _a = this;
                        _b = observable;
                        return [4 /*yield*/, this.searchEmployeesByNameByOrg(name)];
                    case 1:
                        _a.employeeAutocompleteList = _b.apply(void 0, [_c.sent()]);
                        return [2 /*return*/];
                }
            });
        }); }, 1500);
    }
    Object.defineProperty(DIEmployeeStore.prototype, "selfEmployee", {
        get: function () {
            return this.selfStore.selfEmployee;
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(DIEmployeeStore.prototype, "employeeListByDepMapped", {
        get: function () {
            return mapKeys(this.employeeListByDep, 'id');
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(DIEmployeeStore.prototype, "employeeListByOrgMapped", {
        get: function () {
            return mapKeys(this.employeeListByOrg, 'id');
        },
        enumerable: false,
        configurable: true
    });
    DIEmployeeStore.prototype.getEmployeesByIds = function (orgId, empIds) {
        return this.service.getEmployeesByIds(orgId, empIds);
    };
    DIEmployeeStore.prototype.findEmployees = function (value) {
        if (value.length > 3 && !this.employeeAutocompleteList.some(function (x) { return x.fullNameWithCode === value; })) {
            this.clearAutocompleteList();
            this.searchEmployees(value);
        }
    };
    DIEmployeeStore.prototype.onEmployeeSelect = function (value, index, dontClear) {
        if (index === void 0) { index = 0; }
        return __awaiter(this, void 0, void 0, function () {
            var employee;
            return __generator(this, function (_a) {
                employee = this.employeeAutocompleteList.find(function (x) { return x.fullNameWithCode === value; });
                if (employee) {
                    try {
                        this.employeeAutocompleteSelected[index] = employee;
                    }
                    catch (error) {
                        throw new Error("\n          \u041F\u043E\u043F\u044B\u0442\u043A\u0430 \u043F\u0440\u0438\u043C\u0435\u043D\u0438\u0442\u044C index: ".concat(index, " \u0432 \u043C\u0430\u0441\u0441\u0438\u0432\u0435 \u0438\u0437 ").concat(this.employeeAutocompleteSelected.length, " \u044D\u043B\u0435\u043C\u0435\u043D\u0442\u043E\u0432!\n          \u041F\u0440\u0438\u043C\u0435\u043D\u0438\u0442\u0435 onEmployeeSelect \u0432 \u043A\u043E\u043C\u043F\u043E\u043D\u0435\u043D\u0442\u0435, \u0435\u0441\u043B\u0438 \u043E\u0434\u0438\u043D \u0438\u0437 \u0430\u0432\u0442\u043E\u043A\u043E\u043C\u043F\u043B\u0438\u0442\u043E\u0432 \u0431\u044B\u043B \u0437\u0430\u043F\u043E\u043B\u043D\u0435\u043D \u0441\u0440\u0430\u0437\u0443\n        "));
                    }
                }
                if (!dontClear) {
                    this.clearAutocompleteList();
                }
                return [2 /*return*/];
            });
        });
    };
    DIEmployeeStore.prototype.clearAutocompleteList = function () {
        this.employeeAutocompleteList = [];
    };
    DIEmployeeStore.prototype.searchEmployeesByName = function (name) {
        var _a;
        return __awaiter(this, void 0, void 0, function () {
            var content, models, noBlank;
            return __generator(this, function (_b) {
                switch (_b.label) {
                    case 0: return [4 /*yield*/, this.service.searchEmployeesByName(name)];
                    case 1:
                        content = (_b.sent()).content;
                        models = (_a = plainToNew(EmployeeModel, content)) !== null && _a !== void 0 ? _a : [];
                        noBlank = models.filter(function (x) { return x.fullNameWithCode !== ''; });
                        return [2 /*return*/, Object.values(mapKeys(noBlank, 'fullNameWithCode'))]; // removes duplicates
                }
            });
        });
    };
    DIEmployeeStore.prototype.searchEmployeesByNameByOrg = function (name) {
        var _a;
        return __awaiter(this, void 0, void 0, function () {
            var content, models, noBlank;
            return __generator(this, function (_b) {
                switch (_b.label) {
                    case 0: return [4 /*yield*/, this.service.searchEmployeesByNameByOrg(name)];
                    case 1:
                        content = (_b.sent()).content;
                        models = (_a = plainToNew(EmployeeModel, content)) !== null && _a !== void 0 ? _a : [];
                        noBlank = models.filter(function (x) { return x.fullNameWithCode !== ''; });
                        return [2 /*return*/, Object.values(mapKeys(noBlank, 'fullNameWithCode'))]; // removes duplicates
                }
            });
        });
    };
    DIEmployeeStore.prototype.updateEmployee = function (orgId, depId, empId, model) {
        return this.service.editEmployee(orgId, depId, empId, model);
    };
    DIEmployeeStore.prototype.editEmployee = function (model) {
        return __awaiter(this, void 0, void 0, function () {
            var _a, orgId, depId, empId;
            var _this = this;
            return __generator(this, function (_b) {
                if (!this.selfEmployee) {
                    return [2 /*return*/];
                }
                _a = this.selfEmployee, orgId = _a.organizationId, depId = _a.departmentId, empId = _a.id;
                this.updateEmployee(orgId, depId, empId, model).then(function (response) {
                    var status = _this.process.processStatus(response, SYSTEM_MESSAGES.employeeEditSuccess);
                    if (status) {
                        _this.savedEmployee = model;
                        _this.selfStore.getSelfEmployee();
                    }
                });
                return [2 /*return*/];
            });
        });
    };
    DIEmployeeStore.prototype.editPhone = function (phoneNumber) {
        return __awaiter(this, void 0, void 0, function () {
            var _this = this;
            return __generator(this, function (_a) {
                return [2 /*return*/, this.service.editPhone(phoneNumber).then(function () {
                        _this.selfStore.selfEmployee = new SelfEmployeeModel(__assign(__assign({}, _this.selfStore.selfEmployee), { mobilePhone: phoneNumber }));
                        return 200;
                    })];
            });
        });
    };
    DIEmployeeStore.prototype.clearSavedEmployee = function () {
        this.savedEmployee = undefined;
    };
    DIEmployeeStore.prototype.initStore = function () {
        this.getAllEmployeesByOrganization();
        this.clearAutocompleteList();
    };
    DIEmployeeStore.prototype.searchDepartmentEmployees = function (params, cancelerSetter) {
        var _a;
        return __awaiter(this, void 0, void 0, function () {
            var _b, orgId, depId, employees;
            return __generator(this, function (_c) {
                switch (_c.label) {
                    case 0:
                        _b = this.selfEmployee, orgId = _b.organizationId, depId = _b.departmentId;
                        return [4 /*yield*/, this.service.searchDepartmentEmployees(orgId, depId, params, cancelerSetter)];
                    case 1:
                        employees = _c.sent();
                        return [2 /*return*/, (_a = plainToNew(EmployeeModel, employees)) !== null && _a !== void 0 ? _a : []];
                }
            });
        });
    };
    DIEmployeeStore.prototype.searchOrganizationEmployees = function (params, cancelerSetter) {
        var _a;
        return __awaiter(this, void 0, void 0, function () {
            var orgId, employees;
            return __generator(this, function (_b) {
                switch (_b.label) {
                    case 0:
                        orgId = this.selfEmployee.organizationId;
                        return [4 /*yield*/, this.service.searchOrganizationEmployees(orgId, params, cancelerSetter)];
                    case 1:
                        employees = _b.sent();
                        return [2 /*return*/, (_a = plainToNew(EmployeeModel, employees)) !== null && _a !== void 0 ? _a : []];
                }
            });
        });
    };
    __decorate([
        inject(TYPES.IResponseService),
        __metadata("design:type", Object)
    ], DIEmployeeStore.prototype, "process", void 0);
    __decorate([
        inject(TYPES.IEmployeeService),
        __metadata("design:type", Object)
    ], DIEmployeeStore.prototype, "service", void 0);
    __decorate([
        inject(TYPES.ISelfEmployeeStore),
        __metadata("design:type", Object)
    ], DIEmployeeStore.prototype, "selfStore", void 0);
    __decorate([
        computed,
        __metadata("design:type", EmployeeModel),
        __metadata("design:paramtypes", [])
    ], DIEmployeeStore.prototype, "selfEmployee", null);
    __decorate([
        observable,
        __metadata("design:type", Array)
    ], DIEmployeeStore.prototype, "employeeAutocompleteSelected", void 0);
    __decorate([
        observable,
        __metadata("design:type", Array)
    ], DIEmployeeStore.prototype, "employeeAutocompleteList", void 0);
    __decorate([
        observable,
        __metadata("design:type", Array)
    ], DIEmployeeStore.prototype, "employeeListByOrg", void 0);
    __decorate([
        observable,
        __metadata("design:type", Array)
    ], DIEmployeeStore.prototype, "employeeListByDep", void 0);
    __decorate([
        computed,
        __metadata("design:type", Object),
        __metadata("design:paramtypes", [])
    ], DIEmployeeStore.prototype, "employeeListByDepMapped", null);
    __decorate([
        computed,
        __metadata("design:type", Object),
        __metadata("design:paramtypes", [])
    ], DIEmployeeStore.prototype, "employeeListByOrgMapped", null);
    __decorate([
        observable,
        __metadata("design:type", Object)
    ], DIEmployeeStore.prototype, "savedEmployee", void 0);
    __decorate([
        action,
        __metadata("design:type", Object)
    ], DIEmployeeStore.prototype, "getAllEmployeesByOrganization", void 0);
    __decorate([
        action,
        __metadata("design:type", Object)
    ], DIEmployeeStore.prototype, "getAllEmployeesByDepartment", void 0);
    __decorate([
        action
        // eslint-disable-next-line @stylistic/max-len
        ,
        __metadata("design:type", Object)
    ], DIEmployeeStore.prototype, "getEmployee", void 0);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String, Array]),
        __metadata("design:returntype", Promise)
    ], DIEmployeeStore.prototype, "getEmployeesByIds", null);
    __decorate([
        action.bound,
        __metadata("design:type", Object)
    ], DIEmployeeStore.prototype, "searchEmployees", void 0);
    __decorate([
        action.bound,
        __metadata("design:type", Object)
    ], DIEmployeeStore.prototype, "searchEmployeesByOrg", void 0);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String]),
        __metadata("design:returntype", void 0)
    ], DIEmployeeStore.prototype, "findEmployees", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String, Object, Boolean]),
        __metadata("design:returntype", Promise)
    ], DIEmployeeStore.prototype, "onEmployeeSelect", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String]),
        __metadata("design:returntype", Promise)
    ], DIEmployeeStore.prototype, "searchEmployeesByName", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String]),
        __metadata("design:returntype", Promise)
    ], DIEmployeeStore.prototype, "searchEmployeesByNameByOrg", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [EmployeeModel]),
        __metadata("design:returntype", Promise)
    ], DIEmployeeStore.prototype, "editEmployee", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String]),
        __metadata("design:returntype", Promise)
    ], DIEmployeeStore.prototype, "editPhone", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", []),
        __metadata("design:returntype", void 0)
    ], DIEmployeeStore.prototype, "clearSavedEmployee", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [Object, Function]),
        __metadata("design:returntype", Promise)
    ], DIEmployeeStore.prototype, "searchDepartmentEmployees", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [Object, Function]),
        __metadata("design:returntype", Promise)
    ], DIEmployeeStore.prototype, "searchOrganizationEmployees", null);
    DIEmployeeStore = __decorate([
        injectable()
    ], DIEmployeeStore);
    return DIEmployeeStore;
}());
export { DIEmployeeStore };
