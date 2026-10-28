# Front Core Microfront - только Контекст

По-сути используются 3 ручки:

```doc
useAppStoreContext - для иницилизации ioc контейнера в микрофронтах

useMfContext - для использования общего стора во внутренних микрофронтах

initProviders - для биндинга сторов во внутренних микрофронтах, типа Авторизации
```

*Также можно создавать общие контексты для использования внутри микрофронтов при необходимости

## Сборка

```js
yarn build
```

## Установка

```js
yarn add @sber-sbertransport/mf-core --save-dev
```

## Апгрейд версии пакета

Порядок внесения изменений:

  1) Правите локально. Делаете сборку! Далее подымаете версию `yarn version --patch`, Коммитом автоматически присваивается тег вересии пакета. Коммитите и пушите. Открываете ПР в develop
  2) Мержите в develop
  4) Идете [сюда](https://sbt-jenkins.sigma.sbrf.ru/job/Sbertransport/job/TEST/job/MICFRONTEND-DEPLOY/), выбираете репу mf и делаете сборку (Отправку в реджистри)
  5) Идете в репозиторий [as-sbertransport-front-tool-kit](https://stash.sigma.sbrf.ru/projects/TRANSPORT/repos/as-sbertransport-front-tool-kit/browse). Правите код [build/mf/mf.config.shared.js](https://stash.sigma.sbrf.ru/projects/TRANSPORT/repos/as-sbertransport-front-tool-kit/browse/build/mf/mf.config.shared.js). А именно - выставляете руками НОВУЮ версию пакета mf. Это нужно для того, чтобы версии во всех микрофронтах были одинаковые для Module Federation. Иначе проект просто не заведется! Далее АПАЕТЕ либу tool-kit по тому же принципу:  Правка, ПР, мерж в develop, `yarn version --patch`
  6) ВАЖНО! обновить все репы с микрофронтами для единообразия контекста, в том числе Auth! Делается это так:
    `yarn up`
  7) Все!

Версия должна строго следовать требованиям [semver](https://semver.org/lang/ru/)

- **Для фиксов**: `yarn version --patch`: v0.0.1 => v0.0.2
- **Для фич**: `yarn version --minor`: v0.0.2 => v0.1.0
- Первый релиз-кандидат: `yarn version --premajor`: v0.1.0 => v1.0.0-rc.0
- Следующий релиз-кандидат: `yarn version --prerelease`: v1.0.0-rc.0 => v1.0.0-rc.1
- **Релиз**: `yarn version --major`: v1.0.0-rc.1 => v1.0.0
