drop table if exists urls;

create table urls (
  id                            bigint generated by default as identity not null,
  name                          varchar(255),
  created_at                    timestamp not null
);