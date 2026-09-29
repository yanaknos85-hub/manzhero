package ru.sberbank.ditsib.transport.approvals.mappers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sberbank.ditsib.transport.approvals.database.model.FraudData;
import ru.sberbank.ditsib.transport.approvals.dto.fraud.FraudCommentDTO;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TripApprovalMapperTest {
    private final TripApprovalMapper mapper = new TripApprovalMapperImpl(new EmployeeMapperImpl());

    @Test
    @DisplayName("Должен вернуть пустой список, если входной список null")
    void test_toFraudComment_shouldReturnEmptyList_WhenInputIsNull() {
        var result = mapper.toFraudComment(null);
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Должен вернуть пустой список, если входной список пуст")
    void test_toFraudComment_shouldReturnEmptyList_WhenInputIsEmpty() {
        var result = mapper.toFraudComment(Collections.emptyList());
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Должен отфильтровать элементы с null type")
    void test_toFraudComment_shouldFilterOutElementsWithNullType() {
        var valid = new FraudData();
        valid.setType("RADIUS");
        valid.setComment("Valid comment");

        var invalid = new FraudData();
        invalid.setType(null);
        invalid.setComment("Should be filtered");

        var input = Arrays.asList(invalid, valid);
        var result = mapper.toFraudComment(input);

        assertThat(result)
                .hasSize(1)
                .extracting(FraudCommentDTO::text)
                .containsExactly("Valid comment");
    }

    @Test
    @DisplayName("Должен убрать дубликаты по type, оставив первый")
    void test_toFraudComment_shouldRemoveDuplicatesByType_KeepingFirst() {
        var first = new FraudData();
        first.setType("RADIUS");
        first.setComment("First comment");

        var second = new FraudData();
        second.setType("RADIUS");
        second.setComment("Second comment");

        var other = new FraudData();
        other.setType("RECIEPT");
        other.setComment("Other comment");

        var input = Arrays.asList(first, second, other);
        var result = mapper.toFraudComment(input);

        assertThat(result)
                .hasSize(2)
                .extracting(FraudCommentDTO::text)
                .containsExactlyInAnyOrder("First comment", "Other comment");
    }

    @Test
    @DisplayName("Должен корректно маппить поля: comment -> comment, остальные null")
    void test_toFraudComment_shouldMapCommentFieldCorrectly() {
        var data = new FraudData();
        data.setType("RADIUS");
        data.setComment("Test comment");

        var result = mapper.toFraudComment(Collections.singletonList(data));

        assertThat(result)
                .hasSize(1)
                .first()
                .satisfies(dto -> {
                    assertThat(dto.text()).isEqualTo("Test comment");
                });
    }

    @Test
    @DisplayName("Должен обработать список с одним элементом")
    void test_toFraudComment_shouldHandleSingleElementList() {
        var data = new FraudData();
        data.setType("RADIUS");
        data.setComment("Single");

        var result = mapper.toFraudComment(Collections.singletonList(data));

        assertThat(result)
                .hasSize(1)
                .extracting(FraudCommentDTO::text)
                .containsExactly("Single");
    }

    @Test
    @DisplayName("Должен вернуть пустой список, если все элементы имеют null type")
    void test_toFraudComment_shouldReturnEmpty_WhenAllHaveNullType() {
        var data1 = new FraudData();
        data1.setType(null);
        data1.setComment("First");

        var data2 = new FraudData();
        data2.setType(null);
        data2.setComment("Second");

        var input = Arrays.asList(data1, data2);
        var result = mapper.toFraudComment(input);

        assertThat(result).isNotNull().isEmpty();
    }

}