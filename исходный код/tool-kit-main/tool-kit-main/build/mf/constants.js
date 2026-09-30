const { getMfShared } = require('./utils');

const { REACT_APP_REMOTE, REACT_APP_NETWORK_LOOP, REACT_APP_BASIC_AUTH } = process.env;
const IS_REMOTE = REACT_APP_REMOTE === 'TRUE';
const IS_BASIC_AUTH = REACT_APP_BASIC_AUTH === 'TRUE';
const networkLoop = REACT_APP_NETWORK_LOOP;
const isBasicAuth = IS_BASIC_AUTH;

const devServerPort = 3000;

const devServerProxyTarget = {
  DEV_AUTOPARK: 'http://api.autopark.transport.apps.a37dgxlc.k8s.delta.sbrf.ru',
  DEV_AUTOSERVICE: 'http://api.autoservice.transport.apps.a37dgxlc.k8s.delta.sbrf.ru',
  DEV_SBERTRANSPORT: 'http://api.sbertransport.transport.apps.a37dgxlc.k8s.delta.sbrf.ru',
  DEV_CARGO: 'http://api.cargo.transport.apps.a37dgxlc.k8s.delta.sbrf.ru',
  ST: 'http://api.st.transport.apps.a8uoqc1q.k8s.delta.sbrf.ru',
  NT: 'http://api.nt.transport.apps.a6gyew75.k8s.delta.sbrf.ru',
  IFT: 'http://api.ift.transport.apps.a6gabrx6.k8s.delta.sbrf.ru',
};

// Конфиг Module Federation
const MF_CONFIG = {
  name: 'main',
  remotes: {
    auth: 'auth@http://localhost:3001/auth.js',
  },
  shared: getMfShared(IS_REMOTE),
};

module.exports = {
  IS_REMOTE,
  networkLoop,
  isBasicAuth,
  devServerPort,
  devServerProxyTarget,
  MF_CONFIG,
};
