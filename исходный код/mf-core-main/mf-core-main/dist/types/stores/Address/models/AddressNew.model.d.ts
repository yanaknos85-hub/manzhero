import { WaypointModel } from '../../../models/geo/Waypoint.model';
import { TAddressNew } from '../../Address/Address.interface';
export declare class AddressNewModel extends WaypointModel implements TAddressNew {
    label: string;
    constructor(address?: TAddressNew);
}
