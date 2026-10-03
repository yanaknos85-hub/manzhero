# НАХОДКИ: исследование платформы для ТЗ «Избранные маршруты» (FavoriteRoute), Манжерок

Запись от 29.09.2026. Первая редакция файла (ранее файл не существовал). Инструкция: `CLAUDE_FavoriteRoute.md`.
Исходники и документы читались без изменений; сборки, тесты, миграции и запросы к стенду не выполнялись.
Пароли, токены и ключи из конфигураций в отчёт не переносились — приводятся только имена параметров.

Метки доказательств: `[КОД]`, `[ТЕСТ: ПРОЧИТАН, НЕ ЗАПУЩЕН]`, `[СТЕНД]` (только ранее полученные наблюдения, в этой сессии стенд не использовался), `[ТРЕБОВАНИЕ/РЕШЕНИЕ]`, `[ПРЕДПОЛОЖЕНИЕ]`.

---

## A. Паспорт обследования

**Абсолютный корень исходников:** `/home/user/manzhero/исходный код/`
**Корень документов:** `/home/user/manzhero/` (плюс подпапки `документы в работе/`, `документация манжерок/`, `все остальное/`).

### A.1. Сокращения путей (используются ниже)

| Сокращение | Относительный путь от корня исходников | Назначение по коду | Версия из build-файла / сборки |
|---|---|---|---|
| **PR** | `passenger_request-main/passenger_request-main/application/src/main/java/ru/sberbank/ditsib/transport/request/` | Сервис пассажирских заявок, `spring.application.name: request` | `request-parent 5.15` (pom) |
| **PR-res** | `passenger_request-main/passenger_request-main/application/src/main/resources/` | Конфигурация и Liquibase заявок | — |
| **TF** | `tariff-main (2)/tariff-main/application/src/main/java/` | Пассажирский сервис тарифов, `spring.application.name: tariff` (REST `/api/tariffs/**`, gRPC `TariffService`) | parent 5.8 |
| **TFF** | `tariff-main/tariff-main/` | Другой сервис: `spring.application.name: tariff-fleet` (пакет `tariff_fleet`) — **не** пассажирский тариф | parent 5.12 |
| **GEO** | `geo-main/geo-main/application/src/main/` | Геосервис (2ГИС): `/api/geo/address`, `/api/geo/route` | parent 4.23 |
| **ADR** | `address-main/address-main/` | Сервис адресов `addresses` (`/api/addresses/**`) | `3.21` (pom), в `_documents/api/http/` лежат спецификации 3.14.0 и 3.16.0 |
| **CP** | `client-passengers-main/client-passengers-main/src/` | Клиентский фронт пассажирских заявок | `version.json`: ветка `release/D-03.014.000`, коммит `0d213fd`, **02.08.2024** |
| **CM** | `client-main/client-main/src/` | Клиентская оболочка | `version.json`: `feat/TRANSPORT-43674`, `b46c5ff`, 03.07.2026 |
| **CRP** | `corp-passengers-main/corp-passengers-main/src/` | Корп. кабинет, пассажирский модуль (тарифы, реестры) | `version.json`: `feat/TRANSPORT-36167`, `c21830f`, 06.11.2025 |
| **CORP** | `corp-main (1)/corp-main/` | Корп. оболочка | `version.json`: `release/D-03.010.000`, 07.06.2024 |
| **RPT** | `passenger_reports-main/passenger_reports-main/application/src/main/java/ru/sberbank/ditsib/transport/reports/` | Реестры/выгрузки пассажирских заявок (`reports`) | parent `${release.version}` |
| **SRM** | `srm-main/srm-main/application/src/main/java/ru/sberbank/ditsib/transport/srm/` | Подбор и расчёт совместных поездок (Magenta/SRM) | parent 5.8 |
| **RA** | `request-aggregation-main/request-aggregation-main/application/` | «Лиды»/планировщик массовых заявок (Excel) | parent 5.8 |
| **RC** | `request-checks-main/request-checks-main/application/src/main/` | Проверки заявок (многоточечность, длительность) | parent 5.12 |
| **TRIPS** | `trips-main/trips-main/application/src/main/java/ru/sber/transport/trip/` | Исполнение поездок TAXI/GROUP_TRANSFER через диспетчерскую | parent 5.7 |
| **ENUMS** | `as_sbertransport_libraries-main/as_sbertransport_libraries-main/enums/src/main/java/ru/sberbank/ditsib/transport/constants/` | Общие enum (статусы, классы) | `enums 260615-1` |
| **CORE** | `transport_core-main/transport_core-main/src/main/java/ru/sber/transport/` | Общие ошибки, `Page`, обработчик исключений | `transport-core 4.22-SNAPSHOT` |
| **GW** | `gateway-main/gateway-main/application/src/main/resources/application.yml` | Маршруты API-шлюза | `gateway 230424-1` (2023) |
| **CFG** | `config-server-main/config-server-main/` | Spring Cloud Config Server | parent `${release.version}` |
| **AUD / JAV** | `audit-main/audit-main/`, `javers-main/javers-main/` | Библиотеки журнала действий и Javers | audit 5.12, javers-jooq-starter 3.24 |
| **RCS** | `role_check_starter-main/role_check_starter-main/` | Проверка ролей по URL | 3.18 |

### A.2. Прочие скачанные архивы
Также присутствуют: `authentication-main`, `authentication-sbid-main`, `client-cargo-main (1)`, `config-server-main`, `constants-main`, `contractor-main`, `corp-cargo-main`, `disp-cargo-main`, `disp-fleet-main`, `disp-integration-proxy-main`, `disp-platform-main`, `dispatcher-main`, `driver-main`, `driver-track-main`, `etrn-main`, `excel-main`, `exchange-request-main`, `fleet_*_grpc-main`, `fleet_journal-main`, `geo-zones-main`, `notifications-main`, `oto-main` (`spring.application.name: oto-cargo` — грузовой контур), `push-main`, `request-external-main`, `request-external-registry-main`, `roles-main`, `tariff-external-main`, `tariff_dzo-main` (тарифы автосервисов ДЗО), `telemechanic-main`, `token-generator-main`, `trips-cargo-main`, `users-main`, `vehicle-main`.
`vehicle-main (1)` содержит только `readme.md` («first commit»). Второго архива `oto-main (1)` в папке нет — только один `oto-main` (грузовой).
Грузовые архивы не использовались как доказательства (правило 11 инструкции).

### A.3. Версии и соответствие стенду
- **Commit/версия развёртывания стенда неизвестны.** Архивы `*-main` без `.git`; для бэкендов доступна только версия из pom. Совпадение со стендом: **неизвестно**.
- **Фронты разновременные:** CP — релиз 02.08.2024, CM — 03.07.2026, CRP — 06.11.2025. Выводы по CP (формы такси/трансфера) могут не соответствовать текущему стенду. `[КОД]` `version.json` каждого фронта.
- **GW** датирован 2023 годом (`230424-1`); стенд отвечал на `/api/organizations/self/addresses/favorite` и `/api/geo/address` — маршруты `/api/organizations/**` и `/api/geo/**` в GW есть (`GW:92-97`, `GW:80-85`).
- **Фактическая конфигурация стенда отсутствует.** Все сервисы импортируют `optional:configserver:${SPRING_CLOUD_CONFIG_URI}` (`PR-res/application.yml:36-38`), а Config Server хранит свойства в БД: `spring.cloud.config.server.jdbc.sql … from "configs"."properties"` (`CFG/application/src/main/resources/application.yml`, профиль `jdbc`). Значения из `application.yml` ниже — **только defaults**.

### A.4. Документы и редакции

| Документ | Путь | Внутренняя редакция / дата | Использование |
|---|---|---|---|
| Модель T01.1 (обновлённая) | `T01.1_FavoriteRoute_updated (1).docx` | «Редакция от 23 сентября 2026 года на основе v1.4» | Основная модель: `waypoints jsonb`, `waitTimeSeconds`, без `wait_times` |
| Модель T01.1 (ранняя) | `документы в работе/T01.1 — Модель данных favorite_route (v6).docx` | без даты; содержит `wait_times`, роль «Управляющий справочниками» | Ранняя редакция |
| Вопросы T01.1 | `Questions_T01.1 (4).docx` | «После встречи 24 сентября и получения спецификации адресов» | D01–D10 |
| Вопросы T01.2 | `Questions_T01.2.docx` | «К спецификации v0.1 • 25 сентября 2026» | Q01–Q14 |
| API T01.2 | `T01.2_FavoriteRoute_API.docx` | «Проект спецификации v0.1»; ссылается на T01.1 от 25.09.2026 | Проект API |
| Смежные вопросы | `Questions_other_tasks.docx` | 23.09.2026 | I01–I04, A01–A06, O01–O02, R01, E01–E03 |
| Ранние БТ | `v1_Схема процесса (1).docx` | «Обновлено по итогам встречи … 10.09.2026» | `wait_times`, `vehicle_id`, CSV/Excel/PDF |
| БТ (варианты) | `все остальное/БТ последний.docx`, `БТ.docx`, `документы в работе/БТ 09.09.2026.docx`, `документация манжерок/*.docx`, PDF v11 (27.08.2026), zip v7/v14/v16 | август–сентябрь 2026 | Ранние требования |
| Протокол 10.09 | `Ответы по итогам встречи 10.09.2026.docx` (+ копия в «документы в работе») | 10.09.2026 | Решения: разные записи для класса/ночи, 2 цены, история, удаление, номер |
| Протокол 17.09 | `Ответы на вопросы  встреча 17.09.2026 (2).docx` | 17.09.2026 | `transportType`, привязка к тарифу, территория по первой точке |
| Вопросы коллеги Q1–Q9 | `Вопрос ответ.docx` | без даты | Раздел C.3 |
| Контракт адресов | `адреса.yml` | OpenAPI 3.0.3, «Платформа — Адреса» 3.16.0 | Семантически совпадает с `ADR/_documents/api/http/addresses_3.16.0.yml` (различия — форматирование и развёрнутые `shared/responses`) |
| Договор + БТ | `Избранные маршруты (1).pdf` | Скан договора (без текстового слоя), Приложение №1 «БТ Манжерок_Избранные маршруты, Exported on 02/19/2026» | Раздел 5 «Порядок сдачи и приёмки» — для Коллега:Q9 |

**Отсутствуют / не найдены:** отдельный файл «T01.1 v1.4»; «T01.1 от 25.09.2026» (на него ссылается T01.2); протокол встречи 24.09.2026 (решения известны только из пересказа в `Questions_T01.1`); «Статус вопросов T01.1 T01.2 и коллеги 28.09.2026»; макеты FavoriteRoute (PDF «Избранные маршруты» — это договор, не макеты); Network/HAR со стенда. Страницы 7–22 PDF (Приложение №1 БТ от 19.02.2026) — сканы; детально не разбирались.

---

## B. Краткая сводка

### B.1. Можно использовать в ТЗ уже сейчас (подтверждено кодом)
1. **Единицы API заявок и расчёта:** `waitTime` и `time` в JSON — **миллисекунды** (UI: минуты × 60 000); `cost` — **копейки**; `distance` — **км** (2ГИС метры / 1000). Детали — D06.
2. **Бесплатное ожидание:** поле тарифа `freeWaitingTime` (минуты, int) у `TaxiTariff` и `GroupTransferTariff`. **Формулы различаются:** трансфер вычитает лимит **на каждой точке**; такси вычитает лимит только из ожидания **в первой точке** (`waitingTime`), а ожидание во всех точках оплачивает по `waitCostPerMinIntermediate` **без лимита**. Переключателя «не включать ожидание» в коде нет.
3. **Предварительный расчёт без создания заявки существует:** REST `POST /api/tariffs/calculate` (+ `/enum/{transportType}/{tariffId}`), вход `TripDto` (расстояние и время берутся клиентом из `POST /api/geo/route`). gRPC `TariffService.calculate` — метод пересчёта изменённой заявки, не подходит «как есть» (см. Q08).
4. **Номер `FR-XXXX-XXXXXXXX` совпадает с платформенным форматом человекочитаемых номеров** (`OT-0001-00000001`, `TF-…`): библиотека `human-readable-generator`, таблица `company_sq` c уникальностью `(prefix, orgDigitId)`, digitId 1…9999, префикс 2 символа.
5. **Статусы TAXI/GROUP_TRANSFER и признак terminal** есть в `TripRequestStatus` — готовая основа для «активных заявок» (Q12).
6. **Отчётность:** только XLSX (+ PDF ТТН для грузов); CSV в `passenger_reports` не найден. Реестры строятся из собственной копии заявок, получаемой из Kafka `service.request`.

### B.2. Требует доработки / предварительная оценка (не трудоёмкость)
- Серверная сторона заявок **не пересчитывает и не проверяет стоимость** при создании (`expected` сохраняется из DTO клиента) и **не проверяет активность/срок тарифа** (только наличие записи в реплике). Для FavoriteRoute фиксированную цену и связь с маршрутом нужно будет валидировать в T04.1 отдельно.
- Маршрут «база → остановка → база» имеет ≥ 3 точек и попадает под **лимит многоточечных заявок** (default 3 в сутки на пассажира) — влияет на D10/Q05.
- `waitTimeSeconds`, на который опирается T01.1 (23.09), **есть только в копии proto в `request-aggregation/_documents`**; в proto фактического сервиса тарифов этого поля нет.
- История/версии: в платформе нет `@Version`/ETag; Envers подключён только в сервисе заявок; библиотека `audit` — журнал HTTP-действий, не история значений.

### B.3. Что мешает окончательному выводу
Отсутствуют исходники библиотек `human-readable-generator`, `authorization`, `ru.sberbank.ditsib.converters` (Jackson-конвертеры Duration↔ms), `request-messaging`, `tariff-messaging`; неизвестна конфигурация стенда (Config Server JDBC) и версии развёртывания. Фронт CP — 2024 года.

---

## C. Таблицы вопросов

### C.1. Наши вопросы T01.1

