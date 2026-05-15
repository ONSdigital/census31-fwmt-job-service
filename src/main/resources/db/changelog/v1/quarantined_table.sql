create table quarantined_message
(
    id                 uuid not null
        constraint quarantined_message_pkey
            primary key,
    action_instruction integer,
    address_level      varchar(255),
    address_type       varchar(255),
    case_id            varchar(255),
    headers            varchar(255),
    message_payload    bytea,
    nc                 boolean,
    queue              varchar(255),
    routing_key        varchar(255),
    skipped_timestamp  timestamp with time zone,
    survey_name        varchar(255)
);