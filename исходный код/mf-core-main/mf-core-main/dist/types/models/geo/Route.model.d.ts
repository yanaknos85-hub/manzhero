import { RequestRoute, Segment } from './types';
import { WaypointModel } from './Waypoint.model';
export declare class RouteModel implements RequestRoute {
    distance: number;
    time: number;
    segments: Segment[];
    waypoints: WaypointModel[];
    cost?: number;
    constructor(route?: RequestRoute);
    getTimeString(): string;
    getDistanceString(): string;
    setCost(value: number): void;
}
