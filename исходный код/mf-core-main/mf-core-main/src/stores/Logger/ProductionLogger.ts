import { injectable } from 'inversify';

import { CommonLogger } from './CommonLogger';
import { ILogger } from './Logger.interface';

@injectable()
export class ProductionLogger extends CommonLogger implements ILogger {
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  toError = (): void => {};
}
