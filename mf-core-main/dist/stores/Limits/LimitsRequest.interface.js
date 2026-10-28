import * as t from 'io-ts';
import * as tt from '../../utils/io-ts';
import { ioTypeFromEnum } from '../../utils/ioTypeFromEnum';
import { TransportTypeEnum } from '../../constants/constants';
export var SiblingsParams = t.strict({
    departmentId: t.string,
    percent: t.number,
    transportType: t.string,
    year: t.number,
    sum: tt.money,
});
export var LimitRequestStatusEnum;
(function (LimitRequestStatusEnum) {
    LimitRequestStatusEnum["AWAITING_APPROVAL"] = "AWAITING_APPROVAL";
    LimitRequestStatusEnum["APPROVED"] = "APPROVED";
    LimitRequestStatusEnum["DONE"] = "DONE";
    LimitRequestStatusEnum["CANCELLED"] = "CANCELLED";
})(LimitRequestStatusEnum || (LimitRequestStatusEnum = {}));
export var LimitRequestStatusTitlesEnum;
(function (LimitRequestStatusTitlesEnum) {
    LimitRequestStatusTitlesEnum["AWAITING_APPROVAL"] = "\u041E\u0436\u0438\u0434\u0430\u0435\u0442 \u0441\u043E\u0433\u043B\u0430\u0441\u043E\u0432\u0430\u043D\u0438\u044F";
    LimitRequestStatusTitlesEnum["APPROVED"] = "\u0421\u043E\u0433\u043B\u0430\u0441\u043E\u0432\u0430\u043D\u0430";
    LimitRequestStatusTitlesEnum["DONE"] = "\u0412\u044B\u043F\u043E\u043B\u043D\u0435\u043D\u0430";
    LimitRequestStatusTitlesEnum["CANCELLED"] = "\u041E\u0442\u043C\u0435\u043D\u0435\u043D\u0430";
})(LimitRequestStatusTitlesEnum || (LimitRequestStatusTitlesEnum = {}));
export var LimitRequestApprovalStateEnum;
(function (LimitRequestApprovalStateEnum) {
    LimitRequestApprovalStateEnum["AWAITING_APPROVAL"] = "AWAITING_APPROVAL";
    LimitRequestApprovalStateEnum["APPROVED"] = "APPROVED";
    LimitRequestApprovalStateEnum["DECLINED"] = "DECLINED";
})(LimitRequestApprovalStateEnum || (LimitRequestApprovalStateEnum = {}));
export var LimitRequestApprovalStateTitlesEnum;
(function (LimitRequestApprovalStateTitlesEnum) {
    LimitRequestApprovalStateTitlesEnum["AWAITING_APPROVAL"] = "\u041E\u0436\u0438\u0434\u0430\u0435\u0442 \u0441\u043E\u0433\u043B\u0430\u0441\u043E\u0432\u0430\u043D\u0438\u044F";
    LimitRequestApprovalStateTitlesEnum["APPROVED"] = "\u0421\u043E\u0433\u043B\u0430\u0441\u043E\u0432\u0430\u043D\u0430";
    LimitRequestApprovalStateTitlesEnum["DECLINED"] = "\u041E\u0442\u043A\u043B\u043E\u043D\u0435\u043D\u0430";
})(LimitRequestApprovalStateTitlesEnum || (LimitRequestApprovalStateTitlesEnum = {}));
export var LimitRequestStatusesCancellable = [LimitRequestStatusEnum.AWAITING_APPROVAL];
export var LimitRequestStatusesFinal = [
    LimitRequestStatusEnum.APPROVED,
    LimitRequestStatusEnum.CANCELLED,
    LimitRequestStatusEnum.DONE,
];
export var IOLimitRequestNew = t.intersection([
    t.type({
        sum: t.number,
        id: t.string,
        humanReadableId: t.string,
        creationTime: t.union([t.string, t.number]),
    }),
    t.partial({
        month: t.number,
        transportType: ioTypeFromEnum('TransportTypeEnum', TransportTypeEnum),
        status: t.string,
        authorId: t.string,
        approvalState: t.string,
        description: t.string,
        askTargets: t.string,
        departments: t.array(t.string),
    }),
]);
export var LimitGeneralRequest = t.strict({
    year: t.number,
    period: t.number,
    transportType: t.string,
    askTargets: t.string,
    sum: tt.money,
    description: t.string,
});
export var LimitSendRequest = t.intersection([LimitGeneralRequest, t.partial({ departments: t.array(t.string) })]);
export var LimitEmpRequest = t.strict({
    year: t.number,
    period: t.number,
    transportType: t.string,
    sum: tt.money,
    description: t.string,
});
