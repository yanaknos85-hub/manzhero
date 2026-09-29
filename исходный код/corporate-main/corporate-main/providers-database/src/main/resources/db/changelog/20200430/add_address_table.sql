CREATE TABLE corporate.address
(
    country   varchar(255),
    region    varchar(255),
    city      varchar(255),
    street    varchar(255),
    house     varchar(255),
    building  varchar(255),
    structure varchar(255),
    id        uuid PRIMARY KEY,
    label     varchar(255) UNIQUE
)