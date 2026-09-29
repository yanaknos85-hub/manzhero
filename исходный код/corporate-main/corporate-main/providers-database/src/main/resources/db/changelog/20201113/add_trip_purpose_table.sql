CREATE TABLE corporate.trip_purpose
(
    id         uuid PRIMARY KEY,
	label      varchar(128) NOT NULL,
	active     boolean NOT NULL DEFAULT true,
    organization uuid CONSTRAINT trip_purpose_organization_fk REFERENCES corporate.organization (id)
);

INSERT INTO corporate.trip_purpose (id, label, active,  organization)
VALUES (
  'dac2ed11-27f1-11eb-9a28-305a3a7d9fe6',
  'Подозрение на коронавирус',
  true,
  (SELECT id FROM corporate.organization LIMIT 1)
);
INSERT INTO corporate.trip_purpose (id, label, active,  organization)
VALUES (
  'dac2ed12-27f1-11eb-9a28-305a3a7d9fe6',
  'Выезды на аварии',
  true,
  (SELECT id FROM corporate.organization LIMIT 1)
);
INSERT INTO corporate.trip_purpose (id, label, active,  organization)
VALUES (
  'dac2ed13-27f1-11eb-9a28-305a3a7d9fe6',
  'Встречи с клиентами',
  true,
  (SELECT id FROM corporate.organization LIMIT 1)
);
INSERT INTO corporate.trip_purpose (id, label, active,  organization)
VALUES (
  'dac2ed14-27f1-11eb-9a28-305a3a7d9fe6',
  'Выезды в государственные органы',
  true,
  (SELECT id FROM corporate.organization LIMIT 1)
);
INSERT INTO corporate.trip_purpose (id, label, active,  organization)
VALUES (
  'dac2ed15-27f1-11eb-9a28-305a3a7d9fe6',
  'Выезды к должникам Банка',
  true,
  (SELECT id FROM corporate.organization LIMIT 1)
);
INSERT INTO corporate.trip_purpose (id, label, active,  organization)
VALUES (
  'dac2ed16-27f1-11eb-9a28-305a3a7d9fe6',
  'Доставка банковских карт',
  true,
  (SELECT id FROM corporate.organization LIMIT 1)
);
INSERT INTO corporate.trip_purpose (id, label, active,  organization)
VALUES (
  'dac2ed17-27f1-11eb-9a28-305a3a7d9fe6',
  'Доставка подменного фонда сотрудников в ВСП и обратно',
  true,
  (SELECT id FROM corporate.organization LIMIT 1)
);
INSERT INTO corporate.trip_purpose (id, label, active,  organization)
VALUES (
  'dac2ed18-27f1-11eb-9a28-305a3a7d9fe6',
  'Доставка сотрудников в ночное время',
  true,
  (SELECT id FROM corporate.organization LIMIT 1)
);
INSERT INTO corporate.trip_purpose (id, label, active,  organization)
VALUES (
  'dac2ed19-27f1-11eb-9a28-305a3a7d9fe6',
  'Проведение контрольных процедур',
  true,
  (SELECT id FROM corporate.organization LIMIT 1)
);
INSERT INTO corporate.trip_purpose (id, label, active,  organization)
VALUES (
  'dac2ed20-27f1-11eb-9a28-305a3a7d9fe6',
  'Проведение наставничества ВСП',
  true,
  (SELECT id FROM corporate.organization LIMIT 1)
);
INSERT INTO corporate.trip_purpose (id, label, active,  organization)
VALUES (
  'dac2ed21-27f1-11eb-9a28-305a3a7d9fe6',
  'Перемещение ВСП',
  true,
  (SELECT id FROM corporate.organization LIMIT 1)
);
INSERT INTO corporate.trip_purpose (id, label, active,  organization)
VALUES (
  'dac2ed22-27f1-11eb-9a28-305a3a7d9fe6',
  'Контроль СМР на объектах переформатирования',
  true,
  (SELECT id FROM corporate.organization LIMIT 1)
);
INSERT INTO corporate.trip_purpose (id, label, active,  organization)
VALUES (
  'dac2ed23-27f1-11eb-9a28-305a3a7d9fe6',
  'Проведение проверки залогов',
  true,
  (SELECT id FROM corporate.organization LIMIT 1)
);
INSERT INTO corporate.trip_purpose (id, label, active,  organization)
VALUES (
  'dac2ed24-27f1-11eb-9a28-305a3a7d9fe6',
  'Проведение служебных расследований',
  true,
  (SELECT id FROM corporate.organization LIMIT 1)
);
INSERT INTO corporate.trip_purpose (id, label, active,  organization)
VALUES (
  'dac2ed25-27f1-11eb-9a28-305a3a7d9fe6',
  'Производственные/банковские мероприятия (семинары, конференции, форумы)',
  true,
  (SELECT id FROM corporate.organization LIMIT 1)
);
INSERT INTO corporate.trip_purpose (id, label, active,  organization)
VALUES (
  'dac2ed26-27f1-11eb-9a28-305a3a7d9fe6',
  'Гемба',
  true,
  (SELECT id FROM corporate.organization LIMIT 1)
);
INSERT INTO corporate.trip_purpose (id, label, active,  organization)
VALUES (
  'dac2ed27-27f1-11eb-9a28-305a3a7d9fe6',
  'Выезды для участия в коллегиальных органах/рабочих группах',
  true,
  (SELECT id FROM corporate.organization LIMIT 1)
);
INSERT INTO corporate.trip_purpose (id, label, active,  organization)
VALUES (
  'dac2ed28-27f1-11eb-9a28-305a3a7d9fe6',
  'Обслуживание УС',
  true,
  (SELECT id FROM corporate.organization LIMIT 1)
);