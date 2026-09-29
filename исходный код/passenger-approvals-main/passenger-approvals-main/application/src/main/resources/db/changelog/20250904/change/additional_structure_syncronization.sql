DO
$do$
    DECLARE
        rec record;
    BEGIN
        IF EXISTS(
                SELECT table_name
                FROM information_schema.tables
                WHERE table_catalog = 'transport'
                  and table_schema = 'corporate'
                  and table_name = 'organization'
            ) THEN
            FOR rec IN select id, digit_id, status from corporate.organization
                LOOP
                    update approvals.message_organization ro
                    set active   = (select CASE WHEN rec.status = 'ACTIVE' THEN true ELSE false END),
                        digit_id = rec.digit_id
                    where ro.id = rec.id;
                END LOOP;
        END IF;
    END
$do$;

DO
$do$
    DECLARE
        rec record;
    BEGIN
        IF EXISTS(
                SELECT table_name
                FROM information_schema.tables
                WHERE table_catalog = 'transport'
                  and table_schema = 'corporate'
                  and table_name = 'department'
            ) THEN
            FOR rec IN select id, organization_id, status, parent_id, name, head from corporate.department
                LOOP
                    update approvals.message_department ro
                    set active             = (select CASE WHEN rec.status = 'ACTIVE' THEN true ELSE false END),
                        organization_id    = rec.organization_id,
                        parent_id          = rec.parent_id,
                        department_head_id = rec.head,
                        department_name    = rec.name
                    where ro.id = rec.id;
                END LOOP;
        END IF;
    END
$do$;

DO
$do$
    DECLARE
        rec record;
    BEGIN
        IF EXISTS(
                SELECT table_name
                FROM information_schema.tables
                WHERE table_catalog = 'transport'
                  and table_schema = 'corporate'
                  and table_name = 'position'
            ) THEN
            FOR rec IN select id, status, self_approved, organization_id from corporate.position
                LOOP
                    update approvals.message_position ro
                    set active          = (select CASE WHEN rec.status = 'ACTIVE' THEN true ELSE false END),
                        organization_id = rec.organization_id,
                        self_approved   = rec.self_approved
                    where ro.id = rec.id;
                END LOOP;
        END IF;
    END
$do$;

DO
$do$
    DECLARE
        rec record;
    BEGIN
        IF EXISTS(
                SELECT table_name
                FROM information_schema.tables
                WHERE table_catalog = 'transport'
                  and table_schema = 'corporate'
                  and table_name = 'employee'
            ) THEN
            FOR rec IN select id,
                              status,
                              patronymic,
                              personnel_number,
                              user_id,
                              department_id,
                              position_id,
                              supervisor_id,
                              humanreadableid,
                              first_name,
                              last_name
                       from corporate.employee
                LOOP
                    update approvals.message_employee ro
                    set active            = (select CASE WHEN rec.status = 'ACTIVE' THEN true ELSE false END),
                        patronymic        = rec.patronymic,
                        personnel_number  = rec.personnel_number,
                        user_id           = rec.user_id,
                        department_id     = rec.department_id,
                        position_id       = rec.position_id,
                        supervisor_id     = rec.supervisor_id,
                        human_readable_id = rec.humanreadableid,
                        first_name        = rec.first_name,
                        last_name         = rec.last_name
                    where ro.id = rec.id;
                END LOOP;
        END IF;
    END
$do$;