| ID и вопрос | Подробный ответ | Что подтверждено и источник | Техническая часть | Решение для FavoriteRoute | Что ещё выяснить и у кого |
|---|---|---|---|---|---|
| **D01** Состав точки и преобразование адреса | Сервис адресов отдаёт `Address{id,country,region,city,street,house,building,structure,latitude,longitude,count,label}` в группах `FREQUENTLY/FAVORITE/COMMON`. Точка заявки (`WaypointDTO`) не содержит `id`, `label`, `district`; содержит служебные `existInVspGosbTbRegistry`, чекины, `active`, `waitTime`. `district` есть только в DTO тарифа/geo. `id` адреса из COMMON-выдачи **генерируется случайно при каждом ответе**, если geo не вернул id — стабильным ключом не является. Проверок «пара координат ↔ текст адреса» на сервере заявок не найдено. Цепочка одинакова для TAXI и GROUP_TRANSFER (один `NewRequestDTO`). | `[КОД]` ADR `AddressControllerImpl.java:78-80`, `GeoClientImpl.java:59`; PR `dto/WaypointDTO.java:28-119`; PR `database/model/Address.java` (нет district); TF `ru/sber/transport/tariff/model/WaypointDTO.java:25`; `адреса.yml` required `id,country,region,latitude,longitude` | Подтверждена кодом (цепочка), Частично (версия адресного API на стенде) | Архитектор: минимальный состав точки и JSONB — проектное решение | Владелец Address: какая версия API на стенде (код 3.21 генерируется из внешней спецификации apistudio, в репо — 3.16.0); нужен ли `objectId` 2ГИС |
| **D02** Ожидание в точках | В UI ожидание вводится только для промежуточных точек при ≥ 3 точках, минуты, без максимума, допускаются дробные (`NumericInput` пропускает `.`). В JSON — мс. Сервер заявок не валидирует `waitTime` (нет аннотаций), хранит `Duration` в `int8`. Единственный порог — асинхронная fraud-проверка **только TAXI**: сумма минут по точкам `> 60` (default) → fraud; при `distance ≤ 50000 м` всегда, иначе только если город один. Проверка не блокирует создание. | `[КОД]` CP `CreateTripRequest/Components/Waypoints.tsx:351`; CP `shared/models/geo/Waypoint.model.ts:72-80`; CP `shared/components/NumericInput/index.tsx`; PR `service/impl/RequestForTaxiServiceImpl.java:284,860-891`; PR-res `application.yml:154-157` | Подтверждена кодом (текущее поведение) | Заказчик: максимум, шаг, пустое значение, крайние точки | Заказчик — бизнес-лимиты; владелец заявок — будет ли fraud-проверка применяться к избранным маршрутам |
| **D03** Бесплатное ожидание | Лимит — `freeWaitingTime` (int, минуты) в тарифе клиента (income-тариф выбранного класса), читается **в момент расчёта** из актуальной записи тарифа. TAXI: вычитается только из ожидания первой точки; GROUP_TRANSFER: из каждой точки отдельно. Между расчётом и оформлением сервер заявок цену не пересчитывает — сохраняет `expected.cost` от клиента. | `[КОД]` TF `database/model/TaxiTariff.java:94`, `GroupTransferTariff.java:88`; TF `service/impl/CalculateServiceImpl.java:870-882,1016-1033`; PR `RequestForGroupTransferServiceImpl.java:162`, `RequestForTaxiServiceImpl.java:198` | Подтверждена кодом (источник и формула); Нужна проверка стенда (фактические значения тарифов) | Смешанный: заказчик — смысл «можно не включать» и политика обновления; архитектор — снимок/чтение | Заказчик (Гусева И. П.); владелец TariffService |
| **D04** История и версия | Envers подключён в сервисе заявок (`@Audited` на `Request`, схема `request_audit`, ревизия с `user_id` и timestamp). `@Version`, ETag, optimistic locking — не найдены ни в одном пассажирском сервисе. Библиотека `audit` пишет журнал HTTP-действий (кто/какой endpoint/результат), без before/after. `javers-jooq-starter` объявлен в `trips`, но в коде не используется. В `trips` история — типизированные строки действий (`TripHistoryItem`) без старого/нового значения стоимости. | `[КОД]` PR `database/model/Request.java:54`; PR `database/model/publicTransport/CustomRevision.java:18,35`; PR `RequestApplication.java:46`; AUD `writer/impl/AuditWriterImpl.java:41-66`; TRIPS `business/model/TripHistoryItem.java`, `web/service/impl/TripServiceImpl.java:590-603` | Подтверждена кодом (что есть в платформе) | Архитектор (Демченко А.) | Выбор: полные версии / снимок в заявке / EAV; ключ версии и конкуренция |
| **D05** Истечение и повторная активация | Аналоги: тариф — флаг `active`; трансфер — `tariffStartDate/EndDate`, фильтр по дате поездки при подборе автомобиля; договор — `endDate` и ночной шедулер `active=false` (без события Kafka). «Удаление» тарифа = `active=false`. Авто-переактивации нет; повторное сохранение похожего тарифа такси деактивирует старый. | `[КОД]` TF `database/model/BaseTariff.java` (`active`); TF `scheduler/ContractCheckerScheduler.java:18`; TF `service/impl/ContractServiceImpl.java:47-57,104`; TF `service/impl/TariffServiceImpl.java:205-214,608-652` | Подтверждена кодом (аналоги) | Заказчик: реактивация истёкшего; Архитектор: поля жизненного цикла | Правило для FavoriteRoute из тарифа не выводится |
| **D06** Единицы и диапазоны | См. матрицу D06: `cost/outcomeCost` — копейки (double в заявке, long в тарифе); `distance` — км double; `time`, `waitTime` — мс в JSON, `Duration` в Java, `int8` в БД; `freeWaitingTime` — минуты int. Округление: минуты везде через `toMinutes()` (усечение), стоимость — `(long)` (усечение), `Math.round(distance)` только для порога `minKm`. | `[КОД]` см. D06 в разделе D | Частично (БД-представление `Duration` зависит от Hibernate-маппинга) | Архитектор/разработчик T01.4 | Проверить фактические значения `int8` в БД заявок (нс или мс) |
| **D07** Цель поездки | Цель — обязательный атрибут заявки (`purpose` `@NotNull`), отдельный справочник `TripPurpose`, есть «частые цели». Новое поле категории в маршруте кодом не требуется. | `[КОД]` PR `dto/NewRequestDTO.java:111-118`; PR `controller/RequestController.java:33` (`frequentlyTripPurpose`); `[ТРЕБОВАНИЕ/РЕШЕНИЕ]` протокол 17.09 п.9 | Подтверждена кодом | Есть согласованное решение (17.09 п.9: цель относится к заявке) | — |
| **D08** Коды классов | `TaxiClass`: ECONOMY, COMFORT, COMFORT_PLUS, BUSINESS, OFFICIAL (+ VIP_BUS, SMALL_BUS, MIDDLE_BUS, LARGE_BUS — автобусы). `GroupTransferClass`: TRANSFER, TRANSFER_COMFORT, TRANSFER_COMFORT_PLUS, TRANSFER_BUSINESS, TRANSFER_VIP, TRANSFER_CAR_CHOICE. Расхождений со стендом нет. | `[КОД]` ENUMS `TaxiClass.java:21-53`, `GroupTransferClass.java:18-28` | Подтверждена кодом | Есть согласованное решение | Автобусные классы такси в объём FavoriteRoute не входят — подтвердить, если появятся в UI |
| **D09** Место ожидания | В платформе ожидание — атрибут точки (`Waypoint.waitTime`). Решение D09 (хранение внутри точки) не противоречит коду. | `[КОД]` PR `database/model/Waypoint.java:47-48` | Подтверждена кодом | Есть согласованное решение (подтверждение Яны, см. `Questions_T01.1` §1) | Имя/единица — D02/D06 |
| **D10** База, начало/конец, число точек | Отдельных «базовых» точек в пассажирских заявках нет — все точки пользовательские. Сервер заявок допускает 2…50 точек. Совпадение начала и конца не запрещено. Заявка с > 2 точками — «многоточечная»: default ≤ 3 в сутки на пассажира (иначе отказ). Удаление точки в пути запрещено, если активных останется < 2. | `[КОД]` PR `dto/ExpectedDataDTO.java:40`; PR `service/impl/AbstractTransportTypeService.java:122-144`; RC `…/service/impl/TripRequestServiceImpl.java:91-99`, RC `resources/application.yml:57`; PR `RequestServiceImpl.java:426-431` | Подтверждена кодом (текущее поведение) | Заказчик: минимум точек с базой, учёт базы в лимите 10 | Владелец request-checks: исключать ли FavoriteRoute из лимита многоточечных |

### C.2. Наши вопросы T01.2

| ID и вопрос | Подробный ответ | Что подтверждено и источник | Техническая часть | Решение для FavoriteRoute | Что ещё выяснить и у кого |
|---|---|---|---|---|---|
| **Q01** Дубли | Сравнения упорядоченных точек маршрутов в платформе нет. Аналоги: (1) тариф такси — при сохранении нового тарифа с тем же организация/регион/подразделение/ночь/договор/класс старый **деактивируется**, а не отклоняется; (2) трансфер `TRANSFER_CAR_CHOICE` — поиск конфликтов по пересечению дат (границы включительно), организации, перевозчику, регионам, ТС; (3) заявки — `CreateRequestConflictException` при пересечении интервалов у пассажира. Нормализации адресов для сравнения нет; сравнение координат — с `EPSILON`. | `[КОД]` TF `service/impl/TariffServiceImpl.java:608-652`; TF `database/dao/GroupTransferTariffRepository.java` (`findConflictTariff`); PR `validate/request/impl/OtherRequestsValidator.java:41-83`; PR `dto/WaypointDTO.java:121-133` | Подтверждена кодом (аналоги) | Заказчик (состав ключа, запрет/предупреждение) | Архитектор — конкурентная проверка |
| **Q02** Период | Тарифы трансфера фильтруются по дате поездки, обе границы включительно (`end >= tripDate AND tripDate >= start`), но дата берётся из `tripDate` в UTC после приведения к поясу региона. Договор истекает на следующий день после `endDate` (`endDate.isBefore(today)`, cron default 21:00, пояс JVM неизвестен). Ночь: час ∈ [22, 6) в поясе региона первой точки; если ночных тарифов такси нет — берутся дневные. У трансфера ночного признака нет. | `[КОД]` TF `service/impl/CalculateServiceImpl.java:141,290-324,112-118,625`; TF `database/dao/GroupTransferTariffRepository.java` (`findAllTariff`); TF `service/impl/ContractServiceImpl.java:52` | Подтверждена кодом (аналоги) | Заказчик (дата оформления или поездки, пояс, открытые границы) | Владелец TariffService: пояс JVM; поведение у полуночи |
| **Q03** Список | Пассажирские справочники используют Spring `Pageable` (тарифы: default size 20, сортировка `humanReadableId ASC`, фильтр телом `TariffSearchDTO`); поиск заявок — POST с DTO фильтра и `Page<>`. В CORE есть обёртка `Page{content,pageData,sortData}`. | `[КОД]` TF `ru/sberbank/ditsib/transport/tariff/controller/TariffController.java:100-109`; PR `controller/RequestController.java:278-289`; CORE `dto/Page.java:12-41` | Подтверждена кодом (соглашения) | Заказчик: фильтры и видимость; Архитектор: формат | Какой формат страницы принять (Spring Page или CORE Page) |
| **Q04** Жизненный цикл | Аналоги см. D05. Восстановления «удалённого» тарифа как операции нет; договор можно снова сохранить активным (метод `save`) — это не проверялось. | `[КОД]` TF `ContractServiceImpl.java:76-104` | Частично | Заказчик (реактивация истёкшего) | — |
| **Q05** Точки/ожидание | Сервер заявок: 2…50 точек, `@Min(0)` на стоимость/расстояние, на `waitTime` проверок нет. UI: ожидание только в промежуточных точках; при удалении точки до двух — ожидание последней обнуляется. Трансфер: при `null` в `waitTime` любой точки расчёт падает (нет фильтра `null`) → в хранении нужен 0, а не null. | `[КОД]` PR `dto/ExpectedDataDTO.java:40-75`; CP `stores/Geo/DIGeo.store.ts:197`; TF `CalculateServiceImpl.java:877-882` | Подтверждена кодом | Заказчик: максимум, шаг, пусто | Проверить стендом: трансфер с `waitTime:null` |
| **Q06** Истёкший тариф | Сервер заявок берёт тариф из **локальной реплики** по id: такси — `getTariffById` без проверки `active`; трансфер — `findById`, проверяется только `minCreateTime`. Удалённый тариф такси удаляется из реплики (сообщение `deleted`) → 404 при оформлении; трансфер при удалении шлёт обычное сообщение → запись остаётся. Истечение договора события не шлёт. Расчёт (`/tariffs/calculate`) исключает тарифы с неактивным договором. | `[КОД]` PR `service/impl/TaxiTariffServiceImpl.java:36-44`; PR `RequestForGroupTransferServiceImpl.java:146-150`; PR `messaging/listeners/impl/tariff/TaxiTariffListenerImpl.java:18-21`; TF `TariffServiceImpl.java:205-214`; TF `CalculateServiceImpl.java:930,977` | Подтверждена кодом; Нужна проверка стенда | Смешанный: заказчик + владелец тарифов | Что показывать/разрешать при недействующем тарифе |
| **Q07** API | Шлюз: `/api/<сервис>/**` → rewrite на корень сервиса. Имена путей неоднородны (`advanced_search`, `self/non_terminal`, `with-update`). Ошибки: `ExceptionBody{timestamp,path,entity,message,problems[{field,value,constraints}]}`; 404 — `EntityNotFoundException`, 409 — `DuplicateDataException`, 400 — валидация; geo возвращает 417 на ошибки провайдера. Идемпотентности HTTP (Idempotency-Key) не найдено. Доступ к URL — `role-check-starter` (роль ↔ метод+URL из БД). | `[КОД]` GW:14-193; CORE `exceptions/dto/ExceptionBody.java`, `handlers/RequestExceptionHandler.java:87-290`; GEO `java/…/geo/handlers/ControllerExceptionHandler.java:50`; RCS `…/check/services/impl/RoleCheckServiceImpl.java:15-24` | Подтверждена кодом (соглашения) | Архитектор / разработчик API | Новые URL потребуют регистрации ролей (скрипты `add_role` по образцу `roles-main/_documents/scripts`) |
| **Q08** Предварительный расчёт | REST `POST /api/tariffs/calculate` (без создания заявки): вход `TripDto{organizationId, employeeId, distance км, time мс, waitingTime мс (первая точка), intermediateWaitingTime мс, tripDate мс, timeZone, startPoint, waypoints, vip, information, options}`; выход — список `CalculatedDto{id(tariffId), outcomeTariffId, cost коп, outcomeCost коп, taxiClass/groupTransferClass, priceDetails}` по всем доступным классам. Территория — по первой точке; классы такси фильтруются по должности **аутентифицированного** пользователя; тарифы — по его подразделению. gRPC `calculate` — «пересчёт изменённой заявки (пока только точки)»: маршрут строит сам, ожидание не принимает, ошибка если результатов ≠ 1. Таймауты/ретраи не найдены. | `[КОД]` TF `…/tariff/controller/CalculatingController.java:22-121`; TF `…/controller/impl/CalculatingControllerImpl.java:15-51`; TF `ru/sber/transport/tariff/model/TripDto.java`; TF `…/grpc/TariffActionReplierImpl.java:16-60`; TF `CalculateServiceImpl.java:101-122,494-527` | Подтверждена кодом (методы); Нужна проверка стенда | Архитектор + владелец TariffService (T01.4) | Как считать от имени корп. пользователя без пассажира; нужен ли новый метод |
| **Q09** Адрес | См. D01. Фактически используемые фронтом API: `GET /api/geo/address` (по координатам), `POST /api/geo/route` (маршрут). Сервис `addresses` доступен как `/api/addresses/**` (в спецификации сервер `/api/v3.16/address`). 417 от geo — любая ошибка провайдера, включая «не найден». | `[КОД]` CP `constants/constants.env.js:52-54`; GW:20-25; `адреса.yml` servers; GEO `…/handlers/ControllerExceptionHandler.java:20-50` | Частично | Архитектор | Какой адресный API будет использовать форма FavoriteRoute |
| **Q10** Номер и повтор POST | `human-readable-generator`: `getNextId(prefix, orgDigitId)` → `PP-DDDD-NNNNNNNN`, счётчик в `company_sq` на каждый сервис, уникальность `(prefix, orgDigitId)` и `(prefix, orgDigitId, sq)`, `orgDigitId` 1…9999, префикс `varchar(2)`. digitId берётся из организации **автора** (сотрудник → подразделение → организация). Механизм атомарности внутри библиотеки не виден (исходника нет). HTTP-идемпотентности нет; в reports — inbox по `messageId` для Kafka. | `[ТЕСТ: ПРОЧИТАН, НЕ ЗАПУЩЕН]` `passenger_request-main/…/src/test/java/…/human_readable_id/service/SQCreatorImplTest.java:49-80`; `[КОД]` `tariff-main (2)/…/resources/db/20200817/create_sq_table.sql:1-19`; PR `RequestForGroupTransferServiceImpl.java:128-138`; RPT `messaging/listeners/impl/TripRequestListenerImpl.java:14-35` | Частично; Нужен репозиторий (`lib/human_readable_generator`) | Архитектор | Область счётчика FR, предел 99 999 999 номеров на организацию |
| **Q11** История и обновление | См. D04: `@Version`/ETag не используются; редактирование заявок — «последний выигрывает». | `[КОД]` поиск `@Version`, `ETag`, `If-Match` в PR, TF, ADR, TRIPS, RPT, SRM, RA — не найдено (кроме строковых упоминаний в RPT, не связанных с HTTP) | Не найдено в просмотренной области | Архитектор | — |
| **Q12** Удаление и активные заявки | Таблица статусов — раздел D (Q12). Нетерминальные: AWAITING_APPROVAL, APPROVED, AWAITING_SEARCH, DRIVER_SEARCH, DRIVER_FOUND, DRIVER_ON_THE_WAY, DRIVER_ARRIVED, (такси: FREE_TIME_EXPIRED, WAYPOINT_ARRIVED), TRIP_IN_PROGRESS, GENAI_CHECK. Терминальные: CANCELLED, TRIP_FINISHED. API поиска заявок по признаку «избранный маршрут» нет (поля нет). | `[КОД]` ENUMS `TripRequestStatus.java:24-79,155,416-461,870-874` | Подтверждена кодом (статусы) | Смешанный: заказчик (какие блокируют), архитектор (гонка) | Владелец заявок: контракт проверки по `favoriteRouteId` |
| **Q13** Тарифные связи | Изменения тарифов идут в Kafka (реплики в заявках, отчётах); при удалении такси — `sendDeleted`, трансфера — `send`; истечение договора события не создаёт; сохранение нового похожего тарифа такси деактивирует старый (id меняется). | `[КОД]` TF `TariffServiceImpl.java:205-214,608-652`; TF `ContractServiceImpl.java:47-57`; PR `messaging/listeners/impl/tariff/*` | Подтверждена кодом | Архитектор | Как FavoriteRoute узнаёт о замене тарифа (id сменился) |
| **Q14** Организация и доступ | Пользователь → сотрудник: `findByUserId(currentUser)` (одна организация на пользователя в сервисе заявок). Организация заявки — организация **автора**. Пассажир берётся из DTO по id; проверки «пассажир из той же организации» в `add()` не найдено. Редактирование: автор, пассажир или согласующий. Reports проверяют `{organizationId}` через `EmployeeOrganizationFunction` (библиотека `authorization`, исходника нет). | `[КОД]` PR `service/corp/impl/EmployeeServiceImpl.java:70-72`; PR `RequestForGroupTransferServiceImpl.java:128-163`; PR `service/impl/RequestValidationServiceImpl.java:128-136`; RPT `config/CheckAccessConfiguration.java:11-18` | Частично; Нужен репозиторий (`authorization`) | Архитектор | Переключение организации пользователем — есть ли на стенде |

