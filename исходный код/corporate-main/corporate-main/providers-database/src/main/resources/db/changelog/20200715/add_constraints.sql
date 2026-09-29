ALTER TABLE corporate.organization
    ALTER COLUMN digit_id SET NOT NULL;

ALTER TABLE corporate.employee
    ALTER COLUMN humanReadableId SET NOT NULL;


