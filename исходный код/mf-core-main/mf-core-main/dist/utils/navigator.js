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
import { computed } from 'mobx';
import { clear as clearStorage, getSudirReAuthAmt, removeRefreshToken, removeSudirReAuthAmt, setSudirReAuthAmt } from './storage';
import { TYPES } from '../ioc/ioc.types';
var Navigator = /** @class */ (function () {
    function Navigator() {
        var _this = this;
        this.navRoot = function () {
            removeRefreshToken();
            if (_this.history) {
                _this.history.push('/');
            }
        };
        this.navLogout = function () {
            if (_this.history) {
                _this.history.push('/oauth/logout');
            }
        };
        this.navSudirReAuth = function (_a) {
            var reAuth = _a.reAuth, fail = _a.fail;
            var locationContext = window.location;
            var reAuthAmt = getSudirReAuthAmt();
            clearStorage();
            if (reAuthAmt < 3) {
                setSudirReAuthAmt(reAuthAmt + 1);
                locationContext.assign(reAuth);
            }
            else {
                removeSudirReAuthAmt();
                locationContext.assign(fail);
            }
        };
    }
    Object.defineProperty(Navigator.prototype, "history", {
        get: function () {
            return this.configStore.history || null;
        },
        enumerable: false,
        configurable: true
    });
    __decorate([
        inject(TYPES.IConfigStore),
        __metadata("design:type", Object)
    ], Navigator.prototype, "configStore", void 0);
    __decorate([
        computed,
        __metadata("design:type", Object),
        __metadata("design:paramtypes", [])
    ], Navigator.prototype, "history", null);
    Navigator = __decorate([
        injectable()
    ], Navigator);
    return Navigator;
}());
export { Navigator };
