const CracoLessPlugin = require('craco-less');

const lessLoaderOptions = {
  lessOptions: {
    javascriptEnabled: true,
    modifyVars: {
      '@primary-color': '#10bf6a',
      '@success-color': '#10bf6a',
      '@error-color': '#ff4d4f',
      '@warning-color': '#ff9a32',
      '@link-color': '#4c4c4c',
      '@font-size-base': '14px',
      '@heading-color': 'rgba(0,0,0,.85)',
      '@text-color': 'rgba(0,0,0,.85)',
      '@text-color-secondary': 'rgba(0,0,0,.45)',
      '@box-shadow-base': '0 4px 12px 4px rgba(4, 0, 58, 0.04) 0 0 1px 0 rgba(3, 0, 27, 0.04)',

      '@disabled-color': 'rgba(0, 0, 0, 0.25)',
      '@border-radius-base': '8px',
      '@border-color-base': '#d9d9d9',

      '@font-color-info-message': 'rgba(0,0,0,.85)',
      '@background-color-info-message': 'transparent',

      '@font-color-warning-message': '#ff9a32',
      '@background-color-warning-message': 'transparent',

      '@font-color-error-message': '#ff4d4f',
      '@background-color-error-message': 'transparent',

      '@start-point-color': '#10bf6a',
      '@way-station-point-color': '#10bf6a',
      '@end-point-color': '#10bf6a',

      '@tabs-horizontal-margin': '0 0 0 12px',
    },
  },
};

const plugins = [
  {
    plugin: CracoLessPlugin,
    options: { lessLoaderOptions },
  },
];

module.exports = plugins;
