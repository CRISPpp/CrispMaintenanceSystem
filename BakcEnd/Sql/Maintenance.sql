-- 用户，包括普通用户和维护工程师
drop table if exists user;
CREATE TABLE user (
                     id BIGINT PRIMARY KEY,
                     is_deleted INT DEFAULT 0 COMMENT '0表示未删除，1表示删除',
                     create_time DATETIME,
                     update_time DATETIME,
                     version BIGINT DEFAULT 0,
                     icon VARCHAR(512),
                     username VARCHAR(512) NOT NULL,
                     phone VARCHAR(512) UNIQUE,
                     mail VARCHAR(512) UNIQUE,
                     password VARCHAR(512),
                     role int not null comment '1表示普通用户，2表示维护工程师'

) comment '用户表';
-- 用户额外属性
drop table if exists user_attribute;
CREATE TABLE user_attribute (
                                id BIGINT PRIMARY KEY,
                                is_deleted INT DEFAULT 0 COMMENT '0表示未删除，1表示删除',
                                create_time DATETIME,
                                update_time DATETIME,
                                version BIGINT DEFAULT 0,
                                balance decimal(10, 2) default 0.0 comment '余额',
                                user_id BIGINT
) comment '用户属性表';
-- 工程师额外属性
drop table if exists engineer_attribute;
CREATE TABLE engineer_attribute (
                                    id BIGINT PRIMARY KEY,
                                    is_deleted INT DEFAULT 0 COMMENT '0表示未删除，1表示删除',
                                    create_time DATETIME,
                                    update_time DATETIME,
                                    version BIGINT DEFAULT 0,
                                    quality decimal(2, 1) default 0.0 comment '星级, 0 - 5',
                                    balance decimal(10, 2) default 0.0 comment '余额',
                                    user_id BIGINT
) comment '工程师属性表';

-- 地址
-- 详细地址后期可划分为多个字段，省、市等，目前统一成一个字段detail
drop table if exists address;
create table address (
                         id bigint primary key,
                         user_id bigint not null comment '用户id',
                         name varchar(512) comment '报修者真实姓名',
                         sex int default 0 comment '0未知,1男,2女',
                         phone varchar(512) comment '手机号',
                         detail varchar(512) comment '详细地址',
                         latitude double not null comment '纬度',
                         longitude double not null comment '经度',
                         is_default int comment '0非默认地址,1默认地址',
                         is_deleted int default 0 comment '0未删除,1删除',
                         create_time datetime comment '创建时间',
                         update_time datetime comment '修改时间',
                         version bigint default 0 comment '版本号'
) comment '地址表';

-- 工单
-- 不用address_id，防止address表后期发生修改
drop table if exists indent;
create table indent (
                        id bigint primary key,
                        user_id bigint not null comment '报修用户id',
                        engineer_id bigint comment '维修工程师id',
                        name varchar(512) comment '报修者姓名',
                        sex int default 0 comment '报修者性别,0未知,1男,2女',
                        phone varchar(512) comment '报修者手机号',
                        address_detail varchar(512) comment '详细地址',
                        latitude int not null comment '纬度',
                        longitude int not null comment '经度',
                        problem text comment '问题',
                        remark varchar(512) comment '备注',
                        cost decimal(10, 2) comment '上门费',
                        quality decimal(2, 1) comment '评价，取值为0.0~5.0',
                        status int default 1 comment '1待处理,2维修中,3待支付,4待评价,5已完成',
                        is_deleted int default 0 comment '0未删除,1删除',
                        create_time datetime comment '创建时间',
                        update_time datetime comment '修改时间',
                        version bigint default 0 comment '版本号'
) comment '工单表';


-- 工单图片
-- 一个工单可以有多个图片
drop table if exists indent_image;
create table indent_image (
                              id bigint primary key,
                              order_id bigint not null comment '工单id',
                              icon varchar(512) not null comment '图片',
                              type int default 1 comment '1维修前，2维修后',
                              is_deleted int default 0 comment '0未删除,1删除',
                              create_time datetime comment '创建时间',
                              update_time datetime comment '修改时间',
                              version bigint default 0 comment '版本号'
) comment '工单图片表';