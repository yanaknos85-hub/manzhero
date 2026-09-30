export var TransportTypeEnum;
(function (TransportTypeEnum) {
    TransportTypeEnum["TAXI"] = "TAXI";
    TransportTypeEnum["PUBLIC"] = "PUBLIC";
    TransportTypeEnum["PERSONAL"] = "PERSONAL";
    TransportTypeEnum["CARSHARING"] = "CARSHARING";
    TransportTypeEnum["BICYCLE"] = "BICYCLE";
    TransportTypeEnum["WALK"] = "WALK";
    TransportTypeEnum["SCOOTER"] = "SCOOTER";
    TransportTypeEnum["DEDICATED"] = "DEDICATED";
    TransportTypeEnum["COURIER"] = "COURIER";
    TransportTypeEnum["INTERREGIONAL"] = "INTERREGIONAL";
})(TransportTypeEnum || (TransportTypeEnum = {}));
export var TransportTypeTitlesEnum;
(function (TransportTypeTitlesEnum) {
    TransportTypeTitlesEnum["TAXI"] = "\u0422\u0430\u043A\u0441\u0438";
    TransportTypeTitlesEnum["PUBLIC"] = "\u041E\u0431\u0449\u0435\u0441\u0442\u0432\u0435\u043D\u043D\u044B\u0439";
    TransportTypeTitlesEnum["PERSONAL"] = "\u041B\u0438\u0447\u043D\u044B\u0439";
    TransportTypeTitlesEnum["CARSHARING"] = "\u041A\u0430\u0440\u0448\u0435\u0440\u0438\u043D\u0433";
    TransportTypeTitlesEnum["BICYCLE"] = "\u0412\u0435\u043B\u043E\u0441\u0438\u043F\u0435\u0434";
    TransportTypeTitlesEnum["WALK"] = "\u041F\u0435\u0448\u043A\u043E\u043C";
    TransportTypeTitlesEnum["SCOOTER"] = "\u0421\u0430\u043C\u043E\u043A\u0430\u0442";
    TransportTypeTitlesEnum["DEDICATED"] = "\u0414\u043E\u0441\u0442\u0430\u0432\u043A\u0430";
    TransportTypeTitlesEnum["COURIER"] = "\u041A\u0443\u0440\u044C\u0435\u0440";
    TransportTypeTitlesEnum["INTERREGIONAL"] = "\u041C\u0435\u0436\u0440\u0435\u0433\u0438\u043E\u043D\u0430\u043B\u044C\u043D\u0430\u044F";
})(TransportTypeTitlesEnum || (TransportTypeTitlesEnum = {}));
export var TransportCompensations;
(function (TransportCompensations) {
    TransportCompensations["CITY_TRIP_COMPENSATION"] = "CITY_TRIP_COMPENSATION";
    TransportCompensations["SUBURB_TRIP_COMPENSATION"] = "SUBURB_TRIP_COMPENSATION";
    TransportCompensations["TRAVEL_CARD_COMPENSATION"] = "TRAVEL_CARD_COMPENSATION";
})(TransportCompensations || (TransportCompensations = {}));
export var TransportCompensationsTitles;
(function (TransportCompensationsTitles) {
    TransportCompensationsTitles["CITY_TRIP_COMPENSATION"] = "\u041A\u043E\u043C\u043F\u0435\u043D\u0441\u0430\u0446\u0438\u044F \u043F\u043E\u0435\u0437\u0434\u043A\u0438 \u043F\u043E \u0433\u043E\u0440\u043E\u0434\u0443";
    TransportCompensationsTitles["SUBURB_TRIP_COMPENSATION"] = "\u041A\u043E\u043C\u043F\u0435\u043D\u0441\u0430\u0446\u0438\u044F \u043C\u0435\u0436\u0434\u0443\u0433\u043E\u0440\u043E\u0434\u043D\u0438\u0445 \u043F\u043E\u0435\u0437\u0434\u043E\u043A";
    TransportCompensationsTitles["TRAVEL_CARD_COMPENSATION"] = "\u041A\u043E\u043C\u043F\u0435\u043D\u0441\u0430\u0446\u0438\u044F \u043F\u0440\u043E\u0435\u0437\u0434\u043D\u043E\u0433\u043E \u0434\u043E\u043A\u0443\u043C\u0435\u043D\u0442\u0430";
})(TransportCompensationsTitles || (TransportCompensationsTitles = {}));
export var PublicTransportTypeEnum;
(function (PublicTransportTypeEnum) {
    PublicTransportTypeEnum["METRO"] = "METRO";
    PublicTransportTypeEnum["TRAM"] = "TRAM";
    PublicTransportTypeEnum["TROLLEYBUS"] = "TROLLEYBUS";
    PublicTransportTypeEnum["BUS"] = "BUS";
    PublicTransportTypeEnum["CITY_BUS"] = "CITY_BUS";
    PublicTransportTypeEnum["CITY_TRAIN"] = "CITY_TRAIN";
    PublicTransportTypeEnum["CITY_TROLLEYBUS"] = "CITY_TROLLEYBUS";
    PublicTransportTypeEnum["CITY_TRAM"] = "CITY_TRAM";
    PublicTransportTypeEnum["CITY_METRO"] = "CITY_METRO";
    PublicTransportTypeEnum["SUBURB_BUS"] = "SUBURB_BUS";
    PublicTransportTypeEnum["SUBURB_TRAIN"] = "SUBURB_TRAIN";
    PublicTransportTypeEnum["SUBURB_FERRY_CROSSING"] = "SUBURB_FERRY_CROSSING";
    PublicTransportTypeEnum["SUBURB_TROLLEYBUS"] = "SUBURB_TROLLEYBUS";
    PublicTransportTypeEnum["TRAVEL_CARD_BUS"] = "TRAVEL_CARD_BUS";
    PublicTransportTypeEnum["TRAVEL_CARD_TRAIN"] = "TRAVEL_CARD_TRAIN";
    PublicTransportTypeEnum["TRAVEL_CARD_TROLLEYBUS"] = "TRAVEL_CARD_TROLLEYBUS";
    PublicTransportTypeEnum["TRAVEL_CARD_TRAM"] = "TRAVEL_CARD_TRAM";
    PublicTransportTypeEnum["TRAVEL_CARD_METRO"] = "TRAVEL_CARD_METRO";
    PublicTransportTypeEnum["TRAVEL_CARD_ALL_CITY_TRANSPORT"] = "TRAVEL_CARD_ALL_CITY_TRANSPORT";
})(PublicTransportTypeEnum || (PublicTransportTypeEnum = {}));
export var PublicCityTypeEnum;
(function (PublicCityTypeEnum) {
    PublicCityTypeEnum["CITY_BUS"] = "CITY_BUS";
    PublicCityTypeEnum["CITY_TRAIN"] = "CITY_TRAIN";
    PublicCityTypeEnum["CITY_TROLLEYBUS"] = "CITY_TROLLEYBUS";
    PublicCityTypeEnum["CITY_TRAM"] = "CITY_TRAM";
    PublicCityTypeEnum["CITY_METRO"] = "CITY_METRO";
})(PublicCityTypeEnum || (PublicCityTypeEnum = {}));
var TransportTypesModel = /** @class */ (function () {
    function TransportTypesModel(transportType) {
        this.name = transportType.name;
        this.id = transportType.id;
        this.rusName = transportType.rusName;
    }
    return TransportTypesModel;
}());
export { TransportTypesModel };