### C.3. Вопросы коллеги Q1–Q9

| ID и вопрос | Подробный ответ | Что подтверждено и источник | Техническая часть | Решение для FavoriteRoute | Что ещё выяснить и у кого |
|---|---|---|---|---|---|
| **Коллега:Q1** Источник бесплатного ожидания; снимок или чтение | Поле `freeWaitingTime` (минуты) income-тарифа выбранного класса (такси — `TaxiTariff`, трансфер — `GroupTransferTariff`). Читается из актуальной записи тарифа в каждом расчёте; снимка нет. Пример «было 3, стало 5»: новый расчёт возьмёт 5; сохранённая заявка не меняется (цена не пересчитывается сервером). На фиксированную цену FavoriteRoute лимит влияет только через `recommended_cost`. | `[КОД]` TF `TaxiTariff.java:94`, `GroupTransferTariff.java:88`, `CalculateServiceImpl.java:881,1031`; CRP `modules/NewTariffs/constants/Tariffs.constants.ts:46,69` | Подтверждена кодом (источник, текущее поведение) | Заказчик + архитектор: снимок или чтение | Гусева И. П.: применять ли новый лимит к старому маршруту |
| **Коллега:Q2** «Ожидание можно не включать» | Переключателя/поля отключения ожидания в тарифе и расчёте не найдено. Пример 10 мин, лимит 3, 20 ₽/мин: трансфер (промежуточная точка) → (10−3)×20 = **140 ₽**; такси (UI вводит ожидание только в промежуточной точке) → 10×`waitCostPerMinIntermediate` (при 20 ₽ = **200 ₽**), лимит не применяется. Гипотезы «выключено»: А) ожидание не оплачивается → 0 ₽; Б) лимит не применяется → 200 ₽. | `[КОД]` TF `CalculateServiceImpl.java:877-895,1016-1033`; поиск `freeWait*`, `waitIncluded`, `includeWait` в TF/PR/CP/CRP — только `freeWaitingTime` | Не найдено в просмотренной области (переключатель) | Заказчик (смысл фразы) | Автор требования: выбрать А/Б или иное |
| **Коллега:Q3** Ожидание на каждой остановке | Трансфер: лимит **на каждую точку**. Пример: 2 остановки по 5 мин, лимит 3 → (5−3)+(5−3) = **4** оплачиваемые мин. Такси: сумма ожиданий всех точек **без лимита** по ставке промежуточной точки → **10** мин; лимит — только к первой точке (в UI там 0). Усечение до минут: трансфер — по точке, такси — по сумме. Время движения отдельно: `expected.time` от 2ГИС ожидание не включает; при подборе автомобиля трансфера UI и сервер добавляют ожидание к длительности. | `[КОД]` TF `CalculateServiceImpl.java:130-137,877-882,1026-1032`; GEO `resources/application.yml:167-168`; CP `…/GroupTransferChoosingBookingInterval.tsx:39-40` | Подтверждена кодом (текущее поведение) | Заказчик: общий лимит или на точку для FavoriteRoute; экран/отчёт | Отчёт такси уже имеет отдельные колонки ожидания при подаче и в промежуточной точке (RPT `service/impl/mapping/AllColumnNames.java:66-71`) |
| **Коллега:Q4** Режимы «моя/для коллеги/для клиента» | В CP: пассажир «Я / Мой коллега» (`passenger: 'me'` / `'notme'`), «Заказать коллеге»; переключатель «За счёт компании / В личных целях». Режим «для клиента» в CP/CM и в DTO заявки не найден; в карточке трансфера есть подписи «ФИО/Телефон клиента» и доп. контакт (`addContactFIO`, `addContactPhone`). Организация заявки = организация автора. Каршеринг «коллеге» запрещён сервером; такси/трансфер — разрешены. | `[КОД]` CP `…/CreateTripRequest/constants/CreateRequest.constants.ts:72,85`; CP `…/hooks/useCreateTripRequest.ts:141`; CP `…/EditTripRequest/CreateTripRequest.tsx` (Switch); PR `validate/request/impl/CarsharingPassengerValidator.java:9-13`; PR `dto/GroupTransferRequestInformationDTO.java:36-43` | Частично (фронт 2024 г.; «для клиента» не найден) | Заказчик (матрица режим × транспорт × организация) | Проверить на стенде актуальную форму; мобильное приложение не обследовано |
| **Коллега:Q5** Маршрут действует, тариф истёк | Технически сервер заявок примет старый `tariffId`, если запись есть в реплике (активность и срок не проверяются); удалённый тариф такси — 404. Перевозчик берётся из `outcomeTariff.contractorId`. Расчёт `/tariffs/calculate` не вернёт тариф с неактивным договором. Сохранённая фиксированная цена — согласована, не переоткрывается. | `[КОД]` PR `RequestForTaxiServiceImpl.java:187-214`; PR `RequestForGroupTransferServiceImpl.java:146-178`; TF `CalculateServiceImpl.java:930,977,1200-1240` | Подтверждена кодом; Нужна проверка стенда | Смешанный: заказчик + владелец тарифа | Разрешать ли оформление со старым тарифом/перевозчиком или подбирать действующий |
| **Коллега:Q6** Цена совместной поездки | Совместные поездки — только TAXI/PERSONAL (SRM); для трансфера `calculateByTariff` возвращает `null`. SRM: цена поездки по тарифу, доля участника = его расстояние / сумма расстояний (7 знаков), цена участника = `(long)(rideCost × доля)`. Пример 1350 ₽ при равных долях → 675 ₽ каждому по текущей логике — **это не правило FavoriteRoute**. Совместную заявку такси нельзя редактировать — только отмена. Факт — `trips.factCost` (правит диспетчер). | `[КОД]` SRM `service/impl/SrmServiceImpl.java:1282,1292-1303,1334-1352`; PR `service/impl/MagentaAuxilaryServiceImpl.java:26-60`; PR `RequestForTaxiServiceImpl.java:346-349`; TRIPS `web/service/impl/TripServiceImpl.java:590-603` | Подтверждена кодом (текущее) | Заказчик | Утвердить пример 1350/1600 и отмену участника |
| **Коллега:Q7** Объединение разных маршрутов, изменение точек | Объединение — SRM `addRequestToSharedRide`/`postNewSharedRide` при создании заявки такси с `coopTrip`. Удаление точки в пути: пересчёт через gRPC тарифа; если новая цена **выше** исходной — отказ, иначе цена заменяется новой (ниже). Связи с «избранным маршрутом» нет — место интеграции: `RequestServiceImpl.deleteWaypoint`, `RequestFor*ServiceImpl.update`, SRM-слияние. | `[КОД]` PR `RequestServiceImpl.java:426-458`; PR `service/impl/TariffGrpcClientImpl.java:30-45`; PR `MagentaAuxilaryServiceImpl.java:17-41` | Подтверждена кодом (места интеграции) | Заказчик | Допустимость объединения, правило цены по каждому участнику |
| **Коллега:Q8** Реестры и объём выгрузок | RPT: XLSX для такси, личного, общественного, каршеринга (эндпоинты `…/xlsx/trip-requests/*`), экспорт реестра трансфера через `GroupTransferReportRegisterResolver`; PDF — только ТТН грузов; CSV — не найден. Источник — копия заявок из Kafka `service.request`. Колонки такси уже включают условную/фактическую стоимость, долю `costSharePart`, ожидания. Для новых колонок нужно расширить `RequestMessage` и БД отчётов. | `[КОД]` RPT `controller/ReportsXlsxOrganizationController.java:44-52`; RPT `controller/ReportPdfTtnController.java:14`; RPT `service/impl/mapping/AllColumnNames.java:38-119`; RPT `service/impl/file_resolvers/GroupTransferReportRegisterResolver.java`; RPT `messaging/listeners/impl/TripRequestListenerImpl.java:14-26` | Подтверждена кодом (форматы); Частично (полный список колонок трансфера) | Владелец отчётности / заказчик | Утвердить состав колонок и форматы (БТ требует CSV/Excel/PDF) |
| **Коллега:Q9** Приёмка и доступы | Договор: заказчик в течение 10 рабочих дней проверяет соответствие ТЗ и подписывает УПД либо даёт мотивированный отказ; стенд в договоре (стр. 2–3) не указан. Тесты сервисов используют embedded Postgres/Kafka и моки JWT; 2ГИС-заглушка `TASK_DONE/result_link` — это **WireMock SRM для асинхронной матрицы расстояний**, а не расчёт маршрута; geo-тесты используют записанные ответы `catalog.api.2gis.com`. | `[КОД]` SRM `…/srm/service/impl/GeoService2GisImpl.java:140`; `[ТЕСТ: ПРОЧИТАН, НЕ ЗАПУЩЕН]` `srm-main/…/test/resources/wiremock/gis-async-result-feign-server/mappings/gis-async-result-feign-server-client.json:17-20`; `geo-main/…/test/resources/responses/catalog.api.2gis.com/`; `[ТРЕБОВАНИЕ/РЕШЕНИЕ]` PDF договора, разд. 2.2, 5.1–5.4 | Частично | Владелец стенда / руководитель проекта | Кто принимает, на каком стенде, допустимость имитаторов |

---

## D. Подробные доказательства

Нумерация строк — фактическая в файле (с учётом import).

## T01.1:D01. Состав точки и преобразование адреса

**Ответ:** точка заявки ≠ `Address` сервиса адресов. Минимальный общий набор полей — `country, region, city, street, house, building, structure, latitude, longitude`. `district` теряется при сохранении заявки; `id` адреса в заявку не передаётся.

**Текущее поведение (таблица преобразований):**

| Поле | Address (ADR 3.16/3.21) | Фронт `WaypointModel` (CP/CM) | `WaypointDTO` заявки (PR) | Хранение заявки (PR `request.address`/`waypoint`) | `WaypointDTO` тарифа (TF) | proto `TariffDescriptor.Waypoint` (TF) |
|---|---|---|---|---|---|---|
| id | UUID, required; для COMMON — случайный, если geo не дал id | нет | нет | свой `address.id` (генерируется БД) | нет | нет |
| country, region | string, required | optional | String | String, nullable | String | string |
| city | string | optional | String | String | String | string |
| district | нет | нет | **нет** | **нет** | String | NullableString |
| street, house, building, structure | string, не required | optional | String | String | String | NullableString |
| latitude, longitude | double, required, −90…90/−180…180, шаг 1e-6 | number, default 0; `isValid` = оба ≠ 0 | `double` (примитив, 0 по умолчанию) | double NOT NULL | Double | double |
| label, count/usages | есть | нет | нет | нет | нет | нет |
| existInVspGosbTbRegistry | нет | нет | boolean | boolean NOT NULL | boolean | нет |
| waitTime | нет | number (мс) | Duration, JSON мс | `wait_time int8` | Duration | **нет** (в TF); `int64 waitTimeSeconds=11` — только в копии RA |

**Доказательства:**
- [КОД] ADR `application/src/main/java/ru/sber/transport/address/web/controller/impl/AddressControllerImpl.java:78-80`, `getMap()`: «result.put("FREQUENTLY"… "FAVORITE"… "COMMON"…)» — ключи групп выдачи.
- [КОД] ADR `…/providers/common_address/client/impl/GeoClientImpl.java:59`: «Optional.of(address.getId()).filter(s -> !s.isBlank()).map(UUID::fromString).orElse(UUID.randomUUID())»; GEO `grpc/src/main/resources/proto/dto/GeoDescriptor.proto:10` — `string id = 1`, а `GeoAddressActionReplierImpl` не заполняет id (поиск `setId` — не найдено) → id случайный.
- [КОД] ADR `application/pom.xml:280-300`: OpenAPI-генератор с `inputSpec` = внешний URL apistudio (не файл репозитория) — версия контракта 3.21 не установлена.
- [КОД] PR `dto/WaypointDTO.java:28-119`: поля точки; `private double latitude` (стр. 70), `@JsonSerialize(using = DurationMillisConverter.class) … private Duration waitTime` (стр. 85-88).
- [КОД] PR `database/model/Address.java` (поля `latitude/longitude @Column(nullable = false)`, `country…structure @Column`, `exist_in_vsp_tb_registry`); поиск `district` в PR — не найдено.
- [КОД] PR `mappers/AddressMapper.java:12-21` `toMessage()`: шесть явных `@Mapping` совпадают по именам; `AddressMessage` (`passenger_request-main/…/messaging/src/main/java/ru/sber/transport/request/messaging/AddressMessage.java:14-64`) содержит также `id, country, latitude, longitude, existInVspGosbTbRegistry`, которые MapStruct сопоставляет неявно (одинаковые имена) — **потери полей нет**; явные аннотации избыточны.
- [КОД] CP `shared/models/geo/Waypoint.model.ts` (`isValid`: «!!this.latitude && !!this.longitude»); `buildAddressString` — представление, не хранение.
- [КОД] PR `dto/mapper/EntityDTOMapper.java:83-86,401-405`: `dtoToWaypoint(waypointDTO, index)`, `orderingIndex` = индекс в массиве.

**Цепочка (TAXI и GROUP_TRANSFER одинаково):** адресный поиск (`/api/geo/address` по координатам или `/api/addresses` по строке) → `WaypointModel` (фронт) → `POST /api/geo/route {coordinates: waypoints}` → `RouteDto{distance км, time мс, waypoints, segments}` → `POST /api/tariffs/calculate {waypoints, startPoint=waypoints[0], waitingTime=waypoints[0].waitTime, distance, time}` → `POST /api/requests/ {expected: {waypoints, segments, cost, outcomeCost, distance, time}, tariffId, outcomeTariffId, transportType, taxiClass|groupTransferClass}` → `request.waypoint` + `request.address`.
Источник цепочки: CP `…/hooks/useCreateTripRequest.ts:505-517`, CP `stores/Trip/DITrip.service.ts:94-128`, CP `constants/constants.env.js:52-54,136,165`.

