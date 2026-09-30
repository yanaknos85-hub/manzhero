const constants = require('./constants');
const cracoConfig = require('./craco/cracoConfig');
const cracoPlugins = require('./craco/cracoPlugins');
const overrideCracoConfig = require('./craco/overrideCracoConfig');
const devServer = require('./devServer/devServer');
const overrideWebpackConfig = require('./webpack/overrideWebpackConfig');

const config = (params) => ({
  ...cracoConfig,
  devServer: devServer({
    port: params.devServerPort,
    proxyTarget: params.devServerProxyTarget || params.devServerProxyTarget.DEV_AUTOSERVICE,
    networkLoop: constants.networkLoop,
    isBasicAuth: constants.isBasicAuth,
    hot: params.hot,
  }),
  plugins: [
    ...cracoPlugins,
    {
      plugin: {
        overrideCracoConfig: overrideCracoConfig(),
      },
    },
    {
      plugin: {
        overrideWebpackConfig: overrideWebpackConfig({
          IS_DEBUG: params.IS_DEBUG,
          IS_REMOTE: params.IS_REMOTE,
          MF_CONFIG: params.MF_CONFIG,
          webpackDevEntryPath: params.webpackDevEntryPath,
        }),
      },
    },
  ],
});

module.exports = config;
