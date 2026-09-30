/* eslint-disable no-console */
import { injectable } from 'inversify';

import { CommonLogger } from './CommonLogger';
import { ILogger } from './Logger.interface';

@injectable()
export class ProductionDebugLogger extends CommonLogger implements ILogger {
  toError = (description: string, title: string, ...args: any[]): void => {
    console.group(title);
    console.error(description);

    if (args.length > 0) {
      args.forEach(x => console.info(x));
    }

    console.groupEnd();
  };
}
