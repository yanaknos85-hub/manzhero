# FavoriteRoute: ответ по Kafka и T01.4

29.10.2026 · к T01.2 от 29.10.2026 · основание — исходный код платформы; сервисы и стенд не запускались.

**Метки.** **[Код]** — подтверждено исходником. **[Локально]** — проверено вне репозитория на настоящих классах или дословной копии алгоритма. **[Документация]** — по документации Spring Cloud Stream или Spring Data, проверить на стенде. **[Решение]** — предлагаемое решение для реализации. **[Запросить]** — получить у владельцев сервисов или на стенде.

**Сокращения путей.** PR — `passenger_request-main`; TF — `tariff-main/application/src/main/java`; KF — `kafka_functional-main`; GZ — `geo-zones-main`; GEO — `geo-main`; ENUMS — `as_sbertransport_libraries-main/enums/.../constants`.

## Коротко

- **Kafka.** Читать `service.request` своей группой; ключ — `id` заявки. Ссылки на маршрут в сообщении пока нет: `favoriteRouteId` и `favoriteRouteVersion` появятся после доработки сервиса заявок (T04.1).
- **Формат дат и длительностей** зависит от версии Spring Cloud Function у сервиса заявок, а её по коду не установить. Единицу доли секунды в `creationTime` сделать настройкой и зафиксировать по реальному сообщению.
- **Терминальных статусов четыре:** `TAXI_CANCELLED`, `TAXI_TRIP_FINISHED`, `GROUP_TRANSFER_CANCELLED`, `GROUP_TRANSFER_TRIP_FINISHED`.
- **Доставка не гарантирована:** продюсер отправляет асинхронно и ошибки только логирует. Восстановление — перечитыванием топика в пределах срока хранения.
- **Цена** — только `POST /calculate/enum/{transportType}/{tariffId}` по каждому тарифу. Каталожный `POST /calculate` не подходит ни для цены, ни для проверки допуска.
- **Каталог тарифов** — `POST /search` с явными `contractType` (INCOME и TRANSITIONAL отдельными запросами) и `organizationId`, затем карточка каждого тарифа.
- **Доступ пользователя к конкретному тарифу** T01.2 требует подтверждать, но в существующих API готового способа нет. Решение готовится в T01.2 и T01.4 (п. 2.2), до него эту проверку не реализовывать.
- **Территорию и пояс** FavoriteRoute определяет сама через geo-zones и передаёт `timeZone` в расчёт.
- **Ожидание** передаётся в тариф целым числом миллисекунд у каждой точки, `waitingTime = 0`.
- **Открыто:** брокеры и SSL, версия зависимостей сервиса заявок, SLA и таймауты, права в БД, ответ geo на точку без здания, тестовые данные — список в разделе 4.

## 1. Kafka `service.request`

### 1.1. Топик и подключение

| Параметр | Значение | Основание |
|---|---|---|
| Топик | `service.request`; у сервиса заявок — биндинг продюсера `requestOutput-out-0` | PR `kafka.yml:225-226` |
| Брокеры | `spring.cloud.stream.kafka.binder.brokers: ${KAFKA_BROKERS}` (у сервиса заявок — внутри `binders.kafka.environment`); в репозитории только заглушка сборки | PR `kafka.yml:16-17` |
| Ключ | `id` заявки. Сериализатор ключа — `JsonSerializer`, поэтому UUID приходит JSON-строкой в кавычках. Потребители платформы читают ключ через `JsonDeserializer`; при `StringDeserializer` кавычки нужно снять | KF `OutputBridge.java:148`; PR `kafka.yml:18-21` |
| Заголовки | `contentType=application/json`, `transportType`, `messageId`, `fraudHandled`, `principal`. `messageId` — новый UUID при каждой отправке: повтор того же состояния придёт с другим значением | KF `OutputBridge.java:74, 149`; PR `BaseRequestSender.java:34-38` |
| Свойства топика | По умолчанию `compression.type=gzip`, `cleanup.policy=compact,delete`, `retention.ms=604800000` (7 дней); переопределяются окружением. `auto-alter-topics=true`: сервис заявок может менять свойства топика | PR `kafka.yml:22-29` |
| Безопасность | В сервисе заявок SSL не настроен. fraud-monitoring читает через отдельный биндер `kafka-ssl` с группой `<имя>-ssl`; параметров этого биндера в репозиториях нет | fraud-monitoring `application-kafka-ssl.yml` |

