import { Console, ILogger, Notify } from './Logger.interface';
export declare class CommonLogger implements ILogger {
    notificationKeys: string[];
    private configStore;
    toConsole: (type: Console, value: unknown) => void;
    toConsoleGroup: (type: Console, description: string, title: string, ...args: any[]) => void;
    toNotify: (type: Notify, description: string, title: string, duration?: number, code?: number) => void;
    toMessage: (type: Notify, description: string) => void;
    toError(description: string, title: string, ...args: any[]): void;
    closeAll: () => void;
}
