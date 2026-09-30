import { IAddressService, TAddressExist, TAddressNew } from './Address.interface';
export declare class DIAddressService implements IAddressService {
    private http;
    private process;
    getAddressList(): Promise<TAddressExist[]>;
    getFrequentList(): Promise<TAddressExist[]>;
    getFavoriteList(): Promise<TAddressExist[]>;
    createFavoriteAddress(address: TAddressNew): Promise<TAddressExist>;
    getFavoriteAddress(addressId: string): Promise<TAddressExist>;
    updateFavoriteAddress(address: TAddressExist): Promise<number>;
    deleteFavoriteAddress(addressId: string): Promise<number>;
    deleteFrequentAddress(addressId: string): Promise<number>;
}
