DO
$$
    DECLARE
        rec           RECORD;
        recPosition RECORD;
        _prefix       varchar(2)='PS';
        humanId       varchar(100);
        digitId       int;
        i             int;
    BEGIN
        FOR rec IN SELECT DISTINCT b.id
                   FROM corporate.organization b
            LOOP
                i = 0;
                FOR recPosition IN SELECT DISTINCT a.id
                                     FROM corporate.position a
                                     WHERE a.organization_id = rec.id
                                       AND humanreadableid is NULL

                    LOOP
                        i = i + 1;
                        SELECT digit_id INTO digitId FROM corporate.organization WHERE id = rec.id;
                        humanId = _prefix || '-' || LPAD(digitId::text, 4, '0') || '-' || i;

                        UPDATE corporate.position
                        SET humanreadableid=humanId
                        WHERE id = recPosition.id;

                        if i = 1 then
                            INSERT INTO corporate.company_sq(id, prefix, orgDigitId, sq)
                            VALUES (uuid_generate_v4(), _prefix, digitId, i);

                        end if;
                        if i > 1 then
                            update corporate.company_sq a
                            set sq=i
                            where a.prefix = _prefix
                              and orgdigitid = digitId;
                        end if;
                    END LOOP;
            END LOOP;
    END;
$$;
