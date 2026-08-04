create table sys_institutions
(
    id                bigint auto_increment comment '机构ID'
        primary key,
    parent_id         bigint null comment '父机构ID(上级机构)',
    institution_code  varchar(30)                        not null comment '机构编码',
    institution_name  varchar(100)                       not null comment '机构名称',
    institution_level tinyint  default 1 null comment '层级: 1-顶级机构,2-子机构,3-孙机构',
    status            tinyint  default 1 null comment '状态: 0-禁用,1-启用',
    created_at        datetime default CURRENT_TIMESTAMP not null,
    updated_at        datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint fk_institution_parent
        foreign key (parent_id) references sys_institutions (id)
            on delete set null
) comment '机构(支持父子层级)' row_format = DYNAMIC;

create table sys_feedbacks
(
    id               bigint auto_increment comment '反馈ID'
        primary key,
    institution_id   bigint                                not null comment '机构ID',
    feedback_type    varchar(30)                           not null comment '反馈类型: question-疑问, suggestion-建议, complaint-投诉, issue-问题报告, other-其他',
    feedback_title   varchar(200)                          not null comment '反馈标题',
    feedback_content text                                  not null comment '反馈内容',
    attachment_url   varchar(500) null comment '附件URL',
    priority         varchar(20) default 'medium' null comment '优先级: low-低, medium-中, high-高, urgent-紧急',
    status           varchar(20) default 'pending' null comment '状态: pending-待处理, processing-处理中, resolved-已解决, closed-已关闭, rejected-已驳回',
    reply_content    text null comment '回复内容',
    replied_by       bigint null comment '回复人ID',
    replied_at       datetime null comment '回复时间',
    feedback_by      bigint null comment '反馈人ID',
    created_at       datetime    default CURRENT_TIMESTAMP not null,
    updated_at       datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    is_deleted       tinyint(1)  default 0                 null comment '软删除',
    constraint fk_if_institution
        foreign key (institution_id) references sys_institutions (id)
            on delete cascade
) comment '机构反馈表' row_format = DYNAMIC;

create index idx_feedback_type
    on sys_feedbacks (feedback_type);

create index idx_institution_id
    on sys_feedbacks (institution_id);

create index idx_is_deleted
    on sys_feedbacks (is_deleted);

create index idx_priority
    on sys_feedbacks (priority);

create index idx_status
    on sys_feedbacks (status);

create table sys_guides
(
    id             bigint auto_increment comment '指南ID'
        primary key,
    institution_id bigint                                not null comment '机构ID',
    guide_title    varchar(200)                          not null comment '指南标题',
    guide_content  text null comment '指南内容(富文本)',
    guide_file_url varchar(500) null comment '指南附件URL(PDF/Word等)',
    guide_type     varchar(30) default 'general' null comment '指南类型: general-通用指南, operation-操作指南, quality-质控指南, other-其他',
    status         varchar(20) default 'active' null comment '状态: active-有效, expired-已过期, archived-已归档',
    published_at   datetime    default CURRENT_TIMESTAMP not null comment '发布时间',
    created_by     bigint null comment '发布人ID',
    created_at     datetime    default CURRENT_TIMESTAMP not null,
    updated_at     datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    is_deleted     tinyint(1)  default 0                 null comment '软删除',
    constraint fk_ig_institution
        foreign key (institution_id) references sys_institutions (id)
            on delete cascade
) comment '机构指南' row_format = DYNAMIC;

create index idx_guide_type
    on sys_guides (guide_type);

create index idx_institution_id
    on sys_guides (institution_id);

create index idx_is_deleted
    on sys_guides (is_deleted);

create index idx_published_at
    on sys_guides (published_at);

create index idx_status
    on sys_guides (status);

create index idx_institution_code
    on sys_institutions (institution_code);

create index idx_parent_id
    on sys_institutions (parent_id);

create index idx_status
    on sys_institutions (status);

