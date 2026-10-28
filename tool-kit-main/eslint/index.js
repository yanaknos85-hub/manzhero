const jestOverrides = require('./overrides/_jest');

// TODO вынести модульно
//  , 'plugin:prettier/recommended',
//         'prettier',
module.exports = {
  extends: ['airbnb'],
  parser: 'babel-eslint',
  parserOptions: {
    ecmaVersion: 7,
    ecmaFeatures: {
      jsx: true,
    },
  },
  env: {
    browser: true,
    jquery: true,
    node: false,
    commonjs: false,
  },
  globals: {},
  plugins: [
    'compat',
    'import',
    'react',
    'react-hooks',
    'jsx-a11y',
    'jquery',
    'eslint-comments',
    // 'promise',
    'unicorn',
    'jsdoc',
  ],
  rules: {
    'no-warning-comments': ['warn', { terms: ['todo', 'fixme', 'xxx'], location: 'start' }],

    'max-len': [
      'error',
      120,
      4,
      {
        ignoreComments: true,
        ignoreTrailingComments: true,
        ignoreUrls: true,
      },
    ],
    // предупреждение?
    'no-trailing-spaces': [
      'warn',
      {
        skipBlankLines: false,
        ignoreComments: false,
      },
    ],
    indent: ['error', 2, { SwitchCase: 1 }],
    'react/jsx-indent': ['error', 2],
    'react/jsx-indent-props': ['error', 2],

    'func-names': 'off',

    // кто-то юзает гит без должной настройки, нивелируется сборкой как минимум
    'linebreak-style': 'off',
    'no-else-return': 'off',

    // для process.env.NODE === 'development
    'no-process-env': 'off',

    'react/state-in-constructor': 'off',

    'no-console': 'warn',
    camelcase: [
      'error',
      {
        properties: 'never',
        allow: ['^UNSAFE_'],
      },
    ],
    'comma-dangle': [
      'error',
      {
        arrays: 'always-multiline',
        objects: 'always-multiline',
        imports: 'never',
        exports: 'never',
        functions: 'never',
      },
    ],
    'no-plusplus': [
      'error',
      {
        allowForLoopAfterthoughts: true,
      },
    ],
    'no-multiple-empty-lines': ['error', { max: 1 }],
    'space-before-function-paren': ['error', { anonymous: 'never', named: 'never' }],
    curly: ['error', 'all'],
    'new-cap': 'off',
    'consistent-this': ['error', 'self'],
    'arrow-parens': ['error', 'as-needed'],
    'prefer-arrow-callback': [
      'error',
      {
        allowNamedFunctions: true,
        allowUnboundThis: true,
      },
    ],
    'prefer-template': 'warn',
    'prefer-destructuring': 'off',
    'no-return-assign': 'off', // из-за ref => this.ref = ref, например
    'operator-linebreak': 'off',
    'no-use-before-define': ['error', { functions: false, classes: true, variables: true }],
    'no-mixed-operators': [
      'warn',
      {
        // the list of arthmetic groups disallows mixing `%` and `**`
        // with other arithmetic operators.
        groups: [
          ['%', '**'],
          ['%', '+'],
          ['%', '-'],
          ['%', '*'],
          ['%', '/'],
          ['**', '+'],
          ['**', '-'],
          ['**', '*'],
          ['**', '/'],
          ['&', '|', '^', '~', '<<', '>>', '>>>'],
          ['==', '!=', '===', '!==', '>', '>=', '<', '<='],
          ['in', 'instanceof'],
        ],
        allowSamePrecedence: false,
      },
    ],

    'react/static-property-placement': ['error', 'static public field'],
    'react/jsx-props-no-spreading': 'off', // спред объекта в пропсы удобен, но опасен
    'react/destructuring-assignment': 'off',
    'react/no-array-index-key': 'off',
    'react/prop-types': 'off',
    'react/no-multi-comp': 'off',
    'react/prefer-stateless-function': 'off',
    'react/prefer-es6-class': ['warn', 'always'],
    'react/jsx-boolean-value': ['error', 'always'],
    'react/sort-comp': [
      'warn',
      {
        order: [
          'static-methods',
          '/^(?!handle|render).+$/',
          'lifecycle',
          '/^(?!handle|render).+$/',
          'render',
          '/^(?!handle).+$/',
          '/^(?!render).+$/',
        ],
      },
    ],

    'react-hooks/rules-of-hooks': 'error',
    'react-hooks/exhaustive-deps': 'warn',

    'jsx-a11y/label-has-for': [
      'error',
      {
        components: ['label'],
        required: {
          some: ['nesting', 'id'],
        },
        allowChildren: false,
      },
    ],
    'jsx-a11y/label-has-associated-control': [
      'error',
      {
        labelComponents: ['label'],
        labelAttributes: ['htmlFor'],
        controlComponents: [
          'Input',
          'MaskedInput',
          'RegexpInput',
          'Textarea',
          'Dropdown',
          'Radio',
          'Checkbox',
          'input',
          'select',
          'textarea',
        ],
      },
    ],

    // api / urls и прочее - для нас лишнее правило
    'import/prefer-default-export': 'off',
    'import/no-webpack-loader-syntax': 'off',
    'import/no-unresolved': ['warn', { ignore: ['^!file'] }],
    'import/dynamic-import-chunkname': [
      2,
      {
        importFunctions: ['dynamicImport'],
        webpackChunknameFormat: '[[a-zA-Z0-9-/_]+',
      },
    ],
    'import/no-anonymous-default-export': [
      'warn',
      {
        allowArray: false,
        allowLiteral: false,

        allowArrowFunction: true,
        allowAnonymousClass: true,
        allowAnonymousFunction: true,
        allowCallExpression: true, // The true value here is for backward compatibility
        allowObject: true,
      },
    ],

    // включаем базовые проверки jsdoc
    'jsdoc/check-alignment': 'warn',
    'jsdoc/check-examples': 'warn',
    'jsdoc/check-indentation': 'warn',
    'jsdoc/check-param-names': 'warn',
    'jsdoc/check-syntax': 'warn',
    'jsdoc/check-tag-names': 'warn',
    'jsdoc/check-types': 'warn',

    // 'promise/always-return': 'warn',
    // 'promise/no-return-wrap': 'error',
    // 'promise/param-names': 'error',
    // 'promise/prefer-await-to-then': 'off', // обсуждаемо

    // 'sonarjs/no-all-duplicated-branches': 'error',
    // 'sonarjs/no-identical-conditions': 'error',
    // 'sonarjs/prefer-single-boolean-return': 'warn',
    // 'sonarjs/no-duplicated-branches': 'warn',
    // 'sonarjs/no-use-of-empty-return-value': 'warn',
    // 'sonarjs/no-duplicate-string': 'warn',
    // 'sonarjs/max-switch-cases': ['warn', 10],
    // 'sonarjs/no-small-switch': ['warn', 3],

    'unicorn/no-abusive-eslint-disable': 'error',
    'unicorn/prefer-includes': 'warn',
    // советы по новому DOM API
    // 'unicorn/prefer-modern-dom-apis': 'warn',
    'unicorn/prefer-add-event-listener': 'warn',
    'unicorn/prefer-node-append': 'warn',
    'unicorn/prefer-node-remove': 'warn',
    'unicorn/prefer-query-selector': 'warn',
    'unicorn/prefer-text-content': 'warn',
    // 'unicorn/prefer-dataset': 'warn',

    // запрещаем полные выключения правил
    'eslint-comments/no-unlimited-disable': 'error',

    // jquery is almost deprecated
    // we use ajax only
    'jquery/no-ajax': 'warn',
    'jquery/no-ajax-events': 'warn',

    // jquery things is deprecated
    'jquery/no-animate': 'error',
    'jquery/no-attr': 'error',
    'jquery/no-bind': 'error',
    'jquery/no-class': 'error',
    'jquery/no-clone': 'error',
    'jquery/no-closest': 'error',
    'jquery/no-css': 'error',
    'jquery/no-data': 'error',
    'jquery/no-deferred': 'error',
    'jquery/no-delegate': 'error',
    'jquery/no-each': 'error',
    'jquery/no-extend': 'error',
    'jquery/no-fade': 'error',
    'jquery/no-filter': 'error',
    'jquery/no-find': 'error',
    'jquery/no-global-eval': 'error',
    'jquery/no-grep': 'error',
    'jquery/no-has': 'error',
    'jquery/no-hide': 'error',
    'jquery/no-html': 'error',
    'jquery/no-in-array': 'error',
    'jquery/no-is-array': 'error',
    'jquery/no-is-function': 'error',
    'jquery/no-is': 'error',
    'jquery/no-load': 'error',
    'jquery/no-map': 'error',
    'jquery/no-merge': 'error',
    'jquery/no-param': 'error',
    'jquery/no-parent': 'error',
    'jquery/no-parents': 'error',
    'jquery/no-parse-html': 'error',
    'jquery/no-prop': 'error',
    'jquery/no-proxy': 'error',
    'jquery/no-ready': 'error',
    'jquery/no-serialize': 'error',
    'jquery/no-show': 'error',
    'jquery/no-size': 'error',
    'jquery/no-sizzle': 'error',
    'jquery/no-slide': 'error',
    'jquery/no-submit': 'error',
    'jquery/no-text': 'error',
    'jquery/no-toggle': 'error',
    'jquery/no-trigger': 'error',
    'jquery/no-trim': 'error',
    'jquery/no-val': 'error',
    'jquery/no-when': 'error',
    'jquery/no-wrap': 'error',

    // более гибкие запреты (например, на импорт из другого виджета) пока нельзя
    // https://github.com/benmosher/eslint-plugin-import/issues/1132
    // запрещенка
    'no-restricted-imports': [
      'error',
      {
        // zones: [{ 'target': './src/js', 'from': './src/server/' }],
        patterns: ['lodash/*'],
        paths: [
          'moment',
          'modernizr',
          'underscore',
          'lodash',
          // тут запрещаем юзать без тришейкинга, на самом деле
          'date-fns',
        ],
      },
    ],
  },
  overrides: [
    {
      files: [
        'eslint/*.js',
        'jest/*.js',
        'stylelint/*.js',
        'dev-server.js',
        'webpack.config.js',
        'webpack.*.js',
        'babel.config.js',
        'babel.*.js',
        '.eslint*.js',
        'commitlint.config.js',
        'stylelint.config.js',
        '*jest*.config.js',
        'postcss.config.js',
        'postcss.*.js',
        'moment.js',
        // обычное расположение эмулятора
        'server/**/*.js',
        'devServer/**/*.js',
        // Скрипты для сборки
        'scripts/**/*.js',
      ],
      env: {
        browser: false,
        jquery: false,
        node: true,
        commonjs: true,
      },
      rules: {
        // можно работать с либами из дев-конфига
        'import/no-extraneous-dependencies': 'off',
        'import/no-dynamic-require': 'off',
        'global-require': 'off',
        'prefer-const': 'warn',
        'prefer-template': 'off',
        'quote-props': 'off',
        'no-extra-boolean-cast': 'off',
        'no-unused-expressions': 'off',
        'no-console': 'off',
      },
    },
    jestOverrides,
  ],
  settings: {
    polyfills: [
      'es6',
      'es6-number',
      'object-values',
      'Promise',
      'performance',
      'fetch',
      'AbortController',
      'Object.values',
      'Object.entries',
      'Object.assign',
      'Number.MAX_SAFE_INTEGER',
      'Number.MIN_SAFE_INTEGER',
      'Number.EPSILON',
      'Number.isNaN',
      'Number.isInteger',
      'Number.isSafeInteger',
      'Number.isFinite',
    ],
  },
};
