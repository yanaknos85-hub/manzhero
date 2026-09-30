var __spreadArray = (this && this.__spreadArray) || function (to, from, pack) {
    if (pack || arguments.length === 2) for (var i = 0, l = from.length, ar; i < l; i++) {
        if (ar || !(i in from)) {
            if (!ar) ar = Array.prototype.slice.call(from, 0, i);
            ar[i] = from[i];
        }
    }
    return to.concat(ar || Array.prototype.slice.call(from));
};
import * as tt from '../utils/io-ts';
// Список статусов взят из DTO: Заявка (чтение);
var approvalStateStatuses = ['AWAITING_APPROVAL', 'APPROVED', 'DECLINED'];
export var ApprovalStateStatuses = tt.oneOf(__spreadArray([], approvalStateStatuses, true));
