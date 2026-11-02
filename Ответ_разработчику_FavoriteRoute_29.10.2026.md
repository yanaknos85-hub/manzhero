# FavoriteRoute: ответ по Kafka и T01.4

Актуализировано 02.11.2026 · к T01.2 и T01.4 от 30.10.2026 · основание — исходный код платформы и итоговые редакции спецификаций; сервисы и стенд не запускались. Первая версия — 29.10.2026.

**Метки.** **[Код]** — подтверждено исходником. **[Локально]** — проверено вне репозитория на настоящих классах. **[Спецификация]** — правило закреплено в T01.2 или T01.4, указан раздел. **[Документация]** — по документации Spring Cloud Stream или Spring Data, проверить на стенде. **[Запросить]** — получить у владельцев сервисов или на стенде.

**Сокращения путей.** PR — `passenger_request-main`; TF — `tariff-main (2)/.../ru/sberbank/ditsib/transport/tariff`; KF — `kafka_functional-main`; GZ — `geo-zones-main`; GEO — `geo-main`; DSP — `dispatcher-main`; CTR — `contractor-main`; AUTH — `authentication-main`; ENUMS — `as_sbertransport_libraries-main/enums/.../constants`. Снимки исходников — ветка `main`, коммит `d8287eb3`.

## Коротко

- **Kafka** — часть T01.2, не T01.4. Читать `service.request` своей группой; ключ — `id` заявки. Ссылки на маршрут в сообщении пока нет: `favoriteRouteId` и `favoriteRouteVersion` добавит сервис заявок (T04.1).
- **`creationTime`** — UTC, массив без смещения; доля секунды в миллисекундах или наносекундах в зависимости от версии Spring Cloud Function у сервиса заявок. Единицу сделать настройкой и зафиксировать по реальному сообщению.
- **Доступ пользователя к тарифу решён:** FavoriteRoute доверяет отбору tariff-main и не проверяет подразделение и должность сотрудника. Независимо проверяются организация тарифа и её вхождение в договор.
- **Цена** — только `POST /calculate/enum/{transportType}/{tariffId}` по каждому тарифу. Каталожный `POST /calculate` не используется.
- **Каталог** — два обхода `POST /search` (INCOME и TRANSITIONAL) с явными `contractType`, `organizationId`, `serviceType` и двумя сортировками, затем карточка и договор каждого тарифа.
- **Геометрия** — `POST /route` с `EMPLOYEE_TRANSPORTATION`; `distance` — км (Double), `time` — целые мс.
- **Первая точка** — `GET /address` только с координатами: `200 []` → 400 `VALIDATION_ERROR`, 417 → 502. Достаточно одного совместимого адресного кандидата.
- **Территория и пояс** — `POST /searchBranch`; ключи ответа `parent_id` и `time_zone`; смещение пояса приводится к виду `+03:00`. Для GET и списка контекст кэшируется.
- **Машины CAR_CHOICE** — contractor → dispatcher v1: машины в эксплуатации, признак мягкого удаления `active` не проверяется (принятое ограничение). Рабочая цепочка технической учётки перевозчика по коду не подтверждена — первая проверка на стенде.
- **Открыто:** адрес стенда и учётки, брокеры Kafka, тестовое сообщение, права в БД, система каждого перевозчика Манжерока, настройки среды, тестовые данные — раздел 4.

## 1. Kafka `service.request`

Правила обработки — **[Спецификация]** T01.2, раздел 6 «Топики Kafka»; терминальные статусы — T01.2, DELETE, шаг 3. В T01.4 Kafka не входит (T01.4, 1.1).

### 1.1. Топик и подключение

| Параметр | Значение | Основание |
|---|---|---|
| Топик | `service.request`; у сервиса заявок — биндинг продюсера `requestOutput-out-0` | PR `kafka.yml:225-226` |
| Брокеры | `spring.cloud.stream.kafka.binder.brokers: ${KAFKA_BROKERS}` (у сервиса заявок — внутри `binders.kafka.environment`); в репозитории только заглушка сборки | PR `kafka.yml:16-17` |
| Ключ | `id` заявки. Сериализатор ключа — `JsonSerializer`, поэтому UUID приходит JSON-строкой в кавычках. Потребители платформы читают ключ через `JsonDeserializer`; при `StringDeserializer` кавычки нужно снять | KF `OutputBridge.java:148`; PR `kafka.yml:18-21` |
| Заголовки | `contentType=application/json`, `transportType`, `messageId`, `fraudHandled`, `principal`. `messageId` — новый UUID при каждой отправке: повтор того же состояния придёт с другим значением | KF `OutputBridge.java:74, 149`; PR `BaseRequestSender.java:34-38` |
| Свойства топика | По умолчанию `compression.type=gzip`, `cleanup.policy=compact,delete`, `retention.ms=604800000` (7 дней); переопределяются окружением. `auto-alter-topics=true`: сервис заявок может менять свойства топика | PR `kafka.yml:22-29` |
| Безопасность | В сервисе заявок SSL не настроен. fraud-monitoring читает через отдельный биндер `kafka-ssl` с группой `<имя>-ssl`; параметров этого биндера в репозиториях нет | fraud-monitoring `application-kafka-ssl.yml` |

