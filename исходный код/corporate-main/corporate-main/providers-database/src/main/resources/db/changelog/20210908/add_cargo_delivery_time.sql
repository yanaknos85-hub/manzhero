insert into corporate.cargo_delivery_time(id, label, urgency, dist_start, dist_end, default_value, value)
values ('6b2de03f-8d75-45b3-88ed-6022401d54c4', 'Срок доставки для "Стандарт", расстояние < 100 км', 'STANDART', 0, 100, 1, 1);

insert into corporate.cargo_delivery_time(id, label, urgency, dist_start, dist_end, default_value, value)
values ('3a9d7b34-df6b-4281-a306-b10ad21cf584', 'Срок доставки для "Экспресс", расстояние < 100 км', 'EXPRESS', 0, 100, 1, 1);

insert into corporate.cargo_delivery_time(id, label, urgency, dist_start, dist_end, default_value, value)
values ('df213ca1-678e-4719-a988-3338ff28d0e9', 'Срок доставки для "Стандарт", 100 км < расстояние < 500 км', 'STANDART', 101, 500, 3, 3);

insert into corporate.cargo_delivery_time(id, label, urgency, dist_start, dist_end, default_value, value)
values ('d0f3bccc-f7d9-4d59-9c01-828e971f4ff7', 'Срок доставки для "Экспресс", 100 км < расстояние < 500 км', 'EXPRESS', 101, 500, 3, 3);

insert into corporate.cargo_delivery_time(id, label, urgency, dist_start, dist_end, default_value, value)
values ('e1c3d068-3031-4071-b339-919c1ad73e1c', 'Срок доставки для "Стандарт", 500 км < расстояние < 1000 км', 'STANDART', 501, 1000, 22, 22);

insert into corporate.cargo_delivery_time(id, label, urgency, dist_start, dist_end, default_value, value)
values ('842ed4da-7174-4d84-a186-71ff296e6016', 'Срок доставки для "Экспресс", 500 км < расстояние < 1000 км', 'EXPRESS', 501, 1000, 11, 11);

insert into corporate.cargo_delivery_time(id, label, urgency, dist_start, dist_end, default_value, value)
values ('62bf0d70-c77c-48e8-8e71-d4098e6d5991', 'Срок доставки для "Стандарт", расстояние > 1000 км', 'STANDART', 1001, 999999, 45, 45);