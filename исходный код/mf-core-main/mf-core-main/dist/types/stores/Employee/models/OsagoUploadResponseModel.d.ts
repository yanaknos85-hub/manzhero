import { IOsagoUploadResponseModel, OrUndefined, OsagoBodyResponce, OsagoEntitie, OsagoProps, PersonalCar } from '../Employee.interface';
import { EmployeeModel } from './EmployeeModel';
export declare class OsagoUploadResponseModel implements IOsagoUploadResponseModel {
    [OsagoEntitie: number]: OrUndefined<OsagoEntitie>;
    vehicleMark: OsagoProps;
    vehicleModel: OsagoProps;
    ownerLastName: OsagoProps;
    ownerFirstName: OsagoProps;
    ownerMiddleName: OsagoProps;
    endDate: OsagoProps;
    insurerLastName: OsagoProps;
    insurerFirstName: OsagoProps;
    vehicleInfoNumber: OsagoProps;
    contractSeries: OsagoProps;
    contractNumber: OsagoProps;
    vehicleInfoSeries: OsagoProps;
    plateNumber: OsagoProps;
    type: OsagoProps;
    vinNumber: OsagoProps;
    driversLastName: OsagoProps;
    driversFirstName: OsagoProps;
    driversMiddleName: OsagoProps;
    driversSeries: OsagoProps;
    driversNumber: OsagoProps;
    status: OrUndefined<boolean>;
    orientation: OrUndefined<string>;
    constructor(osago: OsagoBodyResponce);
    collectByPersonalCar(employee: EmployeeModel): PersonalCar;
}
