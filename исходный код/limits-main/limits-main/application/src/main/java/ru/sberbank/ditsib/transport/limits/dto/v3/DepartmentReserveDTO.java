package ru.sberbank.ditsib.transport.limits.dto.v3;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumDeserializer;
import ru.sberbank.ditsib.transport.limits.dto.serde.SumSerializer;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentReserveDTO {

    private UUID id;

    private UUID departmentId;

    private Map<String, TransportTypeData> transportTypes;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransportTypeData{

        @JsonSerialize(using = SumSerializer.class)
        @JsonDeserialize(using = SumDeserializer.class)
        private BigDecimal sum;

        @JsonAlias(value = "public")
        private boolean isPublic;

    }

}
