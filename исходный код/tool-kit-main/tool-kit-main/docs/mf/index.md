# Сборка микрофронта

## Запуск в проекте (package.json)

```js
craco start --config scripts/index.js
```

Где index - ваш файл. Пример:

```js
module.exports = require('@sber-sbertransport/tool-kit/build/mf');
```

Или настраиваемый вариант:

**[Константы](constants.md)**

**[Прокси таргеты](networks.md)**

```js
const cracoConfig = require('@sber-sbertransport/tool-kit/build/mf/craco/cracoConfig');
const cracoPlugins = require('@sber-sbertransport/tool-kit/build/mf/craco/cracoPlugins');
const overrideCracoConfig = require('.@sber-sbertransport/tool-kit/build/mf/craco/overrideCracoConfig');
const overrideWebpackConfig = require('@sber-sbertransport/tool-kit/build/mf/webpack/overrideWebpackConfig');
const devServer = require('@sber-sbertransport/tool-kit/build/mf/devServer/devServer');
const { devServerProxyTarget } = require('@sber-sbertransport/tool-kit/build/mf/constants');

const mocksRouter = require('./devServer/devServer.mocks.router');

const {
  IS_REMOTE,
  networkLoop,
  isBasicAuth,
  devServerPort,
  MF_CONFIG,
  webpackDevEntryPath,
} = require('./constants'); // Ваши константы

const config = {
    // Базовый крако конфиг
  ...cracoConfig,

  devServer: {
    ...devServer({
      port: devServerPort,
      proxyTarget: devServerProxyTarget,
      networkLoop,
      isBasicAuth,
    }),

    // Если необходимо (добавить прокси и тд) можно доконфигурировать дальше
    // ...

    setupMiddlewares: (middlewares, devServer) => {
      if (!devServer) {
        throw new Error('webpack-dev-server is not defined');
      }
      devServer.app.use('/api/mock', mocksRouter);
      return middlewares;
    },
  },
  plugins: [
      // Базовые плагины крако
    ...cracoPlugins,
    {
      plugin: {
        // Перенастройка крако если надо
        overrideCracoConfig: ({ cracoConfig }) => {
           // ...
        }
      },
      plugin: {
        // Перенастройка веб пака
        overrideWebpackConfig: overrideWebpackConfig({
          IS_REMOTE,
          MF_CONFIG,
          webpackDevEntryPath,
        }),
      },
    },
  ],
};


module.exports = config;

```
