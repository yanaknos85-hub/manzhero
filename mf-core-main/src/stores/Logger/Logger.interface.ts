export type Notify = 'info' | 'success' | 'warning' | 'error';
export type Message = 'info' | 'success' | 'warning' | 'error';
export type Console = 'info' | 'log' | 'warn' | 'error';

export interface ILogger {
  toConsole(type: Console, value: any): void;
  toConsoleGroup(type: Console, description: string, title: any, ...args: any[]): void;
  toNotify(type: Notify, description: string, title: string, duration?: number, code?: number): void;
  toMessage(type: Message, description: string): void;
  toError(description: string, title: string, ...args: any[]): void;
}
