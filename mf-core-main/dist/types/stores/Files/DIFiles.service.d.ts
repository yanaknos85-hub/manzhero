import { IFilesService, TUploadImportSummary, TUploadParsingSummary, getFilesArgs } from './Files.interface';
export declare class DIFilesService implements IFilesService {
    private http;
    private process;
    uploadFile(args: getFilesArgs): Promise<TUploadImportSummary>;
    preloadFile(args: getFilesArgs): Promise<TUploadParsingSummary>;
}
