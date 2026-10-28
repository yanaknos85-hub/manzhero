import {
  OsagoUploadResponseParams,
  PersonalOwnerInformation
} from '../../../constants/constants';

import {
  IOsagoUploadResponseModel,
  OrUndefined,
  OsagoBodyResponce,
  OsagoEntitie,
  OsagoProps,
  PersonalCar
} from '../Employee.interface';
import { EmployeeModel } from './EmployeeModel';

export class OsagoUploadResponseModel implements IOsagoUploadResponseModel {
  [OsagoEntitie: number]: OrUndefined<OsagoEntitie>;

  public vehicleMark: OsagoProps;

  public vehicleModel: OsagoProps;

  public ownerLastName: OsagoProps;

  public ownerFirstName: OsagoProps;

  public ownerMiddleName: OsagoProps;

  public endDate: OsagoProps;

  public insurerLastName: OsagoProps;

  public insurerFirstName: OsagoProps;

  public vehicleInfoNumber: OsagoProps;

  public contractSeries: OsagoProps;

  public contractNumber: OsagoProps;

  public vehicleInfoSeries: OsagoProps;

  public plateNumber: OsagoProps;

  public type: OsagoProps;

  public vinNumber: OsagoProps;

  public driversLastName: OsagoProps;

  public driversFirstName: OsagoProps;

  public driversMiddleName: OsagoProps;

  public driversSeries: OsagoProps;

  public driversNumber: OsagoProps;

  public status: OrUndefined<boolean>;

  public orientation: OrUndefined<string>;

  constructor(osago: OsagoBodyResponce) {
    this.status = osago.status;
    this.orientation = osago.orientation;

    Object.entries<OsagoEntitie>(osago.entities).forEach(([, value]): void => {
      this[OsagoUploadResponseParams[value.entity_name as any] as keyof typeof OsagoUploadResponseParams] = value;
    });
  }

  public collectByPersonalCar(employee: EmployeeModel): PersonalCar {
    const getString = (prop: OsagoProps): string => (prop ? prop.entity_value.trim().normalize() : '');

    // Без пробелов из-за бека
    const registrationCertificate = `${getString(this.vehicleInfoSeries)}${getString(this.vehicleInfoNumber)}`;
    const insuranceNumber = `${getString(this.contractSeries)}${getString(this.contractNumber)}`;

    const ownerInfo: string
      = employee.firstName === getString(this.ownerFirstName)
      && employee.lastName === getString(this.ownerLastName)
      && employee.patronymic === getString(this.ownerMiddleName)
        ? PersonalOwnerInformation.USER
        : '';

    return {
      registrationCertificate,
      insuranceNumber,
      ownerInfo,
      employeeId: employee.id,
      brandName: getString(this.vehicleMark),
      model: getString(this.vehicleModel),
      registrationNumber: getString(this.plateNumber),
    } as PersonalCar;
  }
}
