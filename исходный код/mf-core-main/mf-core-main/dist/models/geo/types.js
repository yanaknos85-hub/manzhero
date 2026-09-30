import * as t from 'io-ts';
export var IOWaypoint = t.intersection([
    t.type({}),
    t.partial({
        latitude: t.number,
        longitude: t.number,
        country: t.string,
        region: t.string,
        city: t.string,
        street: t.string,
        house: t.string,
        building: t.string,
        structure: t.string,
        waitTime: t.number,
        checkinAutomatic: t.boolean,
        checkinManual: t.boolean,
        absenceReason: t.string,
        icon: t.string,
    }),
]);
var IOWaypointField = t.intersection([
    IOWaypoint,
    t.type({
        fieldName: t.string,
        addressString: t.string,
        waitingTimeString: t.string,
        isValid: t.boolean,
    }),
]);
export var IOCoordinates = t.partial({
    latitude: t.number,
    longitude: t.number,
});
export var Segment = t.type({
    distance: t.number,
    time: t.number,
    coordinates: t.array(IOCoordinates),
});
export var RequestRoute = t.intersection([
    t.type({
        distance: t.number,
        time: t.number,
        segments: t.array(Segment),
        waypoints: t.array(IOWaypoint),
    }),
    t.partial({
        cost: t.number,
    }),
]);
export var IOGeoZone = t.partial({
    country: t.string,
    region: t.string,
    city: t.string,
    street: t.string,
    house: t.string,
});
export var GeoZoneInfo = t.type({
    id: t.string,
    name: t.string,
    code: t.number,
    parentId: t.number,
});
