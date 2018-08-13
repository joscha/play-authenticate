# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table linked_account (
  id                            bigint auto_increment not null,
  user_id                       bigint,
  provider_user_id              varchar(255),
  provider_key                  varchar(255),
  constraint pk_linked_account primary key (id)
);

create table users (
  id                            bigint auto_increment not null,
  email                         varchar(255),
  name                          varchar(255),
  active                        tinyint(1) default 0,
  email_validated               tinyint(1) default 0,
  constraint pk_users primary key (id)
);

create table cookie_series (
  id                            bigint auto_increment not null,
  series                        varchar(100),
  token                         varchar(100)
)

alter table linked_account add constraint fk_linked_account_user_id foreign key (user_id) references users (id) on delete restrict on update restrict;
create index ix_linked_account_user_id on linked_account (user_id);

create index ix_cookie_series_series on cookie_series (series);

# --- !Downs

alter table linked_account drop foreign key fk_linked_account_user_id;
drop index ix_linked_account_user_id on linked_account;

drop table if exists linked_account;

drop table if exists users;

drop table if exists cookie_series;

