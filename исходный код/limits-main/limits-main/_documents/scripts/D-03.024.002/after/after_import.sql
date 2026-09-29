create or replace function limits.sum_children(_limit uuid, _sum numeric) returns numeric
language plpgsql as
$$
    declare
        _full_sum numeric = 0;
        _foundLimit limits."limit"%rowtype;
        _logger text;
        _current_limit limits."limit"%rowtype;
    begin
        select * into _current_limit from limits.limit where id = _limit;
        _full_sum := _current_limit.sum;
        for _foundLimit in select * from limits."limit" where parent_id = _limit loop
            select limits.sum_children(_foundLimit.id, _foundLimit.sum) into _sum;
            _full_sum := _full_sum + _sum;
        end loop;
        raise notice '% %', _current_limit.human_readable_id, _full_sum;
        update limits."limit" set sum = _full_sum where id = _current_limit.id;
        return _full_sum;
    end;
$$;

do
$$
declare
  _orgNames text[] = '{000001_Добрые сердца}';
  _serviceTypes text[] = '{PASSENGER}';
  _year int = 2027;
  _org corporate.organization%rowtype;
  _orgName text;
  _orgId uuid;
  _serviceType text;
  _limit limits."limit"%rowtype;
  _foundLimit limits."limit"%rowtype;
  _sum numeric;
begin
    foreach _orgName in array _orgNames loop
        select id into _orgId from corporate.organization where official_name = _orgName;
        raise notice '%', _orgId;
        foreach _serviceType in array _serviceTypes loop
            for _limit in select l.* from limits."limit" l where l.year = _year and l.organization_id = _orgId and l.limit_service_type = _serviceType loop
                select sum(sum) into _sum from limits.limit_sharing where limit_id = _limit.id;
                update limits."limit"
                set sum = _sum
                where id = _limit.id;
            end loop;
        end loop;
        select l.* into _foundLimit from limits."limit" l where l.year = _year and l.organization_id = _orgId and l.limit_service_type = _serviceType and l.parent_id is null;
        select limits.sum_children(_foundLimit.id, _foundLimit.sum) into _sum;
    end loop;
end
$$;

drop function limits.sum_children(uuid, numeric);