Образец потребителя — reports: `destination: service.request`, `group: ${spring.application.name}` (`passenger_reports-main/.../application.yml:37-39`). Для FavoriteRoute — своя стабильная группа, отдельная от групп других сервисов (T01.2, раздел 6).

**[Запросить]** адреса брокеров стенда и продуктива; протокол безопасности, сертификаты, учётную запись и ACL на чтение для группы FavoriteRoute; фактические `retention.ms` и `cleanup.policy` топика.

### 1.2. Схема сообщения

Класс `RequestMessage` (PR `messaging/src/main/java/ru/sber/transport/request/messaging/RequestMessage.java`). Поля, которые использует FavoriteRoute:

| Поле | Строка | Тип и смысл |
|---|---|---|
| `id` | 33 | UUID заявки, совпадает с ключом |
| `organizationId` | 63 | UUID организации; сверяется с организацией маршрута |
| `creationTime` | 73 | `LocalDateTime` в UTC, массив без смещения (п. 1.3) |
| `transportType` | 88 | `"TAXI"` или `"GROUP_TRANSFER"` |
| `status` | 178 | имя статуса `TripRequestStatus` (п. 1.4) |
| `statusCode` | 188 | код подстатуса 101–207; на классификацию не влияет |
| `deleted` | 228 | `boolean`; по T01.2 условием сброса ссылки не является |

Остальные поля: `timeZone` (78); `waypoints[]` (98) — `id`, `address`, `waitTime`, `checkinAutomatic`, `checkinManual`, `absenceReason`, `orderingIndex`; `tariffId` и `outcomeTariffId` (108, 113); `desiredDate` (143); `expected` (203) — `cost` и `outcomeCost` в копейках, `distance` в км, `time`; `contractorId` (244).

**[Код]** Полей `favoriteRouteId` и `favoriteRouteVersion` нет. Их добавление в сервис заявок — доработка T04.1; до неё потребитель не получит ссылку на маршрут.

**[Спецификация]** T01.2, раздел 6 и «Сервис заявок». Наличие ссылки определяется по значениям, а не по ключам:

- оба значения не заданы — ссылки нет; допустимы отсутствие ключей, два `null` и сочетание отсутствующего ключа с `null`. Прежняя связь удаляется;
- задано ровно одно значение — некорректная частичная пара: прежняя связь сохраняется, событие диагностируется;
- заданы оба — проверяются UUID и версия; версия `0` — заданное значение.

Основание: пустые поля сообщения опускаются или приходят как `null` в зависимости от версии Spring Cloud Function (п. 1.3).

### 1.3. Даты, длительности и пустые поля

Сообщение сериализует ObjectMapper из Spring Cloud Function (SCF). **[Код]** Поведение двух веток подтверждено байткодом:

- 4.2.x копирует ObjectMapper приложения заявок: `WRITE_DATES_AS_TIMESTAMPS: true`, `WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS: false`, `default-property-inclusion: non_null` (PR `application.yml:79-83`); `WRITE_DURATIONS_AS_TIMESTAMPS` выключен по умолчанию Spring Boot;
- 4.1.x создаёт новый ObjectMapper с настройками Jackson по умолчанию.

**Какая ветка у сервиса заявок, не установлено:** версии приходят из недоступного родительского POM `java-parent 4.13.2`.

**[Локально]** Одно и то же сообщение в двух ветках:

| Поле | SCF 4.2.x | SCF 4.1.x |
|---|---|---|
| `creationTime`, `desiredDate` | `[2026,10,29,9,0,5,123]` — доля в миллисекундах | `[2026,10,29,9,0,5,123456000]` — в наносекундах |
| `expected.time`, `waypoints[].waitTime` | `"PT22M"`, `"PT1M"` | `1320.000000000`, `60.000000000` |
| Пустые поля | ключ отсутствует | `null` |

