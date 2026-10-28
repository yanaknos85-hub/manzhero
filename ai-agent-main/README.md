# Front AI Agent

## Установка

```js
yarn add @sber-sbertransport/ai-agent
```

## Апгрейд версии пакета

Порядок внесения изменений:

  1) Правите локально. Делаете сборку! Далее подымаете версию `yarn version --patch`, Коммитом автоматически присваивается тег вересии пакета. Коммитите и пушите. Открываете ПР в develop
  2) Мержите в develop
  3) Идете [сюда](https://sbt-jenkins.sigma.sbrf.ru/job/Sbertransport/job/TEST/job/MICFRONTEND-DEPLOY/), выбираете репу и делаете сборку (Отправку в реджистри)
  4) Всё!

Версия должна строго следовать требованиям [semver](https://semver.org/lang/ru/)

- **Для фиксов**: `yarn version --patch`: v0.0.1 => v0.0.2
- **Для фич**: `yarn version --minor`: v0.0.2 => v0.1.0
- Первый релиз-кандидат: `yarn version --premajor`: v0.1.0 => v1.0.0-rc.0
- Следующий релиз-кандидат: `yarn version --prerelease`: v1.0.0-rc.0 => v1.0.0-rc.1
- **Релиз**: `yarn version --major`: v1.0.0-rc.1 => v1.0.0

## Работа с гитом

**[Хук-линтер коммитов](docs/lefthook.md)**

## Линтер

**[Настройка в проекта](docs/linter.md)**
