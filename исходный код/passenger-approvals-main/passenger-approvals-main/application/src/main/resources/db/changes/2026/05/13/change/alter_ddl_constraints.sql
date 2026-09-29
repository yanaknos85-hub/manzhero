-- Добавляем первичный ключ
ALTER TABLE approvals.approval_journal
    ADD CONSTRAINT pk_approval_journal
    PRIMARY KEY (id);

-- Добавляем уникальные констрейнты
ALTER TABLE approvals.approval_journal
    ADD CONSTRAINT uk_approval_journal_approval_id
    UNIQUE (approval_id);

-- Добавляем внешние ключи
ALTER TABLE approvals.approval_journal
    ADD CONSTRAINT fk_approval_journal_actor_id
    FOREIGN KEY (actor_id) REFERENCES approvals.message_employee(id);

ALTER TABLE approvals.approval_journal
    ADD CONSTRAINT fk_approval_journal_approved_by_id
    FOREIGN KEY (approved_by_id) REFERENCES approvals.message_employee(id);

ALTER TABLE approvals.approval_journal
    ADD CONSTRAINT fk_approval_journal_trip_purpose_id
    FOREIGN KEY (trip_purpose_id) REFERENCES approvals.trip_purpose(id);