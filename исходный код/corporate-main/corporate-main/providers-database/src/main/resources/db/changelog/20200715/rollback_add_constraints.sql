ALTER TABLE corporate.organization
    ALTER COLUMN digit_id drop NOT NULL;

ALTER TABLE corporate.employee
    ALTER COLUMN humanReadableId drop NOT NULL;


