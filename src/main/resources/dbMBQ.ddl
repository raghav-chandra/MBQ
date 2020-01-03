create table MBQueueMessage (
    Id              varchar(60) primary key not null,
    QueueName       varchar(20) not null,
    Sequence        varchar(20) not null,
    Status          varchar(10) not null,
    Data            Text not null,
    CreatedTime     datetime not null,
    UpdatedTime     datetime null
) LOCK_TABLE_ROWS