**Применимость к FavoriteRoute:** для повторного заполнения заявки достаточно объекта точки (вызов справочника адресов по id не нужен и невозможен — id нестабилен). Минимальный состав и JSONB — решение архитектора.
**Неопределённость:** версия адресного API на стенде; используется ли `/api/addresses` в форме корп. кабинета.
**Следующий шаг:** Network-запись формы создания заявки на стенде (раздел E, сценарий E1). **Адресат:** архитектор, владелец Address. **Связанные:** T01.2:Q09; D02; D06.

## T01.1:D02. Ожидание в точках

**Ответ:** в платформе ожидание — `Duration` в точке; ввод в минутах только в промежуточных точках; серверной бизнес-валидации нет; есть только асинхронная fraud-проверка такси.

**Текущее поведение и границы:**

| Граница | Единица | Где | Правило |
|---|---|---|---|
| Ввод UI | минуты (число, допускаются дробные) | CP `shared/components/NumericInput/index.tsx` (регулярка `[^0-9.]`) | max нет; пусто → значение не меняется |
| Модель фронта | мс | CP `shared/models/geo/Waypoint.model.ts:72-80` | `waitTime = val*60*1000`; обратно `Math.trunc(ms/60000)` |
| Где вводится | — | CP `…/CreateTripRequest/Components/Waypoints.tsx:351` | `fields.length > 2 && !isFirstRow && !isLastRow` |
| JSON заявки | мс | PR `dto/WaypointDTO.java:85-88` | конвертер `DurationMillisConverter` (исходник библиотеки отсутствует) |
| БД заявки | `int8` | PR `database/model/Waypoint.java:47-48` | — |
| Расчёт тарифа | мин (усечение) | TF `CalculateServiceImpl.java:1026-1032` | `Duration.of(d.toSeconds()/1000, SECONDS)` — код трактует `Duration` из JSON как «секунды = миллисекунды» [ПРЕДПОЛОЖЕНИЕ: `WaypointDTO` тарифа без конвертера, Jackson читает число как секунды; `/1000` компенсирует] |
| Fraud (только TAXI) | мин (усечение по точке), м | PR `RequestForTaxiServiceImpl.java:860-891`; PR-res `application.yml:154-157` | сумма `> limitInMinutes` (default 60, строго больше); `distance = (int)км × 1000`; если `distance <= 50000` — fraud; иначе fraud только при одном городе. Асинхронно (`runAsync`, стр. 284), создание заявки не блокирует |

- Смысл null: при суммировании fraud `null` пропускается (`filter(Objects::nonNull)`, стр. 868); в расчёте трансфера фильтра нет (TF стр. 877-882) → `null` приведёт к ошибке [ПРЕДПОЛОЖЕНИЕ по коду, проверить стендом]. 0 — «без ожидания». Отрицательные значения сервером заявок не запрещены; в расчёте трансфера отрицательное даёт 0 через `max(...,0)`.
- Fraud-порог не является максимумом ввода и к трансферу не применяется (метод есть только в `RequestForTaxiServiceImpl`).

**Применимость к FavoriteRoute:** хранить 0 вместо null; формат JSON-поля FavoriteRoute можно выбрать любой, но при оформлении заявки требуется перевод в **мс**. Предложение T01.1 «waitTimeSeconds» не соответствует ни API заявки (мс), ни proto фактического тарифа (поля нет).
**Адресат:** заказчик (максимум/шаг/пусто), архитектор (формат). **Связанные:** T01.2:Q05; Коллега:Q3.

## T01.1:D03. Бесплатное ожидание

**Ответ:** см. таблицу C.1. Цепочка: тариф в корп. кабинете (`freeWaitingTime`, «Бесплатное время ожидания при подаче, мин.» для такси; «Бесплатное время ожидания, мин» для трансфера — CRP `modules/NewTariffs/constants/Tariffs.constants.ts:46,69`) → TF `TaxiTariff.freeWaitingTime` (стр. 94, `@Max(1000_00)`, default 0) / `GroupTransferTariff.freeWaitingTime` (стр. 88, default 0) → `CalculateServiceImpl.calculateData()` при каждом `POST /tariffs/calculate` → `CalculatedDto.cost`.
- Тариф выбирается: регион по первой точке (`getRegionBranch`, стр. 1261), организация из `TripDto.organizationId`, подразделение и должность аутентифицированного сотрудника, `EMPLOYEE_TRANSPORTATION`, ночной признак (только такси), договор — случайный взвешенный выбор по сумме договоров среди активных (`chooseContract`, стр. 1200-1240).
- **Между расчётом и оформлением** сервер заявок не пересчитывает стоимость: PR `RequestForTaxiServiceImpl.java:198`, `RequestForGroupTransferServiceImpl.java:162` — `expected(mapper.dtoToExpectedData(...))`.
- Стоимость платной минуты: такси — `waitCostPerMin` (первая точка), `waitCostPerMinIntermediate` (промежуточные); трансфер — `waitCostPerMin` (все точки; `waitCostPerMinIntermediate` в расчёте трансфера не используется).
- [ТЕСТ: ПРОЧИТАН, НЕ ЗАПУЩЕН] `tariff-main (2)/…/src/test/java/…/tariff/util/CalcUtil.java:96-111` считает промежуточное ожидание из `intermediateWaitingTime`, а прод-код — из суммы `waypoints` → тестовая модель расходится с реализацией; тесты с нулевым ожиданием расхождение не выявят.

**Решение для FavoriteRoute:** открыто (снимок `free_wait_time` или чтение). **Адресат:** заказчик + архитектор. **Связанные:** Коллега:Q1–Q3; T01.2:Q08.

## T01.1:D04. История и версия

**Доказательства:**
- [КОД] PR `database/model/Request.java:54` `@Audited` + `@SQLRestriction("active=true")`; стр. 77/85/113 — `@NotAudited` на части полей; `RequestForTaxi.java:106`, `RequestForGroupTransfer.java:101,179` — `@NotAudited`.
- [КОД] PR `database/model/publicTransport/CustomRevision.java:5-35`: `@RevisionEntity`, таблица `request_audit.request_for_public_revinfo`, `rev` serial, `timestamp`, `user_id`.
- [КОД] PR-res `application.yml:47-53`: `org.hibernate.envers.store_data_at_delete: true`; PR `RequestApplication.java:46`: `EnversRevisionRepositoryFactoryBean`.
- [КОД] Поиск `@Version`, `OptimisticLock`, `ETag`, `If-Match` в PR, TF, ADR, TRIPS, SRM, RA, RPT — не найдено.
- [КОД] AUD `src/main/java/ru/sber/transport/audit/writer/impl/AuditWriterImpl.java:41-66`: «AUDIT: source = %s; user = %s; … action = %s; result = %s», сохранение `dbResolver.save(source, authenticated, now, action)` — журнал действий без значений полей.
- [КОД] `trips-main/…/application/pom.xml:86` — `javers-jooq-starter`; в Java-коде TRIPS упоминаний Javers нет.
- [КОД] TRIPS `business/model/TripHistoryItem.java:9-40`: фиксированные пары old/new (диспетчер, водитель, статус, смена, ТС); изменение `factCost` пишется как `ActionType.FACT_DATA_CHANGING` без значения (`TripServiceImpl.java:590-603`).
- [КОД] Статусная история заявок: `RequestHistoryElementFor*` (статус, код, комментарий, инициатор, дата), напр. PR `RequestForGroupTransferServiceImpl.java:197-205`.

**Варианты для архитектора (факты для выбора):**

| Вариант | Опора в платформе | Восстановление на дату | Риски |
|---|---|---|---|
| Полные версии записи (Envers или своя таблица версий) | Envers уже используется в `request` (ревизия с user_id, timestamp) | Запрос ревизии ≤ даты; порядок — номер ревизии (serial) | Нужна схема `*_audit`; Envers не даёт optimistic locking |
| Снимок в заявке (номер, название, fixed_cost, …) | Заявка уже хранит снимки (`expected.*`, `tariffId`, `outcomeTariffId`) | Не нужно — значение в заявке | Требует расширения `RequestMessage` и отчётов (T04.1/T05.1) |
| EAV `favorite_route_history` | Прямого аналога нет (`TripHistoryItem` — типизированный) | Нужны начальные строки всех полей, группа изменения, порядок | `changed_at` не уникален; нужен идентификатор группы/ревизии |
| Комбинация (снимок в заявке + версии/EAV для аудита справочника) | — | Отчёты из снимка, аудит из истории | Два источника; согласованность |

**Неопределённость:** механизм конкурентности — ни в одном аналоге нет. **Адресат:** Демченко А. **Связанные:** T01.2:Q11.

## T01.1:D05. Истечение срока и повторная активация

| Причина недоступности | Механизм-аналог | Доказательство | Влияние на существующие заявки |
|---|---|---|---|
| Истёк период | Договор: `endDate`, шедулер `SCHEDULER_CONTRACT_CHECK` (default `0 0 21 * * *`) ставит `active=false`, если `endDate < сегодня`; события не шлёт | TF `scheduler/ContractCheckerScheduler.java:18-23`; TF `ContractServiceImpl.java:47-57` | Заявки не затрагиваются; новые расчёты не видят тарифы этого договора |
| Истёк период (трансфер CAR_CHOICE) | `tariffStartDate/EndDate`, фильтр по дате поездки при запросе | TF `GroupTransferTariff.java:50-54`; TF `GroupTransferTariffRepository.findAllTariff` | Проверка только в момент расчёта |
| Ручное удаление | `delete()` → `active=false` + событие (`sendDeleted` для такси) | TF `TariffServiceImpl.java:205-214` | Реплика такси удаляет запись → новые заявки с этим id — 404 |
| Замена новой версией | `disableOldTariff()` деактивирует похожие активные тарифы такси при сохранении нового | TF `TariffServiceImpl.java:116,608-652` | Id тарифа меняется |

Реактивация как операция не найдена. Правило FavoriteRoute из этого не выводится. **Адресат:** заказчик; затем архитектор.

## T01.1:D06. Единицы и диапазоны — матрица

| Величина | UI | REST/JSON | Java (заявка / тариф) | БД заявки | proto | Преобразования и округление | Источник |
|---|---|---|---|---|---|---|---|
| cost (план клиента) | ₽ (`cost/100`) | копейки, number | `double` (`@Min(0)`) / `long` коп | `expected_cost` (Double) | `int64 cost` | тариф: `(long)(… × коэффициенты)` усечение; SRM: `(long)(rideCost × доля)` | CP `stores/Trip/models/TripRequest.model.ts:138-139`; PR `dto/ExpectedDataDTO.java:48-50`; TF `ru/sber/transport/tariff/model/CalculatedDto.java:77-87` |
| outcomeCost (перевозчик) | — | копейки | `double` / `long` | `outcome_expected_cost` | — | для трансфера = cost (не такси/каршеринг) | TF `CalculateServiceImpl.java:403-406` |
| recommended/fixed (FR) | — | — | — | — | — | в платформе отсутствуют; int4 вмещает до 21 474 836,47 ₽ | T01.1 |
| distance | км | км, double | `double` | `expected_distance` Double | `double distance` | 2ГИС `total_distance/1000`; fraud `(int)км×1000` — усечение до целых км; трансфер `Math.round(distance)` для `minKm` | GEO `resources/application.yml:167`; GEO `…/routing/UnitProperties.java:21`; PR `RequestForTaxiServiceImpl.java:874` |
| time (движение) | ч:мм | мс | `Duration` | `expected_time int8` | `int64 time` (мс) | 2ГИС `total_duration` (с) → `Duration.ofSeconds` → JSON мс; тариф `toMinutes()` усечение | GEO `resources/application.yml:168`; GEO `…/service/impl/RouteServiceImpl.java:269`; GEO `…/dto/RouteDto.java:37`; TF `…/grpc/TariffActionReplierImpl.java:44` |
| waitTime | мин | мс | `Duration` | `wait_time int8` | нет (TF) | см. D02 | — |
| freeWaitingTime | мин | мин, int | `int`/`Integer` | — | — | — | TF `TaxiTariff.java:94` |

Замечание: в БД заявок `int8` для `Duration`; `OtherRequestsValidator` умножает значение `expected_time` на `0.000000001` (нс→с) — косвенный признак хранения в наносекундах [ПРЕДПОЛОЖЕНИЕ; PR `validate/request/impl/OtherRequestsValidator.java:14,50`].
Пример стенда `[СТЕНД, ранее]`: cost=40000 → 400 ₽; distance=0.886 → 886 м; time=203000 → 3 мин 23 с — согласуется с единицами кода, но сам по себе их не доказывает.
Налоги/доплаты: в найденном расчёте такси участвуют коэффициенты времени суток, пробок, опций, организации (`calculateTotalTaxiCoefficient`, TF стр. 1050-1071); НДС в расчёт не входит (есть только в договоре, `Contract.includeVat/vatValue`).

## T01.1:D07–D09
- **D07:** PR `dto/NewRequestDTO.java:111-118` — `@NotNull TripPurposeDTO purpose`; `commentForPurpose` ≤ 250. Цель переносится в заявку, не в маршрут. Решение — протокол 17.09 п.9.
- **D08:** ENUMS `TaxiClass.java:21-53`, `GroupTransferClass.java:18-28`. Совпадает с перечнем стенда. `OFFICIAL` имеет тот же порядок сортировки 3, что и `BUSINESS`.
- **D09:** место хранения совпадает с платформой (PR `Waypoint.java:47-48`).

## T01.1:D10. База, начало/конец и число точек
- Базовых точек в заявке нет: поиск `garage|basePoint|returnToBase|место базирования` в PR, TF, CP, CRP, CM, GEO — найдено только в тестовых данных 2ГИС и статусах телемеханики.
- Совпадение начала и конца не запрещено; для поиска совместной поездки при совпадении первых двух точек возвращается пустой список (PR `service/impl/RequestControllerServiceImpl.java:149-152`).
- Лимит 2…50 точек — PR `dto/ExpectedDataDTO.java:40`.
- **Многоточечность:** PR `AbstractTransportTypeService.java:122-144` (если точек > 2 → gRPC request-checks); RC `…/service/impl/TripRequestServiceImpl.java:91-99` — отказ, если у пассажира уже `>= MAX_MULTIPOINT_REQUESTS` (default 3) многоточечных заявок в сутки; RC `resources/application.yml:56-61` также лимит длительности 12 ч/сутки (`MAX_DURATION` 43 200 000 мс). Проверка вызывается после `saveAndFlush`, но внутри `@Transactional` сервиса [ПРЕДПОЛОЖЕНИЕ: исключение откатывает сохранение].
- Удаление точки в пути: не менее двух активных (PR `RequestServiceImpl.java:430`).
**Влияние на ТЗ:** маршрут «база → остановка → база» (3 точки) всегда многоточечный. **Адресат:** заказчик + владелец request-checks.

