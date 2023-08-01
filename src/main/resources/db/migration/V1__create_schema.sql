create table link
(
    id         text primary key,
    url        text                      not null,
    visits     bigint      default 0     not null,
    creator    text,
    created_at timestamptz default now() not null,
    updated_at timestamptz default now() not null
);

create index link_created_at_desc_idx on link (created_at desc);