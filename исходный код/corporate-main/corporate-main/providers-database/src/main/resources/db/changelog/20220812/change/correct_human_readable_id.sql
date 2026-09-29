update corporate.employee
set humanreadableid = concat(split_part(humanreadableid, '-', 1), '-', to_char(split_part(humanreadableid, '-', 2)::int, 'FM0000'), '-', to_char(split_part(humanreadableid, '-', 3)::int, 'FM00000000'));

update corporate.department
set humanreadableid = concat(split_part(humanreadableid, '-', 1), '-', to_char(split_part(humanreadableid, '-', 2)::int, 'FM0000'), '-', to_char(split_part(humanreadableid, '-', 3)::int, 'FM00000000'));

update corporate.position
set humanreadableid = concat(split_part(humanreadableid, '-', 1), '-', to_char(split_part(humanreadableid, '-', 2)::int, 'FM0000'), '-', to_char(split_part(humanreadableid, '-', 3)::int, 'FM00000000'));