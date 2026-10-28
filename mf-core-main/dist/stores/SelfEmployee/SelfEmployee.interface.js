import * as t from 'io-ts';
import { ioTypeFromEnum } from '../../utils/ioTypeFromEnum';
import { OrgStructureType } from './../../constants/constants';
export var EmployeeStatus;
(function (EmployeeStatus) {
    EmployeeStatus["ACTIVE"] = "ACTIVE";
    EmployeeStatus["INACTIVE"] = "INACTIVE";
})(EmployeeStatus || (EmployeeStatus = {}));
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
export var SelfEmployee = t.intersection([
    Employee,
    t.type({
        consent: t.boolean,
        orgStructureType: ioTypeFromEnum('OrgStructureType', OrgStructureType),
    }),
    t.partial({
        isDepartmentHead: t.boolean,
        isPhoneConfirmed: t.boolean,
    }),
]);
