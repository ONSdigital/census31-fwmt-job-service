create table gateway_cache
(
    case_id varchar(40) PRIMARY KEY,
    exists_in_fwmt boolean,
    is_delivered boolean,
    preferred_name varchar(255),
    address_1 varchar(255),
    address_2 varchar(255),
    care_code varchar(4000),
    access_info varchar(4000)
--    manager_title varchar(40),
--    manager_surname varchar(255),
--    manager_firstname varchar(255),
--    contact_phone_number varchar(255)
);

create table request_log
(
    id bigserial UNIQUE NOT NULL PRIMARY KEY,
    target_service varchar(255) NOT NULL,
    date_time timestamp
);