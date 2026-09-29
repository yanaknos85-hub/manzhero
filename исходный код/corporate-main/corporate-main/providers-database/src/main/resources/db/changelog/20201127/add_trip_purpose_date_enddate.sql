ALTER TABLE corporate.trip_purpose_date
  ADD COLUMN purpose_date_end DATE;

COMMENT ON COLUMN corporate.trip_purpose_date.purpose_date_end
IS 'Конечная дата';
COMMENT ON COLUMN corporate.trip_purpose_date.purpose_date
IS 'Начальная дата';

UPDATE corporate.trip_purpose_date
SET purpose_date_end = purpose_date;