Правила разбора `creationTime`:

- время в UTC: TAXI берёт `LocalDateTime.now(Clock.systemUTC())`, GROUP_TRANSFER — `LocalDateTime.now(ZoneOffset.UTC)` (PR `RequestForTaxiServiceImpl.java:189`, `BeanConfig.java:26-28`, `RequestForGroupTransferServiceImpl.java:127`);
- массив из 5–7 чисел: `[год, месяц, день, час, минута]`, затем секунды, затем доля. Секунды и доля опускаются, только если обе равны нулю; при нулевой доле элементов шесть. В ветке 4.2 доля меньше 1 мс записывается явным `0`;
- **[Локально]** стандартный `JavaTimeModule` читает 7-й элемент как наносекунды: `[...,5,123]` превратится в `09:00:05.000000123`. Единицу доли задать настройкой потребителя (мс или нс). Значение вне диапазона выбранной единицы — некорректный payload по T01.2;
- `Duration` стандартный десериализатор читает в обоих видах, отдельной настройки не нужно.

Ветку на реальном сообщении однозначно видно по `expected.time` или `waitTime`: строка `PT…` — 4.2 (миллисекунды), число с дробью — 4.1 (наносекунды). Это верно, если Config Server не переопределяет `spring.jackson.*`.

**[Запросить]** реальные сообщения TAXI и GROUP_TRANSFER со стенда (с заполненным `expected` и ожиданием в точке); версию `spring-cloud-function-context` в сервисе заявок (`mvn dependency:tree` или список `BOOT-INF/lib` образа); `spring.jackson.*` из Config Server.

### 1.4. Терминальные статусы

**[Код]** Признак `terminal` в `TripRequestStatus` (ENUMS `TripRequestStatus.java:467`).

- Терминальные: `TAXI_CANCELLED` (29), `TAXI_TRIP_FINISHED` (79), `GROUP_TRANSFER_CANCELLED` (421), `GROUP_TRANSFER_TRIP_FINISHED` (461).
- Нетерминальные для обоих видов транспорта: `*_AWAITING_APPROVAL`, `*_APPROVED`, `*_AWAITING_SEARCH`, `*_DRIVER_SEARCH`, `*_DRIVER_FOUND`, `*_DRIVER_ON_THE_WAY`, `*_DRIVER_ARRIVED`, `*_TRIP_IN_PROGRESS`. Только у TAXI: `TAXI_FREE_TIME_EXPIRED`, `TAXI_WAYPOINT_ARRIVED`. Общий `GENAI_CHECK` (155) тоже нетерминальный.
- **[Спецификация]** T01.2: неизвестное имя сохраняется и считается блокирующим; обратный переход из терминального статуса применяется; в DELETE терминальная связь перечитывается, только если её возраст от `updated_at` строго меньше N часов.

### 1.5. Смещения, повторы и восстановление

Что гарантирует источник:

- **[Код]** Продюсер отправляет асинхронно пулом из 10 потоков, ошибка отправки только пишется в лог (KF `OutputBridge.java:39, 132`). Сообщение может потеряться, прийти не по порядку (в том числе по одной заявке) или прийти до фиксации транзакции заявки. Формальной гарантии доставки нет.
- **[Код]** В сообщении нет версии события и времени изменения; `changeDate` в него не входит.
- **[Код]** Повторной отправки нет. `/synchronize` сервиса заявок для восстановления не подходит: он обходит только заявки такси и не охватывает GROUP_TRANSFER (PR `RequestRepository.java:172-192`).

Настройки потребителя FavoriteRoute:

- **[Спецификация]** T01.2, раздел 6: offset подтверждается только после успешного применения или предусмотренной фиксации отклонённого сообщения; ошибка БД успехом не считается. Новая группа читает с начала доступной истории; автоматический переход на `latest` запрещён.
- **[Документация]** Группа с явным именем при первом подключении начинает с самого раннего смещения: Spring Cloud Stream ставит `startOffset=earliest`. При `compact,delete` и 7 днях новая группа получит последние состояния заявок примерно за 7 дней. Проверить на версии биндера стенда.
- DLQ (`enableDlq: true`) — только для некорректного payload: разбор, схема, `creationTime`. По документации биндера имя DLQ по умолчанию — `error.service.request.<группа>`.
- Для ошибки БД нужен повтор с паузой без фиксации смещения. `autoCommitOnError: true`, как у reports, и `maxAttempts: 1` с `defaultRetryable: false`, как у fraud-monitoring, для FavoriteRoute не подходят.
- Оповещения: лаг группы, остановка потребителя, сброс смещения клиентом Kafka при выходе за срок хранения.

