create type limits.limit_type_custom as (id uuid[], sum numeric, reserve numeric,  economy numeric, status text[], department_id uuid, employee_id uuid);

alter table limits.limit_sharing
	drop constraint limits_limit_sharing_transport_type_limit_uk;

alter table limits.limit_sharing_per_period
	drop constraint limits_limit_sharing_per_period_limit_sharing_id_period_uk;

create or replace procedure limits.remove_duplicates() language plpgsql as
$$
	declare
		_temp_data limits.limit_type_custom;
		_target_temp_id uuid;
		_source_temp_id uuid;
	begin
		for _temp_data in
			select
				array_agg(id) as ids,
				sum(sum) as sum,
				sum(reserve) as reserve,
				sum(economy) as economy,
				array_agg(limit_status) as status,
				department_id,
				employee_id
				from limits."limit" where parent_id is null group by organization_id, year, limit_service_type, limit_status, parent_id, department_id, employee_id having count(id) > 1 loop
			_target_temp_id := _temp_data.id[1];
			for i in 2..array_length(_temp_data.id, 1) loop
				_source_temp_id := _temp_data.id[i];

				update limits.limit_sharing
				set limit_id = _target_temp_id
				where limit_id = _source_temp_id;

				update limits."limit"
				set parent_id = _target_temp_id
				where parent_id = _source_temp_id;

				call limits.remove_duplicates(_source_temp_id);

				delete from limits."limit" where id = _source_temp_id;
			end loop;

			update limits."limit"
			set sum = _temp_data.sum,
			reserve = _temp_data.reserve,
			economy  = _temp_data.economy
			where id = _target_temp_id;

		end loop;
	end;
$$;

create or replace procedure limits.remove_duplicates(_parent_id uuid) language plpgsql as
$$
	declare
		_temp_data limits.limit_type_custom;
		_target_temp_id uuid;
		_source_temp_id uuid;
	begin
		raise notice 'Reparenting %', _parent_id;

		for _temp_data in select
							array_agg(id) as ids,
							sum(sum) as sum,
							sum(reserve) as reserve,
							sum(economy) as economy,
							array_agg(limit_status) as status,
							department_id,
							employee_id
							from limits."limit"
							where parent_id = _parent_id
							group by organization_id, year, limit_service_type, limit_status, parent_id, department_id, employee_id having count(id) > 1 loop

			_target_temp_id := _temp_data.id[1];
			for i in 2..array_length(_temp_data.id, 1) loop
				_source_temp_id := _temp_data.id[i];

				update limits.limit_sharing
				set limit_id = _target_temp_id
				where limit_id = _source_temp_id;

				update limits."limit"
				set parent_id = _target_temp_id
				where parent_id = _source_temp_id;

				update limits.limit_history
				set limit_id = _target_temp_id
				where limit_id = _source_temp_id;

				update limits.limit_transfer_history
				set source_limit_id = _target_temp_id
				where source_limit_id = _source_temp_id;

				update limits.limit_transfer_history
				set target_limit_id = _target_temp_id
				where target_limit_id = _source_temp_id;

				call limits.remove_duplicates(_source_temp_id);

				delete from limits."limit" where id = _source_temp_id;
			end loop;

			update limits."limit"
			set sum = _temp_data.sum,
			reserve = _temp_data.reserve,
			economy  = _temp_data.economy
			where id = _target_temp_id;

		end loop;
	end;
$$;

call limits.remove_duplicates();

do
$$
	declare
		_temp_data limits.limit_type_custom;
		_target_temp_id uuid;
		_source_temp_id uuid;
	begin
		for _temp_data in select
			array_agg(id) as ids,
			sum(sum) as sum,
			sum(reserve) as reserve,
			sum(economy) as economy,
			array_agg(limit_status) as status,
			department_id,
			null
		from limits."limit" where employee_id is null
		group by organization_id, year, limit_service_type, limit_status, parent_id, department_id having count(id) > 1
		loop

			_target_temp_id := _temp_data.id[1];
			for i in 2..array_length(_temp_data.id, 1) loop
				_source_temp_id := _temp_data.id[i];

				update limits.limit_sharing
				set limit_id = _target_temp_id
				where limit_id = _source_temp_id;

				update limits."limit"
				set parent_id = _target_temp_id
				where parent_id = _source_temp_id;

				call limits.remove_duplicates(_source_temp_id);

				delete from limits."limit" where id = _source_temp_id;
			end loop;

			update limits."limit"
			set sum = _temp_data.sum,
			reserve = _temp_data.reserve,
			economy  = _temp_data.economy
			where id = _target_temp_id;

		end loop;
	end;
