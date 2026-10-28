import { plainToNew, getDistanceString, getTimeString } from '../../utils/utils';
import { WaypointModel } from './Waypoint.model';
var RouteModel = /** @class */ (function () {
    function RouteModel(route) {
        var _a;
        this.distance = 0;
        this.time = 0;
        this.segments = [];
        this.waypoints = [];
        if (route) {
            this.distance = route.distance;
            this.time = route.time;
            this.segments = route.segments;
            this.waypoints = (_a = plainToNew(WaypointModel, route.waypoints)) !== null && _a !== void 0 ? _a : [];
            this.cost = route.cost;
        }
    }
    RouteModel.prototype.getTimeString = function () {
        return getTimeString(this.time);
    };
    RouteModel.prototype.getDistanceString = function () {
        return getDistanceString(this.distance);
    };
    RouteModel.prototype.setCost = function (value) {
        this.cost = value;
    };
    return RouteModel;
}());
export { RouteModel };
