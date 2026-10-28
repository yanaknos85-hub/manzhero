const fs = require("fs");

const mocksRouter = require('./devServer.mocks.router');

const sudirRule = (uri) => (req, res, next) =>
  req.path.includes('sudir/oauth2') ? res.writeHead(302, { Location: uri + req.baseUrl + req.path }).end() : next();

module.exports = ({ port, proxyTarget, networkLoop, isBasicAuth, hot, ...params }) => {
  const target = proxyTarget[networkLoop];

  const proxy = (options) => ({
    ...{
      target,
      changeOrigin: true,
      secure: false,
    },
    ...options,
  });

  return {
    hot: !!hot,  // Отключем при использовании MF. Ошибки HMR (Update failed: ChunkLoadError: Loading hot update chunk). Возможно из за Module Federation!
    open: false, // Открывать в браузере
    port,
    client: {
      logging: 'log',
      progress: false,
      overlay: {
        errors: true,
        warnings: false,
        runtimeErrors: false, // Не выводить в браузер!
      },
    },
    setupMiddlewares: (middlewares, devServer) => {
      if (!devServer) {
        throw new Error('webpack-dev-server is not defined');
      }
      devServer.app.get('/apps.json', (_, response) => {
        response.send({
          fleet: 'http://micro-fleet.platform.transport.apps.dev-terra000003-ids.ocp.delta.sbrf.ru',
          cargo: 'http://micro-cargo.platform.transport.apps.dev-terra000003-ids.ocp.delta.sbrf.ru',
          auth: 'http://micro-auth.platform.transport.apps.dev-terra000003-ids.ocp.delta.sbrf.ru',
          passengers: 'http://micro-platform.cargo.transport.apps.dev-terra000003-ids.ocp.delta.sbrf.ru',
        });
      });

      devServer.app.use('/api/mock', mocksRouter);

      return middlewares;
    },
    proxy: {
      '/api': ((_proxy) => (!isBasicAuth ? [sudirRule(target), _proxy] : _proxy))(proxy({ ws: true })),
      '/tiles': proxy(),
    },
    ...params,
  };
};
