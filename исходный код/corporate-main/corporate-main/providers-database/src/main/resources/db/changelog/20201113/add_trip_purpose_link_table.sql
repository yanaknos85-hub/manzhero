
CREATE TABLE corporate.trip_purpose_attribute
(
    id         uuid PRIMARY KEY,
    trip_purpose uuid CONSTRAINT trip_purpose_attribute_trip_purpose_fk REFERENCES corporate.trip_purpose (id),
    attribute_id uuid CONSTRAINT trip_purpose_attribute_attribute_fk REFERENCES corporate.attribute (id)
);

CREATE TABLE corporate.trip_purpose_department
(
    id         uuid PRIMARY KEY,
    trip_purpose uuid CONSTRAINT trip_purpose_department_trip_purpose_fk REFERENCES corporate.trip_purpose (id),
    department uuid CONSTRAINT trip_purpose_department_department_fk REFERENCES corporate.department (id)
);

CREATE TABLE corporate.trip_purpose_date
(
    id         uuid PRIMARY KEY,
    trip_purpose uuid CONSTRAINT trip_purpose_date_trip_purpose_fk REFERENCES corporate.trip_purpose (id),
    purpose_date date not null
);

CREATE TABLE corporate.trip_purpose_time
(
    id         uuid PRIMARY KEY,
    trip_purpose uuid CONSTRAINT trip_purpose_time_trip_purpose_fk REFERENCES corporate.trip_purpose (id),
	start_time     timestamp    NOT NULL,
 	end_time     timestamp    NOT NULL
);

CREATE TABLE corporate.trip_purpose_weekday
(
    id         uuid PRIMARY KEY,
    trip_purpose uuid CONSTRAINT trip_purpose_weekday_trip_purpose_fk REFERENCES corporate.trip_purpose (id),
	weekday     varchar(128)    NOT NULL
);