## T01.2:Q08. Предварительный расчёт — детали
- REST `POST /api/tariffs/calculate` → TF `…/tariff/controller/CalculatingController.java:34-38` → `CalculatingControllerImpl.java:16-18` (сотрудник = `getAuthenticatedEmployee`) → `CalculateServiceImpl.calculate(TripDto, Employee)` (стр. 101-122): регион первой точки, пояс региона, ночь → `calculateCommon` → `findAllTariffs` (активные тарифы `EMPLOYEE_TRANSPORTATION` организации из тела), фильтр подразделения, ночного признака, классов по должности (`getAvailableTaxiClasses`, стр. 494-527; без должности — только ECONOMY), выбор договора, расчёт, лимиты, подбор расходного тарифа.
- Конкретный тариф: `POST /api/tariffs/calculate/enum/{transportType}/{tariffId}` — проверяет `active` тарифа (стр. 84-99), но не договор/даты.
- Выбор автомобиля трансфера: `POST /api/tariffs/calculate/transport` — тарифы `TRANSFER_CAR_CHOICE` по дате, запрос свободных ТС у перевозчика (`contractorClient.getTransports`), окно = время + 30 мин + ожидание (стр. 124-196).
- gRPC `TariffService.calculate`: TF `resources/proto/dto/TariffDescriptor.proto:9-23` — комментарий «пересчёт стоимости поездки по изменённой заявке (пока только точки)», без `waitTimeSeconds`; `TariffActionReplierImpl.java:16-47` — маршрут через geo gRPC, `checkLimits=false`, ошибка при ≠1 результата. Используется в PR только из `deleteWaypoint` (`TariffGrpcClientImpl.java:16-45`).
- Копия proto с `int64 waitTimeSeconds = 11`: `request-aggregation-main/…/application/_documents/proto/tariff/dto/TariffDescriptor.proto:62`; в Java-коде RA `TariffServiceGrpc` не используется.
**Ошибки:** `RegionResolvingFailedException` (нет пояса региона), `EntityNotFoundException`, `RuntimeException("Не найден расходный тариф")` (стр. 452,470). Таймауты gRPC/REST в конфигурации не найдены (defaults).
**Адресат:** архитектор + владелец TariffService. **Связанные:** D03, D06, Коллега:Q1–Q3.

## T01.2:Q10. Номер
- [ТЕСТ: ПРОЧИТАН, НЕ ЗАПУЩЕН] `passenger_request-main/…/src/test/java/ru/sberbank/ditsib/transport/request/human_readable_id/service/SQCreatorImplTest.java:52-54`: «OT-0001-00000001», «OT-0001-00000002», «OT-9999-00000001»; стр. 60-80 — digitId −1 и 10000 отклоняются (`ConstraintViolationException`).
- [КОД] `tariff-main (2)/…/resources/db/20200817/create_sq_table.sql:1-19`: `prefix varchar(2)`, `orgDigitId numeric`, `sq numeric`, `UNIQUE (prefix, orgDigitId, sq)`, `UNIQUE (prefix, orgDigitId)`; аналог в PR-res `db/changelog/20210829/changes/init_table.sql:295-321`.
- [КОД] PR `human_readable_id/model/Prefix.java:6-21` — префиксы `OT, LD, US`; TF — `TF`. Префикса `FR` нет — потребуется в новом сервисе.
- digitId: из `Organization.getDigitId()` организации автора (PR `RequestForGroupTransferServiceImpl.java:131-138`; TF `TariffServiceImpl.java:110-114`).
**Неопределённость:** блокировки/ретраи внутри `SQGenerator` — исходник отсутствует. **Адресат:** архитектор.

## T01.2:Q12. Статусы (TAXI и GROUP_TRANSFER)

Параметры enum: `(описание, editable, approvable, cancelable, terminal)` — ENUMS `TripRequestStatus.java:463-468`.

| Код | Смысл | Терминальный по коду | Блокирует удаление FR по проекту |
|---|---|---|---|
| TAXI_/GROUP_TRANSFER_AWAITING_APPROVAL | На согласовании | нет | решить (вероятно да) |
| TAXI_/GROUP_TRANSFER_APPROVED | Согласована | нет | решить |
| *_AWAITING_SEARCH | Ожидайте назначения водителя | нет | решить |
| *_DRIVER_SEARCH | Поиск водителя | нет | решить |
| *_DRIVER_FOUND | Водитель назначен | нет | решить |
| *_DRIVER_ON_THE_WAY | Водитель в пути | нет | решить |
| *_DRIVER_ARRIVED | Водитель ожидает в точке отправления | нет | решить |
| TAXI_FREE_TIME_EXPIRED | Время бесплатного ожидания истекло | нет | решить |
| TAXI_WAYPOINT_ARRIVED | Прибытие в промежуточный пункт | нет | решить |
| *_TRIP_IN_PROGRESS | Поездка началась | нет | решить |
| GENAI_CHECK | Проверка GenAI (входит в `TAXI_STATUSES`) | нет | решить |
| *_CANCELLED | Отменено (коды 201/202/203/207) | да | нет (подтверждено: завершённые не мешают — для отменённых решить явно) |
| *_TRIP_FINISHED | Поездка завершена (101/102/103) | да | нет (согласовано) |

Источник: ENUMS `TripRequestStatus.java:24-79,155,416-461,473-528,870-874,901-931,1266-1286`. `getTerminalStatus(false)` используется для поиска активных заявок пассажира (PR `OtherRequestsValidator.java:55`).

## Коллега:Q2/Q3 — формулы (текущая реализация)

Такси (TF `CalculateServiceImpl.java:1016-1033`):
`waitingPrice = max(0, waitingTime_мин − freeWaitingTime) × waitCostPerMin + waitCostPerMinIntermediate × Σ(waitTime всех точек)_мин`, где `waitingTime` — поле `TripDto` (фронт передаёт ожидание первой точки, CP `useCreateTripRequest.ts:514`), сумма включает и первую точку [ПРЕДПОЛОЖЕНИЕ: при ненулевом ожидании в первой точке оно учитывается дважды; в UI первая точка ожидания не имеет].

Трансфер (TF стр. 870-895):
если `minKm > round(distance)` или `minMin > time_мин` → `minRideCost` (ожидание не оплачивается);
иначе `cost = costPerMin × max(time−minMin,0) + costPerKm × max(distance−minKm,0) + Σ_точки max(wait_i − freeWaitingTime, 0) × waitCostPerMin + minRideCost`; ставки город/пригород по числу различных `city` (> 1 — пригород).

| Пример | Такси (UI: ожидание в промежуточных) | Трансфер |
|---|---|---|
| 10 мин, лимит 3, 20 ₽/мин | 10 × ставка промежуточной (при 20 ₽ → 200 ₽); лимит не применяется | (10−3) × 20 = 140 ₽ |
| 2 остановки по 5 мин, лимит 3 | 10 оплачиваемых мин | 4 оплачиваемые мин |
| Гипотеза «общий лимит» (не реализована) | 7 мин | 7 мин |

## Коллега:Q6/Q7 — совместные поездки и изменение точек
- SRM: `sharedRide.rideCost = calculateByTariff(...)` (SRM `service/impl/SrmServiceImpl.java:1282`); для `GROUP_TRANSFER` — `default -> null` (стр. 1302); доля `roundDouble(requestDistance / totalDistance, 7)` (стр. 1341); цена участника `(long)(rideCost × доля)` (стр. 1347); экономия = индивидуальная цена − цена в совместной (стр. 1357-1370).
- PR получает долю и экономию из SRM (`MagentaAuxilaryServiceImpl.java:26-60`), `expected.cost` участника при этом не меняется (поиск `setCost(` в PR — только `TariffGrpcClientImpl`).
- Совместную заявку такси нельзя редактировать (PR `RequestForTaxiServiceImpl.java:346-349`).
- Удаление точки: PR `RequestServiceImpl.java:426-458` + `TariffGrpcClientImpl.java:34-37` — «Невозможно изменить маршрут, стоимость поездки по новому маршрут превышает изначальную»; иначе цена уменьшается до пересчитанной. Для FavoriteRoute противоречит протоколу 10.09 п.12 («при уменьшении точек тариф будет по фиксированной цене», статус «Требуется подтверждение»).

---

## E. Проверки на стенде (существующая платформа; фактический результат — «не проверено»)

| № | Вопрос | Предусловия | Шаги | Что записать (Network/UI) | Ожидаемо по коду | Ограничение вывода |
|---|---|---|---|---|---|---|
| E1 | D01, D06, Q09 | Пользователь клиентского приложения, тарифы такси и трансфера в регионе | Создать заявку такси на 3 точки (выбор адресов из поиска) | `GET /api/geo/address` / `/api/addresses`, `POST /api/geo/route`, `POST /api/tariffs/calculate`, `POST /api/requests/` — тела и ответы | `waitTime` в мс, `distance` км, `time` мс, `cost` коп, нет `district`/`id` в точке заявки | Один стенд, одна версия фронта |
| E2 | D02, Q3 | Как E1 | Промежуточная точка: ожидание 5 мин; затем две промежуточные по 5 мин | `waypoints[].waitTime` в calculate и requests; ответ calculate по классам | 300000 мс на точку; цена такси растёт на 10 × ставка промежуточной (без лимита) | Значения ставок тарифа — снять в корп. кабинете |
| E3 | Q2/Q3 трансфер | Тариф трансфера с известными `freeWaitingTime`, `waitCostPerMin`, `minKm/minMin` | Те же сценарии для GROUP_TRANSFER, маршрут длиннее `minKm`/`minMin` | Ответ calculate | Лимит вычитается по каждой точке | Если маршрут короче минимумов — ожидание не оплачивается |
| E4 | D02 | Как E3 | Отправить calculate с `waitTime: null` у точки (через DevTools replay) | Код ответа | Ошибка 500 [ПРЕДПОЛОЖЕНИЕ] | Изменяющих запросов не создаёт (calculate без записи) |
| E5 | Q06, Коллега:Q5 | Тариф, который можно деактивировать на тестовом стенде (по согласованию владельца) | Рассчитать, деактивировать тариф/договор, отправить сохранённый payload `POST /api/requests/` | Ответ создания заявки | Такси: 404 после удаления тарифа; трансфер: заявка создаётся | Изменяет данные стенда — только с разрешения владельца |
| E6 | D10 | Пассажир без заявок | Создать 4 заявки с 3 точками на один день | Ответ 4-й заявки | Отказ по лимиту многоточечных (default 3) | Конфигурация стенда может отличаться |
| E7 | Коллега:Q4 | Пользователь с правом заказа коллеге | Оформить «Я», «Мой коллега»; поискать режим «для клиента» | payload `passenger`, `organizationId` в ответе | Организация = организация автора | Фронт стенда может быть новее CP |
| E8 | Коллега:Q6/Q7 | Такси, совместная поездка | Создать совместную поездку двух участников; отменить одного | `costSharePart`, `expected.cost`, `savingsCash` в ответах | Доли по расстоянию, `expected.cost` не меняется | Трансфер совместно не поддерживается |
| E9 | Коллега:Q8 | Доступ к реестрам | Выгрузить реестр такси и трансфера | Формат файла, колонки | XLSX; CSV отсутствует | — |
| E10 | Q08 | Корп. пользователь без должности/с ограниченными классами | `POST /api/tariffs/calculate` из корп. кабинета | Список классов в ответе | Классы по должности пользователя, без должности — ECONOMY | — |

**Будущие критерии FavoriteRoute (не проверяются на текущем стенде):** сохранение точек с ожиданием в мс/минутах без потерь; блокировка удаления при нетерминальной заявке; номер FR уникален при параллельном создании; исторические значения в заявке неизменны после правки справочника.

---

## F. Недостающие исходники и документы

| Класс / артефакт | Где используется | Вопрос | Репозиторий-кандидат | Уверенность |
|---|---|---|---|---|
| `ru.sber.transport.humanreadableid.*` (`SQGenerator`, `BaseCompanySQ`), артефакт `ru.sber.transport:human-readable-generator` | PR `human_readable_id/*`, TF `humanReadableId/*` | Q10 | `lib / human_readable_generator` | высокая (имя артефакта совпадает) |
| `ru.sberbank.ditsib.converters.DurationMillisConverter`, `MillisDurationConverter` | PR `dto/WaypointDTO.java:7-8`, TF `TripDto`, GEO `RouteDto` | D02, D06 | неизвестно; возможно общая lib конвертеров [ПРЕДПОЛОЖЕНИЕ] | низкая |
| `ru.sber.transport.authorization.*` (`ControllerUtils`, `EmployeeOrganizationFunction`), артефакт `authorization` | PR `EmployeeServiceImpl.java:7`, RPT `CheckAccessConfiguration.java:5` | Q14 | `lib / authorization` [ПРЕДПОЛОЖЕНИЕ] | средняя |
| `ru.sber.transport:tariff-messaging:250527-1`, `request-model`, `tariff-model` | PR `application/pom.xml:100-102,199-201,255-258` | Q13, Q08 | `lib / grpc` или messaging-библиотеки [ПРЕДПОЛОЖЕНИЕ] | низкая |
| Фактическая конфигурация (`configs.properties`) | все сервисы (`optional:configserver`) | D02, D10, Q02 | БД Config Server стенда (исходники CFG настроек не содержат) | высокая |
| Актуальный клиентский фронт пассажиров | CP (2024) | D02, Коллега:Q4 | `front / Client passengers` актуальной ветки | высокая |
| Протокол 24.09.2026, T01.1 от 25.09.2026, сводка 28.09.2026 | ссылки в T01.2 и Questions_T01.1 | D09, D10, Q05 | у аналитика | высокая |
| Макеты FavoriteRoute | T01.2 ссылается на «утверждённые макеты» | Q03, Коллега:Q4 | у аналитика/дизайнера | высокая |

`platform / Trips reports`, `platform / Trip registries` не потребовались: `passenger_reports` не делегирует выгрузку в них (поиск по коду RPT).

---

## G. Вопросы для согласования

### G.1. Заказчику (Гусева И. П.)
1. **Ожидание (D02/Q05):** в платформе ожидание вводится в минутах только в промежуточных точках, без максимума. Нужен ли максимум (например, 120 мин) и шаг (1 мин / 5 мин)? Пусто = 0?
2. **«Можно не включать» (Коллега:Q2):** при ожидании 10 мин, лимите 3 мин, ставке 20 ₽/мин: вариант А — ожидание не оплачивается (0 ₽); вариант Б — лимит не применяется (200 ₽); вариант В — иное. Кто выбирает?
3. **Лимит на остановку или общий (Коллега:Q3):** сейчас трансфер применяет лимит к каждой остановке (2×5 мин → 4 мин оплаты), такси — не применяет к промежуточным (10 мин оплаты). Какое правило нужно для FavoriteRoute?
4. **Изменение лимита в тарифе (Коллега:Q1):** было 3, стало 5 — применять новое значение к маршруту, созданному ранее?
5. **Минимум точек (D10):** допустим ли маршрут «база → база»? Учитываются ли база и возврат в лимите 10? Учтите: заявка с ≥ 3 точками считается «многоточечной», и по умолчанию у пассажира не более 3 таких заявок в сутки.
6. **Истёкший тариф (Коллега:Q5):** при сохранённой фиксированной цене разрешать оформление со старым перевозчиком/тарифом или подбирать действующий?
7. **Уменьшение точек (Q7):** платформа сейчас при удалении точки в пути пересчитывает цену вниз. Для FavoriteRoute — сохранять фиксированную цену (протокол 10.09 п.12) или пересчитывать?
8. **Совместные поездки (Коллега:Q6):** совместные поездки есть только у такси; распределение по расстоянию. Фиксированная цена 1 350 ₽ — на поездку (по 675 ₽) или на каждого (по 1 350 ₽)? Что при факте 1 600 ₽ и отмене участника?
9. **Режимы (Коллега:Q4):** в текущем фронте есть «Я / Мой коллега» и «За счёт компании / В личных целях»; режим «для клиента» не найден. В каких режимах доступен FavoriteRoute для такси и трансфера?
10. **Отчётность (Коллега:Q8):** платформа выгружает только XLSX; CSV и PDF для пассажирских реестров нет. Подтвердите объём: только XLSX или новые форматы?
11. **Дубли (Q01), период (Q02), реактивация (Q04/D05), видимость в списке (Q03)** — по формулировкам Questions_T01.2.

### G.2. Архитектору (Демченко А.)
1. **История (D04/Q11):** Envers (как в `request`) / снимок в заявке / EAV / комбинация; ключ версии (в платформе нет `@Version`/ETag).
2. **Формат ожидания:** API заявок требует мс; предлагаемый `waitTimeSeconds` опирается на неиспользуемую копию proto (RA). Выбрать единицу хранения и адаптер.
3. **Расчёт (Q08):** использовать REST `/tariffs/calculate` (классы зависят от должности аутентифицированного пользователя, тарифы — от подразделения) или новый метод для расчёта «от имени организации»?
4. **Контроль тарифа (Q13):** истечение договора не порождает события; при повторной загрузке тарифа такси id меняется — как FavoriteRoute поддерживает `tariff_id`?
5. **Номер (Q10):** собственная `company_sq` с префиксом `FR` через `human-readable-generator` или иной генератор; поведение после 99 999 999.
6. **Гонка удаления и оформления (Q12):** в платформе нет механизма; нужен контракт проверки нетерминальных заявок по FavoriteRoute.
7. **Доступ (Q14):** организация — через сотрудника автора; нужна ли поддержка переключения организации.

