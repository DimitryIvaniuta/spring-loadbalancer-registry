-- 1) add tenant & group (backfill existing rows with defaults)
alter table lb_instance
    add column if not exists tenant_id varchar(64) not null default 'default',
    add column if not exists service_group varchar(64) not null default 'default';

-- 2) drop old global uniqueness (if you had it)
drop index if exists ux_lb_instance_address;

-- 3) new uniqueness per tenant+group
create unique index if not exists ux_lb_instance_tenant_group_address
    on lb_instance(tenant_id, service_group, address);

-- 4) LRU index per tenant+group
create index if not exists ix_lb_instance_tenant_group_lru
    on lb_instance(tenant_id, service_group, last_used_at asc nulls first, id asc);
