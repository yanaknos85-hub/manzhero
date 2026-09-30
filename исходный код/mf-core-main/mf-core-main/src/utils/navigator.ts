import type { History } from 'history';
import { inject, injectable } from 'inversify';
import { computed } from 'mobx';

import {
  clear as clearStorage,
  getSudirReAuthAmt,
  removeRefreshToken,
  removeSudirReAuthAmt,
  setSudirReAuthAmt
} from './storage';

import { TYPES } from '../ioc/ioc.types';
import type { IConfigStore } from '../stores/Config/Config.interface';

type NavSudirReAuth = ({ reAuth, fail }: { reAuth: string; fail: string }) => void;

export interface INavigator {
  readonly navRoot: () => void;
  readonly navLogout: () => void;
  readonly navSudirReAuth: NavSudirReAuth;
}

@injectable()
export class Navigator implements INavigator {
  @inject(TYPES.IConfigStore)
  private configStore!: IConfigStore;

  @computed
  get history(): History | null {
    return this.configStore.history || null;
  }

  readonly navRoot = (): void => {
    removeRefreshToken();

    if (this.history) {
      this.history.push('/');
    }
  };

  readonly navLogout = (): void => {
    if (this.history) {
      this.history.push('/oauth/logout');
    }
  };

  readonly navSudirReAuth: NavSudirReAuth = ({ reAuth, fail }): void => {
    const locationContext: Location = (window as Window).location;
    const reAuthAmt: number = getSudirReAuthAmt();

    clearStorage();

    if (reAuthAmt < 3) {
      setSudirReAuthAmt(reAuthAmt + 1);
      locationContext.assign(reAuth);
    } else {
      removeSudirReAuthAmt();
      locationContext.assign(fail);
    }
  };
}
