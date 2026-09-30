import { CommonLogger } from './CommonLogger';
import { ILogger } from './Logger.interface';
export declare class ProductionDebugLogger extends CommonLogger implements ILogger {
    toError: (description: string, title: string, ...args: any[]) => void;
}
