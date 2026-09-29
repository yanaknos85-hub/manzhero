DO
$do$
    BEGIN
        IF EXISTS(
                SELECT table_name
                FROM information_schema.tables
                WHERE table_catalog = 'transport'
                  and table_schema = 'corporate'
                  and table_name = 'organization'
            ) THEN
            insert into approvals.message_organization (id, digit_id)
            select co.id, co.digit_id
            from corporate.organization co
            on conflict do nothing;
        END IF;
    END
$do$;

DO
$do$
    BEGIN
        IF EXISTS(
                SELECT table_name
                FROM information_schema.tables
                WHERE table_catalog = 'transport'
                  and table_schema = 'corporate'
                  and table_name = 'department'
            ) THEN
            insert
            into approvals.message_department (id, organization_id, parent_id, department_head_id, department_name)
            with recursive entries as (
                select id,
                       organization_id,
                       parent_id,
                       head,
                       name,
                       id                                                   as root_id,
                       1                                                    as level
                from corporate.department
                where parent_id is null-- this should be IS NULL
                union all
                select c.id,
                       c.organization_id,
                       c.parent_id,
                       c.head,
                       c.name,
                       p.root_id,
                       p.level + 1
                from corporate.department c
                         join entries p on p.id = c.parent_id
            )
            select entries.id,
                   entries.organization_id,
                   entries.parent_id,
                   entries.head,
                   entries.name
            from entries
            order by level, root_id, id
            on conflict do nothing;
        END IF;
    END
$do$;

DO
$do$
    BEGIN
        IF EXISTS(
                SELECT table_name
                FROM information_schema.tables
                WHERE table_catalog = 'transport'
                  and table_schema = 'corporate'
                  and table_name = 'employee'
            ) THEN
            insert into approvals.message_employee (id, patronymic, personnel_number, user_id, delegated_by,
                                                    department_id, position_id, supervisor_id, humanreadableid,
                                                    first_name, last_name)
            select co.id,
                   co.patronymic,
                   co.personnel_number,
                   co.user_id,
                   null,
                   co.department_id,
                   co.position_id,
                   co.supervisor_id,
                   co.humanreadableid,
                   co.first_name,
                   co.last_name
            from corporate.employee co
            where co.user_id is not null
            on conflict do nothing;
        END IF;
    END
$do$;