**[Запросить]** процедуру сброса смещений группы на стенде и в продуктиве и ответственного; фактический срок хранения и политику очистки.

## 2. T01.4 — расчёт, тарифы, территория

Ниже — факты и форматы для реализации со ссылками на разделы T01.4 от 30.10.2026. Правила и алгоритмы — в самой спецификации; здесь они повторяются только там, где нужен контекст.

### 2.1. Адреса и авторизация

**[Спецификация]** T01.4, 4.1. Вызовы — через шлюз стенда (`https://<адрес стенда>/api/<сервис>/...`, префикс отрезается) либо напрямую по настроенному адресу сервиса.

| Назначение | Внутренний путь | Через шлюз |
|---|---|---|
| Геометрия | geo `POST /route` | `POST /api/geo/route` |
| Здание первой точки | geo `GET /address` | `GET /api/geo/address` |
| Ветка зон | geo-zones `POST /searchBranch` | `POST /api/geo-zones/searchBranch` |
| Каталог | tariff `POST /search` | `POST /api/tariffs/search` |
| Карточка TAXI | tariff `GET /taxi/{id}` | `GET /api/tariffs/taxi/{id}` |
| Карточка GROUP_TRANSFER | tariff `GET /group_transfer/{id}` | `GET /api/tariffs/group_transfer/{id}` |
| Договор | tariff `GET /contracts/{contractId}` | `GET /api/tariffs/contracts/{contractId}` |
| Автомобили перевозчика | contractor `GET /{contractorId}/transport/` | `GET /api/contractors/{contractorId}/transport/` |
| Точная цена | tariff `POST /calculate/enum/{transportType}/{tariffId}` | `POST /api/tariffs/calculate/enum/{transportType}/{tariffId}` |

Не используются: общая карточка `GET /{transportTypeId}/{tariffId}`, каталожный `POST /calculate`, `/calculate/transport…`, `POST /contracts/search`, geo `/region` (заглушка «Москва»), gRPC.

- **[Код]** Авторизация — `Authorization: Bearer <JWT пользователя-инициатора>`, тот же токен, что пришёл в FavoriteRoute; сервисные учётки не нужны. Для ручных проверок токен берётся из вкладки Network веб-клиента после входа тестовым пользователем.
- **[Спецификация]** Роли FavoriteRoute — EMPLOYEE, ADMIN, ENGINEER (любая из трёх); `data_master` — признак в JWT, а не роль (T01.2, раздел 2).
- **[Код]** Права на зависимости: geo пускает с любым действующим JWT. tariff и geo-zones проверяют права по таблицам `urls` и `roles` в БД среды (role_check_starter); в миграциях репозиториев назначений нет. Права contractor не проверялись.
- **[Спецификация]** Доступ к тарифу: FavoriteRoute доверяет отбору tariff-main и не воспроизводит правила подразделения и должности (T01.2, «Тарифный сервис»; T01.4, 1.2). Независимо проверяются `organizationId` тарифа и вхождение организации в `organizationIds` договора. Ответ 403 тарифного API — технический отказ 502 `DEPENDENCY_ERROR`, а не непригодность тарифа.
- **[Код]** Таймауты клиентов в конфигурации не заданы, SLA в репозиториях нет. В T01.4 это параметры среды `Tgeo`, `Tzones`, `Ttariff`, `Tcontractor` (7.1).

### 2.2. Геометрия `POST /route`

**[Спецификация]** T01.4, 4.2 и 5.3.

```json
{
  "coordinates": [
    { "latitude": 51.83, "longitude": 85.77 },
    { "latitude": 51.84, "longitude": 85.78 }
  ],
  "routeType": "CAR",
  "distanceUnit": "KILOMETERS",
  "transportServiceType": "EMPLOYEE_TRANSPORTATION"
}
```

