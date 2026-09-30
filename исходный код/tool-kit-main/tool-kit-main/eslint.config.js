const { params, config } = require('./tslint');

module.exports = config({
  ...params,
  files: ['src/**/*.js*', 'src/**/*.ts*'],
  rules: {
    ...params.rules,
    // ...свои правила
  },
},
{
  ignores: [
    'src/test/*' // Закомментировать эту директорию, чтобы тестировать linter!
  ],
});
