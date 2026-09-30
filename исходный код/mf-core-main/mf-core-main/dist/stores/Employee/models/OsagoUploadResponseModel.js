import { OsagoUploadResponseParams, PersonalOwnerInformation } from '../../../constants/constants';
var OsagoUploadResponseModel = /** @class */ (function () {
    function OsagoUploadResponseModel(osago) {
        var _this = this;
        this.status = osago.status;
        this.orientation = osago.orientation;
        Object.entries(osago.entities).forEach(function (_a) {
            var value = _a[1];
            _this[OsagoUploadResponseParams[value.entity_name]] = value;
        });
    }
    OsagoUploadResponseModel.prototype.collectByPersonalCar = function (employee) {
        var getString = function (prop) { return (prop ? prop.entity_value.trim().normalize() : ''); };
        // Без пробелов из-за бека
        var registrationCertificate = "".concat(getString(this.vehicleInfoSeries)).concat(getString(this.vehicleInfoNumber));
        var insuranceNumber = "".concat(getString(this.contractSeries)).concat(getString(this.contractNumber));
        var ownerInfo = employee.firstName === getString(this.ownerFirstName)
            && employee.lastName === getString(this.ownerLastName)
            && employee.patronymic === getString(this.ownerMiddleName)
            ? PersonalOwnerInformation.USER
            : '';
        return {
            registrationCertificate: registrationCertificate,
            insuranceNumber: insuranceNumber,
            ownerInfo: ownerInfo,
            employeeId: employee.id,
            brandName: getString(this.vehicleMark),
            model: getString(this.vehicleModel),
            registrationNumber: getString(this.plateNumber),
        };
    };
    return OsagoUploadResponseModel;
}());
export { OsagoUploadResponseModel };