Образец потребителя — reports: `destination: service.request`, `group: ${spring.application.name}` (`passenger_reports-main/.../application.yml:37-39`). Для FavoriteRoute — своя стабильная группа, отдельная от групп других сервисов.

**[Запросить]** адреса брокеров стенда и продуктива; протокол безопасности, сертификаты, учётную запись и ACL на чтение для группы FavoriteRoute; фактические `retention.ms` и `cleanup.policy` топика.

### 1.2. Схема сообщения

Класс `RequestMessage` (PR `messaging/src/main/java/ru/sber/transport/request/messaging/RequestMessage.java`). Поля, которые использует FavoriteRoute:

| Поле | Строка | Тип и смысл |
|---|---|---|
| `id` | 33 | UUID заявки, совпадает с ключом |
| `organizationId` | 63 | UUID организации |
| `creationTime` | 73 | `LocalDateTime` в UTC, массив без смещения (п. 1.3) |
| `transportType` | 88 | `"TAXI"` или `"GROUP_TRANSFER"` |
| `status` | 178 | имя статуса `TripRequestStatus` (п. 1.4) |
| `statusCode` | 188 | код подстатуса 101–207; на классификацию не влияет |
| `deleted` | 228 | `boolean`; по T01.2 условием сброса ссылки не является |

Остальные поля: `timeZone` (78); `waypoints[]` (98) — `id`, `address`, `waitTime`, `checkinAutomatic`, `checkinManual`, `absenceReason`, `orderingIndex`; `tariffId` и `outcomeTariffId` (108, 113); `desiredDate` (143); `expected` (203) — `cost` и `outcomeCost` в копейках, `distance` в км, `time`; `contractorId` (244).

**[Код]** Полей `favoriteRouteId` и `favoriteRouteVersion` нет. Их добавление в сервис заявок — доработка T04.1; до неё потребитель не получит ссылку на маршрут.

**[Решение]** Наличие ссылки определяется по значениям, а не по ключам (уточнение вносится в T01.2):

- оба значения отсутствуют — ссылки нет; допустимы отсутствие ключей, два `null` и сочетание отсутствующего ключа с `null`;
- задан ровно один реквизит — некорректная частичная пара, прежняя связь не удаляется;
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
- По T01.2 неизвестное имя сохраняется и считается блокирующим; обратный переход из терминального статуса применяется.

### 1.5. Смещения, повторы и восстановление

Что гарантирует источник:

- **[Код]** Продюсер отправляет асинхронно пулом из 10 потоков, ошибка отправки только пишется в лог (KF `OutputBridge.java:39, 132`). Сообщение может потеряться, прийти не по порядку (в том числе по одной заявке) или прийти до фиксации транзакции заявки. Формальной гарантии доставки нет.
- **[Код]** В сообщении нет версии события и времени изменения; `changeDate` в него не входит.
- **[Код]** Повторной отправки нет. `/synchronize` сервиса заявок для восстановления не подходит: он обходит только заявки такси и не охватывает GROUP_TRANSFER (PR `RequestRepository.java:172-192`).

Настройки потребителя FavoriteRoute:

- **[Документация]** Группа с явным именем при первом подключении начинает с самого раннего смещения: Spring Cloud Stream ставит `startOffset=earliest`. При `compact,delete` и 7 днях новая группа получит последние состояния заявок примерно за 7 дней. `latest` не задавать: T01.2 запрещает автоматический переход на `latest`. Проверить на версии биндера стенда.
- **[Решение]** DLQ (`enableDlq: true`) — только для некорректного payload: разбор, схема, `creationTime`. По документации биндера имя DLQ по умолчанию — `error.service.request.<группа>`.
- **[Решение]** Ошибка БД FavoriteRoute не считается успешной обработкой (T01.2): нужен повтор с паузой без фиксации смещения. `autoCommitOnError: true`, как у reports, и `maxAttempts: 1` с `defaultRetryable: false`, как у fraud-monitoring, для FavoriteRoute не подходят.
- **[Решение]** Оповещения: лаг группы, остановка потребителя, сброс смещения клиентом Kafka при выходе за срок хранения.

