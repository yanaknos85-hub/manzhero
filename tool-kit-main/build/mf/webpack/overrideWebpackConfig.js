const { container } = require('webpack');
const TerserPlugin = require('terser-webpack-plugin');
const { DynamicRemotes } = require('../utils');

const overrideWebpackConfig =
  (params) =>
  ({ webpackConfig, context: { paths } }) => {
    const { MF_CONFIG, IS_DEBUG, IS_REMOTE, webpackDevEntryPath } = params;
    const config = webpackConfig;
    const isDevMode = config.mode === 'development'
    const isDebug = isDevMode || IS_DEBUG;

    config.output.publicPath = 'auto';
    config.output.chunkFilename = '[id].[contenthash].js';

    config.plugins[5].options.filename = 'static/css/bundle.[name].[id].[contenthash].css';

    config.optimization = {
      chunkIds: 'named',
      minimize: !isDebug,
      minimizer: [
        new TerserPlugin({
          terserOptions: {
            compress: {
              // дропаем все кроме info
              drop_console: isDebug ? false : ['log', 'warn'],
            },
          },
        }),
      ],
    };

    const htmlWebpackPlugin = config.plugins.find((plugin) => plugin.constructor.name === 'HtmlWebpackPlugin');

    htmlWebpackPlugin.userOptions = {
      ...htmlWebpackPlugin.userOptions,
      publicPath: paths.publicUrlOrPath,
    };

    config.plugins = [
      ...config.plugins,
      ...(!IS_REMOTE && !!MF_CONFIG ? [new DynamicRemotes()] : []),
      ...(!!MF_CONFIG ? [new container.ModuleFederationPlugin(MF_CONFIG)] : []),
    ];

    if (!IS_REMOTE && isDevMode && webpackDevEntryPath && typeof config.entry === 'string') {
      config.entry = config.entry.replace(new RegExp('index.ts$'), webpackDevEntryPath);
      console.log('\x1b[35m', 'Входной путь изменен:', config.entry);
    }

    return config;
  };

module.exports = overrideWebpackConfig;
