export interface ITransportTypesStore {
    transportTypes: TransportTypesModel[];
    activeTransportType?: TransportType;
    availableTransportTypes: TransportTypesModel[];
    availableTransportTypesByService: TransportTypesModel[];
    setActiveTransportType(type: TransportType): void;
    rusNamesByTransportType: Record<string, string>;
    clearActiveTransportType(): void;
    getAvailableTransportTypes(): void;
    getAvailableTransportTypesByService(serviceType: string): void;
    initStore(): void;
}
export interface ITransportTypesService {
    getTransportTypes(): Promise<ITransportType[]>;
    getAvailableTransportTypes(orgId: string): Promise<ITransportType[]>;
    getAvailableTransportTypesByService(serviceType: string, orgId: string): Promise<ITransportType[]>;
}
export interface ITransportType {
    id: string;
    name: string;
    rusName: string;
}
export declare enum TransportTypeEnum {
    TAXI = "TAXI",
    PUBLIC = "PUBLIC",
    PERSONAL = "PERSONAL",
    CARSHARING = "CARSHARING",
    BICYCLE = "BICYCLE",
    WALK = "WALK",
    SCOOTER = "SCOOTER",
    DEDICATED = "DEDICATED",
    COURIER = "COURIER",
    INTERREGIONAL = "INTERREGIONAL"
}
export declare enum TransportTypeTitlesEnum {
    TAXI = "\u0422\u0430\u043A\u0441\u0438",
    PUBLIC = "\u041E\u0431\u0449\u0435\u0441\u0442\u0432\u0435\u043D\u043D\u044B\u0439",
    PERSONAL = "\u041B\u0438\u0447\u043D\u044B\u0439",
    CARSHARING = "\u041A\u0430\u0440\u0448\u0435\u0440\u0438\u043D\u0433",
    BICYCLE = "\u0412\u0435\u043B\u043E\u0441\u0438\u043F\u0435\u0434",
    WALK = "\u041F\u0435\u0448\u043A\u043E\u043C",
    SCOOTER = "\u0421\u0430\u043C\u043E\u043A\u0430\u0442",
    DEDICATED = "\u0414\u043E\u0441\u0442\u0430\u0432\u043A\u0430",
    COURIER = "\u041A\u0443\u0440\u044C\u0435\u0440",
    INTERREGIONAL = "\u041C\u0435\u0436\u0440\u0435\u0433\u0438\u043E\u043D\u0430\u043B\u044C\u043D\u0430\u044F"
}
export declare enum TransportCompensations {
    CITY_TRIP_COMPENSATION = "CITY_TRIP_COMPENSATION",
    SUBURB_TRIP_COMPENSATION = "SUBURB_TRIP_COMPENSATION",
    TRAVEL_CARD_COMPENSATION = "TRAVEL_CARD_COMPENSATION"
}
export declare enum TransportCompensationsTitles {
    CITY_TRIP_COMPENSATION = "\u041A\u043E\u043C\u043F\u0435\u043D\u0441\u0430\u0446\u0438\u044F \u043F\u043E\u0435\u0437\u0434\u043A\u0438 \u043F\u043E \u0433\u043E\u0440\u043E\u0434\u0443",
    SUBURB_TRIP_COMPENSATION = "\u041A\u043E\u043C\u043F\u0435\u043D\u0441\u0430\u0446\u0438\u044F \u043C\u0435\u0436\u0434\u0443\u0433\u043E\u0440\u043E\u0434\u043D\u0438\u0445 \u043F\u043E\u0435\u0437\u0434\u043E\u043A",
    TRAVEL_CARD_COMPENSATION = "\u041A\u043E\u043C\u043F\u0435\u043D\u0441\u0430\u0446\u0438\u044F \u043F\u0440\u043E\u0435\u0437\u0434\u043D\u043E\u0433\u043E \u0434\u043E\u043A\u0443\u043C\u0435\u043D\u0442\u0430"
}
export declare enum PublicTransportTypeEnum {
    METRO = "METRO",
    TRAM = "TRAM",
    TROLLEYBUS = "TROLLEYBUS",
    BUS = "BUS",
    CITY_BUS = "CITY_BUS",
    CITY_TRAIN = "CITY_TRAIN",
    CITY_TROLLEYBUS = "CITY_TROLLEYBUS",
    CITY_TRAM = "CITY_TRAM",
    CITY_METRO = "CITY_METRO",
    SUBURB_BUS = "SUBURB_BUS",
    SUBURB_TRAIN = "SUBURB_TRAIN",
    SUBURB_FERRY_CROSSING = "SUBURB_FERRY_CROSSING",
    SUBURB_TROLLEYBUS = "SUBURB_TROLLEYBUS",
    TRAVEL_CARD_BUS = "TRAVEL_CARD_BUS",
    TRAVEL_CARD_TRAIN = "TRAVEL_CARD_TRAIN",
    TRAVEL_CARD_TROLLEYBUS = "TRAVEL_CARD_TROLLEYBUS",
    TRAVEL_CARD_TRAM = "TRAVEL_CARD_TRAM",
    TRAVEL_CARD_METRO = "TRAVEL_CARD_METRO",
    TRAVEL_CARD_ALL_CITY_TRANSPORT = "TRAVEL_CARD_ALL_CITY_TRANSPORT"
}
export declare enum PublicCityTypeEnum {
    CITY_BUS = "CITY_BUS",
    CITY_TRAIN = "CITY_TRAIN",
    CITY_TROLLEYBUS = "CITY_TROLLEYBUS",
    CITY_TRAM = "CITY_TRAM",
    CITY_METRO = "CITY_METRO"
}
export type PublicCityType = keyof typeof PublicCityTypeEnum;
export type TransportType = keyof typeof TransportTypeEnum;
export interface PublicInfoCard {
    compensationType: string;
    cost: number;
    publicTransportType: string;
    quantity: number;
    sum: number;
    ticketCount: number;
}
export declare class TransportTypesModel implements ITransportType {
    name: string;
    id: string;
    rusName: string;
    constructor(transportType: ITransportType);
}
