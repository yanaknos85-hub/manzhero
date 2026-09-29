package ru.sber.transport.tariff_fleet.dto;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.domain.Sort;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.constant.RequestSortOption;
import ru.sber.transport.tariff_fleet.database.model.Tariff_;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "SearchTariffDto", description = "Запрос на поиск тарифов")
public class SearchTariffRequest {
    @Schema(description = "Типы договоров и тарифов")
    @NotNull
    private DocumentType documentType;
    @Schema(description = "ID контрагента", example = "001b5a57-0a8f-4e7b-9c32-21c18c39fb8c")
    @Nullable
    private UUID contractorId;
    @Schema(description = "ID договора", example = "001b5a57-0a8f-4e7b-9c32-21c18c39fb8c")
    @Nullable
    private UUID contractId;
    @Schema(description = "ID организации", example = "001b5a57-0a8f-4e7b-9c32-21c18c39fb8c")
    @Nullable
    private UUID organizationId;
    @Schema(description = "Человокопонятный ID тарифа", example = "TF-0002-00000342")
    @Nullable
    private String humanReadableId;
    @Schema(description = "Статус тарифа", example = "active")
    @Nullable
    private Boolean active;
    @Schema(description = "Параметры пагинации")
    @NotNull
    private PageSetting pageSetting;
    @Nullable
    @Schema(description = "Настройки сортировки")
    private SortSetting sortSetting;

    @Getter
    @Setter
    @ToString
    public static class SortSetting {
        @Schema(description = "Выбор сортировки")
        private RequestSortOption property = RequestSortOption.HUMAN_READABLE_ID;

        @Schema(description = "Направление сортировки")
        private boolean directionAsc = true;
    }

    @Getter
    @Setter
    @ToString
    public static class PageSetting {
        @Schema(description = "Номер страницы")
        private int page = 0;

        @Schema(description = "Количество элементов на странице")
        private int size = 10;
    }

    @Hidden
    public Sort getSort() {

        if (this.getSortSetting() == null) {
            return Sort.by(Sort.Direction.ASC, Tariff_.HUMAN_READABLE_ID);
        }

        Sort sort = Sort.by(Tariff_.HUMAN_READABLE_ID);
        return this.getSortSetting().isDirectionAsc() ? sort.ascending() : sort.descending();
    }
}
