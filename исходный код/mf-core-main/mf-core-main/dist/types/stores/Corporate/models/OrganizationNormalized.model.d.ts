import { Dictionary } from 'lodash';
import { IOrganization, IShortDepartment, IShortPosition } from '../Corporate.interface';
export declare class OrganizationNormalizedModel {
    id: string;
    officialName: string;
    address: string;
    positions: Dictionary<IShortPosition>;
    departments: Dictionary<IShortDepartment>;
    constructor(org: IOrganization);
}
