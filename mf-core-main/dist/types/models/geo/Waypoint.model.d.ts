import { TWaypoint } from './types';
export declare class WaypointModel implements TWaypoint {
    country?: string;
    region?: string;
    city?: string;
    street?: string;
    house?: string;
    building?: string;
    structure?: string;
    latitude: number;
    longitude: number;
    waitTime: number;
    checkinAutomatic?: boolean;
    checkinManual?: boolean;
    absenceReason?: string;
    icon?: string;
    private _addressString;
    private _regionData;
    get isValid(): boolean;
    get addressString(): string;
    set addressString(val: string);
    /**
     * @returns время ожидания в минутах
     */
    get waitTimeMinutes(): number;
    /**
     * @param val: number - время ожидания в минутах
     */
    set waitTimeMinutes(val: number);
    get regionData(): string | {
        country?: string;
        region?: string;
        city?: string;
        street?: string;
        house?: string;
    };
    constructor(location?: TWaypoint);
    buildAddressString(): string;
    addressStructure(): {
        country?: string;
        region?: string;
        city?: string;
        street?: string;
        house?: string;
    };
}
