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
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
/* eslint-disable no-console */
import { injectable } from 'inversify';
import { CommonLogger } from './CommonLogger';
var ProductionDebugLogger = /** @class */ (function (_super) {
    __extends(ProductionDebugLogger, _super);
    function ProductionDebugLogger() {
        var _this = _super !== null && _super.apply(this, arguments) || this;
        _this.toError = function (description, title) {
            var args = [];
            for (var _i = 2; _i < arguments.length; _i++) {
                args[_i - 2] = arguments[_i];
            }
            console.group(title);
            console.error(description);
            if (args.length > 0) {
                args.forEach(function (x) { return console.info(x); });
            }
            console.groupEnd();
        };
        return _this;
    }
    ProductionDebugLogger = __decorate([
        injectable()
    ], ProductionDebugLogger);
    return ProductionDebugLogger;
}(CommonLogger));
export { ProductionDebugLogger };
