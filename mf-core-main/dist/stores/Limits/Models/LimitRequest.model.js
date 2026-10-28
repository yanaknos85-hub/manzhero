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
import { RequestBaseModel } from '../../../models/RequestBase.model';
import { LimitRequestStatusesCancellable, LimitRequestStatusesFinal } from '../LimitsRequest.interface';
var LimitRequestModel = /** @class */ (function (_super) {
    __extends(LimitRequestModel, _super);
    function LimitRequestModel(request) {
        var _this = _super.call(this, request) || this;
        _this.sum = request.sum;
        _this.month = request.month;
        _this.transportType = request.transportType;
        return _this;
    }
    Object.defineProperty(LimitRequestModel.prototype, "isCancellable", {
        get: function () {
            return !!this.status && LimitRequestStatusesCancellable.includes(this.status);
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(LimitRequestModel.prototype, "isFinal", {
        get: function () {
            return !!this.status && LimitRequestStatusesFinal.includes(this.status);
        },
        enumerable: false,
        configurable: true
    });
    return LimitRequestModel;
}(RequestBaseModel));
export { LimitRequestModel };
