var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
var _a;
import * as t from 'io-ts';
import * as tt from '../../utils/io-ts';
import { ioTypeFromEnum } from '../../utils/ioTypeFromEnum';
import { ApprovalStateStatuses } from '../../models/types';
import { TransportTypeEnum } from '../../constants/constants';
export var Errors;
(function (Errors) {
    Errors["LIMIT_NOT_FOUND"] = "LIMIT_NOT_FOUND";
})(Errors || (Errors = {}));
export var DepSiblings = t.strict({
    id: t.string,
    humanReadableId: t.string,
    organizationId: t.string,
    code: t.string,
    departmentName: t.string,
    departmentHeadId: t.string,
    parentId: t.string,
    active: t.boolean,
});
export var IOLimitValue = t.type({
    initial: t.number,
    spent: t.number,
    reserved: t.number,
});
export var LIMIT_TYPE;
(function (LIMIT_TYPE) {
    LIMIT_TYPE["DEPARTMENT"] = "DEPARTMENT";
    LIMIT_TYPE["EMPLOYEE"] = "EMPLOYEE";
})(LIMIT_TYPE || (LIMIT_TYPE = {}));
export var LimitTypeTitles = (_a = {},
    _a[LIMIT_TYPE.EMPLOYEE] = 'Личный лимит',
    _a[LIMIT_TYPE.DEPARTMENT] = 'На подразделение',
    _a);