- **[Код]** В 2ГИС уходят только latitude, longitude и type (GEO `GeoServiceClientImpl.processCoordinates:249-265`). `distanceUnit` в 2ГИС не передаётся вовсе, деление на 1000 задано в GEO `application.yml:167`. Ключ `unit` из тестов geo в DTO нет — не использовать.
- **[Код]** + **[Локально]** Ответ: `distance` — JSON number (Double), км, без округления; `time` — JSON integer, мс (`DurationMillisConverter`), разрешение источника — 1 с. Контрольный пример: 1234,5 м и 123 с → `"distance":1.2345,"time":123000`.
- Пустой штатный ответ: `{"waypoints":[],"distance":0.0,"time":0,"segments":[]}` (GEO `GeoControllerImpl.java:42-52`) → `ROUTE_NOT_BUILT`. Если у одной пары точек маршрута нет, а у других есть, geo отвечает 500 (GEO `RouteServiceImpl.java:79`) → `DEPENDENCY_ERROR`.
- **[Код]** Потери точности на стороне geo: время каждой пары усекается до целых секунд (`RouteServiceImpl.java:196`); при отсутствии общего времени — сумма манёвров с усечением на каждом шаге (`:209`); расстояние пар складывается в double (`fillRoute:151-152`). Пример: 1,5 м + 573 м → 0.5744999999999999 → `totalKm` 0.574. Адаптер это не исправляет.
- Метрики FavoriteRoute: `totalKm` — half-up до 3 знаков; `travelTime` и `totalTime` — half-up до 0,1 минуты; ожидание не прибавляется к `rawTravelMs` и учитывается в цене один раз (T01.4, 5.3). `rawTravelMs` — integer (int64).

### 2.3. Первая точка `GET /address`

**[Спецификация]** T01.4, 4.3 и 5.2; T01.2, «Geo и сверка первой точки».

- Только координаты: `GET /api/geo/address?latitude=51.83&longitude=85.77`. Без `radius`, `sort` и строки адреса.
- `200 []` → 400 `VALIDATION_ERROR` по `waypoints[0]` с сообщением «Не удалось подтвердить здание по координатам первой точки. Уточните точку на карте». 417 → 502 `DEPENDENCY_ERROR`, текст `message` не анализируется.
- **[Код]** Координатный маппинг geo не заполняет `type` и `point`; `city` при отсутствии становится `""` (GEO `AddressServiceImpl.getAddresses:102-186`, `AddressMapper:20`). Один объект 2ГИС может дать несколько адресных элементов; порядок и уникальность не гарантируются.
- Достаточно одного кандидата с совместимой веткой зон; первый элемент не является доказательством. Полный порядок исходов — алгоритм 5.2.

### 2.4. Ветка зон, пояс и контекст для чтения

**[Спецификация]** T01.4, 4.4 и 5.1; T01.2, «Геозоны и часовой пояс».

- Запрос `POST /api/geo-zones/searchBranch`, тело — непустые `region`, `district`, `city`, `street`, `house`. Перед запросом из копии адреса удаляются пустые и пробельные строки, включая `city=""`.
- **[Код]** Почему это обязательно: в geo-zones пустая строка совпадает с любым родителем (`parent.getName().contains("")`), а строка из пробелов совпадает только с многословными названиями (GZ `SearchRepositoryImpl.java:94-112`). `country` в поиске не участвует (GZ `GeoZoneCasesImpl.java:136-140`).
- **[Код]** Ответ — массив от подробной зоны к корню: `{"id","name","code","parent_id","time_zone"}` (GZ `GeoZoneDto.java:45, 49`, `GeoZoneProviderImpl.java:101-109`). У корня ключа `parent_id` нет; у зоны без пояса нет `time_zone`; ничего не найдено — `[]`.
- Пояс — первый непустой `time_zone` от подробной зоны к корню. IANA-имя (`Asia/Barnaul`) передаётся без изменения; смещение приводится к виду `±HH:MM` (`"+3"` → `"+03:00"`) до формирования `CalculationRequest` и `inputDigest`. Пусто или некорректно → 503 `CONTEXT_NOT_READY`.
- Контекст для GET карточки и списка (включая `checkTariff`) кэшируется в FavoriteRoute: ключ — канонический JSON нормализованного тела `searchBranch`, срок — параметр T01.2 «Свежесть территориального контекста». Создание, изменение, `/calculate`, продление и availability получают ветку заново. Подробно — T01.4, 5.1, абзац «Контекст для чтения».

### 2.5. Каталог `POST /search`

**[Спецификация]** T01.4, 4.5 и 5.4.

```
POST /api/tariffs/search?page=0&size=20&sort=humanReadableId,asc&sort=id,asc
{"contractType":"INCOME","organizationId":"<UUID организации>","serviceType":"EMPLOYEE_TRANSPORTATION"}
```

Второй обход — то же с `"contractType":"TRANSITIONAL"`.

