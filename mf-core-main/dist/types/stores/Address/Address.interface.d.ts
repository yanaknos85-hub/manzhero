import * as t from 'io-ts';
import { AddressModel } from './models/Address.model';
export interface IAddressStore {
    selfAddressList: AddressModel[];
    selfFrequentList: AddressModel[];
    selfFavoriteList: AddressModel[];
    isAddressChanged: boolean | undefined;
    loadAddressList(): Promise<void>;
    loadFavoriteList(): Promise<void>;
    loadFrequentList(): Promise<void>;
    addFavoriteAddress(item: TAddressNew): Promise<void>;
    deleteFavoriteAddress(addressId: string): Promise<void>;
    deleteFrequentAddress(addressId: string): Promise<void>;
    isUnique(item: TAddressNew): boolean;
    initStore(): void;
}
export interface IAddressService {
    getAddressList(): Promise<TAddressExist[]>;
    getFrequentList(): Promise<TAddressExist[]>;
    getFavoriteList(): Promise<TAddressExist[]>;
    createFavoriteAddress(address: TAddressNew): Promise<TAddressExist>;
    getFavoriteAddress(addressId: string): Promise<TAddressExist>;
    updateFavoriteAddress(address: TAddressExist): Promise<number>;
    deleteFavoriteAddress(addressId: string): Promise<number>;
    deleteFrequentAddress(addressId: string): Promise<number>;
}
export declare const IOAddressNew: t.IntersectionC<[t.PartialC<{
    label: t.StringC;
}>, t.IntersectionC<[t.TypeC<{}>, t.PartialC<{
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
}>]>]>;
export declare const IOAddressExist: t.IntersectionC<[t.TypeC<{
    id: t.StringC;
}>, t.PartialC<{
    usages: t.NumberC;
}>, t.IntersectionC<[t.PartialC<{
    label: t.StringC;
}>, t.IntersectionC<[t.TypeC<{}>, t.PartialC<{
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
}>]>]>]>;
export type TAddressNew = t.TypeOf<typeof IOAddressNew>;
export type TAddressExist = t.TypeOf<typeof IOAddressExist>;
