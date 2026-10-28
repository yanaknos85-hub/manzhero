import { History } from 'history';

export interface ENVConfig {
  /** Является ли ДЗО */
  IS_SDO: boolean;
  /** Ссылка на приложения водителей */
  DRIVER_APPS_URL: string;
  /** Является ли устройство мобильным */
  IS_PERSONAL_DEVICE: boolean;
}

export interface IConfigStore {
  isBasicAuth: boolean;
  isMockedAuth: boolean;
  isMockedApi: boolean;
  history: History;
  env: ENVConfig;
  /**
   * Должно быть true для корпа и диспетчерской
   * - Не будет использоваться selfStore (вместо него useProfile)
   * - Своя обработка ошибок в httpMiddleware
  */
  isCorp?: boolean;

  setConfig(config: Partial<IConfigStore>): void;
}
