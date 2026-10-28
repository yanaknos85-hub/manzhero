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
import { action, computed, observable } from 'mobx';
import { TYPES } from '../../../ioc/ioc.types';
import { MappedStore } from '../../Mapped/DIMapped.store';
import { includesByLowerCaseAndSpaces } from '../../../utils/utils';
var DICorporateFiltersStore = /** @class */ (function () {
    function DICorporateFiltersStore() {
        this.filters = new Map();
    }
    Object.defineProperty(DICorporateFiltersStore.prototype, "departmentsFiltered", {
        get: function () {
            var result = [];
            var data = Object.values(this.mappedStore.departmentsListDetailed);
            this.filters.forEach(function (value, key) {
                if (result.length > 0) {
                    result = result.filter(function (dep) { return includesByLowerCaseAndSpaces(dep[key], value); });
                }
                else {
                    // @ts-ignore
                    result = data.filter(function (dep) { return includesByLowerCaseAndSpaces(dep[key], value); });
                }
            });
            // @ts-ignore
            return this.filters.size === 0 && result.length === 0 ? data : result;
        },
        enumerable: false,
        configurable: true
    });
    DICorporateFiltersStore.prototype.editFilteres = function (filters) {
        var _this = this;
        filters.forEach(function (filter, key) { return _this.filters.set(key, filter); });
    };
    DICorporateFiltersStore.prototype.refreshFilteres = function () {
        this.filters = new Map();
    };
    __decorate([
        inject(TYPES.MappedStore),
        __metadata("design:type", MappedStore)
    ], DICorporateFiltersStore.prototype, "mappedStore", void 0);
    __decorate([
        observable,
        __metadata("design:type", Object)
    ], DICorporateFiltersStore.prototype, "filters", void 0);
    __decorate([
        computed,
        __metadata("design:type", Array),
        __metadata("design:paramtypes", [])
    ], DICorporateFiltersStore.prototype, "departmentsFiltered", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [Map]),
        __metadata("design:returntype", void 0)
    ], DICorporateFiltersStore.prototype, "editFilteres", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", []),
        __metadata("design:returntype", void 0)
    ], DICorporateFiltersStore.prototype, "refreshFilteres", null);
    DICorporateFiltersStore = __decorate([
        injectable()
    ], DICorporateFiltersStore);
    return DICorporateFiltersStore;
}());
export { DICorporateFiltersStore };
