import type { StorybookConfig } from '@storybook/react-webpack5';

const config: StorybookConfig = {
  stories: ["../src/**/*.mdx", "../src/**/*.stories.@(js|jsx|mjs|ts|tsx)"],
  addons: [
    "@storybook/addon-links",
    "@storybook/addon-essentials",
    "@storybook/preset-create-react-app",
    "@storybook/addon-interactions",
    // { // и без этого работает sass
    //   name: '@storybook/addon-styling',
    //   options: {
    //     sass: {
    //       // Require your Sass preprocessor here
    //       implementation: require('sass'),
    //     },
    //     // less: {
    //     //   // Require your Less preprocessor here
    //     //   implementation: require('less'),
    //     // },
    //   },
    // },
  ],
  framework: {
    name: "@storybook/react-webpack5",
    options: {},
  },
  docs: {
    autodocs: "tag",
  },
  staticDirs: ["../public"],
  // Вырубаем линтер сторибука
  webpackFinal: config => {
    return {
      ...config,
      plugins: config.plugins.filter(plugin => {
        if (plugin.constructor.name === 'ESLintWebpackPlugin') {
          return false
        }
        return true
      }),
    }
  },
}

export default config;
