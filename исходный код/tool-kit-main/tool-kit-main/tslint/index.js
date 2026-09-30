const eslint = require('@eslint/js');
const tseslint = require('typescript-eslint');

const pluginStylistic = require('@stylistic/eslint-plugin');
const pluginJsxA11y = require('eslint-plugin-jsx-a11y');
const pluginReact = require('eslint-plugin-react');
const pluginReactHooks = require('eslint-plugin-react-hooks');

const rules = require('./rules');

module.exports.params = {
  files: ['**/*.js*', '**/*.ts*'],
  extends: [
    eslint.configs.recommended,
    ...tseslint.configs.recommended,
    ...tseslint.configs.stylistic,
  ],
  plugins: {
    '@stylistic': pluginStylistic,
    'jsx-a11y': pluginJsxA11y,
    'react': pluginReact,
    'react-hooks': pluginReactHooks,
  },
  rules: {
    // ---- рекомендуемые правила a11y
    ...pluginJsxA11y.configs.recommended.rules,

    // ---- рекомендуемые правила стилистика
    ...pluginStylistic.configs.customize({
      semi: true, // указываем важность точки с запятой
      braceStyle: '1tbs',
    }).rules,

    // Далее переписываем на свои правила
    ...rules,
  },
}

module.exports.config = tseslint.config;
