var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
import { observable } from 'mobx';
import moment from 'moment';
import { isMomentTuple } from '../utils/types';
var RequestBaseModel = /** @class */ (function () {
    function RequestBaseModel(request) {
        var _this = this;
        this.inDateRange = function (range) {
            if (isMomentTuple(range)) {
                return moment(_this.creationTime).isBetween(range[0].startOf('day'), range[1].endOf('day'), undefined, '[)');
            }
            return false;
        };
        this.id = request.id;
        this.humanReadableId = request.humanReadableId;
        this.status = request.status;
        this.creationTime = request.creationTime;
        this.approvalState = request.approvalState;
        this.authorId = request.authorId;
    }
    Object.defineProperty(RequestBaseModel.prototype, "isExisting", {
        get: function () {
            return !!this.id;
        },
        enumerable: false,
        configurable: true
    });
    RequestBaseModel.prototype.isMatched = function (statuses) {
        return !!this.status && !!(statuses === null || statuses === void 0 ? void 0 : statuses.includes(this.status));
    };
    __decorate([
        observable,
        __metadata("design:type", String)
    ], RequestBaseModel.prototype, "status", void 0);
    return RequestBaseModel;
}());
export { RequestBaseModel };