### G.3. Владельцам сервисов (ИТ-лид Ярцев Р. и команды)
- **Заявки:** будет ли сервер проверять фиксированную цену/связь с маршрутом (сейчас `expected` принимается от клиента без пересчёта); применять ли к FavoriteRoute лимит многоточечных и fraud по ожиданию; поведение `waitTime: null`.
- **Тарифы:** пояс JVM шедулера договоров; дата `tripDate.toLocalDate()` в UTC для трансфера; расхождение `CalcUtil` (тест) и прод-формулы такси.
- **Адреса:** версия API на стенде (код 3.21 генерируется из внешней спецификации).
- **Отчёты:** расширение `RequestMessage` и БД отчётов для номера/названия/исходной цены.
- **Библиотеки:** предоставить `human-readable-generator`, `authorization`, конвертеры Duration.

### G.4. Организатору приёмки
- Договор (разд. 5) предусматривает проверку соответствия ТЗ за 10 рабочих дней и подписание УПД; стенд и состав проверок не указаны. Нужны: принимающий, стенд, тестовые организации/пользователи/тарифы, доступ к корп. кабинету и Network-логам.
- Допустимость имитаторов: существующая заглушка 2ГИС (`TASK_DONE`) относится к матрице расстояний SRM и не подтверждает расчёт маршрута FavoriteRoute; реальные `geo/route` и `tariffs/calculate` нужны для интеграционной проверки.

---

## H. Противоречия документов

| Источник A | Источник B | Противоречие | Влияние | Кто разрешает |
|---|---|---|---|---|
| `v1_Схема процесса (1).docx` стр. «wait_times JSON» | `T01.1_FavoriteRoute_updated` (23.09) §2 | Отдельный `wait_times` vs ожидание внутри точек | Модель/DDL | Закрыто решением D09; БТ обновить (Яна Нос) |
| `v1_Схема процесса` «vehicle_id … под каждое ТС отдельная запись» | Протокол 17.09 п.4; T01.1 §1 | Привязка к ТС vs выбор автомобиля при заявке | Модель, UI | Закрыто 17.09; БТ обновить |
| `T01.1_updated` §2, §5 `waitTimeSeconds` «присутствует в .proto» | TF `resources/proto/dto/TariffDescriptor.proto:51-62` (поля нет); поле есть только в RA `_documents` | Основание модели — неиспользуемая копия proto | Формат JSON, T01.4 | Архитектор + владелец TariffService |
| `T01.1_updated` §5 «единица waitTime в ответах заявок не установлена» | PR `WaypointDTO.java:85-88`; CP `Waypoint.model.ts:72-80` | Единица установлена: мс | D02 | Яна Нос (исправить формулировку) |
| `БТ последний.docx` «Корректировка маршрута не доступна (только удаление)» | Протокол 10.09 п.7 «Корректировка разрешена» | Редактирование | T01.2 PUT | Закрыто 10.09; БТ обновить |
| `v1_Схема процесса` риск №7 «динамический пересчёт; применяется бо́льшая из двух» | Протокол 10.09 п.3 «сброс номера, заявка становится обычной»; Questions_T01.2 «Уже подтверждено» | Сравнение с фиксированной ценой vs простой сброс | T04.1 | Заказчик |
| Протокол 10.09 п.12 «при уменьшении точек — фиксированная цена» (требует подтверждения) | Текущая платформа: удаление точки пересчитывает цену вниз (PR `RequestServiceImpl.java:426-458`) | Поведение при уменьшении точек | T04.1 | Заказчик |
| `БТ последний.docx` риск №8 «Предупреждение, блокировка выбора» / «переход на динамический расчёт» | Протокол 10.09 п.15 «истёкший просто не появляется в списке» | Следствие истечения | UI/API | Закрыто 10.09; БТ обновить |
| `v1_Схема процесса`, `БТ последний` «CSV/Excel/PDF», T05.2 `GET /reports/export` | RPT: только XLSX (+ PDF ТТН грузов) | Объём отчётности | T05.x | Заказчик / владелец отчётности |
| `v1_Схема процесса` «Расхождение в 3 раза — флаг для менеджера, процесс согласования» | Протокол 17.09 п.12 «отдельный признак не нужен» | Эскалация | T04.1 | Закрыто 17.09; БТ обновить |
| `T01.1 (v6)` «роль Управляющий справочниками (BR-04)» | Протокол 10.09 п.10 «доступ есть у всех авторизованных с правами»; T01.2 «отдельная бизнес-роль не выделяется» | Роли | Доступ, role-check | Закрыто; обновить v6/БТ |
| Протокол 17.09 п.5 «Служебный — в документации отсутствует» | ENUMS `TaxiClass.OFFICIAL("Official","Служебный",3)` | Наличие класса | D08 | Закрыто кодом |
| Договор, Прил. №1 «БТ … Exported on 02/19/2026» | Решения встреч 10.09–24.09 | Договорная редакция БТ старше согласованных изменений | Приёмка по ТЗ | Руководитель проекта / заказчик |
| `Вопрос ответ.docx` D02 «В существующей заявке такси waitTime — Duration» | Код: `Duration` в Java, **мс** в JSON | Уточнение единицы | D02 | Яна Нос |

Документы, требующие синхронной корректировки: БТ (все версии) — `wait_times`, `vehicle_id`, форматы выгрузки, редактирование, эскалация; T01.1 (обновлённая) — формулировки про proto и единицы `waitTime`; T01.2 — формат ошибок/страниц по соглашениям платформы. Сами документы в этой работе не изменялись (кроме дополнения `Вопрос ответ.docx` по запросу пользователя — см. ниже).

---

## Уточнение 29.09.2026 (2): сверка с каталогом GitLab `sbertransport`

Источник: документ пользователя со структурой GitLab (14 скриншотов групп `dzo`, `autoservice`, `cargo`, `platform` — 4 страницы, `front`, `lib` — 3 страницы, `passenger`, `fleet`). Текстового слоя нет, прочитаны изображения.

**Ограничения источника:** подгруппа `front / lib` на скриншоте не раскрыта; между страницами `platform` («Roles» → «Request external registry») и `lib` («javers» → «indexer») возможны пропущенные проекты — стыки скриншотов не перекрываются.

**Соответствие скачанных архивов группам (уточняет A.1):**
- `passenger / Tariff` = `tariff-main (2)` (`spring.application.name: tariff`); `fleet / Tariff` = `tariff-main` (`tariff-fleet`) — подтверждает разделение из A.1.
- `passenger / oto` в архивах отсутствует; скачан `cargo / OTO` (`oto-cargo`). Именно это объясняет упоминание «двух oto» в инструкции.
- `passenger / Passenger reports`, `Request aggregation`, `SRM`, `Passenger_request`, `platform / Address`, `Config server`, `Gateway`, `GEO`, `Request checks`, `Trips` — уже скачаны.

**Исправление раздела F (уверенность повышена):**

| Артефакт / класс | Репозиторий в каталоге | Основание | Вопросы |
|---|---|---|---|
| `human-readable-generator` (`SQGenerator`) | `lib / human_readable_generator` | имя совпадает | Q10 |
| `authorization` (`ControllerUtils`, `EmployeeOrganizationFunction`) | `lib / authorization` | имя совпадает | Q14 |
| `ru.sberbank.ditsib.converters.DurationMillisConverter` | `lib / core` — **кандидат, уверенность средняя-высокая** | [КОД] `transport_core-main/transport_core-main/pom.xml` зависит от артефакта `core`; PR, TF и GEO получают его транзитивно; TF зависит от `core` напрямую; в скачанных исходниках пакета нет | D02, D06 |
| `tariff-grpc`, `request-model`, `tariff-model`, `srm-model`, `geo grpc` | `lib / grpc` — кандидат | TF pom: `tariff-grpc`; PR pom: `request-model`, `tariff-model` | Q08, I01 (актуальный контракт `TariffService`) |

**Дополнительно найденные в каталоге проекты, релевантные открытым вопросам:** `platform / Corporate` (организации, digitId, `/api/organizations/self/addresses/favorite` → `corporate-service`), `platform / Trip purpose request check` (D07), `platform / Fraud monitoring` (D02), `platform / Limits` + `lib / limits_sdk` (резервирование лимита на сумму заявки), `passenger / Passenger approvals` (согласование, Q12), `passenger / Passanger integrations` (внешние агрегаторы такси, факт/план), `lib / jooq_envers` (вариант истории для D04), `front / lib` (общие компоненты фронта).

---

## Уточнение 29.09.2026 (3): анализ загруженных репозиториев первого приоритета

Основание: коммит `d8287eb3` «Чистый коммит без секретов» в `main` (история переписана; ветка исследования перенесена на новый `main` без изменения содержимого). Методика прежняя: только чтение.

### (3).A. Что загружено и в каком состоянии

| Папка (корень исходников) | Проект GitLab | Версия (pom / version.json) | Состояние |
|---|---|---|---|
| `human_readable_generator-main` | `lib / human_readable_generator` | `human-readable-generator 4.6` | полный исходник |
| `authorization-main` | `lib / authorization` | `authorization 4.10` | полный исходник |
| `core-main` | `lib / core` | `core 3.24` | полный исходник |
| `grpc-main` | `lib / grpc` | `ru.sber.transport.grpc parent 4.15` | полный, но это **инфраструктура gRPC** (перехватчики авторизации/трассировки), контрактов тарифа нет |
| `jooq_envers-main` | `lib / jooq_envers` | `jooq-envers 4.2-SNAPSHOT` | **только pom и README**, исходников нет; потребителей в скачанных pom не найдено |
| `corporate-main` | `platform / Corporate` | parent 5.15, `corporate-service` | полный исходник |
| `limits-main` | `platform / Limits` | parent `${release.version}` | полный исходник |
| `passenger-approvals-main` | `passenger / Passenger approvals` | parent 5.10, `approvals` | полный исходник |
| `fraud-monitoring-main` | `platform / Fraud monitoring` | parent 5.14 | полный исходник |
| `passanger-integrations-main` | `passenger / Passanger integrations` | parent 5.15, `integrations` | полный исходник |
| `trip-purpose-request-check-main` | `platform / Trip purpose request check` | — | **только `CODEOWNERS`** (и `.gitignore`), исходников нет |
| `corp-passengers-main (1)`, `(2)` | `front / Corp passengers` | `feat/TRANSPORT-36167`, `c21830f`, 06.11.2025 | **идентичны** уже имевшемуся `corp-passengers-main` (`diff -rq` — различий нет) |
| — | `front / Client passengers` (актуальная ветка), `front / lib` | — | **не загружены**; `client-passengers-main` остался версией 02.08.2024 (удалён только `build.sh`) |

### (3).B. Новые факты по вопросам

**T01.2:Q10 — номер и конкуренция (исправление вывода от 29.09 (1): «механизм атомарности не виден» → установлен).**
- [КОД] `human_readable_generator-main/…/humanreadableid/service/impl/CompanySQServiceImpl.java:24` — `@Transactional(propagation = Propagation.NOT_SUPPORTED)`: счётчик читается и пишется **вне транзакции вызывающего**.
- [КОД] там же, стр. 32-44, `getOrCreateCompanySQ()`: «findByPrefixAndOrgDigitId(...)» → «companySQ.setSq(start + count)» → «saveAndFlush» — чтение-изменение-запись **без блокировки строки**; `@Version` в `BaseCompanySQ` нет.
- [КОД] `…/humanreadableid/dao/AbstractRepository.java:27-28` — метод `findByPrefixAndOrgDigitIdForWrite` объявлен обычным `@Query` без `@Lock`/`FOR UPDATE`; генератором `SQGeneratorImpl` не используется.
- [КОД] `…/service/impl/HumanReadableIdFormatterImpl.java:24` — «String.format("%s-%04d-%08d", …)»; при счётчике > 99 999 999 номер станет длиннее (формат не обрезает), `varchar(20)` T01.1 это вмещает, но шаблон `FR-XXXX-XXXXXXXX` нарушится.
- [КОД] `…/service/SQGenerator.java:22,32` — `@Min(1) @Max(9999)` для digitId; `…/model/BaseCompanySQ.java:29` — префикс ровно 2 символа.
- [ТЕСТ: ПРОЧИТАН, НЕ ЗАПУЩЕН] `human_readable_generator-main/…/test/…/SQGeneratorImplTest.java` — только последовательный сценарий «US-0001-00000001», конкурентных тестов нет.
- **Следствие [ПРЕДПОЛОЖЕНИЕ по коду]:** два одновременных `getNextId` для одной организации могут получить **одинаковый номер**; защита — только уникальный индекс номера в целевой таблице (в T01.1 есть `UNIQUE favorite_route_number`) → второй запрос упадёт на ограничении, нужен повтор. Первичное создание строки счётчика защищено `UNIQUE (prefix, orgDigitId)` — одновременная первая вставка тоже даст ошибку у одного из запросов.
- [КОД] Альтернативная реализация в `corporate`: `corporate-main/…/application/src/main/java/ru/sber/transport/corporate/providers/human_readable/BaseHumanReadableProvider.java:28` — метод `synchronized` (блокировка только в пределах одного экземпляра JVM) и проверка уже занятых номеров в целевой таблице перед выдачей (стр. 45-63 по циклу). В стр. 83-86 `context().update(table()).set(…SQ…).execute()` **без условия WHERE** — обновляет все строки `company_sq` [КОД; как образец для FR не использовать, сообщить владельцу `corporate`].
- [КОД] digitId: `corporate-main/…/web/http/mappers/OrganizationWebMapper.java:24` — `digitId` не принимается от клиента; `providers-database/…/changelog/20230809/changelog.yml:40-44` — колонка `digit_id` переведена с `serial` на `bigint` (исходно назначалась последовательностью БД). Организация с digitId > 9999 не сможет получить номер (ограничение генератора).
- **Статус Q10:** техническая часть — Подтверждена кодом; решение — Архитектор (блокировка `SELECT … FOR UPDATE`/advisory-lock/последовательность БД, повтор при конфликте, поведение после 99 999 999).

**T01.2:Q14 — организация и доступ (исправление: «Частично; нужен репозиторий» → Подтверждена кодом).**
- [КОД] `authorization-main/…/authorization/utils/ControllerUtils.java:28` — идентификатор пользователя берётся из JWT `jti` («token.getToken().getId()»).
- [КОД] `…/authorization/service/impl/CheckUserAccessServiceImpl.java:44-56` — организация пользователя вычисляется функцией сервиса (`EmployeeOrganizationFunction`: пользователь → сотрудник → подразделение → организация) и **должна совпадать** с запрошенной; исключение — claim `data_master=true` (стр. 86, «пользователь системы мастер-данных»).
- [КОД] `…/authorization/aspect/CheckOrganizationAspect.java:39` — работает через аннотации `@CheckOrganizationAccess` на методе и `@Organization` на параметре (UUID или поле DTO).
- [КОД] `…/authorization/exceptions/UnauthorizedException.java:11` — ответ **403** (`@ResponseStatus(FORBIDDEN)`); политика «404 для чужого» в платформе не используется.
- [КОД] `corporate-main/corporate-main/providers-database/src/main/resources/db/changelog/20221114/changelog.yml:40-46` — уникальный индекс `corporate_employee_user_id_uk` на `employee.user_id`: **один пользователь = один сотрудник = одна организация**; переключения организации в модели данных нет.
- [КОД] Использование: 35 файлов, в основном контроллеры `corporate` (цели поездки, сотрудники, подразделения, должности) и выгрузки `passenger_reports`; в `passenger_request` и `tariff` аннотация не применяется.
- **Рекомендация (не решение):** для API FavoriteRoute — путь с `{organizationId}` + `@CheckOrganizationAccess`, как у `corporate /{organizationId}/purposes`. Решение — Архитектор.