**[Запросить]** процедуру сброса смещений группы на стенде и в продуктиве и ответственного; фактический срок хранения и политику очистки.

## 2. T01.4 — расчёт, тарифы, территория

### 2.1. Адреса

**[Код]** Вызывать можно двумя способами:

- через шлюз: `/api/<сервис>/**` → `lb://<сервис>`, префикс отрезается (`gateway-main/.../application.yml`, маршруты `tariff`, `geo`, `geo-zones`, `contractors`);
- напрямую, как сервис заявок: Feign с адресом из окружения (`FEIGN_URL_TARIFF`, `FEIGN_URL_GEO`, `FEIGN_URL_CORPORATE`) и внутренним путём без префикса (PR `application.yml:184-188`); gRPC — через discovery, например `discovery:///grpc-geo-zones` (PR `application.yml:1-25`).

| Назначение | Внутренний путь | Через шлюз | Основание |
|---|---|---|---|
| Цена по конкретному тарифу | tariff `POST /calculate/enum/{transportType}/{tariffId}`; `transportType` — `TAXI` или `GROUP_TRANSFER` | `/api/tariffs/calculate/enum/…` | TF `CalculatingController.java:71`; `CalculateServiceImpl.java:84-98` |
| Каталог тарифов | tariff `POST /search` | `/api/tariffs/search` | TF `TariffController.java:100-110` |
| Карточка тарифа | tariff `GET /taxi/{id}`, `GET /group_transfer/{id}`; общий `GET /{transportTypeId}/{tariffId}` | `/api/tariffs/…` | TF `TaxiTariffController.java:64`, `GroupTransferTariffController.java:64`, `TariffController.java:48-52` |
| Договор | tariff `GET /contracts/{contractId}`, `POST /contracts/search` | `/api/tariffs/contracts/…` | TF `ContractController.java:89, 124` |
| Маршрут и обратный геокодер | geo `POST /route`, `GET /address` | `/api/geo/…` | GEO `GeoController.java` |
| Ветка зон | geo-zones `POST /searchBranch`; gRPC `GeoZonesService.RegionBranch` | `/api/geo-zones/searchBranch` | GZ `GeoZoneController.java:69-73`, `GeoZonesService.proto:11` |
| Автомобили перевозчика | contractor `GET /{contractorId}/transport/` | `/api/contractors/…` | TF `ContractorClient.java:17-37` |

UUID видов транспорта для `{transportTypeId}`: TAXI — `7f18ce71-99a7-47b5-b285-335058c6715c`, GROUP_TRANSFER — `ab6efcb9-dce2-4545-82f7-f75e51e670c3` (ENUMS `TransportTypeEnum.java:25, 48`).

### 2.2. Авторизация и SLA

- **[Код]** HTTP — заголовок `Authorization` с JWT пользователя. Сервис заявок так и передаёт токен в каждом вызове (PR `client/TariffService.java:31`, `GeoResolver.java:22, 26`). Вызовы T01.4 выполняются в запросе пользователя, поэтому сервисный токен для них не нужен.
- **[Код]** Права tariff, geo-zones (HTTP) и сервиса заявок проверяет role_check_starter по таблицам `urls` и `roles` в БД среды; путь нормализуется (шаблон `_%`, добавление `/`). geo пускает с любым действующим JWT. Общая карточка `GET /{transportTypeId}/{tariffId}` помечена `@NoAuthorize`.
- **[Код]** gRPC geo-zones: перехватчиков авторизации нет, канал `plaintext`; так ветку зон получает тариф. Можно ли FavoriteRoute ходить так же, решает архитектор.
- **[Код]** Таймауты Feign и gRPC в конфигурации не заданы, SLA в репозиториях нет.

**Доступ пользователя к конкретному тарифу — открыт.** T01.2 требует одинаково подтверждать доступ для поиска, прямого `tariffId`, продления и availability, но способ выполнить это существующими API не установлен:

| Вызов tariff | Организация | Подразделение | Класс TAXI по должности |
|---|---|---|---|
| `POST /calculate` (каталог) | из тела запроса | тарифы других подразделений исключаются | только разрешённые должностью |
| `POST /search` | организация сотрудника, кроме data master | нет | нет |
| `calculate/enum/{type}/{tariffId}` | нет | нет | нет, только `active` |
| Карточки тарифа | нет | нет | нет |

