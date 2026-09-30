import { CommonLogger } from './CommonLogger';
import { ILogger } from './Logger.interface';
export declare class ProductionLogger extends CommonLogger implements ILogger {
    toError: () => void;
}
