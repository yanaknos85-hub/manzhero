import { inject, injectable } from 'inversify';
import type { IHttpService } from '../Http/http.interface';
import type { IResponseService } from '../Http/Response.service';

import {
  CALC_ROUTE,
  GET_ADDRESS_BY_COORDINATES,
  GET_COORDINATES_BY_ADDRESS
} from '../../constants/constants';

import { TYPES } from '../../ioc/ioc.types';

import { RouteModel } from '../../models/geo/Route.model';
import { LatLngTuple, RequestRoute, TWaypoint } from '../../models/geo/types';
import { WaypointModel } from '../../models/geo/Waypoint.model';

import { IGeoService } from './Geo.interface';

@injectable()
export class DIGeoService implements IGeoService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  async getAddressByCoordinates(coordinates: LatLngTuple): Promise<TWaypoint[]> {
    const latitude = coordinates[0].toFixed(6);
    const longitude = coordinates[1].toFixed(6);

    return this.http
      .get<WaypointModel[]>(`${GET_ADDRESS_BY_COORDINATES}`, { params: { latitude, longitude } })
      .then(this.process.getResponseData);
  }

  async getAddressBySortCoordinates(coordinates: LatLngTuple): Promise<TWaypoint[]> {
    const latitude = coordinates[0].toFixed(6);
    const longitude = coordinates[1].toFixed(6);

    return this.http
      .get<WaypointModel[]>(`${GET_ADDRESS_BY_COORDINATES}`, {
        params: {
          sortLatitude: latitude,
          sortLongitude: longitude,
        },
      })
      .then(this.process.getResponseData);
  }

  async getCoordinateByAddress(location: string, centerCoordinates?: LatLngTuple): Promise<TWaypoint[]> {
    return this.http
      .get<WaypointModel[]>(`${GET_COORDINATES_BY_ADDRESS}`, {
        params: {
          location,
          centerLatitude: centerCoordinates?.[0],
          centerLongitude: centerCoordinates?.[1],
        },
      })
      .then(this.process.getResponseData);
  }

  async calcRoute(route: WaypointModel[]): Promise<RequestRoute> {
    return this.http
      .post<RouteModel>(`${CALC_ROUTE}`, { coordinates: route })
      .then(x => this.process.getResponseData(x, RequestRoute));
  }
}
