create or replace function change_trigger() RETURNS trigger AS $$
    begin
        if      TG_OP = 'INSERT'
        then
            insert into corporate.t_history (table_name, schema_name, operation, new_val)
                    values (TG_RELNAME, TG_TABLE_SCHEMA, TG_OP, row_to_json(new));
            return new;
        elsif   TG_OP = 'UPDATE'
        then
            insert into corporate.t_history (table_name, schema_name, operation, new_val, old_val)
                    values (TG_RELNAME, TG_TABLE_SCHEMA, TG_OP,
                            row_to_json(new), row_to_json(OLD));
            return new;
        elsif   TG_OP = 'DELETE'
        then
            insert into corporate.t_history (table_name, schema_name, operation, old_val)
                    values (TG_RELNAME, TG_TABLE_SCHEMA, TG_OP, row_to_json(OLD));
            return OLD;
        end if;
    end;

$$ LANGUAGE 'plpgsql' SECURITY DEFINER;

create trigger t before insert or update or delete on corporate.employee
for each row EXECUTE procedure change_trigger();
