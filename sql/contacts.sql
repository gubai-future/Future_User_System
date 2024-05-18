use `future-system`;

create table if not exists `contacts`(
    id bigint primary key auto_increment comment 'id',
    user_id bigint not null comment '用户id',
    contacts_id bigint not null comment '联系人id',
    remarks_name varchar(255) null comment '备注名字',
    contacts_identity tinyint not null default 0 comment '联系人身份',
    grouping_id tinyint comment '分组',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间'
) comment '联系人表';


create table if not exists `contacts_auditing`(
    id bigint primary key auto_increment comment 'id',
    applicant_id bigint not null comment '申请人',
    user_id bigint not null comment '用户id',
    auditing_result tinyint not null default 0 comment '审核结果（0-待审核 1-同意 2-不同意 3-过时）',
    auditing_view varchar(1024) null comment '审核意见',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间'
) comment '联系人审核表';


create table if not exists `contacts_grouping`(
    id bigint primary key auto_increment comment 'id',
    name varchar(255) not null comment '分组名称',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    create_user bigint not null comment '创建者',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间',
    update_user bigint not null comment '修改者'
) comment '联系人分组表';