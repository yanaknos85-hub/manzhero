import type { IAddressStore } from '../Address/Address.interface';
import { AddressModel } from './models/Address.model';
import { AddressNewModel } from './models/AddressNew.model';
export declare class DIAddressStore implements IAddressStore {
    private service;
    private logger;
    private process;
    selfAddressList: AddressModel[];
    selfFrequentList: AddressModel[];
    selfFavoriteList: AddressModel[];
    isAddressChanged: boolean | undefined;
    get addressExistLabels(): string[];
    loadAddressList: () => Promise<void>;
    loadFavoriteList: () => Promise<void>;
    loadFrequentList: () => Promise<void>;
    addFavoriteAddress: (item: AddressNewModel) => Promise<void>;
    deleteFrequentAddress: (addressId: string) => Promise<void>;
    deleteFavoriteAddress: (addressId: string) => Promise<void>;
    checkLableUniqness(label: string): boolean;
    isUnique(item: AddressNewModel): boolean;
    private getFrequentList;
    private getAddressList;
    private getFavoriteList;
    private createFavoriteAddress;
    initStore(): void;
}
