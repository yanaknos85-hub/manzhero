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
import { AVAILABLE_TRANSPORTTYPES, AVAILABLE_TRANSPORTTYPES_BY_SERVICE, TRANSPORTTYPES } from '../../constants/constants';
import { TYPES } from '../../ioc/ioc.types';
var DITransportTypesService = /** @class */ (function () {
    function DITransportTypesService() {
    }
    DITransportTypesService.prototype.getTransportTypes = function () {
        return this.http.get("".concat(TRANSPORTTYPES)).then(this.process.getResponseData);
    };
    DITransportTypesService.prototype.getAvailableTransportTypes = function (orgId) {
        return this.http
            .get("".concat(AVAILABLE_TRANSPORTTYPES), { urlParams: { orgId: orgId } })
            .then(this.process.getResponseData);
    };
    DITransportTypesService.prototype.getAvailableTransportTypesByService = function (serviceType, orgId) {
        return this.http
            .get("".concat(AVAILABLE_TRANSPORTTYPES_BY_SERVICE), { urlParams: { serviceType: serviceType, orgId: orgId } })
            .then(this.process.getResponseData);
    };
    __decorate([
        inject(TYPES.IHttpService),
        __metadata("design:type", Object)
    ], DITransportTypesService.prototype, "http", void 0);
    __decorate([
        inject(TYPES.IResponseService),
        __metadata("design:type", Object)
    ], DITransportTypesService.prototype, "process", void 0);
    DITransportTypesService = __decorate([
        injectable()
    ], DITransportTypesService);
    return DITransportTypesService;
}());
export { DITransportTypesService };
