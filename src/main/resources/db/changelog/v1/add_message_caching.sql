CREATE TABLE message_cache (
    case_id varchar(40) PRIMARY KEY,
    cached_type varchar(40),
    message varchar(4000)
);