- **[Код]** Если `contractType` не передать при заданном `transportType` (TAXI или GROUP_TRANSFER), tariff подставит `TRANSITIONAL` (TF `TariffControllerImpl.java:83-86`), и тарифы INCOME потеряются.
- **[Код]** Обе сортировки работают: `Pageable` передаётся в Spring Data без изменений (TF `TariffServiceImpl.java:468-474`).
- **[Документация]** Переопределения максимума страницы нет — действует умолчание Spring Data 2000; больший `size` обрезается молча. Обход опирается на `size`, `number` и `totalPages` из ответа.
- Ответ — `PageImpl`: `content`, `pageable`, `totalElements`, `totalPages`, `size`, `number`, `sort`, `first`, `last`, `numberOfElements`, `empty` (подтверждено снимком стенда).
- **[Код]** `ShortTariffDto`: `id`, `humanReadableId`, `transportType`, `serviceType`, `active`, `organizationId`, одиночный `regionId`; договорные `contractId`, `contractNumber`, `contractorId`, `contractorName` — только у TAXI и GROUP_TRANSFER. Класса, подразделения, `transportIds` и периода нет — по каждому тарифу читается карточка.
- **[Код]** Страница целиком падает: 404 — у договора есть `contractorId`, но перевозчика нет в копии tariff; 500 — у тарифа TAXI или GROUP_TRANSFER нет договора; 404 — инициатора нет в копии сотрудников tariff. Всё это техническая неполнота поиска (`partialSearch`), а не пустой каталог.

### 2.6. Карточки тарифа и договор

**[Спецификация]** T01.4, 4.6, 4.7 и 5.10 (полная таблица влияющих полей).

- **[Код]** Карточки `GET /taxi/{id}` и `GET /group_transfer/{id}`: деньги — целые копейки; `distanceIncluded`, `minKm` — км; `timeIncluded`, `minMin`, `freeWaitingTime` — минуты; даты — `yyyy-MM-dd`; пустые поля не выводятся (`non_null`).
- **[Код]** Зоны: у TAXI — `regionId`, массив из одного UUID (TF `EntityDTOMapper.java:36`); ключа `regionIds` нет. У GROUP_TRANSFER — `regionId` (массив из одного UUID) и `regionIds` (набор). **[Спецификация]** ВР-05: территория TAXI проверяется по `regionId`, GROUP_TRANSFER — по `regionIds`; массивы не объединяются.
- Подразделение: нет `department` или `null` → `tariffDepartmentId: null` (общий тариф); объект с корректным UUID в `id` → этот UUID; `{}`, неверный `id` или тип → 502 `DEPENDENCY_INVALID_RESPONSE`.
- **[Код]** Договор `GET /contracts/{contractId}` (`GetContractDTO`): `id`, `active`, `organizationIds`, `contractType` (INCOME, OUTCOME, TRANSITIONAL), `startDate`, `endDate`, `contractorId`, `contractorName`, `contractNumber`.
  - `startDate` и `endDate` в БД обязательны; конец включительный — tariff выключает договор, когда `endDate` раньше текущей даты (TF `ContractServiceImpl.java:47-57`).
  - Пустой `contractType` в данных → GET договора падает с 500 (`switch` в TF `ContractControllerImpl.java:341`) → `DEPENDENCY_ERROR`. Подходят только INCOME и TRANSITIONAL.
  - `contractorName` необязателен: подставляется, только если перевозчик есть в копии tariff (`ContractControllerImpl.java:329-330`). Нет ключа или `null` → в описании варианта `null`, ошибкой это не считается.

### 2.7. Расчёт цены `POST /calculate/enum/{transportType}/{tariffId}`

**[Спецификация]** T01.4, 4.9, 5.6–5.8. Запрос — `TripDto` (TF `model/TripDto.java`):

| Поле | Значение от FavoriteRoute |
|---|---|
| `distance` | `totalKm`, км |
| `time` | `travelTime × 60000`, целые мс движения |
| `waitingTime` | `0` |
| `intermediateWaitingTime` | `0` |
| `tripDate` | эпоха в мс выбранного момента: для TAXI — нейтральный момент, для GROUP_TRANSFER — момент операции в поясе первой точки |
| `timeZone` | канонический пояс первой точки; значение по умолчанию `"GMT+03"` не использовать |
| `trafficJamScore` | `0` |
| `startPoint` | адрес первой точки |
| `waypoints[]` | все точки; `waitTime` — целое число мс у каждой точки, у первой и последней `0` |
| `suburbTripData` | не заполнять — штатные нули |
| Опции (`vip`, `information`) | не передавать |