- **[Код]** Каталог отсекает тарифы и по причинам, не связанным с допуском (п. 2.6), поэтому как источник допустимых идентификаторов не используется.
- **[Код]** Готового API классов по должности, совпадающего с фильтром tariff, нет. `GET /self/allowed-classes` в corporate ищет должность по id сотрудника и при нормальных данных отвечает 404. HTTP- и gRPC-методы corporate по сотруднику без должности возвращают `[]`, а tariff в этом случае разрешает ECONOMY. Реализации `GET /api/organizations/self` в доступном коде corporate нет.
- **[Код]** Отдельного ответа 403 «тариф недоступен» в tariff-main нет. 403 от проверки прав на HTTP-метод не означает недоступность конкретного тарифа.

До решения в T01.2 и T01.4 собственные правила подразделения и должности не реализовывать.

**[Запросить]** права в `urls` и `roles` на пути из п. 2.1 для ролей EMPLOYEE, ADMIN и ENGINEER; SLA, лимиты и таймауты tariff, geo, geo-zones и contractor.

### 2.3. Запрос и ответ расчёта

**[Код]** Запрос — `TripDto` (TF `ru/sber/transport/tariff/model/TripDto.java`):

| Поле | Единица и правило |
|---|---|
| `distance` | км |
| `time` | мс, целое число (`MillisDurationConverter`) |
| `waitingTime` | мс; для FavoriteRoute — `0` (п. 2.4) |
| `intermediateWaitingTime` | мс; в формулах TAXI и GROUP_TRANSFER не используется |
| `tripDate` | эпоха в мс; по умолчанию «сейчас + 5 мин» |
| `timeZone` | по умолчанию `"GMT+03"` (`:77`); передавать всегда — пояс первой точки (п. 2.5) |
| `trafficJamScore` | по умолчанию 0 (`:82`) |
| `suburbTripData` | по умолчанию нули (`:90`) |
| `startPoint` | адрес первой точки; передавать как фронт: `startPoint = waypoints[0]` |
| `waypoints[]` | `WaypointDTO`; `waitTime` — целое число мс у каждой точки (п. 2.4) |
| `vip`, `information` | `animal`, `childSeat` и другие опции |

`startPoint` в DTO без `@NotNull`, это не правило валидации API. Адресные поля нужны каталожным путям (`/calculate`, `/calculate/transport…`): там `country`, `region` и `city` уходят в gRPC geo-zones без проверки, `null` даёт `NullPointerException` и 417 (TF `RegionDataResolverImpl.java:54-56`, `CalculateServiceImpl.java:1261-1273`). Значения, по которым зона не нашлась, дают пустую ветку и пустой список тарифов. Расчёт по конкретному тарифу ветку не строит.

**[Код]** Ответ — `CalculatedDto` (`CalculatedDto.java:65-127`): `id` (это `tariffId`), `outcomeTariffId`, `cost` и `outcomeCost` в копейках (`cost` округлён до 100 коп., `CalculateServiceImpl.java:727`), `priceDetails`, класс, `active`, `limitAvailable`. Номера тарифа, договора и подразделения в ответе нет. Их берут из карточки тарифа: `humanReadableId`, `contractId`, `contractNumber`, `department`, `active`, `transportIds`, `regionIds`, `tariffStartDate` и `tariffEndDate`, `freeWaitingTime`, `isNightTariff`.

**[Код]** Ошибки:

| Ситуация | Ответ | Основание |
|---|---|---|
| Тариф не найден или неактивен | 404, `ExceptionBody`: `timestamp`, `path`, `entity`, `message` | TF `CalculateServiceImpl.java:85-87` |
| Не определены регион или пояс | 417 | TF `RegionResolvingFailedException.java:9` |
| Ошибка валидации запроса | 400 | — |
| Прочие сбои | 500, тело-`Map`: `timestamp`, `status`, `message: "Request failed. Please contact support"`, `error`, `path` | `transport_core-main/.../RequestExceptionHandler.java:283-291, 332-350` |
| geo: адрес или маршрут не получены | 417 без машинного кода | GEO `ControllerExceptionHandler.java:37-50`, `GeoServiceClientImpl.java:231-246` |