$$;

do
$$
	declare
		_temp_data limits.limit_type_custom;
		_target_temp_id uuid;
		_source_temp_id uuid;
	begin
		for _temp_data in select
			array_agg(id) as ids,
			sum(sum) as sum,
			sum(reserve) as reserve,
			sum(economy) as economy,
			array_agg(limit_status) as status,
			employee_id,
			null
		from limits."limit" where department_id is null
		group by organization_id, year, limit_service_type, limit_status, parent_id, employee_id having count(id) > 1
		loop

			_target_temp_id := _temp_data.id[1];
			for i in 2..array_length(_temp_data.id, 1) loop
				_source_temp_id := _temp_data.id[i];

				update limits.limit_sharing
				set limit_id = _target_temp_id
				where limit_id = _source_temp_id;

				update limits.limit_sharing_percents
				set limit_id = _target_temp_id
				where limit_id = _source_temp_id;

				update limits."limit"
				set parent_id = _target_temp_id
				where parent_id = _source_temp_id;

				call limits.remove_duplicates(_source_temp_id);

				delete from limits."limit" where id = _source_temp_id;
			end loop;

			update limits."limit"
			set sum = _temp_data.sum,
			reserve = _temp_data.reserve,
			economy  = _temp_data.economy
			where id = _target_temp_id;

		end loop;
	end;
$$;

create unique index
	limits_limit_parent_id_employee_id_manual_uk on limits."limit" (organization_id, "year", limit_status, limit_service_type, parent_id, employee_id)
where department_id is null and employee_id is not null;

create unique index
	limits_limit_parent_id_department_id_manual_uk on limits."limit" (organization_id, "year", limit_status, limit_service_type, parent_id, department_id)
where employee_id is null and department_id is not null;

drop type limits.limit_type_custom;

create type limits.limit_sharing_type_custom as (id uuid[], sum numeric, balance numeric);

do
$$
	declare
		_item limits.limit_sharing_type_custom;
		_target_temp_id uuid;
		_source_temp_id uuid;
	begin
		for _item in select array_agg(id), sum(sum), sum(balance)  from limits.limit_sharing group by transport_type, limit_id having count(id) > 1 loop
			_target_temp_id := _item.id[1];

			for i in 2..array_length(_item.id, 1) loop
				_source_temp_id := _item.id[i];

				update limits.limit_sharing_per_period
				set limit_sharing_id = _target_temp_id
				where limit_sharing_id = _source_temp_id;

				delete from limits.limit_sharing where id = _source_temp_id;
			end loop;

			update limits.limit_sharing
			set sum = _item.sum,
			balance  = _item.balance
			where id = _target_temp_id;

		end loop;
	end;
$$;

do
$$
	declare
		_item limits.limit_sharing_type_custom;
		_target_temp_id uuid;
		_source_temp_id uuid;
	begin
		for _item in select array_agg(id), sum(sum), sum(balance)  from limits.limit_sharing_per_period group by period, limit_sharing_id having count(id) > 1 loop
			_target_temp_id := _item.id[1];

			for i in 2..array_length(_item.id, 1) loop
				_source_temp_id := _item.id[i];

				update limits.limit_spending
				set limit_sharing_per_period_id = _target_temp_id
				where limit_sharing_per_period_id = _source_temp_id;

				delete from limits.limit_sharing_per_period where id = _source_temp_id;
			end loop;

			update limits.limit_sharing_per_period
			set sum = _item.sum,
			balance  = _item.balance
			where id = _target_temp_id;

		end loop;
	end;
$$;

drop type limits.limit_sharing_type_custom;

drop procedure limits.remove_duplicates();
drop procedure limits.remove_duplicates(uuid);

alter table limits.limit_sharing
	add constraint limits_limit_sharing_transport_type_limit_uk unique (transport_type, limit_id);

alter table limits.limit_sharing_per_period
	add constraint limits_limit_sharing_per_period_limit_sharing_id_period_uk unique (limit_sharing_id, period);

drop table limits.jv_commit cascade;
drop table limits.jv_commit_property cascade;
drop table limits.jv_global_id cascade;
drop table limits.jv_snapshot cascade;
