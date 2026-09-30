import React from 'react';
import { Container } from 'inversify';
import { IRootStore, RootCallBackProvider } from '../types/types';
import { rootContainer, initRootStore } from './ioc.stores';
import { logFatalError } from '../utils/logError';

export class AppStore {
  rootContainer: Container;
  rootStore: IRootStore;

  constructor(container: Container) {
    this.rootContainer = container,

    this.rootStore = {
      ...initRootStore(rootContainer),
    };
  }

  init() {
    if (!Object.keys(this.rootStore.authStore || {}).length) {
      const error = `
        Внимание! reBuild AuthStore не сработал!
        Проверьте одинаковые ли версии 'mf-core' в Shared конфиге!
      `;
      logFatalError(error);
      throw new Error(error);
    }

    return {
      ...this.rootStore,
    };
  }

  reBuild(store: Partial<IRootStore>) {
    this.rootStore = {
      ...this.rootStore,
      ...store,
    };
  }
}

export const appStore = new AppStore(rootContainer);

export const createContext = (value: AppStore) => {
  return React.createContext(value || null);
};

/**
  Возвращает весь контекст вместе с root контейнером.
  Используется для иницилизации контейнера во внутренних микрофронтах
*/
export const useAppStoreContext = (): AppStore => {
  const context: AppStore | null = React.useContext<AppStore>(createContext(appStore));
  if (!context) {
    const error = 'AppStoreContext usage before initiation.';
    logFatalError(error);
    throw new Error(error);
  }
  return context;
};

/**
  Возвращает только root store контекст.
  Используется во внутренних микрофронтах для работаы со стором
*/
export const useMfContext = () => {
  const context = useAppStoreContext();
  return { ...context.init() };
};

/**
  Биндинг внешних сторов, например в Авторизации.
  Внимание!!! Если в микрофронтах будут разные версии этого `mf-core` пакета
  ,то это приведет к 2 экземплярам контекста и подмена не сработает. Поэтому важно следить за версионностью!
*/
export const initProviders = (storeName: string, provider: RootCallBackProvider) => {
  return () => {
    try {
      appStore.reBuild({ [storeName]: provider(rootContainer) });
    } catch (e: any) {
      logFatalError(e);
      throw new Error(e);
    }
  };
};
