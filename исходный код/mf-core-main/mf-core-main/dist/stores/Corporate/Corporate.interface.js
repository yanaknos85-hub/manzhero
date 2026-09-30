import * as t from 'io-ts';
import { EmployeeStatus } from '../../constants/constants';
import { ioTypeFromEnum } from '../../utils/ioTypeFromEnum';
export var IODepartmentEmployee = t.type({
    id: t.string,
    firstName: t.string,
    lastName: t.string,
    personnelNumber: t.string,
});
export var IODepartment = t.recursion('IODepartment', function () { return t.intersection([
    t.type({
        organizationId: t.string,
        code: t.string,
        departmentName: t.string,
        fullStructurePath: t.string,
        employees: t.array(IODepartmentEmployee),
        children: t.array(IODepartment),
        status: ioTypeFromEnum('EmployeeStatus', EmployeeStatus),
    }),
    t.partial({
        id: t.string,
        humanReadableId: t.string,
        departmentHeadId: t.string,
        parentId: t.string,
        location: t.string,
    }),
]); });
