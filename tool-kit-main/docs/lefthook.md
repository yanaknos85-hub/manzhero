# LEFTHOOK

Для подключения к хукам гита используется [lefhook](https://github.com/Arkweid/lefthook/blob/master/docs/full_guide.md).

Пример `lefthook.yml` в шаблонах, добавляется при инициализации автоматически.

Как пропускать проверки, варианты:

  git commit --no-verify
  git commit -n -am "Lefthook skipped"
  LEFTHOOK_EXCLUDE=ruby,security git commit -am "Skip some tag checks"
  LEFTHOOK=0 git push

##### Правила выставляется на основе [conventional commits](https://www.conventionalcommits.org/en/v1.0.0-beta.4/).

## Правила для бранчей

- `feat/<имя ветки>` - ветка с разработкой нового/доработкой старого функционала
- `fix/<имя ветки>` - ветка с исправлением существующего дефекта
- `refactor/<имя ветки>` - ветка с рефакторингом
- `perf/<имя ветки>` - ветка с задачами производительности
- `style/<имя ветки>` - ветка с задачами стилизации
- `docs/<имя ветки>` - ветка с задачами документации
- `test/<имя ветки>` - ветка с задачами тестирования
- `revert/<имя ветки>` - откат
- `hotfix/<имя ветки>` - зарезервировано для хотфиксов
- `release/<имя ветки>` - зарезервировано для релиз-веток

## Правила для коммитов

Формат: type: WEBSITE-XXXX commit message

Где `type` - тип работ в коммите, соответствующих [списку](https://github.com/commitizen/conventional-commit-types/blob/master/index.json)

- `feat` для фич,
- `fix` - для исправлений,
- `revert` - для ручных ревертов (*при этом стандартные сообщения реверта и мерж-коммита - валидны*),
- `refactor` - рефакторинг
- `style` - оптимизация,
- `perf` - оптимизация,
- `test` - тесты,
- `docs` - документация,
- `chore` - мелкие инфраструктрные изменения в репозитории, не учитываются в changelog.

Номер тикета берите из Jira. Нужно для связывания коммитов и тикетов.

Сообщение должно быть на английском языке (но нет строгого ограничения - может быть на русском) и наиболее полно
отражать суть изменений в атомарном коммите.

## pre-commit

- проверка git whitespaces
- проверка, что ветка соответствует правилам (см. выше)
- проверка, что мы коммитим не в protected branch

## commit-msg

- проверка, что ветка соответствует правилам (см. выше)

Для проверки тикетов можно изменить в проекте префикс тикета:

```js
  // commitlint.config.js
  parserPreset: {
    parserOpts: {
      issuePrefixes: ['TRANSPORT-'],
    },
  },
  rules: {
      'references-empty': [2, 'never'],
  }
```

## pre-push

- проверка версии node по ограничениям `engines`, закрепленным в этом репозитории,
- проверка, что мы не разошлись с target branch больше, чем на 3 коммита,
- проверка консистентности package.json / package-lock.json / node_modules.

## post-merge

- проверка консистентности package.json / package-lock.json / node_modules.
