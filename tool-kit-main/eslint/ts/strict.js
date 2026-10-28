const configOverrides = require('../overrides/_config');

module.exports = {
  extends: require.resolve('./recommended'),
  rules: {
    // Заставляем ставить скобки в стрелочных функциях, всегда
    'arrow-parens': ['error', 'always'],
    // Строгие сравнения
    eqeqeq: ['error', 'always'],
    // Запрещаем консоль, лучше выключить правило в том месте где консоль нужна, чем случайно оставить ее в коде
    'no-console': 'error',
    // Запрещаем использовать конструктор объекта
    'no-new-object': 'error',
    // Запрещаем прочие бесполезные конструкторы
    'no-new-wrappers': 'error',
    // Предупреждаем и по желанию запрещаем всякого рода бесполезные вычисления
    'no-unused-expressions': [
      'error',
      {
        allowShortCircuit: true,
        allowTernary: true,
      },
    ],
    // Запрещаем сравнивать с NaN, везде
    'use-isnan': [
      'error',
      {
        enforceForSwitchCase: true,
        enforceForIndexOf: true,
      },
    ],
  },
  overrides: [
    {
      // enable the rule specifically for TypeScript files
      files: ['*.ts', '*.tsx'],
      rules: {
        // Настраиваем правила нейминга в проекте
        '@typescript-eslint/naming-convention': [
          // Документация https://github.com/typescript-eslint/typescript-eslint/blob/master/packages/eslint-plugin/docs/rules/naming-convention.md
          'error',
          // Делаем по умолчанию формат именвоания через camelCase
          {
            selector: 'default',
            format: ['camelCase'],
          },
          // Все классы прописываются через PascalCase
          {
            selector: 'class',
            format: ['PascalCase'],
          },
          // Все функции должны быть проименованы через camelCase, исключение для функциональных компонент - PascalCase
          {
            selector: 'function',
            format: ['camelCase', 'PascalCase'],
          },
          // Все параметры всех функций должны быть через camelCase
          {
            selector: 'parameter',
            format: ['camelCase', 'PascalCase'],
            leadingUnderscore: 'allow',
          },
          // Все проперти всех объектов и интерфейсов должны быть через camelCase, иногда делаем исключение в UPPER_CASE
          {
            selector: 'property',
            format: ['camelCase', 'UPPER_CASE', 'PascalCase'],
          },
          // Переменные нужно именовать через camelCase и PascalCase для функциональных компонент, глобальные константы через UPPER_CASE
          {
            selector: 'variable',
            format: ['camelCase', 'PascalCase', 'UPPER_CASE'],
          },
          // Интерфейсы имеются строго с I
          {
            selector: 'interface',
            format: ['PascalCase'],
            custom: {
              regex: '^I[A-Z]',
              match: true,
            },
          },
          // Типы имеются строго с T и через PascalCase
          {
            selector: 'typeAlias',
            format: ['PascalCase'],
            custom: {
              regex: '^T[A-Z]',
              match: true,
            },
          },
          // Типы имеются строго с T, практически дубль предыдущего правила, но это правило для дженериков и прочего
          {
            selector: 'typeParameter',
            format: ['PascalCase'],
            prefix: ['T'],
          },
          // Enum имеются строго с E
          {
            selector: 'enum',
            format: ['PascalCase'],
            custom: {
              regex: '^E[A-Z]',
              match: true,
            },
          },
          // Все члены Enum должны прописываться через UPPER_CASE
          {
            selector: 'enumMember',
            format: ['UPPER_CASE'],
          },
        ],
      },
    },
    // Необходимо для отключения части правил, включенных в строгом режиме
    configOverrides,
  ],
};
