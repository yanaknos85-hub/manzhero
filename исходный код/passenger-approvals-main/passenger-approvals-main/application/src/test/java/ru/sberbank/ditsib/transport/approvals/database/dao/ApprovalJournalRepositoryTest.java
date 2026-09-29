package ru.sberbank.ditsib.transport.approvals.database.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.approvals.database.projection.ApprovalJournalProjection;
import ru.sberbank.ditsib.transport.approvals.dto.ApprovalJournalDto;
import ru.sberbank.ditsib.transport.approvals.dto.EmployeeDTO;
import ru.sberbank.ditsib.transport.approvals.mappers.ApprovalJournalMapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@EmbeddedPostgres
@AutoConfigureMockMvc
class ApprovalJournalRepositoryTest {

    @Autowired
    private ApprovalJournalRepository approvalJournalRepository;
    @Autowired
    private ApprovalJournalMapper approvalJournalMapper;

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/trip_purpose.sql",
            "/scripts/approvers.sql"})
    @Sql(scripts = "/scripts/approval_journal.sql", config = @SqlConfig(separator = "/", commentPrefix = "--"))
    void findAllForJournal() {
        Map<String, Object> address1 = Map.of(
                "id", "b9fa1186-0820-4a2a-bc89-1eddb08faef3",
                "city", "Москва",
                "house", "32 к1",
                "region", "Москва",
                "street", "Кутузовский проспект",
                "country", "Россия",
                "latitude", 55.74134604533717,
                "longitude", 37.53081783098175,
                "existInVspGosbTbRegistry", false
        );
        Map<String, Object> address2 = Map.of(
                "id", "2032b2c6-f951-46aa-bfb5-e98b0848d08a",
                "city", "Москва",
                "house", "19а",
                "region", "Москва",
                "street", "Сумской проезд",
                "country", "Россия",
                "latitude", 55.63188965748339,
                "longitude", 37.59622928721561,
                "existInVspGosbTbRegistry", false
        );

        var expectedWaypoints = List.of(address1, address2);
        var pageable = PageRequest.of(0, 2, Sort.by("creationTime").descending());
        var actual = approvalJournalRepository.findAllForJournal(
                List.of("APPROVED"),
                List.of(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8"), UUID.fromString("7dd56ea0-fa38-400d-93a6-2a4ef4b7df70")),
                false,
                Set.of("PERSONAL"),
                UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8"),
                pageable
        );
        assertResultPageable(actual, pageable, 44, 22, 2, 2);
        var mappedContent = actual.getContent().stream()
                .map(approvalJournalProjection -> approvalJournalMapper.approvalJournalProjectionToApprovalJournalDto(approvalJournalProjection,
                        Collections.emptyList()))
                .toList();
        assertThat(mappedContent)
                .hasSize(2)
                .usingRecursiveComparison()
                .ignoringFields("waypoints")
                .isEqualTo(List.of(createApprovalJournalDto1(expectedWaypoints), createApprovalJournalDto2(expectedWaypoints)));

        var pageable2 = PageRequest.of(1, 2, Sort.by("creationTime").descending());
        var actual2 = approvalJournalRepository.findAllForJournal(
                List.of("APPROVED"),
                List.of(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8"), UUID.fromString("7dd56ea0-fa38-400d-93a6-2a4ef4b7df70")),
                false,
                Set.of("PERSONAL"),
                UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8"),
                pageable2
        );
        assertResultPageable(actual2, pageable2, 44, 22, 2, 2);
        var mappedContent2 = actual2.getContent().stream()
                .map(approvalJournalProjection -> approvalJournalMapper.approvalJournalProjectionToApprovalJournalDto(approvalJournalProjection,
                        Collections.emptyList()))
                .toList();
        assertThat(mappedContent2)
                .hasSize(2)
                .extracting(ApprovalJournalDto::id)
                .isEqualTo(List.of(UUID.fromString("d4a1b2c3-e5f6-7890-1234-567890abcdef"),
                        UUID.fromString("f4a1b2c3-d5f6-7890-1234-567890abcdef")));

        var pageable3 = PageRequest.of(1, 2, Sort.by("desiredDate").descending());
        var actual3 = approvalJournalRepository.findAllForJournal(
                List.of("APPROVED"),
                List.of(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8"), UUID.fromString("7dd56ea0-fa38-400d-93a6-2a4ef4b7df70")),
                false,
                Set.of("PERSONAL"),
                UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8"),
                pageable3
        );
        assertResultPageable(actual3, pageable3, 44, 22, 2, 2);
        var mappedContent3 = actual3.getContent().stream()
                .map(approvalJournalProjection -> approvalJournalMapper.approvalJournalProjectionToApprovalJournalDto(approvalJournalProjection,
                        Collections.emptyList()))
                .toList();
        assertThat(mappedContent3)
                .hasSize(2)
                .extracting(ApprovalJournalDto::id)
                .isEqualTo(List.of(UUID.fromString("d4a1b2c3-e5f6-7890-1234-567890abcdef"),
                        UUID.fromString("f4a1b2c3-d5f6-7890-1234-567890abcdef")));
    }

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/trip_purpose.sql",
            "/scripts/approvers.sql"})
    @Sql(scripts = "/scripts/approval_journal.sql", config = @SqlConfig(separator = "/", commentPrefix = "--"))
    void findAllForJournalExtended() {
        var pageable1 = PageRequest.of(0, 10, Sort.by("creationTime").descending());
        var actual1 = approvalJournalRepository.findAllForJournal(
                List.of("APPROVED","DECLINED", "CANCELLED"),
                List.of(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8"), UUID.fromString("7dd56ea0-fa38-400d-93a6-2a4ef4b7df70")),
                false,
                Set.of("PERSONAL"),
                UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8"),
                pageable1
        );
        assertResultPageable(actual1, pageable1, 120, 12, 10, 10);
        var mappedContent1 = actual1.getContent().stream()
                .map(approvalJournalProjection -> approvalJournalMapper.approvalJournalProjectionToApprovalJournalDto(approvalJournalProjection,
                        Collections.emptyList()))
                .toList();
        assertThat(mappedContent1)
                .hasSize(10)
                .extracting(ApprovalJournalDto::id)
                .isEqualTo(List.of(
                        UUID.fromString("c4a1b2c3-d5f6-7890-1234-567890abcdef"),
                        UUID.fromString("e4a1b2c3-d5f6-7890-1234-567890abcdef"),
                        UUID.fromString("d4a1b2c3-e5f6-7890-1234-567890abcdef"),
                        UUID.fromString("f4a1b2c3-d5f6-7890-1234-567890abcdef"),
                        UUID.fromString("d4a1b2c3-e5f6-7890-1234-567890abcde0"),
                        UUID.fromString("c4a1b2c3-d5f6-7890-1234-567890abcde0"),
                        UUID.fromString("f4a1b2c3-d5f6-7890-1234-567890abcde0"),
                        UUID.fromString("e4a1b2c3-d5f6-7890-1234-567890abcde0"),
                        UUID.fromString("c4a1b2c3-d5f6-7890-1234-567890abcde1"),
                        UUID.fromString("d4a1b2c3-e5f6-7890-1234-567890abcde1")
                ));

        var pageable2 = PageRequest.of(1, 10, Sort.by("creationTime").descending());
        var actual2 = approvalJournalRepository.findAllForJournal(
                List.of("APPROVED","DECLINED", "CANCELLED"),
                List.of(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8"), UUID.fromString("7dd56ea0-fa38-400d-93a6-2a4ef4b7df70")),
                false,
                Set.of("PERSONAL"),
                UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8"),
                pageable2
        );
        assertResultPageable(actual2, pageable2, 120, 12, 10, 10);
        var mappedContent2 = actual2.getContent().stream()
                .map(approvalJournalProjection -> approvalJournalMapper.approvalJournalProjectionToApprovalJournalDto(approvalJournalProjection,
                        Collections.emptyList()))
                .toList();
        assertThat(mappedContent2)
                .hasSize(10)
                .extracting(ApprovalJournalDto::id)
                .isEqualTo(List.of(
                        UUID.fromString("f4a1b2c3-d5f6-7890-1234-567890abcde1"),
                        UUID.fromString("d4a1b2c3-e5f6-7890-1234-567890abcde1"),
                        UUID.fromString("f4a1b2c3-d5f6-7890-1234-567890abcde2"),
                        UUID.fromString("d4a1b2c3-e5f6-7890-1234-567890abcde2"),
                        UUID.fromString("e4a1b2c3-d5f6-7890-1234-567890abcde2"),
                        UUID.fromString("c4a1b2c3-d5f6-7890-1234-567890abcde2"),
                        UUID.fromString("c4a1b2c3-d5f6-7890-1234-567890abcde3"),
                        UUID.fromString("d4a1b2c3-e5f6-7890-1234-567890abcde3"),
                        UUID.fromString("e4a1b2c3-d5f6-7890-1234-567890abcde3"),
                        UUID.fromString("f4a1b2c3-d5f6-7890-1234-567890abcde3")
                ));
        var pageable3 = PageRequest.of(2, 10, Sort.by("creationTime").descending());
        var actual3 = approvalJournalRepository.findAllForJournal(
                List.of("APPROVED","DECLINED", "CANCELLED"),
                List.of(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8"), UUID.fromString("7dd56ea0-fa38-400d-93a6-2a4ef4b7df70")),
                false,
                Set.of("PERSONAL"),
                UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8"),
                pageable3
        );
        assertResultPageable(actual3, pageable3, 120, 12, 10, 10);
        var mappedContent3 = actual3.getContent().stream()
                .map(approvalJournalProjection -> approvalJournalMapper.approvalJournalProjectionToApprovalJournalDto(approvalJournalProjection,
                        Collections.emptyList()))
                .toList();
        assertThat(mappedContent3)
                .hasSize(10)
                .extracting(ApprovalJournalDto::id)
                .isEqualTo(List.of(
                        UUID.fromString("d4a1b2c3-e5f6-7890-1234-567890abcde4"),
                        UUID.fromString("e4a1b2c3-d5f6-7890-1234-567890abcde4"),
                        UUID.fromString("f4a1b2c3-d5f6-7890-1234-567890abcde4"),
                        UUID.fromString("c4a1b2c3-d5f6-7890-1234-567890abcde4"),
                        UUID.fromString("e4a1b2c3-d5f6-7890-1234-567890abcde5"),
                        UUID.fromString("c4a1b2c3-d5f6-7890-1234-567890abcde5"),
                        UUID.fromString("f4a1b2c3-d5f6-7890-1234-567890abcde5"),
                        UUID.fromString("d4a1b2c3-e5f6-7890-1234-567890abcde5"),
                        UUID.fromString("e4a1b2c3-d5f6-7890-1234-567890abcde6"),
                        UUID.fromString("d4a1b2c3-e5f6-7890-1234-567890abcde6")
                ));
    }

    @Test
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/trip_purpose.sql",
            "/scripts/approvers.sql"})
    @Sql(scripts = "/scripts/approval_journal.sql", config = @SqlConfig(separator = "/", commentPrefix = "--"))
    void countAllByDepartmentsAndTypeAndPassenger() {
        assertThat(approvalJournalRepository.countForJournal(
                List.of("APPROVED","DECLINED"),
                List.of(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8"), UUID.fromString("7dd56ea0-fa38-400d-93a6-2a4ef4b7df70")),
                false,
                Set.of("PERSONAL"),
                UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8")
        )).isEqualTo(82);
    }

    private static void assertResultPageable(Page<ApprovalJournalProjection> actual, PageRequest pageable,
                                             int totalElements, int totalPages, int size, int numberOfElements) {
        assertThat(actual.getPageable()).isEqualTo(pageable);
        assertThat(actual.getNumber()).isEqualTo(pageable.getPageNumber());
        assertThat(actual.getTotalElements()).isEqualTo(totalElements);
        assertThat(actual.getTotalPages()).isEqualTo(totalPages);
        assertThat(actual.getSize()).isEqualTo(size);
        assertThat(actual.getNumberOfElements()).isEqualTo(numberOfElements);
        assertThat(actual.getSort()).isEqualTo(pageable.getSort());
    }

    private ApprovalJournalDto createApprovalJournalDto1(List<Map<String, Object>> waypoints) {
        return new ApprovalJournalDto(
                UUID.fromString("d4a1b2c3-e5f6-7890-1234-567890abcdef"),
                new EmployeeDTO(
                        UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8"),
                        "Петр",
                        "Петров",
                        null,
                        "2016497",
                        UUID.fromString("482e6dcb-03a9-4927-90b4-c7081114a9d8"),
                        UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8")
                ),
                UUID.fromString("a1b2c3d4-e5f6-7890-1234-567890abcdef"),
                "PERSONAL",
                null,
                LocalDateTime.of(2026, 4, 29, 12, 0, 0),
                UUID.fromString("9ae969e4-3369-459e-8505-7784dd7d7250"),
                "Первая V 1.0",
                12000.0,
                "APPROVED",
                waypoints,
                Duration.ofNanos(2220000000000L),
                12.567,
                1,
                "OT-0001-00030001",
                false,
                "FINAL_TRIP",
                "GMT+03:00",
                null,
                Collections.emptyList()
        );
    }

    private ApprovalJournalDto createApprovalJournalDto2(List<Map<String, Object>> waypoints) {
        return new ApprovalJournalDto(
                UUID.fromString("e4a1b2c3-d5f6-7890-1234-567890abcdef"),
                new EmployeeDTO(
                        UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8"),
                        "Петр",
                        "Петров",
                        null,
                        "2016497",
                        UUID.fromString("482e6dcb-03a9-4927-90b4-c7081114a9d8"),
                        UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8")
                ),
                UUID.fromString("b1b2c3d4-e5f6-7890-1234-567890abcdef"),
                "PERSONAL",
                null,
                LocalDateTime.of(2026, 4, 29, 12, 0, 0),
                UUID.fromString("9ae969e4-3369-459e-8505-7784dd7d7250"),
                "Первая V 1.0",
                18000.0,
                "APPROVED",
                waypoints,
                Duration.ofNanos(4650000000000L),
                18.237,
                1,
                "OT-0001-00040001",
                false,
                "SHARED_RIDE_JOIN",
                "GMT+03:00",
                UUID.fromString("b1b2c3d4-e5f6-7890-1234-567890abcdef"),
                Collections.emptyList()
        );
    }
}