export var LIMIT_STATUS;
(function (LIMIT_STATUS) {
    LIMIT_STATUS["PLANNING"] = "PLANNING";
    LIMIT_STATUS["SHARED"] = "SHARED";
    LIMIT_STATUS["CLOSED"] = "CLOSED";
    LIMIT_STATUS["CANCELED"] = "CANCELED";
})(LIMIT_STATUS || (LIMIT_STATUS = {}));
var limitStatus = ioTypeFromEnum('limitStatus', LIMIT_STATUS);
export var LIMIT_SHARING_TYPE;
(function (LIMIT_SHARING_TYPE) {
    LIMIT_SHARING_TYPE["MONTHLY"] = "MONTHLY";
    LIMIT_SHARING_TYPE["QUARTER"] = "QUARTER";
    LIMIT_SHARING_TYPE["PERCENTS"] = "PERCENTS";
})(LIMIT_SHARING_TYPE || (LIMIT_SHARING_TYPE = {}));
export var LIMIT_SHARING_TYPE_TITLES;
(function (LIMIT_SHARING_TYPE_TITLES) {
    LIMIT_SHARING_TYPE_TITLES["MONTHLY"] = "\u041D\u0430 \u043A\u0430\u0436\u0434\u044B\u0439 \u043C\u0435\u0441\u044F\u0446";
    LIMIT_SHARING_TYPE_TITLES["QUARTER"] = "\u041F\u043E\u043A\u0432\u0430\u0440\u0442\u0430\u043B\u044C\u043D\u043E";
    LIMIT_SHARING_TYPE_TITLES["PERCENTS"] = "\u041F\u0440\u043E\u0446\u0435\u043D\u0442\u044B";
})(LIMIT_SHARING_TYPE_TITLES || (LIMIT_SHARING_TYPE_TITLES = {}));
var limitSharingType = ioTypeFromEnum('limitSharingType', LIMIT_SHARING_TYPE);
export var LIMIT_SERVICE_TYPE;
(function (LIMIT_SERVICE_TYPE) {
    LIMIT_SERVICE_TYPE["PASSENGER"] = "PASSENGER";
    LIMIT_SERVICE_TYPE["CARGO"] = "CARGO";
})(LIMIT_SERVICE_TYPE || (LIMIT_SERVICE_TYPE = {}));
var limitServiceType = ioTypeFromEnum('limitServiceType', LIMIT_SERVICE_TYPE);
export var limitSharingPerPeriodDTO = t.type({
    author: t.string,
    balance: t.number,
    creationTime: t.string,
    id: t.string,
    limitSharing: t.string,
    periodNumber: t.number,
    sum: t.number,
});
export var LimitSharing = t.intersection([
    t.strict({
        id: t.string,
        author: t.string,
        creationTime: t.string,
        transportType: t.string,
        sum: tt.money,
        balance: tt.money,
        limitId: t.string,
    }),
    t.partial({ limitSharingPerPeriodDTO: limitSharingPerPeriodDTO }),
]);
var common = {
    year: t.number,
    sum: tt.money,
    limitSharingType: limitSharingType,
    limitServiceType: limitServiceType,
    finalSharing: t.boolean,
    useThisLimit: t.boolean,
};
var employee = t.strict({
    id: t.string,
    humanReadableId: t.string,
    firstName: t.string,
    lastName: t.string,
    personnelNumber: t.string,
});
var department = t.strict({
    id: t.string,
    code: t.string,
    departmentName: t.string,
});
var limitCommon = {
    id: t.string,
    humanReadableId: t.string,
    limitOwner: t.string,
    reserve: tt.money,
    limitStatus: limitStatus,
};
export var Limit = t.intersection([
    t.strict(__assign(__assign(__assign({}, common), limitCommon), { limitType: t.union([t.literal(LIMIT_TYPE.EMPLOYEE), t.literal(LIMIT_TYPE.DEPARTMENT)]) })),
    t.partial({
        employee: employee,
        department: department,
        parentLimitId: t.string,
    }),
]);
export var LimitColorsPercent = t.strict({
    name: t.string,
    value: t.string,
});
export var IOLimitColorsPercent = t.type({
    name: t.string,
    value: t.string,
});
export var author = t.strict({
    id: t.string,
    humanReadableId: t.string,
    firstName: t.string,
    patronymic: tt.optional(t.string),
    lastName: t.string,
    personnelNumber: t.string,
    positionId: tt.optional(t.string),
    organizationId: tt.optional(t.string),
});
export var ApproverDTOList = t.strict({
    id: t.string,
    departmentId: t.string,
    employeeId: t.string,
    limitRequestId: t.string,
    sum: tt.money,
    approvalState: ApprovalStateStatuses,
});
export var LIMIT_REQUEST_STATUS;
(function (LIMIT_REQUEST_STATUS) {
    LIMIT_REQUEST_STATUS["INIT"] = "INIT";
    LIMIT_REQUEST_STATUS["DONE_FULLY"] = "DONE_FULLY";
    LIMIT_REQUEST_STATUS["DONE_PARTLY"] = "DONE_PARTLY";
    LIMIT_REQUEST_STATUS["CANCELLED"] = "CANCELLED";
    LIMIT_REQUEST_STATUS["DECLINED"] = "DECLINED";
})(LIMIT_REQUEST_STATUS || (LIMIT_REQUEST_STATUS = {}));
export var LimitRequestTitlesEnum;
(function (LimitRequestTitlesEnum) {
    LimitRequestTitlesEnum["INIT"] = "\u041E\u0442\u043A\u0440\u044B\u0442\u0430";
    LimitRequestTitlesEnum["DONE_FULLY"] = "\u0412\u044B\u043F\u043E\u043B\u043D\u0435\u043D\u0430 \u043F\u043E\u043B\u043D\u043E\u0441\u0442\u044C\u044E";
    LimitRequestTitlesEnum["DONE_PARTLY"] = "\u0412\u044B\u043F\u043E\u043B\u043D\u0435\u043D\u0430 \u0447\u0430\u0441\u0442\u0438\u0447\u043D\u043E";
    LimitRequestTitlesEnum["CANCELLED"] = "\u041E\u0442\u043C\u0435\u043D\u0435\u043D\u0430";
    LimitRequestTitlesEnum["DECLINED"] = "\u0417\u0430\u043A\u0440\u044B\u0442\u0430";
})(LimitRequestTitlesEnum || (LimitRequestTitlesEnum = {}));
export var LIMIT_REQUEST_LEVEL;
(function (LIMIT_REQUEST_LEVEL) {
    LIMIT_REQUEST_LEVEL["SIBLINGS"] = "SIBLINGS";
    LIMIT_REQUEST_LEVEL["PARENT"] = "PARENT";
})(LIMIT_REQUEST_LEVEL || (LIMIT_REQUEST_LEVEL = {}));
export var LIMIT_REQUEST_LEVEL_TITLES;
(function (LIMIT_REQUEST_LEVEL_TITLES) {
    LIMIT_REQUEST_LEVEL_TITLES["SIBLINGS"] = "\u0421\u043C\u0435\u0436\u043D\u044B\u0435 \u043F\u043E\u0434\u0440\u0430\u0437\u0434\u0435\u043B\u0435\u043D\u0438\u044F";
    LIMIT_REQUEST_LEVEL_TITLES["PARENT"] = "\u0412\u044B\u0448\u0435\u0441\u0442\u043E\u044F\u0449\u0435\u0435 \u043F\u043E\u0434\u0440\u0430\u0437\u0434\u0435\u043B\u0435\u043D\u0438\u0435";
})(LIMIT_REQUEST_LEVEL_TITLES || (LIMIT_REQUEST_LEVEL_TITLES = {}));
export var LimitRequestStatusesTitlesEnum;
(function (LimitRequestStatusesTitlesEnum) {
    LimitRequestStatusesTitlesEnum["INIT"] = "\u041D\u0430 \u0441\u043E\u0433\u043B\u0430\u0441\u043E\u0432\u0430\u043D\u0438\u0438";
    LimitRequestStatusesTitlesEnum["DONE_FULLY"] = "\u0412\u044B\u043F\u043E\u043B\u043D\u0435\u043D\u0430 \u043F\u043E\u043B\u043D\u043E\u0441\u0442\u044C\u044E";
    LimitRequestStatusesTitlesEnum["DONE_PARTLY"] = "\u0412\u044B\u043F\u043E\u043B\u043D\u0435\u043D\u0430 \u0447\u0430\u0441\u0442\u0438\u0447\u043D\u043E";
    LimitRequestStatusesTitlesEnum["CANCELLED"] = "\u041E\u0442\u043C\u0435\u043D\u0435\u043D\u0430";
    LimitRequestStatusesTitlesEnum["DECLINED"] = "\u0417\u0430\u043A\u0440\u044B\u0442\u0430";
})(LimitRequestStatusesTitlesEnum || (LimitRequestStatusesTitlesEnum = {}));
export var LimitRequestData = t.strict({
    id: t.string,
    humanReadableId: t.string,
    year: t.number,
    period: t.number,
    transportType: ioTypeFromEnum('TransportTypeEnum', TransportTypeEnum),
    sum: tt.money,
    description: tt.optional(t.string),
    declineReason: tt.optional(t.string),
    status: ioTypeFromEnum('LIMIT_REQUEST_STATUS', LIMIT_REQUEST_STATUS),
    creationTime: t.string,
    limitType: ioTypeFromEnum('LIMIT_TYPE', LIMIT_TYPE),
});
export var LimitRequestInfo = t.intersection([
    LimitRequestData,
    t.type({
        approverDtoList: t.array(ApproverDTOList),
        author: author,
    }),
    t.partial({
        limitId: t.string || t.string,
        limitSum: tt.money,
        limitBalance: tt.money,
        limitHumanreadableid: t.string || t.string,
        plannedSum: tt.money,
        askTargets: tt.oneOf(['PARENT', 'SIBLINGS']),
        limitSharingType: ioTypeFromEnum('LIMIT_SHARING_TYPE', LIMIT_SHARING_TYPE),
    }),
]);
var Sorting = t.type({
    sorted: t.boolean,
    unsorted: t.boolean,
    empty: t.boolean,
});
var Pagination = t.type({
    sort: Sorting,
    offset: t.number,
    pageNumber: t.number,
    pageSize: t.number,
    paged: t.boolean,
    unpaged: t.boolean,
});
export var ActiveLimitRequestInfo = t.type({
    totalElements: t.number,
    totalPages: t.number,
    number: t.number,
    sort: Sorting,
    size: t.number,
    content: t.array(LimitRequestInfo),
    first: t.boolean,
    last: t.boolean,
    pageable: Pagination,
    numberOfElements: t.number,
    empty: t.boolean,
});
export var OldLimitRequestInfo = t.type({
    totalElements: t.number,
    totalPages: t.number,
    number: t.number,
    sort: Sorting,
    size: t.number,
    content: t.array(LimitRequestInfo),
    first: t.boolean,
    last: t.boolean,
    pageable: Pagination,
    numberOfElements: t.number,
    empty: t.boolean,
});
export var LimitRequestSavingObject = t.type({
    requestId: t.string,
    sum: tt.money,
    approvalState: ApprovalStateStatuses,
});
export var LimitRequestSaving = t.type({
    sum: tt.money,
    year: t.number,
    description: t.string,
    period: t.number,
    transportType: t.string,
});
export var ECONOMY_HISTORY_TYPE;
(function (ECONOMY_HISTORY_TYPE) {
    ECONOMY_HISTORY_TYPE["FROM_ECONOMY"] = "FROM_ECONOMY";
    ECONOMY_HISTORY_TYPE["TO_ECONOMY"] = "TO_ECONOMY";
    ECONOMY_HISTORY_TYPE["GENERAL"] = "GENERAL";
})(ECONOMY_HISTORY_TYPE || (ECONOMY_HISTORY_TYPE = {}));
export var LimitTransferHistory = t.strict({
    id: t.string,
    author: t.string,
    creationTime: t.string,
    sum: tt.money,
    sourceLimit: t.string,
    targetLimit: t.string,
    sourceTransportType: ioTypeFromEnum('TransportTypeEnum', TransportTypeEnum),
    targetTransportType: ioTypeFromEnum('TransportTypeEnum', TransportTypeEnum),
    year: t.number,
    period: tt.optional(t.number),
    historyType: ioTypeFromEnum('ECONOMY_HISTORY_TYPE', ECONOMY_HISTORY_TYPE),
});
export var LimitCostHistory = t.strict({
    limitId: t.string,
    sumReserved: tt.money,
    sumSpent: tt.optional(tt.money),
});
export var BonusOperation;
(function (BonusOperation) {
    BonusOperation["DEPOSIT"] = "DEPOSIT";
    BonusOperation["SPEND"] = "SPEND";
})(BonusOperation || (BonusOperation = {}));
export var BonusStatus;
(function (BonusStatus) {
    BonusStatus["RESERVED"] = "RESERVED";
    BonusStatus["DONE"] = "DONE";
    BonusStatus["CANCELED"] = "CANCELED";
})(BonusStatus || (BonusStatus = {}));
export var BonusesRequest = t.type({
    id: t.string,
    sum: t.number,
    operation: ioTypeFromEnum('BonusOperation', BonusOperation),
    status: ioTypeFromEnum('BonusStatus', BonusStatus),
    updateTime: t.string,
    reason: t.string,
});
export var Bonuses = t.type({
    ownerId: t.string,
    sum: t.number,
    balance: t.number,
    requests: t.array(BonusesRequest),
});
export var ISpentActionsType = t.strict({
    id: t.string,
    fullName: tt.optional(t.string),
    transportType: t.string,
    period: t.number,
    sum: tt.money,
    creationTime: t.string,
    approverDtoList: t.array(ApproverDTOList),
    status: t.string,
    limitSum: tt.optional(tt.money),
    humanReadableId: t.string,
    author: author,
    limitBalance: tt.optional(tt.money),
    limitSharingType: tt.optional(t.string),
    description: tt.optional(t.string),
    limitType: ioTypeFromEnum('LIMIT_TYPE', LIMIT_TYPE),
    limitHumanreadableid: tt.optional(t.string),
    level: tt.optional(t.string),
    declineReason: tt.optional(t.string),
});
