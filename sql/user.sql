-- 用户表
create table user
(
    id            bigint auto_increment comment '编号'
        primary key,
    user_account  varchar(255)                       not null comment '账户',
    user_password varchar(255)                       not null comment '密码',
    user_name     varchar(255)                       not null comment '用户名',
    user_sex      tinyint  default 2                 null comment '性别 0-男 1-女 2-未知',
    user_age      tinyint                            null comment '年龄',
    email         varchar(255)                       null comment '邮箱',
    phone         varchar(255)                       null comment '手机号',
    id_delete     tinyint  default 0                 null comment '是否删除 0-未删除 1-删除',
    close_static  tinyint  default 0                 null comment '是否封禁(0-正常 1-封禁)',
    user_role     tinyint  default 0                 null comment '用户身份 0-普通用户 1-会员 2-管理员 3-超级管理员(唯一) ',
    user_avatar   varchar(255)                       null comment '用户头像',
    user_tags     varchar(1024)                      null comment '用户标签',
    create_time   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '修改时间'
)
    comment '用户表';