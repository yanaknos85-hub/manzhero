var EmployeeDetailedModel = /** @class */ (function () {
    function EmployeeDetailedModel(employee) {
        var _a, _b, _c, _d, _e, _f, _g, _h, _j, _k, _l, _m, _o, _p;
        this.id = (_a = employee.id) !== null && _a !== void 0 ? _a : '';
        this.humanReadableId = (_b = employee.humanReadableId) !== null && _b !== void 0 ? _b : '';
        this.userId = (_c = employee.userId) !== null && _c !== void 0 ? _c : '';
        this.nameWithInitials = (_d = employee.nameWithInitials) !== null && _d !== void 0 ? _d : '';
        this.fullNameString = (_e = employee.fullName) !== null && _e !== void 0 ? _e : '';
        this.department = (_f = employee.department) !== null && _f !== void 0 ? _f : '';
        this.position = (_g = employee.position) !== null && _g !== void 0 ? _g : '';
        this.personnelNumber = (_h = employee.personnelNumber) !== null && _h !== void 0 ? _h : '';
        this.supervisor = (_j = employee.supervisor) !== null && _j !== void 0 ? _j : '';
        this.delegatedBy = (_k = employee.delegatedBy) !== null && _k !== void 0 ? _k : '';
        this.mobilePhone = (_l = employee.mobilePhone) !== null && _l !== void 0 ? _l : '';
        this.email = (_m = employee.email) !== null && _m !== void 0 ? _m : '';
        this.organization = (_o = employee.organization) !== null && _o !== void 0 ? _o : '';
        this.availableTransportTypes = (_p = employee.availableTransportTypes) !== null && _p !== void 0 ? _p : [];
    }
    return EmployeeDetailedModel;
}());
export { EmployeeDetailedModel };
