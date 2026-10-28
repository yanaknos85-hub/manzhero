import { injectable } from 'inversify';
import { action, observable } from 'mobx';
import { IConfigStore } from './Config.interface';
import type { ENVConfig } from './Config.interface';
import { useHistory } from '../../hooks/useHistory';

const ENV_CONFIG_FILE_PATH = '/env.json';

const defaultEnvConfig: ENVConfig = {
  IS_SDO: false,
  DRIVER_APPS_URL: 'https://apps.sbertransport.ru',
  IS_PERSONAL_DEVICE: false,
};

@injectable()
export class DIConfigStore implements IConfigStore {
  @observable
    isBasicAuth = false;

  @observable
    isMockedAuth = false;

  @observable
    isMockedApi = false;

  @observable
    isCorp = false;

  @observable
    history = useHistory();

  @observable
    env: ENVConfig = {} as ENVConfig;

  constructor() {
    this.fillEnvConfig();
  }

  private fillEnvConfig() {
    fetch(ENV_CONFIG_FILE_PATH)
      .then(response => response.json())
      .then(this.parseEnv.bind(this))
      .catch(this.setDefaultConfig.bind(this));
  }

  private parseEnv(response: ENVConfig) {
    this.env = response;
  }

  private setDefaultConfig() {
    console.info('Не удалось загрузить env.json, установлен дефолтный конфиг');
    this.env = defaultEnvConfig;
  }

  @action
    setConfig = (config: Partial<IConfigStore>): void => {
      this.isBasicAuth = config.isBasicAuth || this.isBasicAuth;
      this.isMockedAuth = config.isMockedAuth || this.isMockedAuth;
      this.isMockedApi = config.isMockedApi || this.isMockedApi;
      this.history = config.history || this.history;
      this.isCorp = config.isCorp || this.isCorp;
    };
}
