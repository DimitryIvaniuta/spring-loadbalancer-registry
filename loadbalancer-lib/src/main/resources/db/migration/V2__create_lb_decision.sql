create table if not exists lb_decision (
    id bigserial primary key,
    request_id varchar(64) not null,
    strategy varchar(64) not null,
    candidates int not null,
    chosen_address varchar(512),
    created_at timestamptz not null default now()
);

create index if not exists ix_lb_decision_created_at on lb_decision(created_at desc);
create index if not exists ix_lb_decision_request_id on lb_decision(request_id);
