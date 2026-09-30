const path = require('path');
const configOverrides = require('../overrides/_config');
const jestOverrides = require('../overrides/_jest');

module.exports = {
  extends: require.resolve('./_base'),
  parser: 'babel-eslint',
  parserOptions: {
    ecmaVersion: 7,
    ecmaFeatures: {
      jsx: true,
    },
  },
  env: {
    browser: true,
    jest: true,
  },
  overrides: [
    {
      // enable the rule specifically for TypeScript files
      files: ['*.ts', '*.tsx'],
      extends: [
        require.resolve('./_base'),
        // Uses eslint-config-prettier to disable ESLint rules from @typescript-eslint/eslint-plugin that would conflict with prettier
        'plugin:@typescript-eslint/recommended',
        'prettier/@typescript-eslint',
        'plugin:import/typescript',
      ],
      parser: '@typescript-eslint/parser',
      parserOptions: {
        project: path.resolve('tsconfig.eslint.json'),
        sourceType: 'module',
      },
      env: {
        browser: true,
        jest: true,
      },
      plugins: ['@typescript-eslint'],
      settings: {
        react: {
          version: 'detect',
        },
        'import/resolver': {
          node: {
            paths: ['src'],
            extensions: ['.js', '.jsx', '.ts', '.tsx'],
          },
          typescript: { alwaysTryTypes: true },
        },
        'import/extensions': ['.ts', '.tsx', '.d.ts', '.js', '.jsx'],
        'import/external-module-folders': ['node_modules', 'node_modules/@types'],
        'import/parsers': {
          '@typescript-eslint/parser': ['.ts', '.tsx', '.d.ts'],
        },
        'import/core-modules': [],
        'import/ignore': ['node_modules', '\\.(coffee|scss|css|less|hbs|svg|json)$'],
      },
      rules: {
        '@typescript-eslint/consistent-type-assertions': 'error',
        // Вырубаем правило запрещающее создавать пустые интерфейсы
        '@typescript-eslint/no-empty-interface': 'off',
        '@typescript-eslint/no-namespace': 'off',
        '@typescript-eslint/ban-ts-comment': 'off',
        '@typescript-eslint/prefer-namespace-keyword': 'error',
        '@typescript-eslint/triple-slash-reference': 'error',
        // Добавляем пустые строки после объявления типов, чтобы проще читать
        '@typescript-eslint/type-annotation-spacing': 'error',
        // Переопределяем правило по неиспользуемым переменным, разрешаем _ и рест операторы
        '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_', ignoreRestSiblings: true }],
        // Правило для TypeScript для использования одинарных кавычек
        '@typescript-eslint/quotes': [
          'error',
          'single',
          {
            avoidEscape: true,
          },
        ],
        /**
         * Исправления конфликта no-use-before-define
         * - note you must disable the base rule as it can report incorrect errors
         * - обратите внимание, что вы должны отключить базовое правило, так как оно может сообщать о неверных ошибках
         * @see https://stackoverflow.com/a/64024916/5378393
         */
        'no-use-before-define': 'off',
        '@typescript-eslint/no-use-before-define': ['error', { functions: false, classes: true, variables: true }],
        /**
         * Исправления конфликта no-shadow
         * - note you must disable the base rule as it can report incorrect errors
         * @see https://github.com/typescript-eslint/typescript-eslint/blob/master/packages/eslint-plugin/docs/rules/no-shadow.md#how-to-use
         */
        'no-shadow': 'off',
        '@typescript-eslint/no-shadow': ['error'],
        // Разрешаем учитывать зависимости из devDependencies, необходимо для импорта типов в TypeScript
        'import/no-extraneous-dependencies': ['error', { devDependencies: true }],
      },
    },
    configOverrides,
    jestOverrides,
  ],
};
