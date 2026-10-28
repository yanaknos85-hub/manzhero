var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
import { injectable } from 'inversify';
import { action, observable } from 'mobx';
import { useHistory } from '../../hooks/useHistory';
var ENV_CONFIG_FILE_PATH = '/env.json';
var defaultEnvConfig = {
    IS_SDO: false,
    DRIVER_APPS_URL: 'https://apps.sbertransport.ru',
    IS_PERSONAL_DEVICE: false,
};
var DIConfigStore = /** @class */ (function () {
    function DIConfigStore() {
        var _this = this;
        this.isBasicAuth = false;
        this.isMockedAuth = false;
        this.isMockedApi = false;
        this.isCorp = false;
        this.history = useHistory();
        this.env = {};
        this.setConfig = function (config) {
            _this.isBasicAuth = config.isBasicAuth || _this.isBasicAuth;
            _this.isMockedAuth = config.isMockedAuth || _this.isMockedAuth;
            _this.isMockedApi = config.isMockedApi || _this.isMockedApi;
            _this.history = config.history || _this.history;
            _this.isCorp = config.isCorp || _this.isCorp;
        };
        this.fillEnvConfig();
    }
    DIConfigStore.prototype.fillEnvConfig = function () {
        fetch(ENV_CONFIG_FILE_PATH)
            .then(function (response) { return response.json(); })
            .then(this.parseEnv.bind(this))
            .catch(this.setDefaultConfig.bind(this));
    };
    DIConfigStore.prototype.parseEnv = function (response) {
        this.env = response;
    };
    DIConfigStore.prototype.setDefaultConfig = function () {
        console.info('Не удалось загрузить env.json, установлен дефолтный конфиг');
        this.env = defaultEnvConfig;
    };
    __decorate([
        observable,
        __metadata("design:type", Object)
    ], DIConfigStore.prototype, "isBasicAuth", void 0);
    __decorate([
        observable,
        __metadata("design:type", Object)
    ], DIConfigStore.prototype, "isMockedAuth", void 0);
    __decorate([
        observable,
        __metadata("design:type", Object)
    ], DIConfigStore.prototype, "isMockedApi", void 0);
    __decorate([
        observable,
        __metadata("design:type", Object)
    ], DIConfigStore.prototype, "isCorp", void 0);
    __decorate([
        observable,
        __metadata("design:type", Object)
    ], DIConfigStore.prototype, "history", void 0);
    __decorate([
        observable,
        __metadata("design:type", Object)
    ], DIConfigStore.prototype, "env", void 0);
    __decorate([
        action,
        __metadata("design:type", Object)
    ], DIConfigStore.prototype, "setConfig", void 0);
    DIConfigStore = __decorate([
        injectable(),
        __metadata("design:paramtypes", [])
    ], DIConfigStore);
    return DIConfigStore;
}());
export { DIConfigStore };