**T01.1:D02 / D06 — единицы (исправление метки: [ПРЕДПОЛОЖЕНИЕ] о конвертере → [КОД]).**
- [КОД] `core-main/core-main/src/main/java/ru/sberbank/ditsib/converters/DurationMillisConverter.java:20,25` — сериализация `Duration` в миллисекунды; в методе `serialize` есть ветка `orElse(0L)`. **[ИСПРАВЛЕНО 01.10.2026: прежний вывод «`null` сериализуется как `0`» неточен — см. (5).D. Для свойства-поля со значением `null` Jackson обычно не вызывает кастомный сериализатор, поэтому эта ветка, скорее всего, недостижима.]**
- [КОД] `…/converters/MillisDurationConverter.java:19,25` — чтение из миллисекунд («Duration.ofMillis», «getLongValue»; дробная часть отбрасывается).
- Следствие (**исправлено 01.10.2026**): ранее здесь утверждалось, что отсутствующее ожидание неотличимо от нуля. Это подтверждено только для сообщения подрядчику (`OutContractorTaxiTripMessageMapperImpl.java:317`, `null` → `0`). Для JSON-ответов заявки `null` скорее всего остаётся `null` (или опускается) — требует проверки на стенде/тестом (E4-подобная проверка, не запускалось). `[ПРЕДПОЛОЖЕНИЕ]`
- [КОД] `fraud-monitoring-main/…/messaging/listeners/TripRequestListener.java:257` — в мониторинге фрода ожидание хранится в мс; `…/business/impl/FraudDecisionServiceImpl.java:26` — решения по случаям принимает пользователь (`solve`), т. е. fraud по ожиданию разбирается вручную и не блокирует заявку.

**T01.1:D04 / T01.2:Q11 — история: найдены ещё два образца в платформе.**
- [КОД] **Копирование при изменении (версии записей):** `corporate-main/…/corpclient/service/impl/TripPurposeServiceImpl.java:70-79` — при редактировании цели поездки создаётся **новая запись**, старая получает `active=false`, версии связывает общий ключ `purposeParentLabel` (`…/corpclient/database/model/TripPurpose.java:63`); удаление — `active=false` + событие Kafka (стр. 240-243); дубль названия в организации — `DuplicateDataException` → 409 (стр. 90). Аналогично ведёт себя тариф такси (`disableOldTariff`). Для FavoriteRoute это вариант «полные версии»: заявка ссылается на id конкретной версии, номер FR играет роль группового ключа.
- [КОД] **Триггерный снимок строк:** `corporate-main/…/providers-database/src/main/resources/db/changelog/20200629/create_function.sql:1-25` — триггер пишет в `corporate.t_history` JSON старой/новой строки (`row_to_json`) для `corporate.employee`; колонка `who` триггером **не заполняется** (автора нет).
- `lib / jooq_envers` — исходников нет; оценить нельзя.
- **Статус D04:** техническая часть — Подтверждена кодом (три образца: Envers в `request`, версии записей в `corporate`/`tariff`, триггерный JSON в `corporate`); решение — Архитектор.

**T01.1:D07 — цель поездки (дополнение).**
- [КОД] `corporate-main/…/corpclient/database/model/TripPurpose.java:30-42` — цель принадлежит организации и ограничивается подразделениями, датами, временем суток и днями недели. Следовательно, доступность цели зависит от даты/времени конкретной заявки — хранение цели в FavoriteRoute не только не требуется (решение 17.09), но и было бы некорректно.
- Серверная проверка цели при создании заявки находится в `platform / Trip purpose request check` — **исходники не загружены** (только CODEOWNERS).

**T01.2:Q03 — список (дополнение).** [КОД] `corporate-main/…/corpclient/controller/TripPurposeController.java` — у справочника раздельные методы «все активные» (`GET /{organizationId}/purposes`) и «все, включая неактивные» (`/all`), поиск `/search?value=`; пагинации нет. Образец для разделения «список выбора» и «административный список».

**T01.2:Q12 / заявки — согласование (новое).**
- [КОД] `passenger-approvals-main/…/approvals/database/model/ApprovalsSettings.java:18,44,51` — настройки на пару «организация + вид транспорта»: `approval_active`, `min_cost_to_be_approved` (коп), плюс исключения по цели и региону.
- [КОД] `…/approvals/services/impl/ApprovalsSettingsInjectionServiceImpl.java:75,88,105,120` — автосогласование, если согласование выключено или **стоимость заявки (коп) строго меньше порога**; иначе заявка ждёт согласующего (статус `*_AWAITING_APPROVAL`).
- Влияние на ТЗ: заявка по FavoriteRoute пройдёт существующее согласование по своей плановой стоимости (фиксированной цене), если бизнес не решит иначе. **Заказчик:** нужны ли исключения для избранных маршрутов.

**Коллега:Q5 / Q6 / O02 — цена перевозчика и факт (новое).**
- [КОД] `passanger-integrations-main/…/integrations/mapper/OrderRequestMapper.java:31,45` — в заказе перевозчику по универсальному API тариф не передаётся (`tariff` ignore), передаются маршрут, время, расстояние, ожидание точек; **цена (плановая/фиксированная) не передаётся**.
- [КОД] там же, стр. 101-111 — комментарий «В рамках проекта Манжерок передаём тип транспорта вместо класса трансфера»: для `GROUP_TRANSFER` перевозчику уходит класс `GROUP_TRANSFER`, а не `TRANSFER_*` — **класс трансфера перевозчику не сообщается**.
- [КОД] `…/mapper/InContractorTaxiTripInProgressMessageMapper.java:38,45` — перевозчик возвращает `price` (Double) и `waitTime`; PR `service/impl/InProgressGroupTransferMessageProcessorImpl.java:107,111-112` — факт сохраняется в `trip.tripFactPrice` (`intValue()`, усечение) и `tripFactWaitTime` (**минуты** перевозчика → `Duration.ofMinutes`). Сравнения факта с планом и флага превышения в коде `passenger_request` нет (поиск `variance|overrun` — только проверка месячного пробега `request-checks`, `maxMonthlyDistance` default 5 000 000).
- Единица `price` перевозчика в коде не документирована [ПРЕДПОЛОЖЕНИЕ: копейки по аналогии с платформой] → **владелец интеграций**.
- Влияние: правило «цена исполнителя выше фиксированной» (O02) потребует новой логики сравнения в `passenger_request`/T04.1; сейчас факт только записывается.

**Лимиты (новое, к Коллега:Q6/Q8).**
- [КОД] PR `service/impl/ReservationServiceImpl.java:58,104` — резерв лимита на `(long) expected.cost` (коп) с `setCheckLimit(false)` (превышение лимита создание не блокирует); PR `RequestForTaxiServiceImpl.java:416,439,510` — списание при завершении/отмене тоже по **плановой** `expected.cost`, не по факту. Для `GROUP_TRANSFER` вызовов резерва не найдено.
- Влияние: при фиксированной цене лимит будет резервироваться и списываться по фиксированной цене, если она попадёт в `expected.cost`.

### (3).C. Раздел F — актуализация
| Было | Стало |
|---|---|
| `human-readable-generator` — нужен репозиторий | Разобран (см. Q10) |
| `authorization` — нужен репозиторий | Разобран (см. Q14) |
| Конвертеры Duration — кандидат `lib/core` | Подтверждено: `core-main/…/ditsib/converters/*` |
| `tariff-grpc`, `request-model`, `tariff-model` — кандидат `lib/grpc` | **Исправление:** `lib/grpc` их не содержит (только инфраструктура). Источник этих артефактов не установлен [ПРЕДПОЛОЖЕНИЕ: модули самих сервисов `tariff`/`passenger_request` или отдельные lib-проекты, не видимые на скриншотах] → владелец сервиса тарифов |
| — | `trip-purpose-request-check` — загружен без исходников |
| — | `jooq_envers` — загружен без исходников |
| — | `front / Client passengers` актуальной ветки и `front / lib` — не загружены |

### (3).D. Новые проверки на стенде
| № | Вопрос | Шаги | Ожидаемо по коду | Ограничение |
|---|---|---|---|---|
| E11 | Q10 | Не на стенде: нагрузочный тест в тестовой среде — 20 параллельных созданий заявок одной организации | Возможны совпадения номеров/ошибки уникальности | Только с разрешения владельца среды |
| E12 | Q12, согласование | Создать заявку такси ниже и выше порога `min_cost_to_be_approved` организации | Ниже порога — автосогласование | Порог — из настроек стенда |
| E13 | O02 | Трансфер с перевозчиком по универсальному API: сравнить `expected.cost` и `tripFactPrice` в карточке/реестре | Факт записывается, флага превышения нет | Нужен тестовый перевозчик |

### (3).E. Дополнительные вопросы для согласования
- **Архитектору:** генератор номеров не защищён от гонки (см. Q10) — выбрать механизм для FR; принять ли для API FavoriteRoute шаблон `{organizationId}` + `@CheckOrganizationAccess` и ответ 403; выбрать схему истории из трёх найденных образцов.
- **Заказчику:** проходит ли заявка по избранному маршруту обычное согласование по сумме; перевозчику цена не передаётся — как перевозчик узнаёт о фиксированной цене (договорённость вне системы или доработка интеграции).
- **Владельцу `corporate`:** обновление `company_sq` без условия WHERE (`BaseHumanReadableProvider.java:83-86`).
- **Владельцу интеграций:** единица `price` и `waitTime` в ответах перевозчика; нужна ли передача класса трансфера.
- **Аналитику (загрузка):** исходники `trip-purpose-request-check`, актуальная ветка `front / Client passengers`, раскрыть `front / lib`.

---

## Уточнение 30.09.2026 (4): develop-ветка клиентского фронта, front/lib, повторная загрузка trip-purpose-request-check

Основание: коммит `22097970` в `main` («Удален build.sh с секретом в client-passengers-develop»), влит в ветку исследования обычным merge.

### (4).A. Что загружено

| Папка | Проект | Версия | Результат сверки |
|---|---|---|---|
| `client-passengers-develop` | `front / Client passengers`, ветка develop | `package.json` 1.0.0; `version.json` тот же, что в старом архиве (`release/D-03.014.000`, 02.08.2024) | `diff -rq` со старым `client-passengers-main`: различаются только `.npmrc` и `yarn.lock`, в старом дополнительно есть 2 папки `tripTypes` (компенсации общественного транспорта). **Исходный код оформления заявок такси/трансфера не изменился** — выводы по CP остаются в силе и теперь относятся к develop. `version.json`, по-видимому, формируется при сборке и версию develop не отражает [ПРЕДПОЛОЖЕНИЕ]. |
| `tariff-main (1)` | `passenger / Tariff` | parent 5.8 | **Идентичен** `tariff-main (2)` (`diff -rq` — 0 различий) |
| `mf-core-main` | `front / lib / mf-core` | 2.0.22 (совпадает с зависимостью `^2.0.22` в CP) | разобран |
| `ui-kit-main` | `front / lib / ui-kit` | 2.1.0 (в CP `^2.0.14`) | разобран поверхностно — базовые компоненты |
| `tool-kit-main` | `front / lib / tool-kit` | 3.2.4 | конфигурации линтеров/сборки — для вопросов не нужен |
| `ai-agent-main` | `front / lib / ai-agent` | 0.8.0 | UI чат-ассистента — для вопросов не нужен |
| `trip-purpose-request-check-main` | `platform / Trip purpose request check` | — | **В Git по-прежнему только `CODEOWNERS` и `.gitignore`** (двойная вложенность есть, исходников нет; `.gitignore` проекта и корня их не исключает; последнее изменение папки — коммит `d8287eb3`) |
| корень: `НАХОДКИ_FavoriteRoute (1).md` | — | — | копия этого отчёта (на момент загрузки совпадала полностью) |
| корень: `~$прос ответ.docx` | — | — | служебный файл блокировки Word — не документ, можно удалить |

### (4).B. Новые факты

- **Организация во фронте (к Q14, Коллега:Q4).** [КОД] `mf-core-main/mf-core-main/src/stores/SelfEmployee/DISelfStore.ts:35-36` — `orgId` фронта = `selfEmployee.organizationId` из `GET /api/organizations/self`; выбора/переключения организации во фронт-ядре нет. Согласуется с бэкендом (один пользователь = одна организация).
- **Таймаут фронта (к Q08, T01.4).** [КОД] `mf-core-main/…/src/stores/Http/HttpMiddleware.ts:47` — у всех HTTP-запросов фронта `timeout: 10000` (10 с) и заголовок `x-client-type`. Расчёт маршрута и тарифа для FavoriteRoute (цепочка `geo/route` → `tariffs/calculate`) должен укладываться в этот предел либо потребует отдельной настройки.
- **Число адресов.** [КОД] `client-passengers-develop/…/CreateTripRequest/Components/TransportOrder.tsx:831` — кнопка «Добавить адрес» в веб-клиенте ничем не ограничена; сервер заявок принимает 2…50 точек. [КОД] `client-main/client-main/src/modules/DashboardCards/components/faq/items.tsx:167-169` — FAQ: «программа не дает указать более 10 адресов… Мы можем составить сложный маршрут с указанием до 19 адресов… обратитесь в службу поддержки». Ограничение 10 в просмотренном веб-коде не найдено → вероятно, в мобильном приложении [ПРЕДПОЛОЖЕНИЕ]. Значение 10 совпадает с лимитом БТ для FavoriteRoute.
- **Виджет на главной (к T01.3).** [КОД] `client-main/…/DashboardCards/components/widgets/quickOrderWidget/QuickOrderWidget.tsx` — «избранные сервисы» быстрого заказа хранятся только в `localStorage` браузера, не более 2 (стр. 36-54). Серверного «избранного» на главной нет; виджет избранных маршрутов — новая функция, образец хранения из этого виджета не подходит.
- **Список (к Q03).** [КОД] `ui-kit-main/ui-kit-main/src/components/Pagination/Pagination.tsx:18` — размеры страницы по умолчанию `[10, 20, 50, 100]`; согласуется с макетом (10 на странице).
- **Контракты gRPC/моделей.** Артефакты `tariff-grpc`, `tariff-model`, `tariff-messaging`, `request-model`, `srm-model` (все версии `250527-1`) не найдены ни в одном загруженном репозитории, включая `as_sbertransport_libraries` (модули: `enums`, `http-request-check-api`, `qr-code`, `common-api-service`) и `fleet / Tariff` (его модуль — `tariff-fleet-grpc`). Для вывода по `waitTimeSeconds` это не критично: фактический сервер тарифов компилирует собственный `resources/proto`, где поля нет.

### (4).C. Статусы после этой загрузки
- Коллега:Q4 (режимы) — остаётся «Частично»: актуальный develop совпадает с проанализированным кодом; режим «для клиента» в веб-клиенте отсутствует. Мобильное приложение не обследовано.
- T01.1:D07 — серверная проверка цели по-прежнему недоступна (нет исходников `trip-purpose-request-check`).
- Остальные статусы уточнения (3) без изменений.

### (4).D. Что ещё не хватает (конкретно)
1. **`platform / Trip purpose request check` — исходники** (ожидаются папки `application/`, `pom.xml`, `src/`). В репозиторий попали только `CODEOWNERS` и `.gitignore`. Проверить: распакован ли ZIP полностью и добавлены ли файлы в коммит.
2. **Мобильное клиентское приложение** (если есть в другой группе GitLab) — для лимита 10 адресов, режимов оформления и формы ожидания.
3. **Источник артефактов `tariff-model`, `tariff-grpc`, `request-model`, `srm-model`, `tariff-messaging` (версия `250527-1`)** — поиск в GitLab по имени артефакта; кандидаты: `platform / Documentation` или проекты `lib`, выпавшие со скриншотов [ПРЕДПОЛОЖЕНИЕ]. Приоритет низкий.
4. **Не репозитории:** выгрузка свойств Config Server (`configs.properties`) для `request`, `tariff`, `request-checks`, `approvals`; версии сервисов на стенде; Network-логи сценариев E1–E13.

