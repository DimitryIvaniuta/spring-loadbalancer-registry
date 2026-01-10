create table if not exists lb_instance (
    id bigserial primary key,
    address varchar(512) not null,
    created_at timestamptz not null default now()
);

create unique index if not exists ux_lb_instance_address on lb_instance(address);
