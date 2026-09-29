do
$$
    begin
        if (select exists(select *
                          from information_schema.tables
                          where table_catalog = 'transport' and table_schema = 'public' and table_name = 'databasechangelog'))
        then
            delete from public.databasechangelog where id in ('approvals/20201029',
                                                              'approvals/20201116',
                                                              'approvals/20201123',
                                                              'approvals/20201125',
                                                              'approvals/20201208',
                                                              'approvals/20201211',
                                                              'approvals/20201217',
                                                              'approvals/20201218',
                                                              'approvals/20201218-2',
                                                              'approvals/20201228',
                                                              'approvals/20210125',
                                                              'approvals/20210128',
                                                              'approvals/20210128-1',
                                                              'approvals/20210219',
                                                              'approvals/20210305',
                                                              'approvals/20210414',
                                                              'approvals/20210519',
                                                              'approvals/20210519-1',
                                                              'approvals/20210531',
                                                              'approvals/20210531-1',
                                                              'approvals/20210531-2',
                                                              'approvals/20210611',
                                                              'approvals/20210824',
                                                              'approvels/20210126-1',
                                                              'approvels/20210126-3',
                                                              'approvels/20210203-1',
                                                              'approvels/20210216-1',
                                                              'authomatic-approvals/20210520');
        end if;
    end;
$$;

