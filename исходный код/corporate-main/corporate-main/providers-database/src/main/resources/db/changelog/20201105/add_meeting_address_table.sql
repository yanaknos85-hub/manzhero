CREATE TABLE corporate.meeting_address
(
    id         uuid PRIMARY KEY,
	label      varchar(255) NOT NULL,
    address_id uuid CONSTRAINT meeting_address_address_fk REFERENCES corporate.address (id),
    UNIQUE (label)
)