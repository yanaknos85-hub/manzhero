import type { ITransportType, ITransportTypesStore, TransportType } from './TransportTypes.interface';
import { TransportTypesModel } from './TransportTypes.interface';
export declare class DITransportTypesStore implements ITransportTypesStore {
    private service;
    private selfStore;
    transportTypes: TransportTypesModel[];
    activeTransportType?: TransportType;
    availableTransportTypes: ITransportType[];
    availableTransportTypesByService: ITransportType[];
    get rusNamesByTransportType(): Record<string, string>;
    setActiveTransportType(type: TransportType): void;
    clearActiveTransportType(): void;
    getTransportTypes(): Promise<void>;
    getAvailableTransportTypes(): Promise<void>;
    getAvailableTransportTypesByService(serviceType: string): Promise<void>;
    initStore(): void;
}
