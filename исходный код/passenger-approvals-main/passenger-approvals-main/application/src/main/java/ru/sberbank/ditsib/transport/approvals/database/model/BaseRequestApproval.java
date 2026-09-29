package ru.sberbank.ditsib.transport.approvals.database.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.MappedSuperclass;

/**
 * Базовый класс для согласований в разделе Заявки на поездки
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseRequestApproval extends Approval {

}
