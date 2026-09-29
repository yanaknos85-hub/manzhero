package ru.sberbank.ditsib.transport.limits.dto.v3;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatchDepartmentReserve {

    private Operation op;

    private String path;

    private String value;

}
