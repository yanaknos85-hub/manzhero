import { WaypointModel } from '../../../models/geo/Waypoint.model';
import { TAddressExist } from '../../Address/Address.interface';
export declare class AddressModel extends WaypointModel implements TAddressExist {
    id: string;
    label?: string;
    constructor(address: TAddressExist);
}
