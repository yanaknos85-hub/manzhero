package ru.sberbank.ditsib.transport.limits.dto.v2;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.constants.limits.LimitSharingType;
import ru.sberbank.ditsib.transport.constants.limits.LimitStatus;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;
import ru.sberbank.ditsib.transport.limits.model.GetEmployeeDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for adding request.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetLimitV2DTO {

        @NotNull
        private UUID id;

        @NotNull
        private String humanReadableId;

        private UUID limitOwner;

        @NotNull
        private LimitStatus limitStatus;

        @NotNull
        private int year;

        @NotNull
        private LimitSharingType limitSharingType;

        @NotNull
        private String limitServiceType;

        @Min(0)
        @NotNull
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        private BigDecimal sum = BigDecimal.ZERO;

        @Min(0)
        @NotNull
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        private BigDecimal reserve = BigDecimal.ZERO;

        @Min(0)
        @NotNull
        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        private BigDecimal economy = BigDecimal.ZERO;

        @NotNull
        private LocalDateTime creationTime;

        private boolean finalSharing;

        private boolean useThisLimit;

        private LimitType limitType;

        private GetDepartmentV2DTO department;

        private GetEmployeeDTO employee;

        private UUID parentLimitId;

        private List<GetLimitSharingV2DTO> sharings;

        private GetEmployeeDTO owner;
}
