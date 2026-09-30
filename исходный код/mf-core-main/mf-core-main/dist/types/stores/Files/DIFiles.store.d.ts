import type { IFilesStore, TUploadHandbookParams, TUploadImportSummary, TUploadParsingSummary } from './Files.interface';
export declare class DIFilesStore implements IFilesStore {
    private self;
    private service;
    private logger;
    parsingSummary: TUploadParsingSummary | undefined;
    loadingSummary: TUploadImportSummary | undefined;
    isUploading: boolean;
    clearSummary(): void;
    private getDataAppended;
    preloadHandbook: (values: TUploadHandbookParams) => Promise<void>;
    loadHandbook: (values: TUploadHandbookParams) => Promise<void>;
    initStore: () => void;
}
