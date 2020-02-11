create table MBQueueMessage (
    Id              varchar(100) primary key not null,
    QueueName       varchar(30) not null,
    Sequence        varchar(30) not null,
    Status          varchar(10) not null,
    Data            Text not null,
    ScheduledAt     datetime null,
    BlockerKey      varchar(50) null,
    CreatedTime     datetime not null,
    UpdatedTime     datetime null
)
;
create index idx_QName_Status on MBQueueMessage(Status, QueueName)
;