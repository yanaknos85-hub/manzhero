import { UploadFile } from 'antd/lib/upload/interface';
import * as t from 'io-ts';
import { EmployeeStatus } from '../../constants/constants';
export interface IFilesStore {
    parsingSummary: TUploadParsingSummary | undefined;
    loadingSummary: TUploadImportSummary | undefined;
    isUploading: boolean;
    clearSummary(): void;
    preloadHandbook(values: TUploadHandbookParams): Promise<void>;
    loadHandbook(values: TUploadHandbookParams): Promise<void>;
    initStore(): void;
}
export interface IFilesService {
    uploadFile(args: getFilesArgs): Promise<TUploadImportSummary>;
    preloadFile(args: getFilesArgs): Promise<TUploadParsingSummary>;
}
export interface IUploadConfig {
    decSeparator: FileDecSeparator;
    nsi: FileNsi;
    orgId: string;
    separator: FileSeparator;
    strategyMode: StrategyMode;
    upload: UploadFile[];
}
export declare enum StrategyMode {
    UPDATE_OR_INSERT = "UPDATE_OR_INSERT",
    UPDATE = "UPDATE",
    INSERT = "INSERT"
}
export declare enum UploadStatus {
    SUCCESS = "SUCCESS",
    FAIL = "FAIL",
    IN_PROGRESS = "IN_PROGRESS"
}
export declare enum FileEmployeeStatus {
    OK = "OK",
    ERROR = "ERROR"
}
export declare enum FileDecSeparator {
    DOT = "DOT",
    COMMA = "COMMA"
}
export declare enum FileSeparator {
    COMMA = "COMMA",
    SEMICOLON = "SEMICOLON",
    WHITESPACE = "WHITESPACE",
    TAB = "TAB",
    OTHER = "OTHER"
}
export declare enum FileNsi {
    EMPLOYEE = "EMPLOYEE"
}
export declare enum FileHeadersPresence {
    WITH = "WITH",
    WITHOUT = "WITHOUT"
}
export declare enum FileUploadMode {
    load = "load",
    preload = "preload"
}
export interface getFilesArgs {
    orgId: string;
    strategyMode: StrategyMode;
    nsi: FileNsi;
    decSeparator: FileDecSeparator;
    separator: FileSeparator;
    data: FormData;
}
export interface TUploadHandbookParams {
    decSeparator: FileDecSeparator;
    separator: FileSeparator;
    nsi: FileNsi;
    strategyMode: StrategyMode;
    upload: UploadFile<any>[];
    headers: string;
}
export declare const IOFileEmployee: t.IntersectionC<[t.TypeC<{}>, t.PartialC<{
    id: t.StringC;
    userId: t.StringC;
    availableTransportTypes: t.UnknownArrayC;
    personalCars: t.UnknownArrayC;
    organizationId: t.StringC;
    firstName: t.StringC;
    lastName: t.StringC;
    patronymic: t.StringC;
    email: t.StringC;
    personnelNumber: t.StringC;
    mobilePhone: t.StringC;
    position: t.StringC;
    department: t.StringC;
    delegatedById: t.StringC;
    supervisorId: t.StringC;
    status: t.Type<EmployeeStatus, EmployeeStatus, unknown>;
}>]>;
export type TFileEmployee = t.TypeOf<typeof IOFileEmployee>;
export declare const IOTicket: t.TypeC<{
    operationId: t.StringC;
    dtOperation: t.StringC;
    login: t.StringC;
}>;
export type TTicket = t.TypeOf<typeof IOTicket>;
export declare const IOValidationResult: t.TypeC<{
    field: t.StringC;
    badValue: t.StringC;
    description: t.StringC;
}>;
export declare const IOEmployeeFileItem: t.IntersectionC<[t.TypeC<{
    item: t.IntersectionC<[t.TypeC<{}>, t.PartialC<{
        id: t.StringC;
        userId: t.StringC;
        availableTransportTypes: t.UnknownArrayC;
        personalCars: t.UnknownArrayC;
        organizationId: t.StringC;
        firstName: t.StringC;
        lastName: t.StringC;
        patronymic: t.StringC;
        email: t.StringC;
        personnelNumber: t.StringC;
        mobilePhone: t.StringC;
        position: t.StringC;
        department: t.StringC;
        delegatedById: t.StringC;
        supervisorId: t.StringC;
        status: t.Type<EmployeeStatus, EmployeeStatus, unknown>;
    }>]>;
    parseStatus: t.Type<FileEmployeeStatus, FileEmployeeStatus, unknown>;
    validationResults: t.ArrayC<t.TypeC<{
        field: t.StringC;
        badValue: t.StringC;
        description: t.StringC;
    }>>;
}>, t.PartialC<{
    description: t.StringC;
}>]>;
export declare const IOParsingResult: t.TypeC<{
    parseStatus: t.Type<FileEmployeeStatus, FileEmployeeStatus, unknown>;
    items: t.ArrayC<t.IntersectionC<[t.TypeC<{
        item: t.IntersectionC<[t.TypeC<{}>, t.PartialC<{
            id: t.StringC;
            userId: t.StringC;
            availableTransportTypes: t.UnknownArrayC;
            personalCars: t.UnknownArrayC;
            organizationId: t.StringC;
            firstName: t.StringC;
            lastName: t.StringC;
            patronymic: t.StringC;
            email: t.StringC;
            personnelNumber: t.StringC;
            mobilePhone: t.StringC;
            position: t.StringC;
            department: t.StringC;
            delegatedById: t.StringC;
            supervisorId: t.StringC;
            status: t.Type<EmployeeStatus, EmployeeStatus, unknown>;
        }>]>;
        parseStatus: t.Type<FileEmployeeStatus, FileEmployeeStatus, unknown>;
        validationResults: t.ArrayC<t.TypeC<{
            field: t.StringC;
            badValue: t.StringC;
            description: t.StringC;
        }>>;
    }>, t.PartialC<{
        description: t.StringC;
    }>]>>;
    totalRead: t.NumberC;
    totalBadRecords: t.NumberC;
    totalWithOutError: t.NumberC;
}>;
export type TParsingResult = t.TypeOf<typeof IOParsingResult>;
export declare const IOLoadingResult: t.IntersectionC<[t.TypeC<{
    resultStatus: t.Type<UploadStatus, UploadStatus, unknown>;
    inserted: t.NumberC;
    replaced: t.NumberC;
    updated: t.NumberC;
    deleted: t.NumberC;
    unchanged: t.NumberC;
    totalBadRecords: t.NumberC;
    totalProcessed: t.NumberC;
    totalRecordsBeforeLoading: t.NumberC;
    validationResults: t.ArrayC<t.TypeC<{
        field: t.StringC;
        badValue: t.StringC;
        description: t.StringC;
    }>>;
}>, t.PartialC<{
    description: t.StringC;
}>]>;
export type TLoadingResult = t.TypeOf<typeof IOLoadingResult>;
export declare const IOUploadImportSummary: t.IntersectionC<[t.TypeC<{
    ticket: t.TypeC<{
        operationId: t.StringC;
        dtOperation: t.StringC;
        login: t.StringC;
    }>;
    loadingResult: t.IntersectionC<[t.TypeC<{
        resultStatus: t.Type<UploadStatus, UploadStatus, unknown>;
        inserted: t.NumberC;
        replaced: t.NumberC;
        updated: t.NumberC;
        deleted: t.NumberC;
        unchanged: t.NumberC;
        totalBadRecords: t.NumberC;
        totalProcessed: t.NumberC;
        totalRecordsBeforeLoading: t.NumberC;
        validationResults: t.ArrayC<t.TypeC<{
            field: t.StringC;
            badValue: t.StringC;
            description: t.StringC;
        }>>;
    }>, t.PartialC<{
        description: t.StringC;
    }>]>;
}>, t.PartialC<{
    parsingResult: t.TypeC<{
        parseStatus: t.Type<FileEmployeeStatus, FileEmployeeStatus, unknown>;
        items: t.ArrayC<t.IntersectionC<[t.TypeC<{
            item: t.IntersectionC<[t.TypeC<{}>, t.PartialC<{
                id: t.StringC;
                userId: t.StringC;
                availableTransportTypes: t.UnknownArrayC;
                personalCars: t.UnknownArrayC;
                organizationId: t.StringC;
                firstName: t.StringC;
                lastName: t.StringC;
                patronymic: t.StringC;
                email: t.StringC;
                personnelNumber: t.StringC;
                mobilePhone: t.StringC;
                position: t.StringC;
                department: t.StringC;
                delegatedById: t.StringC;
                supervisorId: t.StringC;
                status: t.Type<EmployeeStatus, EmployeeStatus, unknown>;
            }>]>;
            parseStatus: t.Type<FileEmployeeStatus, FileEmployeeStatus, unknown>;
            validationResults: t.ArrayC<t.TypeC<{
                field: t.StringC;
                badValue: t.StringC;
                description: t.StringC;
            }>>;
        }>, t.PartialC<{
            description: t.StringC;
        }>]>>;
        totalRead: t.NumberC;
        totalBadRecords: t.NumberC;
        totalWithOutError: t.NumberC;
    }>;
}>]>;
export declare const IOUploadParsingSummary: t.TypeC<{
    parsingResult: t.TypeC<{
        parseStatus: t.Type<FileEmployeeStatus, FileEmployeeStatus, unknown>;
        items: t.ArrayC<t.IntersectionC<[t.TypeC<{
            item: t.IntersectionC<[t.TypeC<{}>, t.PartialC<{
                id: t.StringC;
                userId: t.StringC;
                availableTransportTypes: t.UnknownArrayC;
                personalCars: t.UnknownArrayC;
                organizationId: t.StringC;
                firstName: t.StringC;
                lastName: t.StringC;
                patronymic: t.StringC;
                email: t.StringC;
                personnelNumber: t.StringC;
                mobilePhone: t.StringC;
                position: t.StringC;
                department: t.StringC;
                delegatedById: t.StringC;
                supervisorId: t.StringC;
                status: t.Type<EmployeeStatus, EmployeeStatus, unknown>;
            }>]>;
            parseStatus: t.Type<FileEmployeeStatus, FileEmployeeStatus, unknown>;
            validationResults: t.ArrayC<t.TypeC<{
                field: t.StringC;
                badValue: t.StringC;
                description: t.StringC;
            }>>;
        }>, t.PartialC<{
            description: t.StringC;
        }>]>>;
        totalRead: t.NumberC;
        totalBadRecords: t.NumberC;
        totalWithOutError: t.NumberC;
    }>;
}>;
export type TUploadParsingSummary = t.TypeOf<typeof IOUploadParsingSummary>;
export type TUploadImportSummary = t.TypeOf<typeof IOUploadImportSummary>;
