/* eslint-disable no-unused-vars */
const overrideCracoConfig =
  (params) =>
  ({ cracoConfig }) => {
    console.dir({
      host: `http://127.0.0.1:${cracoConfig.devServer.port}`,
      api: cracoConfig.devServer.proxy['/api'].target,
      tiles: cracoConfig.devServer.proxy['/tiles'].target,
    });
    return cracoConfig;
  };

module.exports = overrideCracoConfig;
