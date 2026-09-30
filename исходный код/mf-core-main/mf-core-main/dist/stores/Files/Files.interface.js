import * as t from 'io-ts';
import { EmployeeStatus } from '../../constants/constants';
import { ioTypeFromEnum } from '../../utils/ioTypeFromEnum';
export var StrategyMode;
(function (StrategyMode) {
    StrategyMode["UPDATE_OR_INSERT"] = "UPDATE_OR_INSERT";
    StrategyMode["UPDATE"] = "UPDATE";
    StrategyMode["INSERT"] = "INSERT";
})(StrategyMode || (StrategyMode = {}));
export var UploadStatus;
(function (UploadStatus) {
    UploadStatus["SUCCESS"] = "SUCCESS";
    UploadStatus["FAIL"] = "FAIL";
    UploadStatus["IN_PROGRESS"] = "IN_PROGRESS";
})(UploadStatus || (UploadStatus = {}));
export var FileEmployeeStatus;
(function (FileEmployeeStatus) {
    FileEmployeeStatus["OK"] = "OK";
    FileEmployeeStatus["ERROR"] = "ERROR";
})(FileEmployeeStatus || (FileEmployeeStatus = {}));
export var FileDecSeparator;
(function (FileDecSeparator) {
    FileDecSeparator["DOT"] = "DOT";
    FileDecSeparator["COMMA"] = "COMMA";
})(FileDecSeparator || (FileDecSeparator = {}));
export var FileSeparator;
(function (FileSeparator) {
    FileSeparator["COMMA"] = "COMMA";
    FileSeparator["SEMICOLON"] = "SEMICOLON";
    FileSeparator["WHITESPACE"] = "WHITESPACE";
    FileSeparator["TAB"] = "TAB";
    FileSeparator["OTHER"] = "OTHER";
})(FileSeparator || (FileSeparator = {}));
export var FileNsi;
(function (FileNsi) {
    FileNsi["EMPLOYEE"] = "EMPLOYEE";
})(FileNsi || (FileNsi = {}));
export var FileHeadersPresence;
(function (FileHeadersPresence) {
    FileHeadersPresence["WITH"] = "WITH";
    FileHeadersPresence["WITHOUT"] = "WITHOUT";
})(FileHeadersPresence || (FileHeadersPresence = {}));
export var FileUploadMode;
(function (FileUploadMode) {
    FileUploadMode["load"] = "load";
    FileUploadMode["preload"] = "preload";
})(FileUploadMode || (FileUploadMode = {}));
export var IOFileEmployee = t.intersection([
    t.type({}),
    t.partial({
        id: t.string,
        userId: t.string,
        availableTransportTypes: t.UnknownArray,
        personalCars: t.UnknownArray,
        organizationId: t.string,
        firstName: t.string,
        lastName: t.string,
        patronymic: t.string,
        email: t.string,
        personnelNumber: t.string,
        mobilePhone: t.string,
        position: t.string,
        department: t.string,
        delegatedById: t.string,
        supervisorId: t.string,
        status: ioTypeFromEnum('EmployeeStatus', EmployeeStatus),
    }),
]);
export var IOTicket = t.type({
    operationId: t.string,
    dtOperation: t.string,
    login: t.string,
});
export var IOValidationResult = t.type({
    field: t.string,
    badValue: t.string,
    description: t.string,
});
export var IOEmployeeFileItem = t.intersection([
    t.type({
        item: IOFileEmployee,
        parseStatus: ioTypeFromEnum('FileEmployeeStatus', FileEmployeeStatus),
        validationResults: t.array(IOValidationResult),
    }),
    t.partial({
        description: t.string,
    }),
]);
export var IOParsingResult = t.type({
    parseStatus: ioTypeFromEnum('FileEmployeeStatus', FileEmployeeStatus),
    items: t.array(IOEmployeeFileItem),
    totalRead: t.number,
    totalBadRecords: t.number,
    totalWithOutError: t.number,
});
export var IOLoadingResult = t.intersection([
    t.type({
        resultStatus: ioTypeFromEnum('UploadStatus', UploadStatus),
        inserted: t.number,
        replaced: t.number,
        updated: t.number,
        deleted: t.number,
        unchanged: t.number,
        totalBadRecords: t.number,
        totalProcessed: t.number,
        totalRecordsBeforeLoading: t.number,
        validationResults: t.array(IOValidationResult),
    }),
    t.partial({
        description: t.string,
    }),
]);
export var IOUploadImportSummary = t.intersection([
    t.type({
        ticket: IOTicket,
        loadingResult: IOLoadingResult,
    }),
    t.partial({
        parsingResult: IOParsingResult,
    }),
]);
export var IOUploadParsingSummary = t.type({
    parsingResult: IOParsingResult,
});
