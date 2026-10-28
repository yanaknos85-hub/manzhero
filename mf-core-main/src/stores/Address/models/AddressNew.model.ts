import { WaypointModel } from '../../../models/geo/Waypoint.model';

import { TAddressNew } from '../../Address/Address.interface';

export class AddressNewModel extends WaypointModel implements TAddressNew {
  label = '';

  constructor(address?: TAddressNew) {
    super(address);
    this.label = address?.label ?? '';
  }
}
