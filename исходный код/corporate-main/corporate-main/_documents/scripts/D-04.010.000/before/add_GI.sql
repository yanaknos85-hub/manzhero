DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-7000-00000001', 'EMPLOYEE_TRANSPORTATION', 'ДВБ/Головное отделение 9070/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Дальневосточный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '289358' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '289526' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1730135' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '290260' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Дальневосточный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Хабаровский край'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Еврейская автономная область'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Амурская область'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Магаданская область'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Чукотский автономный округ'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-7000-00000002', 'EMPLOYEE_TRANSPORTATION', 'ДВБ/Приморское отделение 8635/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Дальневосточный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '289358' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1075106' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1869220' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '281901' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '289260' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '282739' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2115472' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '285717' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1582223' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1897988' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1923487' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1661986' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Дальневосточный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Приморский край'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Камчатский край'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Сахалинская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4000-00000001', 'EMPLOYEE_TRANSPORTATION', 'СРБ/Московская область Восточное ГО/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1335248' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '365348' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1645136' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0011-00000668' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00003754' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00003760' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00003762' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00003770' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00003764' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00005605' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00005611' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00006830' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00006898' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00004359' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00004359' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00004359' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00020495' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00020585' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00020590' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Москва и Московская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4000-00000002', 'EMPLOYEE_TRANSPORTATION', 'СРБ/Московская область Западное ГО/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1335248' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '365348' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1645136' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0011-00000671' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Москва и Московская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4000-00000003', 'EMPLOYEE_TRANSPORTATION', 'СРБ/Московская область Северное ГО/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1335248' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '365348' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1645136' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0011-00000673' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00003768' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00003766' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Москва и Московская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4000-00000004', 'EMPLOYEE_TRANSPORTATION', 'СРБ/Московская область Южное ГО/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1335248' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '365348' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1645136' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0011-00000669' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00003756' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00003758' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00021303' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Москва и Московская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4000-00000005', 'EMPLOYEE_TRANSPORTATION', 'СРБ/Рязанское отделение 8606/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '670130' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2120521' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0011-00004916' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00003821' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0001-00023196' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Рязанская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4000-00000006', 'EMPLOYEE_TRANSPORTATION', 'СРБ/Ярославское отделение 0017/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1567959' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1558559' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '300129' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00020663' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00007602' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00005990' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00006958' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00003339' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00001943' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00004065' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0001-00000224' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0001-00003868' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0001-00023422' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Ярославская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4000-00000007', 'EMPLOYEE_TRANSPORTATION', 'СРБ/Костромское отделение 8640/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1900237' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '865949' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Костромская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4000-00000008', 'EMPLOYEE_TRANSPORTATION', 'СРБ/Тверское отделение 8607/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '360140' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '370186' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1677582' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Тверская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4000-00000009', 'EMPLOYEE_TRANSPORTATION', 'СРБ/Брянское отделение 8605/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '358019' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '367119' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1395862' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Брянская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4000-00000010', 'EMPLOYEE_TRANSPORTATION', 'СРБ/Тульское отделение 8604/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '624457' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '637319' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2020117' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Тульская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4000-00000001', 'EMPLOYEE_TRANSPORTATION', 'СРБ/Калужское отделение 8608/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '365646' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0011-00004913' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00002642' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0002-00020660' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0011-00005808' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0011-00005749' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0011-00000777' and status = 'ACTIVE'));

    insert into corporate.executor_group_department (executor_group_id, department_id)
    values (temp_uuid_executor_group, (select id from corporate.department where humanreadableid = 'DT-0011-00005848' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Калужская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4000-00000011', 'EMPLOYEE_TRANSPORTATION', 'СРБ/Смоленское отделение 8604/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '968789' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '618766' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1456164' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Смоленская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4000-00000012', 'EMPLOYEE_TRANSPORTATION', 'СРБ/Ивановское отделение 8639/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1694735' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '295452' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Среднерусский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Ивановская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-9900-00000001', 'EMPLOYEE_TRANSPORTATION', 'ОСЦ/ОСЦ Москва/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1790627' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1963317' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1208733' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1848479' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1843127' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1694112' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1838183' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '935815' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1838088' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1838378' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1839949' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1848465' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1839924' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1946587' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1503388' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1498352' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1497462' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1210798' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1678034' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Москва и Московская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-9900-00000002', 'EMPLOYEE_TRANSPORTATION', 'ОСЦ/ОСЦ Москва/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1790627' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1963317' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1208733' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1848479' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1843127' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1694112' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1838183' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '935815' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1838088' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1838378' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1839949' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1848465' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1839924' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1946587' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1503388' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1498352' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1497462' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1210798' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1678034' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Московский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Тульская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5200-00000001', 'EMPLOYEE_TRANSPORTATION', 'ЮЗБ/Ставропольское отделение 5230/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1754292' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1673305' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1674107' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Ставропольский край'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5200-00000002', 'EMPLOYEE_TRANSPORTATION', 'ЮЗБ/Чеченское отделение 8643/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1916795' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Чеченская Республика'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5200-00000003', 'EMPLOYEE_TRANSPORTATION', 'ЮЗБ/Дагестанское отделение 8590/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1888181' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1960817' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Республика Дагестан'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5200-00000004', 'EMPLOYEE_TRANSPORTATION', 'ЮЗБ/Ингушское отделение 8633/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1951524' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Республика Ингушетия'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5200-00000005', 'EMPLOYEE_TRANSPORTATION', 'ЮЗБ/Кабардино-Балкарское отделение 8631/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1790918' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Кабардино-Балкарская Республика'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5200-00000006', 'EMPLOYEE_TRANSPORTATION', 'ЮЗБ/Северо-Осетинское отделение 8632/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1703412' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Республика Северная Осетия — Алания'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5200-00000007', 'EMPLOYEE_TRANSPORTATION', 'ЮЗБ/Карачаево-Черкесское отделение 8585/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '309965' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Карачаево-Черкесская Республика'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5200-00000008', 'EMPLOYEE_TRANSPORTATION', 'ЮЗБ/Калмыцкое отделение 8579/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '307019' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Республика Калмыкия'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5200-00000009', 'EMPLOYEE_TRANSPORTATION', 'ЮЗБ/Головное отделение по Республике Крым/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1326680' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1904318' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2090076' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Республика Крым'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5200-00000010', 'EMPLOYEE_TRANSPORTATION', 'ЮЗБ/Головное отделение по ДНР/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2104090' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2116683' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Донецкая Народная Республика'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5200-00000011', 'EMPLOYEE_TRANSPORTATION', 'ЮЗБ/Ростовское отделение 5221/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '761959' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1998534' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1936606' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '772058' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Ростовская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5200-00000012', 'EMPLOYEE_TRANSPORTATION', 'ЮЗБ/Краснодарское отделение 8619/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '770372' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1529224' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1630537' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1005931' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Краснодарский край'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5200-00000013', 'EMPLOYEE_TRANSPORTATION', 'ЮЗБ/Адыгейское отделение 8620/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '770372' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1529224' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1630537' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1005931' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Юго-Западный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Республика Адыгея'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5400-00000001', 'EMPLOYEE_TRANSPORTATION', 'ПБ/Астраханское отделение 8625/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Поволжский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1707553' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1589999' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '461581' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1713698' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '394391' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2022690' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Поволжский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Астраханская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5400-00000002', 'EMPLOYEE_TRANSPORTATION', 'ПБ/Волгоградское отделение 8621/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Поволжский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1707553' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1589999' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '461581' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1713698' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '905532' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '452386' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1678874' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Поволжский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Астраханская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5400-00000003', 'EMPLOYEE_TRANSPORTATION', 'ПБ/Оренбургское отделение 8623/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Поволжский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1707553' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1589999' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '461581' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1713698' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2030239' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2074843' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2014471' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1349107' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Поволжский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Волгоградская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5400-00000004', 'EMPLOYEE_TRANSPORTATION', 'ПБ/Пензенское отделение 8624/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Поволжский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1707553' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1589999' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '461581' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1713698' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '445305' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2098554' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2007432' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Поволжский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Пензенская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5400-00000005', 'EMPLOYEE_TRANSPORTATION', 'ПБ/Саратовское отделение 8622/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Поволжский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1707553' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1589999' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '461581' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1713698' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2116570' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1994572' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '454185' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2080242' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Поволжский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Саратовская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5400-00000006', 'EMPLOYEE_TRANSPORTATION', 'ПБ/Ульяновское отделение 8588/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Поволжский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1707553' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1589999' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '461581' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1713698' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1459416' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '397317' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '552649' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Поволжский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Ульяновская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5400-00000007', 'EMPLOYEE_TRANSPORTATION', 'ПБ/Самарское отделение 6991/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Поволжский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1707553' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1589999' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '461581' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1713698' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1741492' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1993506' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1596343' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2079952' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '392348' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1468549' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '672730' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '461581' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1713698' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1530306' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1264047' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '443969' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Поволжский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Самарская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-1300-00000001', 'EMPLOYEE_TRANSPORTATION', 'ЦЧБ/ГО по Воронежской области 9013/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Центрально-Черноземный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '408178' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2009647' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '404734' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1451694' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '402727' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1855571' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Центрально-Черноземный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Воронежская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-1300-00000002', 'EMPLOYEE_TRANSPORTATION', 'ЦЧБ/ГО по Луганской Народной Республике/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Центрально-Черноземный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1904318' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2112736' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '404734' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1451694' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '402727' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Центрально-Черноземный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Луганская Народная Республика'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-1300-00000003', 'EMPLOYEE_TRANSPORTATION', 'ЦЧБ/Белгородское отделение 8592/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Центрально-Черноземный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '409307' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '410596' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '404734' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1451694' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '402727' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '412805' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Центрально-Черноземный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Белгородская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-1300-00000004', 'EMPLOYEE_TRANSPORTATION', 'ЦЧБ/Курское отделение 8596/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Центрально-Черноземный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '411854' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1358024' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '404734' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1451694' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '402727' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '401028' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Центрально-Черноземный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Курская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-1300-00000005', 'EMPLOYEE_TRANSPORTATION', 'ЦЧБ/Липецкое отделение 8593/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Центрально-Черноземный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '415067' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '402349' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '404734' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1451694' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '402727' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '412401' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Центрально-Черноземный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Липецкая область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-1300-00000006', 'EMPLOYEE_TRANSPORTATION', 'ЦЧБ/Орловское отделение 8595/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Центрально-Черноземный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1679404' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1343170' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '404734' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1451694' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '402727' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Центрально-Черноземный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Орловская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-1300-00000007', 'EMPLOYEE_TRANSPORTATION', 'ЦЧБ/Тамбовское отделение 8594/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Центрально-Черноземный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1473079' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '409095' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '404734' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1451694' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '402727' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '409114' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Центрально-Черноземный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Тамбовская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-1800-00000001', 'EMPLOYEE_TRANSPORTATION', 'ББ/Иркутское отделение 8586 Аппарат/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Байкальский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '130473' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '931416' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '119599' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1021914' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1547231' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1985611' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '121916' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '128382' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Байкальский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Иркутская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-1800-00000002', 'EMPLOYEE_TRANSPORTATION', 'ББ/Читинское отделение 8600/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Байкальский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '130473' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '931416' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '121170' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '917783' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1222718' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '118017' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '121916' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '128382' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Байкальский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Забайкальский край'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-1800-00000003', 'EMPLOYEE_TRANSPORTATION', 'ББ/Бурятское отделение 8601/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Байкальский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '130473' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '931416' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '917354' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1834390' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1123647' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '121916' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '128382' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Байкальский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Республика Бурятия'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-1800-00000004', 'EMPLOYEE_TRANSPORTATION', 'ББ/Якутское отделение 8603/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Байкальский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '130473' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '931416' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1820921' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1463517' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '888043' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '121916' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '128382' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Байкальский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Республика Саха (Якутия)'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-1600-00000001', 'EMPLOYEE_TRANSPORTATION', 'УБ/Свердловское отделение 7003/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Уральский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1760626' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1504224' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1984902' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1353088' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '384004' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1199278' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '379113' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2019430' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1097851' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1964923' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1407504' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Байкальский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Свердловская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-1600-00000002', 'EMPLOYEE_TRANSPORTATION', 'УБ/Челябинское отделение 8597/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Уральский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1760626' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1504224' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '863402' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '418890' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1515651' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '419287' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '419099' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1476903' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Байкальский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Челябинская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-1600-00000003', 'EMPLOYEE_TRANSPORTATION', 'УБ/Башкирское отделение 8598/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Уральский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1760626' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1504224' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '390660' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1573477' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1345661' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '379310' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '974196' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '623099' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '381321' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '624220' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Байкальский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Республика Башкортостан'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-1600-00000004', 'EMPLOYEE_TRANSPORTATION', 'УБ/Курганское отделение 8599/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Уральский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1760626' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1504224' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '885941' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '390867' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2095688' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Байкальский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Курганская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-1600-00000005', 'EMPLOYEE_TRANSPORTATION', 'УБ/Западно-Сибирское отделение 8647/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Уральский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1760626' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1504224' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '92223' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1373110' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '103559' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1715600' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Байкальский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Тюменская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-1600-00000006', 'EMPLOYEE_TRANSPORTATION', 'УБ/Югорское отделение 5940/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Уральский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1760626' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1504224' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '89802' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '90652' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1335733' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2099987' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Байкальский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Ханты-Мансийский автономный округ — Югра'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-1600-00000007', 'EMPLOYEE_TRANSPORTATION', 'УБ/Ямало-Ненецкое отделение 8369/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Уральский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1760626' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1504224' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1495894' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '105515' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Байкальский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Ямало-Ненецкий автономный округ'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4200-00000001', 'EMPLOYEE_TRANSPORTATION', 'ВВБ/ГО по Нижегородской области 9042_Аппарат/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Волго-Вятский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1153887' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '928199' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '322641' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1285129' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '319677' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '966251' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1190844' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Волго-Вятский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Нижегородская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4200-00000002', 'EMPLOYEE_TRANSPORTATION', 'ВВБ/Мордовское отделение 8589/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Волго-Вятский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1437590' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '972553' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '928199' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '322641' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1285129' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '319677' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '966251' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1190844' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Волго-Вятский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Республика Мордовия'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4200-00000003', 'EMPLOYEE_TRANSPORTATION', 'ВВБ/Банк Татарстан 8610/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Волго-Вятский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1627835' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2050720' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '331537' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '928199' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '322641' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1285129' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '319677' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '966251' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1190844' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Волго-Вятский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Республика Татарстан'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4200-00000004', 'EMPLOYEE_TRANSPORTATION', 'ВВБ/Владимирское отделение 8611/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Волго-Вятский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '337962' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '335615' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '928199' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '322641' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1285129' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '319677' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '966251' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1190844' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Волго-Вятский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Владимирская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4200-00000005', 'EMPLOYEE_TRANSPORTATION', 'ВВБ/Кировское отделение 8612/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Волго-Вятский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1706439' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '316618' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2025495' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '928199' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '322641' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1285129' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '319677' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '966251' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1190844' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Волго-Вятский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Кировская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4200-00000006', 'EMPLOYEE_TRANSPORTATION', 'ВВБ/Чувашское отделение 8613/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Волго-Вятский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '325884' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1537618' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1438402' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '335130' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '928199' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '322641' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1285129' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '319677' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '966251' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1190844' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Волго-Вятский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Чувашская Республика'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4200-00000007', 'EMPLOYEE_TRANSPORTATION', 'ВВБ/отделение Марий Эл 8614/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Волго-Вятский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '335365' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1446706' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '335703' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '928199' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '322641' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1285129' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '319677' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '966251' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1190844' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Волго-Вятский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Республика Марий Эл'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4200-00000008', 'EMPLOYEE_TRANSPORTATION', 'ВВБ/Пермское отделение 6984/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Волго-Вятский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1471309' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2042743' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1148673' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '787117' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1413070' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '928199' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '322641' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1285129' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '319677' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '966251' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1190844' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Волго-Вятский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Пермский край'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4200-00000009', 'EMPLOYEE_TRANSPORTATION', 'ВВБ/Удмудское отделнеие 8618/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Волго-Вятский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '810168' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1593613' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '945039' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '928199' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '322641' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1285129' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '319677' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '966251' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1190844' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Волго-Вятский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Удмуртская Республика'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5500-00000001', 'EMPLOYEE_TRANSPORTATION', 'СЗБ/ГО по Санкт-Петербургу 9055_Аппарат/ Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Северо-Западный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1958289' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '690331' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1839569' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1398093' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '699188' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '947953' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '682377' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Северо-Западный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Санкт-Петербург и Ленинградская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5500-00000002', 'EMPLOYEE_TRANSPORTATION', 'СЗБ/Архангельское отделение 8637/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Северо-Западный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '296680' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '297810' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '291461' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2014754' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Северо-Западный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Архангельская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5500-00000003', 'EMPLOYEE_TRANSPORTATION', 'СЗБ/Карельское отделение 8628/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Северо-Западный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '702672' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1579966' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2091828' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Северо-Западный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Республика Карелия'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5500-00000004', 'EMPLOYEE_TRANSPORTATION', 'СЗБ/Калининградское отделение 8626/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Северо-Западный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '916955' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '680709' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '676197' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Северо-Западный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Калининградская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5500-00000005', 'EMPLOYEE_TRANSPORTATION', 'СЗБ/Псковское отделение 8630/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Северо-Западный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '812431' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2009777' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2012816' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Северо-Западный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Псковская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5500-00000006', 'EMPLOYEE_TRANSPORTATION', 'СЗБ/Вологодское отделение 8638/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Северо-Западный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '819547' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1861959' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1633056' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Северо-Западный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Вологодская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5500-00000007', 'EMPLOYEE_TRANSPORTATION', 'СЗБ/Новгородское отделение 8629/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Северо-Западный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '694516' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '697660' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1440599' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '694365' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Северо-Западный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Новгородская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5500-00000008', 'EMPLOYEE_TRANSPORTATION', 'СЗБ/Коми отделение 8617/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Северо-Западный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1543541' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '809579' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '801227' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Северо-Западный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Республика Коми'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-5500-00000009', 'EMPLOYEE_TRANSPORTATION', 'СЗБ/Мурманское ГОСБ 8627/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Северо-Западный банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1196494' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '687089' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '678274' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Северо-Западный банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Мурманская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4400-00000001', 'EMPLOYEE_TRANSPORTATION', 'СИБ/Абаканское отделение 8602/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Сибирский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1684035' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '280975' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Сибирский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Республика Хакасия'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4400-00000002', 'EMPLOYEE_TRANSPORTATION', 'СИБ/Алтайское отделение 8644/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Сибирский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1792611' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '344897' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Сибирский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Алтайский край'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4400-00000003', 'EMPLOYEE_TRANSPORTATION', 'СИБ/Кемеровское отделение 8615/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Сибирский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '342556' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1437260' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Сибирский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Кемеровская область — Кузбасс'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4400-00000004', 'EMPLOYEE_TRANSPORTATION', 'СИБ/Красноярское отделение 8646/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Сибирский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1684035' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '280975' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Сибирский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Красноярский край'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4400-00000005', 'EMPLOYEE_TRANSPORTATION', 'СИБ/Кызылское отделение 8591/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Сибирский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1684035' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '280976' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Сибирский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Республика Тыва'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4400-00000006', 'EMPLOYEE_TRANSPORTATION', 'СИБ/Новосибирское ГОСБ 8047_Аппарат/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Сибирский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1430234' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '353162' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1806105' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '876950' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '2116320' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Сибирский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Новосибирская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4400-00000007', 'EMPLOYEE_TRANSPORTATION', 'СИБ/Омское отделение 8634/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Сибирский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1398178' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1759057' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Сибирский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Омская область'));

    END
