var WaypointModel = /** @class */ (function () {
    function WaypointModel(location) {
        var _a, _b, _c;
        this.latitude = 0;
        this.longitude = 0;
        this.waitTime = 0;
        this._addressString = '';
        this._regionData = '';
        if (location) {
            this.country = location.country;
            this.region = location.region;
            this.city = location.city;
            this.street = location.street;
            this.house = location.house;
            this.building = location.building;
            this.structure = location.structure;
            this.latitude = (_a = location.latitude) !== null && _a !== void 0 ? _a : 0;
            this.longitude = (_b = location.longitude) !== null && _b !== void 0 ? _b : 0;
            this.waitTime = (_c = location.waitTime) !== null && _c !== void 0 ? _c : 0;
            this.checkinAutomatic = location.checkinAutomatic;
            this.checkinManual = location.checkinManual;
            this.absenceReason = location.absenceReason;
        }
        this.addressString = this.buildAddressString();
    }
    Object.defineProperty(WaypointModel.prototype, "isValid", {
        get: function () {
            return !!this.latitude && !!this.longitude;
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(WaypointModel.prototype, "addressString", {
        get: function () {
            return this._addressString || this.buildAddressString();
        },
        set: function (val) {
            this._addressString = val !== null && val !== void 0 ? val : '';
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(WaypointModel.prototype, "waitTimeMinutes", {
        /**
         * @returns время ожидания в минутах
         */
        get: function () {
            return Math.trunc(this.waitTime / 1000 / 60);
        },
        /**
         * @param val: number - время ожидания в минутах
         */
        set: function (val) {
            this.waitTime = val * 60 * 1000;
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(WaypointModel.prototype, "regionData", {
        get: function () {
            return this._regionData || this.addressStructure();
        },
        enumerable: false,
        configurable: true
    });
    WaypointModel.prototype.buildAddressString = function () {
        return ("".concat(this.street ? "".concat(this.street, ", ") : '')
            + "".concat(this.house ? "".concat(this.house, ", ") : '')
            + "".concat(this.city ? "".concat(this.city, ",") : '')).slice(0, -1);
    };
    WaypointModel.prototype.addressStructure = function () {
        var _a, _b, _c, _d, _e;
        return {
            country: (_a = this.country) !== null && _a !== void 0 ? _a : '',
            region: (_b = this.region) !== null && _b !== void 0 ? _b : '',
            city: (_c = this.city) !== null && _c !== void 0 ? _c : '',
            street: (_d = this.street) !== null && _d !== void 0 ? _d : '',
            house: (_e = this.house) !== null && _e !== void 0 ? _e : '',
        };
    };
    return WaypointModel;
}());
export { WaypointModel };
