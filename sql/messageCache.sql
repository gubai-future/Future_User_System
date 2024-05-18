use `future-system`;
create table `message_cache`(
    id bigint primary key auto_increment comment 'id',
    message text null comment '消息',
    date date not null comment '消息日期'
) comment '消息缓存表';


