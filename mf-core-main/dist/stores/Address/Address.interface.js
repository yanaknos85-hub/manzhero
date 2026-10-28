import * as t from 'io-ts';
import { IOWaypoint } from '../../models/geo/types';
export var IOAddressNew = t.intersection([
    t.partial({
        label: t.string,
    }),
    IOWaypoint,
]);
export var IOAddressExist = t.intersection([t.type({ id: t.string }), t.partial({ usages: t.number }), IOAddressNew]);
