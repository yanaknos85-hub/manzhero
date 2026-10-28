var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g;
    return g = { next: verb(0), "throw": verb(1), "return": verb(2) }, typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (g && (g = 0, op[0] && (_ = 0)), _) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
var __spreadArray = (this && this.__spreadArray) || function (to, from, pack) {
    if (pack || arguments.length === 2) for (var i = 0, l = from.length, ar; i < l; i++) {
        if (ar || !(i in from)) {
            if (!ar) ar = Array.prototype.slice.call(from, 0, i);
            ar[i] = from[i];
        }
    }
    return to.concat(ar || Array.prototype.slice.call(from));
};
import { inject, injectable } from 'inversify';
import debounce from 'lodash/debounce';
import mapKeys from 'lodash/mapKeys';
import { action, observable } from 'mobx';
import { TYPES } from '../../ioc/ioc.types';
import { AddressModel } from '../Address/models/Address.model';
import { RouteModel } from '../../models/geo/Route.model';
import { WaypointModel } from '../../models/geo/Waypoint.model';
import { plainToNew } from '../../utils/utils';
// Moscow coordinates
var defaultMapCoords = [55.752693, 37.617423];
var DIGeoStore = /** @class */ (function () {
    function DIGeoStore() {
        var _this = this;
        this.currentCoordinates = defaultMapCoords;
        this.addressAutocompleteList = [];
        this.waypoints = [new WaypointModel(), new WaypointModel()];
        this.calculatedRoute = undefined;
        this.searchLocation = debounce(function (location, centerCoordinates) { return __awaiter(_this, void 0, void 0, function () {
            var _a, _b;
            return __generator(this, function (_c) {
                switch (_c.label) {
                    case 0:
                        _a = this;
                        _b = observable;
                        return [4 /*yield*/, this.getCoordinateByAddress(location, centerCoordinates)];
                    case 1:
                        _a.addressAutocompleteList = _b.apply(void 0, [_c.sent()]);
                        return [2 /*return*/];
                }
            });
        }); }, 1500);
        this.calcRoute = debounce(function () { return __awaiter(_this, void 0, void 0, function () {
            var data;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0:
                        if (!this.waypoints.every(function (x) { return x.isValid; })) return [3 /*break*/, 2];
                        return [4 /*yield*/, this.service.calcRoute(this.waypoints)];
                    case 1:
                        data = _a.sent();
                        this.calculatedRoute = new RouteModel(data);
                        return [3 /*break*/, 3];
                    case 2:
                        this.clearRoute();
                        _a.label = 3;
                    case 3: return [2 /*return*/];
                }
            });
        }); }, 1000);
    }
    DIGeoStore.prototype.setCurrentAddressByCoordinates = function (coordinates) {
        return __awaiter(this, void 0, void 0, function () {
            var point;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0:
                        this.currentCoordinates = coordinates;
                        return [4 /*yield*/, this.getAddressByCoordinates(coordinates)];
                    case 1:
                        point = _a.sent();
                        this.editWaypoints(0, point[0]);
                        this.calcRoute();
                        return [2 /*return*/];
                }
            });
        });
    };
    DIGeoStore.prototype.setWaypointFromAddress = function (address, currentInputNumber) {
        this.editWaypoints(currentInputNumber, address);
        this.calcRoute();
    };
    DIGeoStore.prototype.editWaypointAddress = function (index, value, centerCoordinates) {
        this.waypoints[index].addressString = value;
        if (!value) {
            this.clearAutocompleteList();
            this.editWaypoints(index, new WaypointModel());
            this.calcRoute();
        }
        if ((value === null || value === void 0 ? void 0 : value.length) > 3 && !this.addressAutocompleteList.some(function (x) { return x.addressString === value; })) {
            this.searchLocation(value, centerCoordinates);
        }
    };
    DIGeoStore.prototype.editWaypointWaitTime = function (index, time) {
        this.waypoints[index].waitTimeMinutes = Number(time);
        this.calcRoute();
    };
    DIGeoStore.prototype.editSingleAddress = function (value, centerCoordinates) {
        if (value.length > 3 && !this.addressAutocompleteList.some(function (x) { return x.addressString === value; })) {
            this.clearAutocompleteList();
            this.searchLocation(value, centerCoordinates);
        }
    };
    DIGeoStore.prototype.onAddressBySortCoordinates = function (coordinates) {
        var _a;
        return __awaiter(this, void 0, void 0, function () {
            var data;
            return __generator(this, function (_b) {
                switch (_b.label) {
                    case 0: return [4 /*yield*/, this.service.getAddressBySortCoordinates(coordinates)];
                    case 1:
                        data = _b.sent();
                        return [2 /*return*/, (_a = plainToNew(WaypointModel, data)) !== null && _a !== void 0 ? _a : []];
                }
            });
        });
    };
    DIGeoStore.prototype.onAddressSelect = function (value, _, index, dontClear) {
        return __awaiter(this, void 0, void 0, function () {
            var point;
            return __generator(this, function (_a) {
                point = this.addressAutocompleteList.find(function (x) { return x.addressString === value; });
                if (point) {
                    this.editWaypoints(index, point);
                }
                if (!dontClear) {
                    this.clearAutocompleteList();
                }
                this.calcRoute();
                return [2 /*return*/];
            });
        });
    };
    DIGeoStore.prototype.addWaypoint = function () {
        this._addWaypoint();
        this.clearRoute();
    };
    DIGeoStore.prototype.removeWaypoint = function (index) {
        this._removeWaypoint(index);
        this.calcRoute();
    };
    DIGeoStore.prototype.clearCurrentState = function () {
        this.currentCoordinates = defaultMapCoords;
        this.clearAutocompleteList();
        this.waypoints = [new WaypointModel(), new WaypointModel()];
        this.clearRoute();
    };
    DIGeoStore.prototype.getAddressByCoordinates = function (coordinates) {
        var _a;
        return __awaiter(this, void 0, void 0, function () {
            var data;
            return __generator(this, function (_b) {
                switch (_b.label) {
                    case 0: return [4 /*yield*/, this.service.getAddressByCoordinates(coordinates)];
                    case 1:
                        data = _b.sent();
                        return [2 /*return*/, (_a = plainToNew(WaypointModel, data)) !== null && _a !== void 0 ? _a : []];
                }
            });
        });
    };
    DIGeoStore.prototype.getCoordinateByAddress = function (location, centerCoordinates) {
        var _a;
        return __awaiter(this, void 0, void 0, function () {
            var data, models, noBlank;
            return __generator(this, function (_b) {
                switch (_b.label) {
                    case 0: return [4 /*yield*/, this.service.getCoordinateByAddress(location, centerCoordinates)];
                    case 1:
                        data = _b.sent();
                        models = (_a = plainToNew(WaypointModel, data)) !== null && _a !== void 0 ? _a : [];
                        noBlank = models.filter(function (x) { return x.addressString !== ''; });
                        return [2 /*return*/, Object.values(mapKeys(noBlank, 'addressString'))]; // removes duplicates
                }
            });
        });
    };
    DIGeoStore.prototype.editWaypoints = function (index, value) {
        this.waypoints[index] = value;
        this.waypoints = __spreadArray([], this.waypoints.slice(), true);
    };
    DIGeoStore.prototype.clearAutocompleteList = function () {
        this.addressAutocompleteList = [];
    };
    DIGeoStore.prototype.clearRoute = function () {
        this.calculatedRoute = undefined;
    };
    DIGeoStore.prototype._addWaypoint = function () {
        this.waypoints = __spreadArray(__spreadArray([], this.waypoints, true), [new WaypointModel()], false);
    };
    DIGeoStore.prototype._removeWaypoint = function (index) {
        var array = __spreadArray([], this.waypoints.filter(function (x, idx) { return idx !== index; }), true);
        if (array.length === 2) {
            array[1].waitTimeMinutes = 0;
        }
        this.waypoints = array;
    };
    __decorate([
        inject(TYPES.IGeoService),
        __metadata("design:type", Object)
    ], DIGeoStore.prototype, "service", void 0);
    __decorate([
        observable,
        __metadata("design:type", Array)
    ], DIGeoStore.prototype, "currentCoordinates", void 0);
    __decorate([
        observable,
        __metadata("design:type", Array)
    ], DIGeoStore.prototype, "addressAutocompleteList", void 0);
    __decorate([
        observable,
        __metadata("design:type", Array)
    ], DIGeoStore.prototype, "waypoints", void 0);
    __decorate([
        observable,
        __metadata("design:type", Object)
    ], DIGeoStore.prototype, "calculatedRoute", void 0);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [Array]),
        __metadata("design:returntype", Promise)
    ], DIGeoStore.prototype, "setCurrentAddressByCoordinates", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [AddressModel, Number]),
        __metadata("design:returntype", void 0)
    ], DIGeoStore.prototype, "setWaypointFromAddress", null);
    __decorate([
        action.bound,
        __metadata("design:type", Object)
    ], DIGeoStore.prototype, "searchLocation", void 0);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [Number, String, Array]),
        __metadata("design:returntype", void 0)
    ], DIGeoStore.prototype, "editWaypointAddress", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [Number, String]),
        __metadata("design:returntype", void 0)
    ], DIGeoStore.prototype, "editWaypointWaitTime", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String, Array]),
        __metadata("design:returntype", void 0)
    ], DIGeoStore.prototype, "editSingleAddress", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [Array]),
        __metadata("design:returntype", Promise)
    ], DIGeoStore.prototype, "onAddressBySortCoordinates", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [String, Object, Number, Boolean]),
        __metadata("design:returntype", Promise)
    ], DIGeoStore.prototype, "onAddressSelect", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", []),
        __metadata("design:returntype", void 0)
    ], DIGeoStore.prototype, "addWaypoint", null);
    __decorate([
        action.bound,
        __metadata("design:type", Function),
        __metadata("design:paramtypes", [Number]),
        __metadata("design:returntype", void 0)
    ], DIGeoStore.prototype, "removeWaypoint", null);
    DIGeoStore = __decorate([
        injectable()
    ], DIGeoStore);
    return DIGeoStore;
}());
export { DIGeoStore };
