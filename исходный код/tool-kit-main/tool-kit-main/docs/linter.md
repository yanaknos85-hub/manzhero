# Линтер

*Внимание! Рабочий линтер находится в папке tslint, вместо устаревшей eslint!*

## Документация и правила

**[typescript-eslint](https://typescript-eslint.io/rules/)**
**[eslint](https://eslint.style/rules)**

**[Наши правила](../tslint/rules.js)**

## Установка в проект

**Внимание! Скорее всего будет необходимо установить eslint в проект! Версия 8.56.0 дожна быть приоритетной! Проверяется командой: yarn why eslint**

Flat конфиг: eslint.config.js

```js

const { params, config } = require('@sber-sbertransport/tool-kit/tslint');

module.exports = config({
  ...params,
  rules: {
    ...params.rules,
    // ...свои правила
  }
});
```

## Устаревший вариант (!)

.eslintrc.js

```js

module.exports = {
  root: true,
  extends: [
    require.resolve('@sber-sbertransport/tool-kit/eslint'),
    require.resolve('@sber-sbertransport/tool-kit/eslint/ts/recommended'),
  ],
  globals: {
    process: true,
  },
  rules: {
    // ...свои правила
  },
};
```

## Варианты запуска в package.json

```js
"lint": "eslint src",
"lint:js": "eslint src/**/*.js*",
"lint:ts": "eslint src/**/*.ts*",
```
