# Пример констант внутри вашего проекта

```js
// Микрофронтовый шаринг раздается на все микрофронты
const { getMfSharedConfig } = require('./utils');

const isDev = process.env.NODE_ENV === 'development';

const { REACT_APP_REMOTE, REACT_APP_NETWORK_LOOP, REACT_APP_BASIC_AUTH } = process.env;
const IS_REMOTE = REACT_APP_REMOTE === 'TRUE';
const IS_BASIC_AUTH = REACT_APP_BASIC_AUTH === 'TRUE';
const networkLoop = REACT_APP_NETWORK_LOOP;
const isBasicAuth = IS_BASIC_AUTH;

const devServerPort = 3002;

// Изменение входного пути webpack для дев разработки
const webpackDevEntryPath = 'app/dev/index.dev.ts';

// Конфиг Module Federation
const MF_CONFIG = {
  name: 'cargo',
  filename: 'cargo.js',
  exposes: {
    './bootstrap': './src/bootstrap',
    './menu': './src/modules/EmployeeApp/ui/SideMenu/MenuItems',
    './routes': './src/constants/constants.routes',
  },
  ...(isDev ? {
    remotes: {
      auth: 'auth@http://localhost:3001/auth.js',
      // auth: 'auth@http://micro-auth.platform.transport.apps.dev-terra000003-ids.ocp.delta.sbrf.ru/auth.js',
    },
  } : {}),
  shared: getMfSharedConfig(IS_REMOTE),
}

module.exports = {
  IS_REMOTE,
  networkLoop,
  isBasicAuth,
  devServerPort,
  MF_CONFIG,
  webpackDevEntryPath,
}
```