create table sys_permission
(
    id              bigint auto_increment
        primary key,
    permission_code varchar(100) not null,
    permission_name varchar(100) not null,
    permission_type int null comment '权限类型：1-接口权限 2-路由权限',
    parent_id       bigint null comment '父权限ID，0表示顶级菜单',
    route_path      varchar(200) null comment '路由路径',
    component_path  varchar(200) null comment '组件路径',
    redirect        varchar(200) null comment '重定向路径',
    icon            varchar(100) null comment '图标',
    sort            int null comment '排序',
    visible         int null comment '是否可见：1-可见 0-隐藏',
    status          int null comment '状态：1-启用 0-禁用',
    created_at      timestamp default CURRENT_TIMESTAMP null,
    updated_at      timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint permission_code
        unique (permission_code)
) comment '权限（1:接口权限 2:路由权限）' row_format = DYNAMIC;

create table sys_role
(
    id          bigint auto_increment
        primary key,
    role_name   varchar(50) not null,
    role_code   varchar(50) not null,
    description varchar(200) null,
    status      int       default 1 null,
    created_at  timestamp default CURRENT_TIMESTAMP null,
    updated_at  timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint role_code
        unique (role_code),
    constraint role_name
        unique (role_name)
) comment '角色' row_format = DYNAMIC;

create table sys_role_permission
(
    role_id       bigint not null,
    permission_id bigint not null,
    created_at    timestamp default CURRENT_TIMESTAMP null,
    updated_at    timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    primary key (role_id, permission_id)
) comment '角色权限关联' row_format = DYNAMIC;

create table sys_user
(
    id             bigint auto_increment
        primary key,
    institution_id bigint null,
    username       varchar(50)  not null,
    password       varchar(100) not null,
    nickname       varchar(50) null,
    email          varchar(100) null,
    phone          varchar(20) null,
    status         int       default 1 null,
    created_at     timestamp default CURRENT_TIMESTAMP null,
    updated_at     timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint username
        unique (username),
    constraint sys_user_sys_institutions_id_fk
        foreign key (institution_id) references sys_institutions (id)
) comment '用户' row_format = DYNAMIC;

create table sys_user_role
(
    user_id    bigint not null,
    role_id    bigint not null,
    created_at timestamp default CURRENT_TIMESTAMP null,
    updated_at timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    primary key (user_id, role_id)
) comment '用户角色关联' row_format = DYNAMIC;

create table sys_job
(
    id            bigint auto_increment primary key,
    job_name      varchar(100)                            not null comment '任务名称',
    job_group     varchar(50)  default 'DEFAULT'           null comment '任务分组',
    invoke_target varchar(255)                            not null comment '调用目标字符串（类名#方法名）',
    cron_expression varchar(50)                            not null comment 'cron执行表达式',
    misfire_policy  tinyint      default 1               null comment '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
    concurrent      tinyint      default 1               null comment '是否并发执行（0允许 1禁止）',
    status          tinyint      default 1               null comment '状态（0停用 1运行）',
    create_by       varchar(64)  default ''                null comment '创建者',
    create_time     timestamp    default CURRENT_TIMESTAMP null comment '创建时间',
    update_by       varchar(64)  default ''                null comment '更新者',
    update_time     timestamp    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    remark          varchar(500) default ''                null comment '备注信息',
    constraint job_name_group unique (job_name, job_group)
) comment '定时任务' row_format = DYNAMIC;

create table sys_job_log
(
    id           bigint auto_increment primary key,
    job_name     varchar(100)                          not null comment '任务名称',
    job_group    varchar(50)  default 'DEFAULT'         null comment '任务分组',
    invoke_target varchar(255)                         not null comment '调用目标字符串',
    job_message  varchar(500) default ''                null comment '执行信息',
    status       tinyint      default 0               null comment '执行状态（0失败 1成功）',
    exception_info text                                 null comment '异常信息',
    create_time  timestamp    default CURRENT_TIMESTAMP null comment '执行时间'
) comment '定时任务日志' row_format = DYNAMIC;
