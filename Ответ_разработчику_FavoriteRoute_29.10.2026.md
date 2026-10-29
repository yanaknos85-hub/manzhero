# FavoriteRoute: ответ по Kafka и T01.4

29.10.2026 · к T01.2 от 28.10.2026 · основание — исходный код платформы; сервисы и стенд не запускались.

**Метки.** **[Код]** — подтверждено исходником. **[Локально]** — проверено сериализацией настоящих классов вне репозитория. **[Документация]** — по документации Spring Cloud Stream, проверить на стенде. **[Решение]** — предлагаемое решение для реализации. **[Запросить]** — получить у владельцев сервисов или на стенде.

**Сокращения путей.** PR — `passenger_request-main`; TF — `tariff-main/application/src/main/java`; KF — `kafka_functional-main`; GZ — `geo-zones-main`; GEO — `geo-main`; ENUMS — `as_sbertransport_libraries-main/enums/.../constants`.

## Коротко

- **Kafka.** Читать `service.request` своей группой; ключ — `id` заявки. Ссылки на маршрут в сообщении пока нет: `favoriteRouteId` и `favoriteRouteVersion` появятся после доработки сервиса заявок (T04.1).
- **Формат дат и длительностей** зависит от версии Spring Cloud Function у сервиса заявок, а её по коду не установить. Единицу доли секунды в `creationTime` сделать настройкой и зафиксировать по реальному сообщению.
- **Терминальных статусов четыре:** `TAXI_CANCELLED`, `TAXI_TRIP_FINISHED`, `GROUP_TRANSFER_CANCELLED`, `GROUP_TRANSFER_TRIP_FINISHED`.
- **Доставка не гарантирована:** продюсер отправляет асинхронно и ошибки только логирует. Восстановление — перечитыванием топика в пределах срока хранения.
- **Цена** — только `POST /calculate/enum/{transportType}/{tariffId}` по каждому тарифу. Каталожный `POST /calculate` не подходит.
- **Территорию и пояс** FavoriteRoute определяет сама через geo-zones и передаёт `timeZone` в расчёт.
- **Ожидание** передаётся в тариф целым числом миллисекунд у каждой точки, `waitingTime = 0`.
- **Открыто:** брокеры и SSL, версия зависимостей сервиса заявок, SLA и таймауты, права в БД, тестовые данные — список в разделе 4.

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
- **[Код]** Сверка первой точки: обратный геокодер `GET /address` ищет только здания, «не найдено» приходит неоднозначным 417; `/region` в geo — заглушка «Москва» (GEO `GeoControllerImpl.java:64-66`). Способа сверить точку вне зданий в коде нет. Это открытый вопрос к заказчику и архитектору, а не задача реализации.

### 2.6. Каталог, пригодность и пара тариф–автомобиль

- **[Код]** Каталожный `POST /calculate` для T01.4 не подходит. Договор на класс выбирается случайно с весом по сумме договора (`chooseContract`, TF `CalculateServiceImpl.java:1200`), а на класс возвращается не больше одного тарифа. Этого достаточно для расхождения с T01.2 («все допустимые варианты»). Кроме того, код отсекает тарифы подразделений, отличных от подразделения сотрудника (`:560`, `:618-623`); на стенде остаётся проверить только, как это проявится на данных.
- **[Код]** Полный перебор — `POST /search` (`Page`, по умолчанию 20 записей, сортировка `humanReadableId`). Фильтры `TariffSearchDTO`: `transportType`, `organizationId`, `contractId`, `contractNumber`, `contractorId`, `humanReadableId`, `regionId` (одно значение), `transportClass`, `active`, `isNightTariff`, `contractType`. `isNightTariff` фильтрует только при `true` (`TariffSearchSpecHelper.java:50-59`), дневные тарифы отбирать на стороне FavoriteRoute. Фильтров по дате и подразделению нет. Перебирать зоны ветки и все страницы; незавершённый перебор — `partialSearch` по T01.2.
- Пригодность по T01.2 проверяется по карточкам тарифа и договора: активность и организация тарифа; активность договора, вхождение организации и даты договора; для трансфера — период тарифа; класс и ночной признак.
- **[Код]** Автомобили привязаны только к тарифам класса `TRANSFER_CAR_CHOICE` (`transportIds`). Пара допустима, если `transport_ids` содержит автомобиль, `region_ids` пересекается с веткой, дата поездки в периоде тарифа и `active = true` (TF `GroupTransferTariffRepository.java:31-56`).
- **[Код]** Цена трансфера от автомобиля не зависит (`CalculateServiceImpl.java:870-897`). Точная пара: `calculate/enum/GROUP_TRANSFER/{tariffId}` и проверка, что `transportIds` тарифа содержит `transportId`. Эндпоинты `/calculate/transport…` сами подбирают один тариф на пару «автомобиль + зона» с приоритетом договора дохода (`:150-245`) и точную пару не гарантируют. Данные автомобиля — из contractor.

### 2.7. Пробки и загородные ставки

- **[Локально]** TAXI: множитель `1 + coefTraffic × (score − порог)` при `score ≥ порог` (TF `CalculateServiceImpl.java:1054-1056`); порог — `traffic.threshold`, по умолчанию 7. При `trafficJamScore = 0` надбавки нет, если порог ≥ 0 или `coefTraffic = 0`.
- **[Код]** Загородные метрики взять негде: geo не делит маршрут на город и пригород, geo-zones не хранит геометрию. Формула TAXI требует их, только если у тарифа больше нуля хотя бы одна из шести ставок `SuburbTariffParams`: `costPerKmSuburb`, `costPerMinSuburb`, `suburbServiceCostPerKm`, `suburbServiceCostPerMin`, `costPerKmInterRegion`, `costPerMinInterRegion` (все по умолчанию 0). Для такого тарифа — `CALCULATION_MODE_UNAVAILABLE` по T01.2.
- **[Код]** GROUP_TRANSFER: поездка загородная, если среди точек больше одного различного `city` (`:876`). Если эффективные городские и загородные ставки совпадают, отсутствие `city` на цену не влияет. Если ставки различаются и у какой-либо точки нет `city` — `CALCULATION_MODE_UNAVAILABLE`.

**[Запросить]** значения `traffic.threshold` и `coefTraffic` тарифов на стенде; у заказчика — есть ли у тарифов TAXI организации ненулевые загородные ставки.

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
| `traffic.threshold`, `coefTraffic`, ненулевые загородные ставки у тарифов TAXI | Владелец tariff; заказчик |
| Способ сверки первой точки вне зданий | Заказчик и архитектор |
| Тестовые данные (список ниже) | Организатор стенда |

Тестовых данных в репозиториях нет: в миграциях geo-zones, tariff и contractor не найдено ни зон Республики Алтай, ни тестовых тарифов и автомобилей. Для стенда нужны:

- организация с `digitId`; пользователи с ролями EMPLOYEE, ADMIN, ENGINEER и data master;
- зоны geo-zones для Манжерока с поясом (`Asia/Barnaul` или фактическим);
- тарифы TAXI — дневной и ночной; GROUP_TRANSFER с периодом, в том числе `TRANSFER_CAR_CHOICE` с автомобилями;
- договоры дохода — действующий, истёкший и будущий;
- тариф с ненулевыми и тариф с нулевыми загородными ставками;
- заявки в терминальных и нетерминальных статусах;
- сотрудник без подразделения: corporate отвечает на него 500, и этот случай не нужно разбирать как сбой FavoriteRoute.
