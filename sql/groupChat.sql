use `future-system`;
-- 群聊表
create table if not exists `group_chat`(
    `id` bigint primary key auto_increment comment 'id',
    `name` varchar(255) not null comment '群聊名',
    `user_id` bigint not null comment '群主',
    `group_chat_txt` text comment '群聊介绍',
    `max_people_num` int default 30 not null comment '最大人数',
    `current_people_num` int not null comment '当前人数',
    `status` tinyint default 0 not null comment '群聊状态 (0-正常 1-封禁)',
    `create_id` bigint not null comment '创建人',
    `create_time` datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `update_id` bigint not null comment '修改人',
    `update_time` datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间',
    `is_delete` tinyint default 0 not null comment '是否删除(0-未删除 1-删除)'
) comment '群聊表';


-- 群聊用户关系表
create table if not exists `user_group_chat_info`(
    `id` bigint primary key auto_increment comment 'id',
    `user_id` bigint not null comment '用户id',
    `group_chat_id` bigint not null comment '群聊id',
    `group_chat_name` varchar(255) comment '群聊用户名',
    `authority` tinyint default 0 comment '权限(0-普通 1-管理员 2-群主)',
    `status` tinyint default 0 comment '状态(0-正常 1-禁言)',
    `create_time` datetime default CURRENT_TIMESTAMP not null comment '加入时间',
    `update_time` datetime default CURRENT_TIMESTAMP not null comment '修改时间',
    `id_delete` tinyint default 0 not null comment '是否删除(0-未删除 1-删除)'
)