$$;

DO $$
DECLARE temp_uuid_executor_group UUID;
    BEGIN
    temp_uuid_executor_group := gen_random_uuid();
    insert into corporate.executor_group (id, humanreadableid, service, name, active, creation_time, updated_at,
                                          author_id, user_id, service_level, organization_id)
    values (temp_uuid_executor_group, 'EG-4400-00000008', 'EMPLOYEE_TRANSPORTATION', 'СИБ/Томское отделение 8616/Пассажирские перевозки', true, now(), now(),
            uuid_in('00000000-0000-0000-0000-000000000000'), uuid_in('00000000-0000-0000-0000-000000000000'), null,
            (select id from corporate.organization where official_name = 'Сибирский банк' and status = 'ACTIVE'));
    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '1850514' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_executor (executor_group_id, employee_id)
    values (temp_uuid_executor_group, (select id from corporate.employee where personnel_number = '348220' and status = 'ACTIVE' and org_structure_type = 'INTERNAL'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'Сибирский банк' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПЦП' and status = 'ACTIVE'));

    insert into corporate.executor_group_organization (executor_group_id, organization_id)
    values (temp_uuid_executor_group, (select id from corporate.organization where official_name = 'ПАО Сбербанк России ЦА' and status = 'ACTIVE'));

    insert into corporate.executor_group_geo_zone (executor_group_id, geo_zone_id)
    values (temp_uuid_executor_group, (select id from corporate.messages_geo_zone where name = 'Томская область'));

    END
$$;

ALTER TABLE corporate.executor_group ALTER COLUMN organization_id DROP NOT NULL;

INSERT INTO corporate.executor_group
(id, humanreadableid, service, "name", active, creation_time, updated_at, author_id, user_id)
VALUES(md5(random()::text || clock_timestamp()::text)::uuid, 'AA-0000-00000000', 'EMPLOYEE_TRANSPORTATION', 'default', true,clock_timestamp(), clock_timestamp(), 
(select id from corporate.employee where personnel_number='00999070781'),(select id from corporate.employee where personnel_number='00999070781'));
