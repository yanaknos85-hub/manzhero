create table limits.bonus
(
    bonus_owner_id uuid             not null
        constraint bonus_pk
            primary key,
    balance        bigint default 0 not null,
    sum            bigint default 0 not null
);

comment
on table limits.bonus is 'Бонусный счёт';

comment
on column limits.bonus.bonus_owner_id is 'Владелец бонусного счёта';

comment
on column limits.bonus.balance is 'Текущий баланс (коп.)';

create unique index bonus_bonus_owner_id_uindex
    on limits.bonus (bonus_owner_id);