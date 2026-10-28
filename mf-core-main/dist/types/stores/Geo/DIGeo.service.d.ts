import { LatLngTuple, RequestRoute, TWaypoint } from '../../models/geo/types';
import { WaypointModel } from '../../models/geo/Waypoint.model';
import { IGeoService } from './Geo.interface';
export declare class DIGeoService implements IGeoService {
    private http;
    private process;
    getAddressByCoordinates(coordinates: LatLngTuple): Promise<TWaypoint[]>;
    getAddressBySortCoordinates(coordinates: LatLngTuple): Promise<TWaypoint[]>;
    getCoordinateByAddress(location: string, centerCoordinates?: LatLngTuple): Promise<TWaypoint[]>;
    calcRoute(route: WaypointModel[]): Promise<RequestRoute>;
}
