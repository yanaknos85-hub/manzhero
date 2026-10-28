const { params, config } = require('@sber-sbertransport/tool-kit/tslint');

module.exports = config({
  ...params,
  files: ['src/**/*.js*', 'src/**/*.ts*'],
  rules: {
    ...params.rules,
    // ...свои правила
    '@typescript-eslint/no-explicit-any': 0,
    'no-use-before-define': 0,
  }
},{
  ignores: [
    '**/*/*.d.ts',
  ],
});
