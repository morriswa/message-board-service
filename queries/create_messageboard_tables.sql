
create table user_profile (
    id uuid primary key,
    auth_zero_id varchar(100) unique not null,
    display_name varchar(32) unique not null,
    email varchar(100) unique not null,
    role varchar(10) not null
);

create table user_post (
    id bigserial primary key,
    user_id uuid not null,
    community_id bigint not null,
    caption varchar(100) not null,
    description varchar(10000),
    date_created timestamp with time zone not null,
    content_type varchar(20) not null,
    resource_id uuid not null unique
);

create table post_resource(
  id uuid primary key,
  data varchar(500)
);

create table community (
    id bigserial primary key,
    community_ref varchar(32) unique not null,
    display_name varchar(100) not null,
    owner uuid not null,
    date_created timestamp with time zone not null
);

create table community_member (
    id bigserial primary key,
    community_id bigint not null,
    user_id uuid not null,
    moderation_level varchar(20) not null,
    standing varchar(20) not null,
    date_created timestamp with time zone not null,
    date_updated timestamp with time zone not null
);

create table user_ui_profile (
    user_id uuid primary key,
    theme varchar(10)
);

create table post_comment (
    id bigserial primary key,
    user_id uuid not null,
    post_id bigint not null,
    parent_id bigint not null,
    body varchar(1000),
    date_created timestamp with time zone not null
);

create table post_vote (
    id bigserial primary key,
    user_id uuid not null,
    post_id bigint not null,
    vote_value int not null,
    date_created timestamp with time zone not null
);

create table comment_vote (
    id bigserial primary key,
    user_id uuid not null,
    post_id bigint not null,
    comment_id bigint not null,
    vote_value int not null,
    date_created timestamp with time zone not null
);

create table post_session (
    id uuid primary key,
    user_id uuid not null,
    community_id bigint not null,
    resource_id uuid unique not null,
    caption varchar(100),
    description varchar(10000)
);