- **[Код]** Точный расчёт берёт `timeZone` из запроса без пересчёта по региону (TF `CalculateServiceImpl.java:84-99`, `getTripTime:327-339`); Java принимает и IANA-имя, и `+03:00`.
- **[Код]** Ответ `CalculatedDto`: `id` (это `tariffId`, сверяется с запрошенным), `cost` в копейках, округлён до 100 (`:715-729`). `outcomeTariffId` и `outcomeCost` не используются. Тариф не найден или неактивен — 404.

### 2.8. Ожидание, нейтральный момент и формулы

- **[Локально]** `waitTime` — целое число миллисекунд: `60000` = 1 минута в обеих формулах. Ключ обязателен: при его отсутствии — `NullPointerException`. Строка `"PT1M"` даст 0 минут, поэтому в DTO адаптера нужен `long`, а не `Duration`.
- **[Код]** TAXI: бесплатное ожидание вычитается только из ожидания первой точки, которое FavoriteRoute передаёт как 0; промежуточное ожидание оплачивается целиком по `waitCostPerMinIntermediate` (TF `CalculateServiceImpl.java:1016-1036`). GROUP_TRANSFER: `freeWaitingTime` вычитается на каждой точке, маршрут, у которого округлённые километры меньше `minKm` или минуты меньше `minMin`, стоит только `minRideCost` (`:872-874`); иначе `minRideCost` — базовая часть, к которой прибавляются километры и минуты сверх минимумов и ожидание (`:896-897`). Нулевые городские или загородные ставки заменяются `rideCostPerKm` и `rideCostPerMin` (`:887-891`).
- **[Код]** Временные полосы TAXI: пн–пт 07–10, 10–18, 18–22, 22–07; сб и вс — `coefDayOff` весь день. В будни 06:00–07:00 действует ночной коэффициент. **[Спецификация]** T01.4, 5.6: среди семи дат от `referenceDate` выбирается самый ранний момент с коэффициентом ровно 1.0 в окне маршрута и сроке договора; нет такого момента → `CALCULATION_MODE_UNAVAILABLE`.
- **[Код]** Пробки TAXI: множитель `1 + coefTraffic × (score − порог)` при `score ≥ порог` (`:1050-1070`). При `trafficJamScore = 0` надбавки нет, если порог ≥ 0 или `coefTraffic = 0`. Значение `traffic.threshold` на стенде — запросить.
- **[Код]** Загородные метрики TAXI взять негде; цена без них принимается как справочная. GROUP_TRANSFER считается загородным при любых двух разных значениях `city`, включая «город и пусто»; если `city` нет ни у одной точки — городские ставки (`:876`).

### 2.9. Автомобили перевозчика (CAR_CHOICE)

**[Спецификация]** T01.4, 4.8 и 5.5; ВР-04.

```
GET /api/contractors/{contractorId}/transport/?result=ID&result=BRAND&result=MODEL&result=STATE_NUMBER&page=0&size=<N>
```

- **[Код]** contractor не хранит машины: для каждого запроса делает `POST {url}/auth` с логином и паролем перевозчика, затем `GET {url}/transport/` (CTR `ContractorControllerServiceImpl.java:216-228, 290-309`) — две внешние операции на страницу. Без параметров интеграции — 409, неизвестный перевозчик — 404; оба исхода → `DEPENDENCY_ERROR`.
- **[Код]** contractor не передаёт `x-version` → dispatcher v1: машины перевозчика в эксплуатации (`in_exploitation = true`), занятые не исключаются (DSP `VehicleServiceImpl.java:354`). Мягкое удаление ставит `active = false` (DSP `Vehicle.java:43`) и в выдаче не учитывается. **[Спецификация]** ВР-04: это принятое ограничение — найденная машина из `transportIds` тарифа допустима, собственная проверка `active` не делается.
- **[Код]** Без `result` dispatcher уходит в ветку истории поездок за большой период — `result` передавать всегда, все четыре значения.
- **[Код]** Полнота: v1 читает весь список перевозчика одним SQL и режет страницу в памяти (DSP `VehicleServiceImpl.java:184-222`); максимума `size` нет. **[Спецификация]** `page=0` и элементов `content` меньше `size` → список получен целиком; ровно `size` — полнота не доказана. `pageData` и `sortData` проверяются, только если пришли. Отсутствие машины доказывается только полной выдачей; неполнота — технический исход, а не `TRANSPORT_INVALID`.
- **[Код]** Ответ v1 по исходнику transport-core: `content[]` с `id`, `brand`, `model`, `stateNumber`, `trips: []`, плюс `pageData` и `sortData`. Версия transport-core у dispatcher на стенде не установлена.
- **[Код]** Риск: цепочка технической учётки в коде не замыкается. Учётку регистрирует dispatcher (DSP `IntegrationClientServiceImpl.java:32-57`), `jti` её токена — случайный id (AUTH `AccountCasesImpl.java:300-331`, `AccessTokenProviderImpl.java:117`). Но v1 ищет `jti` только среди id перевозчика, диспетчера и `oauthId` (DSP `AuthCheckServiceImpl.java:22-33`) и таблицу этой учётки не читает; иначе — 403. **[Спецификация]** СТ-05: первым шагом проверить реальный вызов по каждому перевозчику Манжерока; без 200 и ожидаемых машин CAR_CHOICE не выпускается.