---

## Уточнение 01.10.2026 (5): структура адреса и точек для «Избранных маршрутов» (сверка с OpenAPI «Платформа — Адреса» v3.16.0)

Контекст: предлагается скопировать для FavoriteRoute структуру из OpenAPI адресного сервиса (файл «…2.yml», `components.schemas`): обязательные `label`, `address.country`, `address.region`, `address.latitude`, `address.longitude`; опциональные `address.city/street/house/building/structure`; плюс `waitingTime`. Ниже — сверка с кодом заявок. Решения остаются за заказчиком/архитектором.

| ID | Вопрос | Что установлено | Источник | Статус |
|---|---|---|---|---|
| Адрес:V1 | Формат отсутствующих значений | `[КОД]` В YAML нет ни одного `nullable` (0 вхождений) — «поле опущено» и `null` не различаются. Адресный сервис сериализует с `default-property-inclusion: non_null` (пустые поля не отдаёт). В заявке `latitude/longitude` — примитивы `double`: отсутствующая координата становится `0`, а не `null`; `equalsByCoords` считает `0` «координат нет». | `address-main/.../application/src/main/resources/application.yml:22`; `passenger_request-main/.../dto/WaypointDTO.java:70,76`; `srm-main/.../model/WaypointDTO.java` (`equalsByCoords`) | Частично: поведение платформы установлено; выбор «опускать/null» — решение архитектора |
| Адрес:V2 | Что уходит в диспетчерскую | `[КОД]` В Kafka-сообщение подрядчику уходит `Waypoint(name, latitude, longitude, waitTime, passengers)`: `name` — одна строка адреса (`address.toStringTrimmed()`, `OutContractorTaxiTripMessageMapperImpl.java:314`), структурированного адреса нет; `waitTime` — `Integer` секунд. Что нужно диспетчерской на самом деле, из кода не видно. | `passenger_request-main/.../messaging/Waypoint.java`; `passanger-integrations-main/.../mapper/OrderRequestMapper.java:57-75` | Не закрыт (внешнее требование) |
| Адрес:V3 | Нужен ли `id` адреса | `[КОД]` Вход `NewFavoriteAddress` без `id`, ответ `UserAddress` с `id`. В адресном сервисе `id` — случайный UUID из `GeoClientImpl` (не стабильный идентификатор адреса). В `WaypointDTO` заявки `id` адреса нет — адрес хранится по значению. `[ПРЕДПОЛОЖЕНИЕ]` Вариант: точки в `jsonb` без `id`, как в заявке; избранное не зависит от справочника. | YAML `components.schemas`; `address-main` `GeoClientImpl` | Частично — решение проекта |
| Адрес:V4 | `waitingTime` внутри `address` или снаружи | `[КОД]` В схемах адресного сервиса поля нет. В платформе ожидание — на уровне точки (`WaypointDTO.waitTime`, `Duration`, в JSON миллисекунды через `DurationMillisConverter`/`MillisDurationConverter`), не внутри адреса. Три варианта имени: `waitTime` (код), `waitTimeSeconds` (T01.1), `waitingTime` (вопрос). `[ПРЕДПОЛОЖЕНИЕ]` Вариант: на уровне точки, снаружи `address`. | `srm-main/.../WaypointDTO.java`; `passenger_request-main/.../dto/WaypointDTO.java:88`; `lib/core` `DurationMillisConverter` | Закрыт по фактам; имя и единицу утвердить |
| Адрес:V5 | Обязательность координат | `[КОД]` Для избранного адреса адресный сервис требует (`NewFavoriteAddress.required`: country, region, latitude, longitude, label). Для адреса встречи — нет (`NewAddress`: координаты необязательные). Заявка при отсутствии координат принимает `0`. Диспетчерская и тариф работают по координатам. | YAML `components.schemas`; `WaypointDTO` заявки | Частично — решение: требовать всегда или геокодировать при использовании |

### (5).B. Дополнительные находки при сверке

- **Корпус/строение перепутаны (новое противоречие, раздел H).** `[КОД]` YAML: `building` = «Номер корпуса», `structure` = «Номер строения». `passenger_request-main/.../dto/WaypointDTO.java:54-64` и `srm-main/.../WaypointDTO.java:58,64`: `building` = «Строение», `structure` = «Корпус». При копировании без выбора избранное разойдётся с заявкой — зафиксировать значение в ТЗ.
- **Ошибка в OpenAPI.** `[КОД]` Схема `NewAddress` содержит `label` в `required`, но поля `label` в `properties` нет (он объявлен в `NewMeetingAddress`). Копировать как есть нельзя; сообщить владельцу адресного сервиса.
- **Единицы ожидания по цепочке (три разные).** `[КОД]` В JSON заявки `waitTime` — миллисекунды; в Kafka-сообщение подрядчику уходит `Integer` **секунд** (`OutContractorTaxiTripMessageMapperImpl.java:317` — `(int) waypoint.getWaitTime().toSeconds()`, при `null` — `0`; для SRM-ветки `:549` берётся `getWaitingTime()` как есть); обратный факт от подрядчика — минуты (`tripFactWaitTime`, раздел D). Единицу `waitTime` в избранном (мс или секунды) и конвертацию при создании заявки описать в ТЗ.

### (5).C. Плановое ожидание в точке маршрута: формулировка для ТЗ и варианты для архитектора

Размещение. Плановое время ожидания хранится в соответствующей точке маршрута — как поле элемента массива waypoints, а не внутри объекта address. Так же устроена текущая платформа: время ожидания (WaypointDTO.waitTime) — свойство точки поездки, а не адреса. [КОД]

Имя поля и единица измерения — на согласование архитектора. Варианты:

- Вариант А — waitTime, миллисекунды. Совпадает с JSON заявки (DurationMillisConverter / MillisDurationConverter): точки избранного маршрута переносятся в запрос создания заявки без пересчёта; дробные минуты, которые допускает поле ввода в UI, не теряются. Недостаток: имя не указывает единицу, а в цепочке заявки единиц уже три (мс — заявка, секунды — сообщение подрядчику, минуты — факт от подрядчика). [КОД]
- Вариант Б — waitTimeMinutes, минуты. Совпадает с вводом пользователя (UI принимает минуты и умножает на 60 000); единица видна из имени. Недостатки: при создании заявки нужен пересчёт ×60 000; необходимо утвердить тип (целое или дробное) и правило округления, иначе дробные минуты из UI будут потеряны. [КОД]
- Вариант В — waitTimeSeconds, секунды: не рекомендуется. Не совпадает ни с форматом заявки (мс), ни с вводом пользователя (минуты). Секунды в платформе используются только при отправке ожидания подрядчику (OutContractorTaxiTripMessageMapperImpl.java:317) — это пересчёт на границе с внешней системой, а не формат хранения. В действующей редакции T01.1 указано именно waitTimeSeconds — требуется привести в соответствие с принятым решением. [КОД][ТРЕБОВАНИЕ/РЕШЕНИЕ]

Предложение (не решение): вариант А — waitTime в миллисекундах, как в заявке; он не требует пересчёта и не теряет значения. Если будет принят вариант Б, в ТЗ необходимо зафиксировать тип, правило округления и пересчёт при создании заявки. [ПРЕДПОЛОЖЕНИЕ]

К утверждению: (1) имя и единица поля; (2) допустимость отсутствия значения и его трактовка (см. (5).D); (3) верхняя граница ожидания — отдельный вопрос заказчику (см. (5).D); (4) правка T01.1 по списку из (5).D.

### (5).D. Уточнения по замечаниям (01.10.2026)

**1. Что на самом деле известно про `null` и `0` у `waitTime`** (исправление ранее записанного).
- `[КОД]` `null` → `0` подтверждено **только** в двух местах: сообщение подрядчику — `waypoint.getWaitTime() != null ? (int) toSeconds() : 0` (`OutContractorTaxiTripMessageMapperImpl.java:317`), SRM-ветка — `Optional.ofNullable(getWaitingTime()).orElse(0)` (`:549`).
- `[КОД]` В JSON заявки `waitTime` — `Duration` (объект, не примитив): `WaypointDTO.java:88`, поле БД `request.waypoint.wait_time bigint` без `NOT NULL` (`db/changelog/20210829/changes/init_table.sql:138`). Хранить `null` технически можно.
- `[КОД]` В `DurationMillisConverter.serialize` есть `orElse(0L)`, но `[ПРЕДПОЛОЖЕНИЕ]` по стандартному поведению Jackson для `null`-свойства кастомный сериализатор не вызывается, значит `null` в ответе остаётся `null` (или опускается при `non_null`). Не запускалось; проверить тестом или на стенде.
- Вывод: «в платформе `null` превращается в `0`» верно для границы с подрядчиком, а не для API заявки в целом. `0` вместо отсутствия — это про `latitude/longitude` (примитив `double`), не про `waitTime`.

**2. Верхняя граница ожидания — два разных вопроса.**
- Технический факт `[КОД]`: ограничения нет ни в БД (в Liquibase-скриптах `request` нет `CHECK` по `wait_time`), ни в `WaypointDTO` (нет аннотаций); в UI максимума нет. Единственный порог — асинхронная fraud-проверка только для TAXI (`> 60` мин суммарно, по умолчанию), заявку она не блокирует.
- Бизнес-вопрос заказчику `[ТРЕБОВАНИЕ/РЕШЕНИЕ — не решён]`: нужен ли максимум для планового ожидания в избранном маршруте и какой. Значение «24 часа» — пример для обсуждения, не требование.

**3. Выбор между А и Б.** `[ПРЕДПОЛОЖЕНИЕ]` Довод за А сильнее: пересчёт при каждом вызове — источник ошибок; UI может показывать миллисекунды в минутах без изменения хранения. Довод за Б (при отладке в БД лежит 7 200 000 мс вместо «120 мин») снимается комментарием к полю в DDL. Итог: А — предпочтительно, Б — допустимо с зафиксированным типом и округлением, В — нет. Окончательно решает архитектор.

**4. Что именно править в T01.1** (файл `T01.1_FavoriteRoute_updated (1).docx`, версия от 23.09). Применить, когда архитектор утвердит вариант:
1. Таблица изменений (строка «wait_times удалено»): `waypoints[i].waitTimeSeconds` → принятое имя. Правая часть строки — `waitTimeSeconds` → принятое имя (сама колонка `wait_times` остаётся удалённой, переименовывать её не нужно). Фразу «Поле и название единицы присутствуют в .proto» уточнить **(исправлено 01.10.2026: ранее здесь было сказано «поля нет», это неточно)**: `int64 waitTimeSeconds = 11` есть в копии `request-aggregation-main/.../_documents/proto/tariff/dto/TariffDescriptor.proto:62`, которая в Java-коде не используется; в proto сервиса тарифов (`tariff-main (1)`, `tariff-main (2)`) и в копии `passenger_request-main/.../_documents/api/grpc/dto/` поля нет (0 вхождений). Просмотрены только эти четыре файла `TariffDescriptor.proto`; другие `.proto`, на которые мог ссылаться автор T01.1, не предоставлены. Если выберут `waitTime` в мс, копия в RA будет расходиться с моделью — решить, кто её обновляет.
2. Описание поля в таблице структуры `waypoints`: имя; тип `int64` (для миллисекунд подходит; для минут решить целое/дробное); текст «Целое число секунд ≥ 0» → принятая единица и правило допустимости `null`/максимума.
3. Два JSON-примера: ключ `"waitTimeSeconds": 0` → принятое имя.
4. Абзац про `free_wait_time` и `waitTime`: фраза «единица waitTime в ответах заявок … не установлена» устарела — по коду это миллисекунды (JSON заявки).
5. Комментарий к столбцу в DDL (`is 'От 2 до 10 точек: адрес, координаты, waitTimeSeconds'`): заменить имя и добавить единицу измерения.
6. Если принимается вариант Б: добавить в ТЗ формулу пересчёта (мин × 60 000) при создании заявки и правило округления.


## Дополнение (7) от 03.10.2026 — проверки заявок, влияющие на избранные маршруты, и счётчик номера

- **Fraud-пометка TAXI_WAITING_TIME** `[КОД]` `passenger_request-main/…/RequestForTaxiServiceImpl.java:860-891`: пометка ставится асинхронно, только для такси, если суммарное ожидание по всем точкам больше 60 минут (значение по умолчанию) и расстояние не больше 50 км; на большем расстоянии — только если все точки в одном городе. Заявку пометка не блокирует. Для избранных маршрутов — вопрос T04.1.
- **Лимит многоточечных заявок** `[КОД]` `request-checks-main/…/TripRequestRepository.java:55-67, 123-137`; `application.yml:55-57` (MAX_MULTIPOINT_REQUESTS, значение по умолчанию 3): заявка с числом точек больше 2 считается многоточечной; при создании заявки отклоняется (409), если у пассажира уже 3 и более неотменённых многоточечных заявок на те же локальные сутки. Минимальный избранный маршрут (база, остановка, база) — многоточечный. `[ПРЕДПОЛОЖЕНИЕ]` фактическое значение на стенде не проверялось.
- **Счётчик номера** `[КОД]` `human_readable_generator-main/…/CompanySQServiceImpl.java`: на каждый сервис — своя таблица `company_sq` (prefix, orgdigitid, sq, dt_insert, dt_modify); строку при первом обращении создаёт сама библиотека (`getOrCreateCompanySQ`), блокировки нет; формат номера `%s-%04d-%08d`, digitId 1–9999. Образец таблицы — `request.company_sq` (`passenger_request-main/…/db/changelog/20210829/changes/init_table.sql:295-324`).
- **Ожидание в расчёте** `[КОД]` `tariff-main (2)/…/CalculateServiceImpl.java:877-880, :1027-1029`: цепочка `getWaitTime → Duration::toSeconds` без проверки на null; отсутствующее ожидание нужно передавать как 0. Веб-клиент передаёт `waitingTime = waypoints[0].waitTime` (`client-passengers-main/…/useCreateTripRequest.ts:514`).

## Журнал изменений
- 29.09.2026 — первая редакция. Параллельно в `Вопрос ответ.docx` добавлен датированный раздел «Результаты исследования исходного кода 29.09.2026» (три таблицы, исходные таблицы сохранены без изменений).
- 29.09.2026 (2) — добавлено уточнение по каталогу GitLab; раздел F уточнён (кандидаты `lib/core`, `lib/grpc`, приоритеты скачивания).
- 29.09.2026 (3) — разобраны загруженные репозитории первого приоритета; исправлены выводы по Q10, Q14, D02 (метка), разделу F (`lib/grpc`); добавлены образцы истории, согласование, цена перевозчика, лимиты.
- 30.09.2026 (4) — сверка develop-ветки Client passengers (идентична), разбор front/lib (mf-core, ui-kit), повторная проверка trip-purpose-request-check (исходников нет); уточнён список недостающего.
- 01.10.2026 (5) — сверка предлагаемой структуры адреса/точек (OpenAPI «Платформа — Адреса» v3.16.0) с кодом заявок: ответы по пяти открытым вопросам, два новых замечания (корпус/строение, ошибка в `NewAddress`); параллельно дополнен `Вопрос ответ.docx`. Добавлены подразделы (5).C (формулировка по плановому ожиданию в точке, варианты для архитектора) и (5).D (исправление вывода про `null`→`0`, раздельные вопросы по верхней границе, список правок T01.1).
- 01.10.2026 (6) — проверка T01.1 v1.8: 19 замечаний и вопросов с вариантами и предложением вынесены в `ПРОВЕРКА_T01.1_v1.8.md` и продублированы в `Вопрос ответ.docx` (последний раздел).
- 03.10.2026 (7) — добавлено дополнение (7): TAXI_WAITING_TIME, лимит многоточечных заявок, счётчик company_sq, передача ожидания в расчёт.
