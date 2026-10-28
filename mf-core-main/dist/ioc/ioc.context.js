var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
import React from 'react';
import { rootContainer, initRootStore } from './ioc.stores';
import { logFatalError } from '../utils/logError';
var AppStore = /** @class */ (function () {
    function AppStore(container) {
        this.rootContainer = container,
            this.rootStore = __assign({}, initRootStore(rootContainer));
    }
    AppStore.prototype.init = function () {
        if (!Object.keys(this.rootStore.authStore || {}).length) {
            var error = "\n        \u0412\u043D\u0438\u043C\u0430\u043D\u0438\u0435! reBuild AuthStore \u043D\u0435 \u0441\u0440\u0430\u0431\u043E\u0442\u0430\u043B!\n        \u041F\u0440\u043E\u0432\u0435\u0440\u044C\u0442\u0435 \u043E\u0434\u0438\u043D\u0430\u043A\u043E\u0432\u044B\u0435 \u043B\u0438 \u0432\u0435\u0440\u0441\u0438\u0438 'mf-core' \u0432 Shared \u043A\u043E\u043D\u0444\u0438\u0433\u0435!\n      ";
            logFatalError(error);
            throw new Error(error);
        }
        return __assign({}, this.rootStore);
    };
    AppStore.prototype.reBuild = function (store) {
        this.rootStore = __assign(__assign({}, this.rootStore), store);
    };
    return AppStore;
}());
export { AppStore };
export var appStore = new AppStore(rootContainer);
export var createContext = function (value) {
    return React.createContext(value || null);
};
/**
  Возвращает весь контекст вместе с root контейнером.
  Используется для иницилизации контейнера во внутренних микрофронтах
*/
export var useAppStoreContext = function () {
    var context = React.useContext(createContext(appStore));
    if (!context) {
        var error = 'AppStoreContext usage before initiation.';
        logFatalError(error);
        throw new Error(error);
    }
    return context;
};
/**
  Возвращает только root store контекст.
  Используется во внутренних микрофронтах для работаы со стором
*/
export var useMfContext = function () {
    var context = useAppStoreContext();
    return __assign({}, context.init());
};
/**
  Биндинг внешних сторов, например в Авторизации.
  Внимание!!! Если в микрофронтах будут разные версии этого `mf-core` пакета
  ,то это приведет к 2 экземплярам контекста и подмена не сработает. Поэтому важно следить за версионностью!
*/
export var initProviders = function (storeName, provider) {
    return function () {
        var _a;
        try {
            appStore.reBuild((_a = {}, _a[storeName] = provider(rootContainer), _a));
        }
        catch (e) {
            logFatalError(e);
            throw new Error(e);
        }
    };
};