По T01.2 417 без машинного признака, ошибки разбора и маппинга дают 502. Отсутствие территории — только штатный пустой ответ `searchBranch` (`200 []`); ошибка маппинга его не доказывает.

### 2.4. Ожидание в расчёте

**[Код]** Формулы TAXI и GROUP_TRANSFER берут ожидание из разных полей:

- TAXI: `waitingTime` (первая точка) минус `freeWaitingTime` по ставке `waitCostPerMin` плюс сумма `waitTime` всех точек по `waitCostPerMinIntermediate`, без лимита (TF `CalculateServiceImpl.java:1026-1032`);
- GROUP_TRANSFER: только `waypoints[].waitTime`, лимит вычитается на каждой точке, `waitingTime` не используется (`:877-882`).

По T01.1 ожидание есть только у промежуточных точек. FavoriteRoute передаёт: `waitingTime = 0`; у первой и последней точки `waitTime = 0`; у промежуточных — миллисекунды.

**[Локально]** `waitTime` — целое число миллисекунд: `60000` = 1 минута в обеих формулах. Ключ обязателен: при его отсутствии — `NullPointerException`. Строка `"PT1M"` будет прочитана как 60 секунд и после деления на 1000 даст 0 минут. Если в клиенте поле объявлено как `Duration`, ObjectMapper Spring Boot запишет его именно такой строкой, поэтому в DTO адаптера нужен `long`.

### 2.5. Территория и часовой пояс

- **[Код]** Ветку зон даёт geo-zones по текстовым полям адреса. Вход: `country`, `region`, `city`, `street`, `house`, `district`. Ответ: список `{id, name, code, parentId, timeZone}` от подробной зоны к корню (GZ `WaypointDto.java`, `GeoZoneDto.java`). Поиск полнотекстовый по названиям; координаты и геометрия не используются (GZ `SearchRepositoryImpl.java:32-85`, `GeoZone.java:40-76`).
- **[Код]** Пояс — первый непустой `timeZone` ветки; если его нет, тариф отвечает 417 (TF `CalculateServiceImpl.java:290-294`). Время тариф пересчитывает по правилу «местное время клиента считается местным временем региона» (`:304-309`). Поэтому `timeZone` в запросе должен быть поясом первой точки.
- **[Код]** Расчёт по конкретному тарифу регион не определяет и берёт `timeZone` из запроса как есть. Ветку и пояс T01.4 получает сама через `searchBranch` или `RegionBranch`.
- **[Код]** Ночь в тарифе — местный час ≥ 22 или < 6 (`:318-325`); это совпадает с окнами T01.2 `[22:00, 06:00)`.
- **[Код]** Первая точка должна быть у здания (T01.2). Обратный геокодер — `GET /address?latitude=&longitude=`: geo вызывает 2ГИС `/items/geocode` с `type=building` (GEO `application.yml:39-72`, `GeoServiceClientImpl.java:61-69, 116-139`). Передавать только координаты: без координат и строки адреса geo сразу отвечает `200 []`, а при координатах вместе со строкой складывает результаты двух поисков.

| Ответ 2ГИС | Ответ geo | Для FavoriteRoute |
|---|---|---|
| Успех без зданий | `200 []` | кандидат на подтверждённое отсутствие здания, контракт дописывается в T01.2 |
| `meta.code=404` | 417 (`GeoServiceClientImpl.java:236-241`) | технический отказ, 502 |
| Ошибка 2ГИС | 417 | технический отказ, 502 |

- Причину 417 по тексту не определять. Как 2ГИС сообщает «ничего не найдено» — кодом 404 или пустым списком, — проверяется на стенде. `200 []` от geo-zones `searchBranch` — поиск зон по названиям; об отсутствии здания он не говорит. `/region` в geo — заглушка «Москва» (GEO `GeoControllerImpl.java:64-66`).

### 2.6. Каталог, пригодность и пара тариф–автомобиль

- **[Код]** Каталожный `POST /calculate` для T01.4 не подходит. На класс остаётся не больше одного тарифа, а отсекаются тарифы и по причинам, не связанным с допуском:
  - в классе остаются тарифы самой подробной зоны, а тариф подразделения вытесняет общие (TF `CalculateServiceImpl.java:546-600`);
  - при наличии INCOME-тарифа вида транспорта отбрасываются все TRANSITIONAL этого вида (`:907-912`, `:955-960`);
  - трансферы фильтруются по опциям запроса (`:947-951`);
  - договор выбирается случайно с весом по сумме (`:1200-1240`); неактивный договор и договор с суммой 0 не выбираются.
