-- Использовать не выше ИФТ!!!

create temporary table corporate_departments_links as
select code as code, organization_id as org_id
from corporate.department
where status = 'ACTIVE'
  and org_structure_type = 'INTERNAL'
group by code, status, organization_id
having count(*) > 1;

update corporate.department
set status = 'INACTIVE'
from corporate_departments_links cdl
where department.organization_id = cdl.org_id
  and department.code = cdl.code
  and department.status = 'ACTIVE';

do
$$
    declare
        item   corporate_departments_links%rowtype;
        active corporate.department%rowtype;
    begin
        for item in select * from corporate_departments_links
            loop
                select dep.*
                into active
                from corporate.department dep
                         inner join corporate_departments_links cdl
                                    on dep.code = cdl.code and dep.organization_id = cdl.org_id and
                                       dep.status = 'INACTIVE' and
                                       org_structure_type = 'INTERNAL'
                where dep.code = item.code
                  and organization_id = item.org_id
                order by humanreadableid desc
                limit 1;
                update corporate.department
                set status = 'ACTIVE'
                where id = active.id;
            end loop;
    end;
$$;

update limits.department
set active = (cdl.status = 'ACTIVE')
from corporate.department cdl
where department.id = cdl.id;

update limits.department
set active = false
where id not in (select id from corporate.department);

drop table corporate_departments_links;