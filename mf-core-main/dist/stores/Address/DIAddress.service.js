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
import { SELF_ADDRESSES, SELF_FAVORITE_ADDRESSES, SELF_FAVORITE_ADDRESSES_PARAMS, SELF_FREQUENT_ADDRESSES, SELF_FREQUENT_ADDRESSES_PARAMS } from '../../constants/constants';
import { TYPES } from '../../ioc/ioc.types';
import { IOAddressExist } from './Address.interface';
var DIAddressService = /** @class */ (function () {
    function DIAddressService() {
    }
    DIAddressService.prototype.getAddressList = function () {
        var _this = this;
        return this.http
            .get("".concat(SELF_ADDRESSES))
            .then(function (x) { return _this.process.getResponseData(x, IOAddressExist); });
    };
    DIAddressService.prototype.getFrequentList = function () {
        var _this = this;
        return this.http
            .get("".concat(SELF_FREQUENT_ADDRESSES))
            .then(function (x) { return _this.process.getResponseData(x, IOAddressExist); });
    };
    DIAddressService.prototype.getFavoriteList = function () {
        var _this = this;
        return this.http
            .get("".concat(SELF_FAVORITE_ADDRESSES))
            .then(function (x) { return _this.process.getResponseData(x, IOAddressExist); });
    };
    DIAddressService.prototype.createFavoriteAddress = function (address) {
        var _this = this;
        return this.http
            .post("".concat(SELF_FAVORITE_ADDRESSES), __assign({}, address))
            .then(function (x) { return _this.process.getResponseData(x, IOAddressExist); });
    };
    DIAddressService.prototype.getFavoriteAddress = function (addressId) {
        var _this = this;
        return this.http
            .get("".concat(SELF_FAVORITE_ADDRESSES_PARAMS), { urlParams: { addressId: addressId } })
            .then(function (x) { return _this.process.getResponseData(x, IOAddressExist); });
    };
    DIAddressService.prototype.updateFavoriteAddress = function (address) {
        return this.http
            .put("".concat(SELF_FAVORITE_ADDRESSES_PARAMS), __assign({}, address), { urlParams: { addressId: address.id } })
            .then(this.process.getResponseStatus);
    };
    DIAddressService.prototype.deleteFavoriteAddress = function (addressId) {
        return this.http
            .delete("".concat(SELF_FAVORITE_ADDRESSES_PARAMS), { urlParams: { addressId: addressId } })
            .then(this.process.getResponseStatus);
    };
    DIAddressService.prototype.deleteFrequentAddress = function (addressId) {
        return this.http
            .delete("".concat(SELF_FREQUENT_ADDRESSES_PARAMS), { urlParams: { addressId: addressId } })
            .then(this.process.getResponseStatus);
    };
    __decorate([
        inject(TYPES.IHttpService),
        __metadata("design:type", Object)
    ], DIAddressService.prototype, "http", void 0);
    __decorate([
        inject(TYPES.IResponseService),
        __metadata("design:type", Object)
    ], DIAddressService.prototype, "process", void 0);
    DIAddressService = __decorate([
        injectable()
    ], DIAddressService);
    return DIAddressService;
}());
export { DIAddressService };
