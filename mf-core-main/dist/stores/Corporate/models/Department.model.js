var DepartmentModel = /** @class */ (function () {
    function DepartmentModel(department) {
        this.children = [];
        this.employees = [];
        this.id = department.id;
        this.humanReadableId = department.humanReadableId;
        this.organizationId = department.organizationId;
        this.code = department.code;
        this.departmentName = department.departmentName;
        this.fullStructurePath = department.fullStructurePath;
        this.departmentHeadId = department.departmentHeadId;
        this.parentId = department.parentId;
        this.location = department.location;
        this.children = department.children;
        this.employees = department.employees;
        this.status = department.status;
    }
    Object.defineProperty(DepartmentModel.prototype, "isExisting", {
        get: function () {
            return !!this.id;
        },
        enumerable: false,
        configurable: true
    });
    return DepartmentModel;
}());
export { DepartmentModel };