- **[Локально]** В выборе договора есть ошибка (`:1234`): при трёх и более договорах класса третий не выбирается никогда, а в части запусков класс пропадает из ответа целиком. Для трёх договоров по 100 это треть запусков.
- **[Код]** Каталог падает целиком для сотрудника без подразделения (`NullPointerException` при проверке лимитов, `:746`) и для INCOME-тарифа TAXI без ровно одного подходящего расходного (`:452`).
- **[Код]** Полный перебор — `POST /search`. Фильтр — тело `TariffSearchDTO`, страницы — query `page`, `size`, `sort` (по умолчанию 20, `humanReadableId`). Предел размера страницы в конфигурации не задан; **[Документация]** по умолчанию Spring Data — 2000.
  - `contractType` — одно значение на запрос. Клиентские тарифы — INCOME и TRANSITIONAL, двумя запросами, без собственного приоритета INCOME. Если `contractType` не передать, для TAXI и GROUP_TRANSFER подставится `TRANSITIONAL` (`TariffControllerImpl.java:83-86`), и тарифы INCOME потеряются.
  - `organizationId` передавать всегда: фильтр по нему действует и для data master, а data master без него видит все организации. Обычному пользователю дополнительно ограничивается организация его сотрудника. Инициатор должен быть в копии сотрудников tariff, иначе 404.
  - Передавать `serviceType = EMPLOYEE_TRANSPORTATION`, как каталог.
  - Остальные фильтры: `transportType`, `contractId`, `contractNumber`, `contractorId`, `humanReadableId`, `regionId` (одно значение), `transportClass`, `active`, `isNightTariff`. `isNightTariff` фильтрует только при `true` (`TariffSearchSpecHelper.java:50-59`), дневные тарифы отбирать на стороне FavoriteRoute. Фильтров по дате и подразделению нет. Перебирать зоны ветки и все страницы; технически незавершённый перебор — `partialSearch` по T01.2.
  - В ответе `ShortTariffDto` нет UUID подразделения, класса, `transportIds` и периода — по каждому тарифу читать карточку.
- **[Код]** Подразделение берётся из карточек `GET /taxi/{id}` и `GET /group_transfer/{id}`: `department` = `{id, humanReadableId, code}` (`NewTaxiTariffDTO.java:37`, `NewGroupTransferTariffDTO.java:45`). У общего тарифа ключа нет: tariff не выводит пустые поля. Маппинг адаптера:
  - нет `department` или `department: null` — `tariffDepartmentId: null`;
  - объект с корректным UUID в `id` — этот UUID;
  - объект без корректного `id`, включая `{}`, — некорректный ответ, 502.

  Общая карточка `GET /{transportTypeId}/{tariffId}` подразделения не содержит.
- Пригодность по T01.2 проверяется по карточкам тарифа и договора: активность и организация тарифа; активность договора, вхождение организации и даты договора; для трансфера — период тарифа; класс и ночной признак.
- **[Код]** Автомобили привязаны только к тарифам класса `TRANSFER_CAR_CHOICE` (`transportIds`). Пара допустима, если `transport_ids` содержит автомобиль, `region_ids` пересекается с веткой, дата поездки в периоде тарифа и `active = true` (TF `GroupTransferTariffRepository.java:31-56`).
- **[Код]** Цена трансфера от автомобиля не зависит (`CalculateServiceImpl.java:870-897`). Точная пара: `calculate/enum/GROUP_TRANSFER/{tariffId}` и проверка, что `transportIds` тарифа содержит `transportId`. Эндпоинты `/calculate/transport…` сами подбирают один тариф на пару «автомобиль + зона» с приоритетом договора дохода (`:150-245`) и точную пару не гарантируют. Данные автомобиля — из contractor.

### 2.7. Пробки и загородные ставки

