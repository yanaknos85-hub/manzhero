import * as t from 'io-ts';
import { Employee } from '../Employee/Employee.interface';
export var Delegate = t.strict({
    id: t.string,
    supervisorId: t.string,
    delegateId: t.string,
    startDate: t.string,
    endDate: t.string,
    transportType: t.string,
    delegateEmployee: Employee,
});
var DelegateModel = /** @class */ (function () {
    function DelegateModel(delegate) {
        this.id = delegate.id;
        this.supervisorId = delegate.supervisorId;
        this.delegateId = delegate.delegateId;
        this.startDate = delegate.startDate;
        this.endDate = delegate.endDate;
        this.transportType = delegate.transportType;
        this.delegateEmployee = delegate.delegateEmployee;
    }
    return DelegateModel;
}());
export { DelegateModel };
