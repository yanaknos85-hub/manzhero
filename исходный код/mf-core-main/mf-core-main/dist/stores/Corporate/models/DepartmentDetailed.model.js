import { EmployeeStatusTitle } from '../../../constants/constants';
var DepartmentDetailedModel = /** @class */ (function () {
    function DepartmentDetailedModel(department) {
        this.children = [];
        this.employees = [];
        this.id = department.id;
        this.humanReadableId = department.humanReadableId;
        this.organization = department.organization;
        this.organizationId = department.organizationId;
        this.code = department.code;
        this.departmentName = department.departmentName;
        this.fullStructurePath = department.fullStructurePath;
        this.departmentHeadId = department.departmentHeadId;
        this.departmentHead = department.departmentHead;
        this.parent = department.parent;
        this.location = department.location;
        this.children = department.children;
        this.employees = department.employees;
        this.status = department.status;
        this.statusTitle = EmployeeStatusTitle[department.status];
    }
    Object.defineProperty(DepartmentDetailedModel.prototype, "isExisting", {
        get: function () {
            return !!this.id;
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(DepartmentDetailedModel.prototype, "editStatusTitle", {
        set: function (statusTitle) {
            this.statusTitle = statusTitle;
        },
        enumerable: false,
        configurable: true
    });
    return DepartmentDetailedModel;
}());
export { DepartmentDetailedModel };