- **[Локально]** TAXI: множитель `1 + coefTraffic × (score − порог)` при `score ≥ порог` (TF `CalculateServiceImpl.java:1054-1056`); порог — `traffic.threshold`, по умолчанию 7. При `trafficJamScore = 0` надбавки нет, если порог ≥ 0 или `coefTraffic = 0`.
- **[Код]** Загородные метрики взять негде: geo не делит маршрут на город и пригород, geo-zones не хранит геометрию. По T01.2 от 29.10 цена TAXI без них принимается как справочная и не даёт `CALCULATION_MODE_UNAVAILABLE`, в том числе при ненулевых загородных ставках. При нулевых `suburbTripData` загородная часть просто равна 0 (`:1038-1047`). Цена передаётся как вернул tariff-main, недостающая надбавка не досчитывается.
- **[Код]** GROUP_TRANSFER: поездка загородная, если среди точек больше одного различного `city` (`:876`). Сочетание известного города и точки без `city` даёт загородные ставки; если `city` нет ни у одной точки, считаются городские. По T01.2 цена принимается в обоих случаях и не блокирует сохранение.

**[Запросить]** значения `traffic.threshold` и `coefTraffic` тарифов на стенде.

## 3. Контракт страницы `FavoriteRoutePage`

- **[Код]** Поля `numberOfElements`, `first`, `last`, `empty` и `sort` объявлены в типах фронтенда, но ни один экран их не читает. Пагинация на Ant Design 4.17.2 использует `content`, `totalElements` и номер страницы.
- **[Решение]** Оставить `content`, `number`, `size`, `totalElements`, `totalPages`. Во фронтенде не переиспользовать `createPagination` (он требует `pageable`); номер страницы брать из `number`, как в `GroupTransferRegistryTable.tsx:153`.

## 4. Что запросить

| Что | У кого |
|---|---|
| Брокеры стенда и продуктива, протокол безопасности, сертификаты, учётная запись и ACL для группы FavoriteRoute | Владельцы Kafka, DevOps |
| Фактические `retention.ms` и `cleanup.policy` топика `service.request`; процедура сброса смещений и ответственный | Владельцы Kafka, DevOps |
| Версия `spring-cloud-function-context` сервиса заявок, `spring.jackson.*` в Config Server, реальные сообщения TAXI и GROUP_TRANSFER | Команда сервиса заявок |
| `favoriteRouteId` и `favoriteRouteVersion` в `service.request` и в GET заявки по id (T04.1) | Команда сервиса заявок |
| Права в `urls` и `roles` для EMPLOYEE, ADMIN и ENGINEER на пути tariff и geo-zones | Владельцы tariff и geo-zones |
| Допустимость прямого gRPC к geo-zones без авторизации | Архитектор |
| SLA, лимиты и таймауты tariff, geo, geo-zones, contractor | Владельцы сервисов |
| `traffic.threshold`, `coefTraffic` | Владелец tariff |
| Способ подтверждения доступа пользователя к конкретному тарифу | Аналитик и архитектор (T01.2, T01.4) |
| Ответ geo `GET /address` на координаты без здания: 417 или `200 []` | Стенд |
| Реализация `GET /api/organizations/self`; исправление `GET /self/allowed-classes` | Владельцы corporate |
| Тестовые данные (список ниже) | Организатор стенда |

Тестовых данных в репозиториях нет: в миграциях geo-zones, tariff и contractor не найдено ни зон Республики Алтай, ни тестовых тарифов и автомобилей. Для стенда нужны:

- организация с `digitId`; пользователи с ролями EMPLOYEE, ADMIN, ENGINEER и data master;
- зоны geo-zones для Манжерока с поясом (`Asia/Barnaul` или фактическим);
- тарифы TAXI — дневной и ночной; GROUP_TRANSFER с периодом, в том числе `TRANSFER_CAR_CHOICE` с автомобилями;
- договоры дохода — действующий, истёкший и будущий;
- тариф с ненулевыми и тариф с нулевыми загородными ставками;
- в одном классе — тарифы с двумя и с тремя договорами дохода; тариф подразделения рядом с общим тарифом того же класса;
- общий тариф без подразделения и тариф подразделения, которого нет в копии tariff;
- точка без здания в зоне Манжерока (поле, вода);
- заявки в терминальных и нетерминальных статусах;
- сотрудник без подразделения: corporate отвечает на него 500, и этот случай не нужно разбирать как сбой FavoriteRoute.
