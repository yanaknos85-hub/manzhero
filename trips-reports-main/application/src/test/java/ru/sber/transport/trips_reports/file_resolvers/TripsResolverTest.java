package ru.sber.transport.trips_reports.file_resolvers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.instancio.Instancio;
import org.instancio.Select;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.service.ConsentFunction;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.file_works.services.UploadStates;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.trip_reports.message.TripReportMessage;
import ru.sber.transport.trips_reports.messaging.mapper.MessageMapper;
import ru.sber.transport.trips_reports.providers.trips.TripProvider;
import ru.sber.transport.trips_reports.web.dto.TripStatus;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings({"java:S5961", "SpringJavaInjectionPointsAutowiringInspection"})
@SpringBootTest
@Slf4j
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@DisplayName("Проверка экспорта отчетов")
@Transactional
@MockitoBean(types = Key.class)
@UnitTest
@IsolatedTest
@Feature("app_platform_trips_reports")
@ActiveProfiles({"test", "kafka", "kafka-ssl"})
public class TripsResolverTest extends KafkaTest {

    public static final String TEMP_DIR = "target/test/files";

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UploadStates uploadStates;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthorizationManager<?> manager;

    @Autowired
    private DSLContext dslContext;

    @MockitoBean
    private ConsentFunction function;

    @Autowired
    private TripProvider tripProvider;

    @Autowired
    private MessageMapper messageMapper;

    @BeforeEach
    void setup() {
        when(function.apply(any())).thenReturn(true);
        AuthorizeUtils.authorize(manager, "ROLE_GUEST");
    }

    @SneakyThrows
    @Test
    @DisplayName("Экспорт")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void exportData() {
        var message = Instancio.of(TripReportMessage.class)
                .set(Select.field(TripReportMessage::getStatus), TripStatus.ORDER_FINISHED.getRusName())
                .set(Select.field(TripReportMessage::getTimeZone), "GMT+03")
                .create();
        var record = messageMapper.toEntity(message);
        tripProvider.save(record);
        var filters = """
                {
                    "contractorId" : "%s"
                }
                """.formatted(record.getContractorId());
        var content = mockMvc.perform(MockMvcRequestBuilders.get("/files/report/?filters=" + Base64.getEncoder().encodeToString(filters.getBytes(StandardCharsets.UTF_8)))
                        .with(jwt().jwt(builder -> builder.claim("scope", "DISPATCHER").jti(UUID.randomUUID().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        var response = new ObjectMapper().readValue(content, new TypeReference<Map<String, String>>() {
        });

        var result = await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(30))
                .until(() -> mockMvc.perform(get(response.get("url") + "/")
                                .with(jwt().jwt(b -> b.claim("scope", "DISPATCHER")).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse(), r -> {
                    try {
                        return !r.getContentAsString().contains("in_progress\": true");
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                });

        try (var byteArrayInputStream = new ByteArrayInputStream(result.getContentAsByteArray());
             var excel = new XSSFWorkbook(byteArrayInputStream)) {
            var sheet = excel.getSheet("Поездки");

            assertThat(sheet).isNotNull();

            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(2); // 1 - заголовок

            var cellIndex = 0;
            var row = sheet.getRow(0);
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("№");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("ID поездки");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("ID заявки(-ок)");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Дата создания заявки");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Время создания заявки");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Желаемая дата начала поездки");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Желаемое время начала поездки");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Фактическая дата начала поездки");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Фактическое время начала поездки");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Адрес подачи");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Промежуточные адреса");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Адрес завершения");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Плановое количество точек маршрута");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Хронология обработки статусов Водителем в приложении");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Проверка геопозиции при прохождении точек маршрута водителем. Да - был на точке. Нет - не было на точке.");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Статус поездки");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Комментарии для водителя");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Пассажир поездки");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Номер автомобиля");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Водитель");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Плановая протяженность (км)");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Плановое время поездки (мин)");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Плановое ожидание на точках (мин)");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Плановая стоимость (руб)");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Фактическая протяженность (км)");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Фактическое время поездки (мин)");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Фактическое время ожидания (мин)");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Тип поездки");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Диспетчер");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Телефон диспетчера");
            assertThat(row.getCell(cellIndex++).getStringCellValue()).isEqualTo("Класс авто");
            assertThat(row.getCell(cellIndex).getStringCellValue()).isEqualTo("Кол-во пассажиров");

            var actualRow = sheet.getRow(1);

            cellIndex = 0;
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(1);
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(record.getTripHumanReadableId());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(record.getRequestIds());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(DateUtil.getExcelDate(record.getCreationTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of("GMT+03").normalized().getId())).toLocalDate()));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(record.getCreationTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of("GMT+03").normalized().getId())).toLocalTime().truncatedTo(ChronoUnit.MINUTES).toString());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(DateUtil.getExcelDate(record.getExpectedStartTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of("GMT+03").normalized().getId())).toLocalDate()));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(record.getExpectedStartTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of("GMT+03").normalized().getId())).toLocalTime().truncatedTo(ChronoUnit.MINUTES).toString());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(DateUtil.getExcelDate(record.getFactStartTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of("GMT+03").normalized().getId())).toLocalDate()));
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(record.getFactStartTime().withOffsetSameInstant(ZoneOffset.of(ZoneId.of("GMT+03").normalized().getId())).toLocalTime().truncatedTo(ChronoUnit.MINUTES).toString());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(record.getRouteStart());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(record.getRouteWaypoints());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(record.getRouteEnd());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(record.getCheckinQuantity().intValue());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(record.getCheckinStatuses());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(record.getCheckinWaypointConfirmation());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(record.getStatus());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(record.getComment());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(record.getPassenger());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(record.getVehicleNumber());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(record.getDriver());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(record.getExpectedDistance());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(Duration.ofMillis(record.getExpectedTime()).toMinutes());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(Duration.ofMillis(record.getExpectedWaitTime()).toMinutes());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(record.getExpectedCost());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(record.getFactDistance());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(Duration.ofMillis(record.getFactTime()).toMinutes());
            assertThat(actualRow.getCell(cellIndex++).getNumericCellValue()).isEqualTo(Duration.ofMillis(record.getFactWaitTime()).toMinutes());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(record.getTripType());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(record.getDispatcherName());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(record.getDispatcherPhone());
            assertThat(actualRow.getCell(cellIndex++).getStringCellValue()).isEqualTo(record.getTaxiClass());
            assertThat(actualRow.getCell(cellIndex).getNumericCellValue()).isEqualTo(record.getPassengerCount().intValue());

        }
    }
}
