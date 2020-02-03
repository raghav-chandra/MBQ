create table MBQueueMessage (
    Id              varchar(100) primary key not null,
    QueueName       varchar(20) not null,
    Sequence        varchar(20) not null,
    Status          varchar(10) not null,
    Data            Text not null,
    scheduledAt     datetime null,
    CreatedTime     datetime not null,
    UpdatedTime     datetime null
)
;
create index idx_QName_Status on MBQueueMessage(Status, QueueName)
;