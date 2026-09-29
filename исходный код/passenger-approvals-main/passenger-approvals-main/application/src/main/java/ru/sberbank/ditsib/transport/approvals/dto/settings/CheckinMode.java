package ru.sberbank.ditsib.transport.approvals.dto.settings;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/** Режимы чек-ин */
@Getter
@Schema(title = "Режимы чек-ин", example = "MANUALLY | AUTO_AND_MANUALLY")
public enum CheckinMode {
    MANUALLY,
    AUTO_AND_MANUALLY
}