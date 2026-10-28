import * as t from 'io-ts';
import { EmployeeStatus } from '../../constants/constants';
export var IOHumanReadable = t.type({
    humanReadableId: t.string,
});
export var Employee = t.intersection([
    IOHumanReadable,
    t.type({
        id: t.string,
        userId: t.string,
        firstName: t.string,
        lastName: t.string,
        personnelNumber: t.string,
        departmentId: t.string,
        organizationId: t.string,
        positionId: t.string,
    }),
    t.partial({
        patronymic: t.string,
        status: t.keyof(EmployeeStatus),
        mobilePhone: t.string,
        email: t.string,
        supervisorId: t.string,
        delegatedById: t.string,
        availableTransportTypes: t.UnknownArray,
        personalCars: t.UnknownArray,
        approvals: t.number,
        positionName: t.string,
        departmentName: t.string,
    }),
]);
// TODO:выяснить про ключ employeeID чтобы выяснить нужен ли он в сущности
export var IOPersonalCar = t.intersection([
    t.partial({
        employeeId: t.string,
        color: t.string,
    }),
    t.type({
        id: t.string,
        transportType: t.string,
        brandName: t.string,
        model: t.string,
        registrationNumber: t.string,
        registrationCertificate: t.string,
        engineVolume: t.number,
        insuranceNumber: t.string,
        passengerSeatsCount: t.number,
        ownerInfo: t.string,
        persDataAccept: t.boolean,
    }),
]);
export var carTypeDescriptions = {
    CAR: 'Автомобиль',
    MOTORCYCLE: 'Мотоцикл',
};
export var getKeyValue = function (key) { return function (obj) { return obj[key]; }; };
