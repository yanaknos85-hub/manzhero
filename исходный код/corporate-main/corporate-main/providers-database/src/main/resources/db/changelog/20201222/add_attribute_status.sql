ALTER TABLE corporate.attribute
  ADD COLUMN status varchar(128);

COMMENT ON COLUMN corporate.attribute.status
IS 'Статус';
