create table if not exists lb_cursor (
    id smallint primary key,
    next_index bigint not null
);

-- single row cursor, id = 1
insert into lb_cursor (id, next_index) values (1, 0)
    on conflict (id) do nothing;
