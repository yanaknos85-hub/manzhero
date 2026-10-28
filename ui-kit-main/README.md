# Front UI Kit

## Установка Сторибука для разработки

Выбираем версию всех пакетов сторибука (если попросит) 7.2.0

```js
yarn сi
```

## Запуск Сторибука для разработки

```js
yarn dev
```

## Сборка компонентов в бандл (если необхоимо!)

Сейчас в проект подключаются исходники (src), а не из папки dist!
Поэтому необходимости в сборке нет

```js
yarn build
```

___

## Установка в проекте

```js
yarn add @sber-sbertransport/ui-kit
```

Обязательно в проекте в вебпаке установить алиас Реакта.
Иначе могу быть проблемы с несколькими библиотеками Реакта

```js
config.resolve.alias = {
  react: path.resolve('./node_modules/react')
}
```

А также добавить правило обработк сорсов кита

```js
config.module.rules.push(
  {
    test: /ui-kit.+\.tsx?$/,
    loader: "babel-loader",
    options: {
      presets: ['@babel/env', '@babel/preset-react', '@babel/preset-typescript']
    }
  },
);
```

Возможно понадобятся правки в конфиге типа:

```js
config.resolve.fallback = {
  buffer: require.resolve('buffer'),
  path: false,
}
```

## Подключение компонентов в проекте

```tsx
import { ConfigProvider, Button } from '@sber-sbertransport/ui-kit/src';
//  или точечно
import { Button } from '@sber-sbertransport/ui-kit/src/components/Button';

<ConfigProvider>
  <Button type="primary" size="large">Hello</Button>
</ConfigProvider>
```

___

## Апгрейд версии кита

Версия должна строго следовать требованиям [semver](https://semver.org/lang/ru/)

- **Для фиксов**: `yarn version --patch`: v0.0.1 => v0.0.2
- **Для фич**: `yarn version --minor`: v0.0.2 => v0.1.0
- Первый релиз-кандидат: `yarn version --premajor`: v0.1.0 => v1.0.0-rc.0
- Следующий релиз-кандидат: `yarn version --prerelease`: v1.0.0-rc.0 => v1.0.0-rc.1
- **Релиз**: `yarn version --major`: v1.0.0-rc.1 => v1.0.0

### Подробнее [в статье "Работа с NPM"](https://confluence.sberbank.ru/pages/viewpage.action?pageId=8501957813)
