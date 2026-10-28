var LimitModel = /** @class */ (function () {
    function LimitModel(limit) {
        this.id = limit.id;
        this.humanReadableId = limit.humanReadableId;
        this.limitOwner = limit.limitOwner;
        this.year = limit.year;
        this.sum = limit.sum;
        this.reserve = limit.reserve;
        this.limitType = limit.limitType;
        this.limitStatus = limit.limitStatus;
        this.limitSharingType = limit.limitSharingType;
        this.limitServiceType = limit.limitServiceType;
        this.finalSharing = limit.finalSharing;
        this.useThisLimit = limit.useThisLimit;
        this.department = limit.department;
        this.parentLimitId = limit.parentLimitId;
    }
    Object.defineProperty(LimitModel.prototype, "availablePersentage", {
        get: function () {
            return Number(((this.sum * 100) / this.sum).toFixed(1));
        },
        enumerable: false,
        configurable: true
    });
    return LimitModel;
}());
export { LimitModel };
