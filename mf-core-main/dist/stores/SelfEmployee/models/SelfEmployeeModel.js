import { EmployeeStatus } from '../SelfEmployee.interface';
var SelfEmployeeModel = /** @class */ (function () {
    function SelfEmployeeModel(employee) {
        var _a, _b, _c, _d, _e, _f, _g, _h, _j, _k, _l, _m, _o, _p, _q, _r, _s, _t, _u, _v;
        // FIXME sonarjs/cognitive-complexity
        this.userId = (_a = employee === null || employee === void 0 ? void 0 : employee.userId) !== null && _a !== void 0 ? _a : '';
        this.firstName = (_b = employee === null || employee === void 0 ? void 0 : employee.firstName) !== null && _b !== void 0 ? _b : '';
        this.lastName = (_c = employee === null || employee === void 0 ? void 0 : employee.lastName) !== null && _c !== void 0 ? _c : '';
        this.patronymic = (_d = employee === null || employee === void 0 ? void 0 : employee.patronymic) !== null && _d !== void 0 ? _d : '';
        this.personnelNumber = (_e = employee === null || employee === void 0 ? void 0 : employee.personnelNumber) !== null && _e !== void 0 ? _e : '';
        this.departmentId = (_f = employee === null || employee === void 0 ? void 0 : employee.departmentId) !== null && _f !== void 0 ? _f : '';
        this.positionId = (_g = employee === null || employee === void 0 ? void 0 : employee.positionId) !== null && _g !== void 0 ? _g : '';
        this.approvals = (_h = employee === null || employee === void 0 ? void 0 : employee.approvals) !== null && _h !== void 0 ? _h : 0;
        this.mobilePhone = (_j = employee === null || employee === void 0 ? void 0 : employee.mobilePhone) !== null && _j !== void 0 ? _j : '';
        this.email = (_k = employee === null || employee === void 0 ? void 0 : employee.email) !== null && _k !== void 0 ? _k : '';
        this.supervisorId = (_l = employee === null || employee === void 0 ? void 0 : employee.supervisorId) !== null && _l !== void 0 ? _l : '';
        this.id = (_m = employee === null || employee === void 0 ? void 0 : employee.id) !== null && _m !== void 0 ? _m : '';
        this.delegatedById = (_o = employee === null || employee === void 0 ? void 0 : employee.delegatedById) !== null && _o !== void 0 ? _o : '';
        this.availableTransportTypes = (_p = employee === null || employee === void 0 ? void 0 : employee.availableTransportTypes) !== null && _p !== void 0 ? _p : [];
        this.organizationId = (_q = employee === null || employee === void 0 ? void 0 : employee.organizationId) !== null && _q !== void 0 ? _q : '';
        this.status = (_r = employee === null || employee === void 0 ? void 0 : employee.status) !== null && _r !== void 0 ? _r : EmployeeStatus.ACTIVE;
        this.humanReadableId = (_s = employee === null || employee === void 0 ? void 0 : employee.humanReadableId) !== null && _s !== void 0 ? _s : '';
        this.consent = (_t = employee === null || employee === void 0 ? void 0 : employee.consent) !== null && _t !== void 0 ? _t : false;
        // eslint-disable-next-line @typescript-eslint/no-non-null-asserted-optional-chain
        this.orgStructureType = employee === null || employee === void 0 ? void 0 : employee.orgStructureType;
        this.isDepartmentHead = (_u = employee === null || employee === void 0 ? void 0 : employee.isDepartmentHead) !== null && _u !== void 0 ? _u : false;
        this.isPhoneConfirmed = (_v = employee === null || employee === void 0 ? void 0 : employee.isPhoneConfirmed) !== null && _v !== void 0 ? _v : false;
    }
    Object.defineProperty(SelfEmployeeModel.prototype, "isExisting", {
        get: function () {
            return !!(this.organizationId && this.departmentId && this.id);
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(SelfEmployeeModel.prototype, "fullNameWithCode", {
        get: function () {
            return "".concat(this.firstName, " ").concat(this.patronymic, " ").concat(this.lastName, " (").concat(this.personnelNumber, ")");
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(SelfEmployeeModel.prototype, "fullName", {
        get: function () {
            return "".concat(this.lastName, " ").concat(this.firstName, " ").concat(this.patronymic, " ");
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(SelfEmployeeModel.prototype, "shortName", {
        get: function () {
            return "".concat(this.firstName.charAt(0), ". ").concat(this.patronymic.charAt(0), ". ").concat(this.lastName);
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(SelfEmployeeModel.prototype, "nameWithInitials", {
        get: function () {
            return "".concat(this.firstName.charAt(0), ". ").concat(this.patronymic.charAt(0), ". ").concat(this.lastName);
        },
        enumerable: false,
        configurable: true
    });
    Object.defineProperty(SelfEmployeeModel.prototype, "shortNameWithNumberString", {
        get: function () {
            return "".concat(this.shortName, " (").concat(this.personnelNumber, ")");
        },
        enumerable: false,
        configurable: true
    });
    return SelfEmployeeModel;
}());
export { SelfEmployeeModel };
