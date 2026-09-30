import * as t from 'io-ts';
export type LatLngTuple = [number, number];
export declare const IOWaypoint: t.IntersectionC<[t.TypeC<{}>, t.PartialC<{
    latitude: t.NumberC;
    longitude: t.NumberC;
    country: t.StringC;
    region: t.StringC;
    city: t.StringC;
    street: t.StringC;
    house: t.StringC;
    building: t.StringC;
    structure: t.StringC;
    waitTime: t.NumberC;
    checkinAutomatic: t.BooleanC;
    checkinManual: t.BooleanC;
    absenceReason: t.StringC;
    icon: t.StringC;
}>]>;
export type TWaypoint = t.TypeOf<typeof IOWaypoint>;
declare const IOWaypointField: t.IntersectionC<[t.IntersectionC<[t.TypeC<{}>, t.PartialC<{
    latitude: t.NumberC;
    longitude: t.NumberC;
    country: t.StringC;
    region: t.StringC;
    city: t.StringC;
    street: t.StringC;
    house: t.StringC;
    building: t.StringC;
    structure: t.StringC;
    waitTime: t.NumberC;
    checkinAutomatic: t.BooleanC;
    checkinManual: t.BooleanC;
    absenceReason: t.StringC;
    icon: t.StringC;
}>]>, t.TypeC<{
    fieldName: t.StringC;
    addressString: t.StringC;
    waitingTimeString: t.StringC;
    isValid: t.BooleanC;
}>]>;
export type WaypointField = t.TypeOf<typeof IOWaypointField>;
export declare const IOCoordinates: t.PartialC<{
    latitude: t.NumberC;
    longitude: t.NumberC;
}>;
export type TCoordinates = t.TypeOf<typeof IOCoordinates>;
export declare const Segment: t.TypeC<{
    distance: t.NumberC;
    time: t.NumberC;
    coordinates: t.ArrayC<t.PartialC<{
        latitude: t.NumberC;
        longitude: t.NumberC;
    }>>;
}>;
export type Segment = t.TypeOf<typeof Segment>;
export declare const RequestRoute: t.IntersectionC<[t.TypeC<{
    distance: t.NumberC;
    time: t.NumberC;
    segments: t.ArrayC<t.TypeC<{
        distance: t.NumberC;
        time: t.NumberC;
        coordinates: t.ArrayC<t.PartialC<{
            latitude: t.NumberC;
            longitude: t.NumberC;
        }>>;
    }>>;
    waypoints: t.ArrayC<t.IntersectionC<[t.TypeC<{}>, t.PartialC<{
        latitude: t.NumberC;
        longitude: t.NumberC;
        country: t.StringC;
        region: t.StringC;
        city: t.StringC;
        street: t.StringC;
        house: t.StringC;
        building: t.StringC;
        structure: t.StringC;
        waitTime: t.NumberC;
        checkinAutomatic: t.BooleanC;
        checkinManual: t.BooleanC;
        absenceReason: t.StringC;
        icon: t.StringC;
    }>]>>;
}>, t.PartialC<{
    cost: t.NumberC;
}>]>;
export type RequestRoute = t.TypeOf<typeof RequestRoute>;
export declare const IOGeoZone: t.PartialC<{
    country: t.StringC;
    region: t.StringC;
    city: t.StringC;
    street: t.StringC;
    house: t.StringC;
}>;
export type TGeoZone = t.TypeOf<typeof IOGeoZone>;
export declare const GeoZoneInfo: t.TypeC<{
    id: t.StringC;
    name: t.StringC;
    code: t.NumberC;
    parentId: t.NumberC;
}>;
export type GeoZoneInfo = t.TypeOf<typeof GeoZoneInfo>;
export {};
