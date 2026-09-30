const { ESLINT_MODES } = require('@craco/craco');
const path = require('path');

const config = {
  babel: {
    loaderOptions: {
      babelrc: true,
    },
  },
  eslint: {
    mode: ESLINT_MODES.file,
  },
  style: {
    postcss: {
      mode: 'extends',
    },
  },
  typescript: {
    enableTypeChecking: true,
  },
  webpack: {
    mode: 'extends',

    configure: (webpackConfig) => {
      const scopePluginIndex = webpackConfig.resolve.plugins.findIndex(
        ({ constructor }) => constructor && constructor.name === 'ModuleScopePlugin'
      );

      webpackConfig.resolve.plugins.splice(scopePluginIndex, 1);

      webpackConfig.resolve.alias = {
        react: path.resolve('./node_modules/react'),
      }

      webpackConfig.resolve.fallback = {
        buffer: require.resolve('buffer'),
        path: false,
      }

      webpackConfig.module.rules.push({
        test: /ui-kit.+\.tsx?$/,
        loader: 'babel-loader',
        options: {
          presets: ['@babel/env', '@babel/preset-react', '@babel/preset-typescript']
        }
      });

      // see  https://github.com/webpack/webpack/issues/11467#issuecomment-808618999/
      // for details
      webpackConfig.module.rules.push({
        test: /\.m?js/,
        resolve: {
          fullySpecified: false
        }
      });

      return webpackConfig;
    },
  },
};

module.exports = config;

