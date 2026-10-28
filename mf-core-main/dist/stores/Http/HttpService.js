var __extends = (this && this.__extends) || (function () {
    var extendStatics = function (d, b) {
        extendStatics = Object.setPrototypeOf ||
            ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
            function (d, b) { for (var p in b) if (Object.prototype.hasOwnProperty.call(b, p)) d[p] = b[p]; };
        return extendStatics(d, b);
    };
    return function (d, b) {
        if (typeof b !== "function" && b !== null)
            throw new TypeError("Class extends value " + String(b) + " is not a constructor or null");
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
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
import { Final } from '../../utils/decorators';
import { HttpMiddleware } from './HttpMiddleware';
var HttpService = /** @class */ (function (_super) {
    __extends(HttpService, _super);
    function HttpService() {
        return _super !== null && _super.apply(this, arguments) || this;
    }
    HttpService.prototype.get = function (url, params) {
        if (params === void 0) { params = {}; }
        return this.client.get(url, params);
    };
    HttpService.prototype.post = function (url, data, params) {
        if (data === void 0) { data = {}; }
        if (params === void 0) { params = {}; }
        return this.client.post(url, data, params);
    };
    HttpService.prototype.postFormData = function (url, data, params) {
        if (data === void 0) { data = {}; }
        if (params === void 0) { params = {}; }
        var paramsFormData = __assign(__assign({}, params), { headers: __assign(__assign({}, params.headers), (!(params.headers && 'Content-Type' in params.headers) && {
                'Content-Type': 'multipart/form-data; boundary="boundary"',
            })) });
        return this.client.post(url, data, paramsFormData);
    };
    HttpService.prototype.put = function (url, data, params) {
        if (data === void 0) { data = {}; }
        if (params === void 0) { params = {}; }
        return this.client.put(url, data, params);
    };
    HttpService.prototype.delete = function (url, params) {
        if (params === void 0) { params = {}; }
        return this.client.delete(url, params);
    };
    HttpService.prototype.patch = function (url, data, params) {
        if (data === void 0) { data = {}; }
        if (params === void 0) { params = {}; }
        return this.client.patch(url, data, params);
    };
    HttpService.prototype.request = function (config) {
        return this.client.request(config);
    };
    __decorate([
        Final,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String, Object]),
        __metadata("design:returntype", Promise)
    ], HttpService.prototype, "get", null);
    __decorate([
        Final,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String, Object, Object]),
        __metadata("design:returntype", Promise)
    ], HttpService.prototype, "post", null);
    __decorate([
        Final,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String, Object, Object]),
        __metadata("design:returntype", Promise)
    ], HttpService.prototype, "postFormData", null);
    __decorate([
        Final,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String, Object, Object]),
        __metadata("design:returntype", Promise)
    ], HttpService.prototype, "put", null);
    __decorate([
        Final,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String, Object]),
        __metadata("design:returntype", Promise)
    ], HttpService.prototype, "delete", null);
    __decorate([
        Final,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String, Object, Object]),
        __metadata("design:returntype", Promise)
    ], HttpService.prototype, "patch", null);
    __decorate([
        Final,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [Object]),
        __metadata("design:returntype", Promise)
    ], HttpService.prototype, "request", null);
    return HttpService;
}(HttpMiddleware));
export { HttpService };
