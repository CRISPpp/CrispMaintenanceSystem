CREATE TABLE user(
                     id BIGINT PRIMARY KEY,
                     is_deleted INT DEFAULT 0 COMMENT '0表示未删除，1表示删除',
                     create_time DATETIME,
                     update_time DATETIME,
                     version BIGINT DEFAULT 0,
                     icon VARCHAR(512),
                     username VARCHAR(512) NOT NULL,
                     phone VARCHAR(512) UNIQUE,
                     mail VARCHAR(512) UNIQUE,
                     password VARCHAR(512)
);