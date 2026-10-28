import { TLoadingResult, TTicket, TUploadImportSummary } from '../Files.interface';
export declare class UploadImportSummaryModel implements TUploadImportSummary {
    ticket: TTicket;
    loadingResult: TLoadingResult;
    constructor(file: TUploadImportSummary);
}