## 3. Контракт страницы `FavoriteRoutePage`

- **[Код]** Поля `numberOfElements`, `first`, `last`, `empty` и `sort` объявлены в типах фронтенда, но ни один экран их не читает. Пагинация на Ant Design 4.17.2 использует `content`, `totalElements` и номер страницы.
- **[Спецификация]** T01.2: `FavoriteRoutePage` содержит `content`, `number`, `size`, `totalElements`, `totalPages`. Во фронтенде не переиспользовать `createPagination` (он требует `pageable`); номер страницы брать из `number`, как в `GroupTransferRegistryTable.tsx:153`.

## 4. Что запросить

| Что | У кого |
|---|---|
| Адрес шлюза стенда; тестовые пользователи EMPLOYEE, ADMIN, ENGINEER в организации Манжерока, один — с признаком data master | DevOps, администратор стенда |
| Права этих ролей в `urls` и `roles` на пути tariff и geo-zones из п. 2.1; права на contractor `/{contractorId}/transport/` | Владельцы tariff, geo-zones, contractor |
| Брокеры Kafka стенда и продуктива, протокол безопасности, сертификаты, учётная запись и ACL для группы FavoriteRoute | Владельцы Kafka, DevOps |
| Фактические `retention.ms` и `cleanup.policy` топика `service.request`; процедура сброса смещений и ответственный | Владельцы Kafka, DevOps |
| Версия `spring-cloud-function-context` сервиса заявок, `spring.jackson.*` в Config Server, реальные сообщения TAXI и GROUP_TRANSFER | Команда сервиса заявок |
| `favoriteRouteId` и `favoriteRouteVersion` в `service.request` и в GET заявки по id (T04.1) | Команда сервиса заявок |
| Система каждого перевозчика Манжерока (`GET /api/contractors/{id}/`: `contractorType`, `serviceType`); реальный ответ `/transport/` с `result=` | Владелец интеграции, стенд |
| Снимается ли признак «в эксплуатации» при удалении машины | Владелец dispatcher |
| Версии geo, geo-zones, tariff, contractor, dispatcher и библиотек на стенде; артефакт `ru.sber.transport:collections` 4.6 | DevOps, манифесты сборок |
| SLA и таймауты tariff, geo, geo-zones, contractor; максимум `size` в tariff из Config Server; `traffic.threshold` | Владельцы сервисов |
| Значение срока «Свежесть территориального контекста»; механизм передачи `correlationId` | Архитектор, DevOps |
| По одному реальному ответу `taxi/{id}`, `group_transfer/{id}`, `contracts/{id}`; `searchBranch` на адрес Манжерока; `/address` для углового здания, нескольких адресов и точки вне здания | Стенд |

Тестовых данных в репозиториях нет: в миграциях geo-zones, tariff и contractor не найдено ни зон Республики Алтай, ни тестовых тарифов и автомобилей. Для стенда нужны:

- организация Манжерока с `digitId`; пользователи с ролями EMPLOYEE, ADMIN, ENGINEER и data master;
- зоны geo-zones для Манжерока с поясом (IANA-имя или смещение);
- тарифы TAXI — дневной и ночной, с нулевыми и ненулевыми загородными ставками;
- тарифы GROUP_TRANSFER с периодом, в том числе `TRANSFER_CAR_CHOICE` с машинами перевозчика Манжерока; тариф, у которого `regionIds` отличается от `regionId`;
- договоры INCOME и TRANSITIONAL — действующий, истёкший и будущий; два договора одного класса;
- общий тариф и тариф подразделения;
- точка без здания в зоне Манжерока (поле, вода);
- заявки в терминальных и нетерминальных статусах;
- сотрудник без подразделения: corporate отвечает на него 500, и этот случай не нужно разбирать как сбой FavoriteRoute.
