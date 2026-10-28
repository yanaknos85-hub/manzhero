module.exports = {
  // ---- базовые

  // Предупреждаем об оставленном console
  'no-console': 'warn',
  // Запрещаем дебаггер
  'no-debugger': 'error',
  // Запрещаем высшее зло
  'no-eval': 'error',
  'no-use-before-define': ['error', { functions: false, classes: true, variables: true }],
  'comma-dangle': 'off',

  // ---- typescript

  // Переопределяем правило по неиспользуемым переменным, разрешаем _ и рест операторы
  '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_', ignoreRestSiblings: true }],
  '@typescript-eslint/no-empty-interface': 'off',
  '@typescript-eslint/no-namespace': 'off',
  '@typescript-eslint/ban-ts-comment': 'off',

  // ---- react

  // Включаем всякие прикольные штуки для хуков
  'react-hooks/exhaustive-deps': 'warn',
  'react-hooks/rules-of-hooks': 'error',
  // Отключено, поскольку слишком много ложных срабатываний.
  'react/jsx-key': 'off',
  'react/display-name': ['off', { ignoreTranspilerName: false }],

  // ---- stylistic / jsx

  '@stylistic/max-len': [
    'error',
    120,
    4,
    {
      ignoreUrls: true,
      ignoreComments: true,
      ignoreRegExpLiterals: true,
      ignoreStrings: true,
      ignoreTemplateLiterals: true,
      ignoreTrailingComments: true,
    },
  ],
  // При перемносах - всегда скобки в стрелочных функциях
  '@stylistic/implicit-arrow-linebreak': ['error', 'beside'],
  // Используем одинарные кавычки для строковых переменных
  '@stylistic/quotes': ['error', 'single', { allowTemplateLiterals: true }],
  // Всегда отступы внутри скобок объекта
  '@stylistic/object-curly-spacing': ['error', 'always'],
  // Перенос строки внутри объекта
  '@stylistic/object-curly-newline': ['error', {
    ObjectExpression: { minProperties: 3, multiline: true, consistent: true },
    ObjectPattern: { minProperties: 3, multiline: true, consistent: true },
    ImportDeclaration: { minProperties: 4, multiline: true, consistent: true },
    ExportDeclaration: { minProperties: 4, multiline: true, consistent: true },
  }],

  // Вырубаем
  '@stylistic/multiline-ternary': ['off', 'never'],
  // Запятая на конце объекти или массива
  '@stylistic/comma-dangle': [
     'error',
     {
       arrays: 'always-multiline',
       objects: 'always-multiline',
       imports: 'never',
       exports: 'never',
       functions: 'never',
       enums: 'always-multiline',
       generics: 'never',
       tuples: 'never',
     },
  ],
  // Запрещаем и удаляем бесполезные пробелы
  '@stylistic/no-trailing-spaces': 'error',
  // Запрещаем больше одной пустой строчки подряд
  '@stylistic/no-multiple-empty-lines': ['error', { max: 1 }],
  '@stylistic/indent':['error', 2, {
    SwitchCase: 1,
    VariableDeclarator: 1,
    outerIIFEBody: 1,
    // MemberExpression: null,
    FunctionDeclaration: {
      parameters: 1,
      body: 1
    },
    FunctionExpression: {
      parameters: 1,
      body: 1
    },
    CallExpression: {
      arguments: 1
    },
    ArrayExpression: 1,
    ObjectExpression: 1,
    ImportDeclaration: 1,
    flatTernaryExpressions: false,
    // list derived from https://github.com/benjamn/ast-types/blob/HEAD/def/jsx.js
    ignoredNodes: ['JSXElement', 'JSXElement > *', 'JSXAttribute', 'JSXIdentifier', 'JSXNamespacedName', 'JSXMemberExpression', 'JSXSpreadAttribute', 'JSXExpressionContainer', 'JSXOpeningElement', 'JSXClosingElement', 'JSXFragment', 'JSXOpeningFragment', 'JSXClosingFragment', 'JSXText', 'JSXEmptyExpression', 'JSXSpreadChild'],
    ignoreComments: false
  }],

  // ---- jsx

  '@stylistic/jsx-indent': ['error', 2],
  '@stylistic/jsx-indent-props': ['error', 2],
  // 2й пропс - всегда с новой строки
  '@stylistic/jsx-first-prop-new-line': ['error', 'multiline-multiprop'],
  // в строку максмум - 2 пропса, в мульти - 1
  '@stylistic/jsx-max-props-per-line': ['error', { maximum: { single: 2, multi: 1 } }],
  // Только 1 сущность может быть в одну строку в родительском компоненте
  '@stylistic/jsx-one-expression-per-line': 'off',
  // Скобки вокруг элемента
  '@stylistic/jsx-wrap-multilines': [
    'error',
    {
      declaration: 'parens-new-line',
      assignment: 'parens-new-line',
      return: 'parens-new-line',
      arrow: 'parens-new-line',
      condition: 'parens-new-line',
      logical: 'parens-new-line',
      prop: 'parens-new-line',
    }
  ],
  //  Скобки вокруг пропсов функции
  '@stylistic/arrow-parens': ['error', 'as-needed'],

  // ---- a11y

  // Вырубаем правило запрещающее навешивать на статичные элементы хендлеры TODO Продумать хорошенько это правило
  'jsx-a11y/no-static-element-interactions': 'off',
  // Вырубаем правило обязывающее навешивать доп события, типо onKeyUp, onKeyDown, onKeyPress
  'jsx-a11y/click-events-have-key-events': 'off',
  // Правило для лейблов
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
  'jsx-a11y/alt-text': ['error', {
    elements: ['img', 'object', 'area', 'input[type="image"]'],
    img: [],
    object: [],
    area: [],
    'input[type="image"]': [],
  }],
  'jsx-a11y/anchor-has-content': ['error', { components: [] }],
  'jsx-a11y/anchor-is-valid': ['error', {
    components: ['Link'],
    specialLink: ['to'],
    aspects: ['noHref', 'invalidHref', 'preferButton'],
  }],
  'jsx-a11y/aria-role': ['error', { ignoreNonDOM: false }],
  'jsx-a11y/autocomplete-valid': ['off', {
    inputComponents: [],
  }],
}
