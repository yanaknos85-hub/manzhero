import { isMoment } from 'moment';
export function isMomentTuple(tuple) {
    return Array.isArray(tuple) && Boolean(tuple.length === 2) && isMoment(tuple[0]) && isMoment(tuple[1]);
}
