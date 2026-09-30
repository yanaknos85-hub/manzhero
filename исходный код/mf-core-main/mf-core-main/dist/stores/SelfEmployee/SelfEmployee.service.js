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
import { CONSENT, CONSENT_DISPATCHER, GET_SELF_EMPLOYEE, MOCKED_API_PREFIX } from '../../constants/constants';
import { TYPES } from '../../ioc/ioc.types';
import { SelfEmployeeModel } from './models/SelfEmployeeModel';
import { Employee } from './SelfEmployee.interface';
var DISelfEmployeeService = /** @class */ (function () {
    function DISelfEmployeeService() {
        var _this = this;
        this.getSelfEmployee = function () {
            return _this.http
                .get("".concat(_this.apiPrefix()).concat(GET_SELF_EMPLOYEE))
                .then(function (x) { return _this.process.getResponseData(x, Employee); })
                .then(function (x) { return new SelfEmployeeModel(x); });
        };
        this.agreeWithPrivacyPolicy = function (disp) {
            return _this.http
                .patch("".concat(_this.apiPrefix()).concat(disp ? CONSENT_DISPATCHER : CONSENT), {});
        };
    }
    DISelfEmployeeService.prototype.apiPrefix = function () {
        return this.configStore.isMockedAuth ? MOCKED_API_PREFIX : '';
    };
    __decorate([
        inject(TYPES.IConfigStore),
        __metadata("design:type", Object)
    ], DISelfEmployeeService.prototype, "configStore", void 0);
    __decorate([
        inject(TYPES.IHttpService),
        __metadata("design:type", Object)
    ], DISelfEmployeeService.prototype, "http", void 0);
    __decorate([
        inject(TYPES.IResponseService),
        __metadata("design:type", Object)
    ], DISelfEmployeeService.prototype, "process", void 0);
    DISelfEmployeeService = __decorate([
        injectable()
    ], DISelfEmployeeService);
    return DISelfEmployeeService;
}());
export { DISelfEmployeeService };
