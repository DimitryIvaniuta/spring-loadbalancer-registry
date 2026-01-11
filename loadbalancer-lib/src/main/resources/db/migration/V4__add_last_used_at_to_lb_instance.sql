alter table lb_instance
    add column if not exists last_used_at timestamptz null;

create index if not exists ix_lb_instance_last_used_at
    on lb_instance (last_used_at asc nulls first, id asc);
