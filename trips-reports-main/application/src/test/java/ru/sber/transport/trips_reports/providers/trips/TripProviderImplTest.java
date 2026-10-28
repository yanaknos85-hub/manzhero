package ru.sber.transport.trips_reports.providers.trips;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.trip_reports.message.TripReportMessage;
import ru.sber.transport.trips_reports.database.trips_reports.Tables;
import ru.sber.transport.trips_reports.messaging.mapper.MessageMapper;
import ru.sber.transport.trips_reports.model.Reports;
import ru.sber.transport.trips_reports.web.dto.ReportSearchDto;
import ru.sber.transport.trips_reports.web.dto.ReportSearchParameters;
import ru.sber.transport.trips_reports.web.dto.TripStatus;
import ru.sber.transport.trips_reports.web.dto.TripsExportFiltersDTO;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.request.Direction;
import ru.sber.transport.messaging.kafka.test.KafkaTest;

import java.util.*;
import java.util.stream.StreamSupport;

import static ru.sberbank.ditsib.request.Direction.ASC;

@SpringBootTest
@DisplayName("Проверка провайдера отчетов")
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@Transactional
@UnitTest
@IsolatedTest
@Feature("app_platform_trips_reports")
@ActiveProfiles({"test", "kafka", "kafka-ssl"})
class TripProviderImplTest extends KafkaTest {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private TripProvider tripProvider;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    @DisplayName("Тест сохранения отчета по поездке")
    void test_save(){
        var message = Instancio.of(TripReportMessage.class)
                .create();
        tripProvider.save(messageMapper.toEntity(message));
        var saved = dslContext.selectFrom(Tables.REPORT)
                .fetchOne();
        assert saved != null;
        Assertions.assertEquals(saved.getContractorId(), message.getContractorId());
        Assertions.assertEquals(saved.getTripHumanReadableId(), message.getTripHumanReadableId());
        Assertions.assertEquals(saved.getStatus(), message.getStatus());
    }

    @Test
    @DisplayName("Тест получения отчета по фильтрам")
    void test_find_by_filters(){
        var message = Instancio.of(TripReportMessage.class)
                .create();
        tripProvider.save(messageMapper.toEntity(message));
        var filters = new TripsExportFiltersDTO();
        filters.setContractorId(message.getContractorId());
        var list = tripProvider.findAllByFilters(filters);
        assert !list.isEmpty();
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals(list.get(0).getContractorId(), message.getContractorId());
        Assertions.assertEquals(list.get(0).getTripHumanReadableId(), message.getTripHumanReadableId());
        Assertions.assertEquals(list.get(0).getStatus(), message.getStatus());
    }

    @Test
    @DisplayName("Тест получения отчета по фильтрам (в фильтре нет contractorId)")
    void test_find_by_filters_no_contractor_id(){
        var message = Instancio.of(TripReportMessage.class)
                .create();
        tripProvider.save(messageMapper.toEntity(message));
        var filters = new TripsExportFiltersDTO();
        filters.setStatuses(List.of(TripStatus.ORDER_FINISHED));
        var list = tripProvider.findAllByFilters(filters);
        Assertions.assertTrue(list.isEmpty());
    }

    @Test
    @DisplayName("Тест получения отчета по ID, когда отчет не существует")
    void test_findById_nonExisting() {
        UUID reportId = UUID.randomUUID();
        Optional<Reports> result = tripProvider.findById(reportId);
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Тест получения всех отчетов с пустыми результатами")
    void test_findAll_withEmptyResults() {
        var message = Instancio.of(TripReportMessage.class)
                .create();
        tripProvider.save(messageMapper.toEntity(message));
        UUID contractorId = message.getContractorId();

        ReportSearchDto searchData = new ReportSearchDto();
        searchData.setPage(0);
        searchData.setSize(10);
        searchData.setDirection(ASC);
        searchData.setField(ReportSearchParameters.HUMAN_READABLE_ID);

        List<TripStatus> statuses = List.of(TripStatus.ORDER_FINISHED);


        Iterable<Reports> results = tripProvider.findAll(contractorId, searchData, statuses);

        Assertions.assertNotNull(results);
        Assertions.assertFalse(results.iterator().hasNext());
    }

    @Test
    @DisplayName("Тест фильтрации по номеру транспортного средства")
    void test_findAll_withVehicleNumberFilter() {
        var message = Instancio.of(TripReportMessage.class)
                .create();
        message.setVehicleNumber("ABC123");
        message.setStatus(TripStatus.ORDER_FINISHED.getRusName());
        tripProvider.save(messageMapper.toEntity(message));

        var saved = dslContext.selectFrom(Tables.REPORT)
                .where(Tables.REPORT.VEHICLE_NUMBER.eq("ABC123"))
                .fetchOne();
        Assertions.assertNotNull(saved, "Отчет не был сохранен в базе данных");
        Assertions.assertEquals("ABC123", saved.getVehicleNumber());
        Assertions.assertEquals(TripStatus.ORDER_FINISHED.getRusName(), saved.getStatus());

        ReportSearchDto searchData = new ReportSearchDto();
        searchData.setVehicleNumber("abc123");
        searchData.setPage(0);
        searchData.setSize(10);
        searchData.setDirection(Direction.ASC);
        searchData.setField(ReportSearchParameters.VEHICLE_NUMBER);

        List<TripStatus> statuses = List.of(TripStatus.ORDER_FINISHED);

        Iterable<Reports> results = tripProvider.findAll(message.getContractorId(), searchData, statuses);

        Assertions.assertNotNull(results);
        List<Reports> resultList = StreamSupport.stream(results.spliterator(), false).toList();
        Assertions.assertFalse(resultList.isEmpty(), "Результаты фильтрации не должны быть пустыми");
        Assertions.assertEquals("ABC123", resultList.get(0).getVehicleNumber());
    }

    @Test
    @DisplayName("Тест фильтрации с null значением фильтра")
    void test_appendFilters_withNullValue() {
        var message = Instancio.of(TripReportMessage.class)
                .create();
        message.setStatus(TripStatus.ORDER_FINISHED.getRusName());
        tripProvider.save(messageMapper.toEntity(message));

        ReportSearchDto searchData = new ReportSearchDto();
        searchData.setVehicleNumber(null);
        searchData.setPage(0);
        searchData.setSize(10);
        searchData.setDirection(Direction.ASC);
        searchData.setField(ReportSearchParameters.HUMAN_READABLE_ID);

        List<TripStatus> statuses = List.of(TripStatus.ORDER_FINISHED);

        Iterable<Reports> results = tripProvider.findAll(message.getContractorId(), searchData, statuses);

        Assertions.assertNotNull(results, "Результаты не должны быть null");
        List<Reports> resultList = StreamSupport.stream(results.spliterator(), false).toList();
        Assertions.assertFalse(resultList.isEmpty(), "Результаты фильтрации не должны быть пустыми");
    }

}