import { ITransportType, ITransportTypesService } from './TransportTypes.interface';
export declare class DITransportTypesService implements ITransportTypesService {
    private http;
    private process;
    getTransportTypes(): Promise<ITransportType[]>;
    getAvailableTransportTypes(orgId: string): Promise<ITransportType[]>;
    getAvailableTransportTypesByService(serviceType: string, orgId: string): Promise<ITransportType[]>;
}
