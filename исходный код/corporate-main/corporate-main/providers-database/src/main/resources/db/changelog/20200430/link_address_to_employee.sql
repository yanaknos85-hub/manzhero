CREATE TABLE corporate.address_user
(
    id         uuid PRIMARY KEY,
    user_id    uuid CONSTRAINT address_user_fk REFERENCES corporate."user" (id),
    address_id uuid CONSTRAINT address_fk REFERENCES corporate.address (id),
    label      varchar(255) NOT NULL,
    UNIQUE (user_id, label)
)