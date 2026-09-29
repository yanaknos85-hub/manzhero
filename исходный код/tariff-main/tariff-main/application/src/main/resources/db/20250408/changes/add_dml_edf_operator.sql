DO
$do$
    BEGIN
        IF EXISTS(
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'tariff_fleet'
                  and table_name = 'edf_operator'
            ) THEN
            insert into tariff_fleet.edf_operator (id, name, title, active)
            values ('2BM', 'ПФ СКБ Контур', '2BM - «ПФ СКБ Контур»', true),
                   ('2AL', 'Такском ЭДО', '2AL - «Такском ЭДО»', true),
                   ('2AE', 'Калуга-Астрал', '2AE - «Калуга-Астрал»', true),
                   ('2BE', 'Компания Тензор', '2BE - «Компания Тензор»', true),
                   ('2BK', 'КОРУС Консалтинг СНГ', '2BK - «КОРУС Консалтинг СНГ»', true),
                   ('2LT', 'Оператор-ЦРПТ', '2LT - «Оператор-ЦРПТ»', true),
                   ('2MH', 'АО Точка', '2MH - «АО Точка»', true),
                   ('2JF', 'НТС Софт', '2JF - «НТС Софт»', true),
                   ('2CI', 'Электронный экспресс', '2EE - «Электронный экспресс»', true),
                   ('2EE', 'Электронный экспресс', '2CI - «Электронный экспресс»', true),
                   ('2BA', 'НТЦ СТЭК', '2BA - «НТЦ СТЭК»', true),
                   ('2AH', 'Инфотекс Интернет Траст', '2AH - «Инфотекс Интернет Траст»', true),
                   ('2IJ', 'Эдивеб', '2IJ - «Эдивеб»', true),
                   ('2LD', 'Э-КОМ', '2LD - «Э-КОМ»', true),
                   ('2VO', 'Эвотор ОФД', '2VO - «Эвотор ОФД»', true),
                   ('2LH', 'Форапром', '2LH - «Форапром»', true)
            on conflict do nothing;
        END IF;
    END
$do$;