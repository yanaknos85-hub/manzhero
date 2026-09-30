import React from 'react';
import { Container } from 'inversify';
import { IRootStore, RootCallBackProvider } from '../types/types';
export declare class AppStore {
    rootContainer: Container;
    rootStore: IRootStore;
    constructor(container: Container);
    init(): {
        authStore: any;
        configStore: import("..").IConfigStore;
        logger: import("..").ILogger;
        http: import("..").IHttpService;
        process: import("..").ResponseService;
        selfStore: import("..").ISelfEmployeeStore;
        employeeStore: import("..").IEmployeeStore;
    };
    reBuild(store: Partial<IRootStore>): void;
}
export declare const appStore: AppStore;
export declare const createContext: (value: AppStore) => React.Context<AppStore>;
/**
  Возвращает весь контекст вместе с root контейнером.
  Используется для иницилизации контейнера во внутренних микрофронтах
*/
export declare const useAppStoreContext: () => AppStore;
/**
  Возвращает только root store контекст.
  Используется во внутренних микрофронтах для работаы со стором
*/
export declare const useMfContext: () => {
    authStore: any;
    configStore: import("..").IConfigStore;
    logger: import("..").ILogger;
    http: import("..").IHttpService;
    process: import("..").ResponseService;
    selfStore: import("..").ISelfEmployeeStore;
    employeeStore: import("..").IEmployeeStore;
};
/**
  Биндинг внешних сторов, например в Авторизации.
  Внимание!!! Если в микрофронтах будут разные версии этого `mf-core` пакета
  ,то это приведет к 2 экземплярам контекста и подмена не сработает. Поэтому важно следить за версионностью!
*/
export declare const initProviders: (storeName: string, provider: RootCallBackProvider) => () => void;
