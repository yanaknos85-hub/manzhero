package ru.sberbank.ditsib.transport.limits.dto.v3;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SecondarySharingData {

    private UUID limitId;

    private UUID departmentId;

}
