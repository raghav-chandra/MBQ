package com.rags.tools.mbq.queue;

import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;

import java.util.List;

public class DBMBQueue extends AbstractMBQueue {

    private static final String GET_BY_QUEUE_AND_ID = "select * form MBQueueMessage where Id=:id";
    private static final String GET_BY_QUEUE_SEQ_AND_STATUS = "select * form MBQueueMessage where QueueName=:queue and Sequence=:seq and Status in (:status)";

    private static final String GET_BY_QUEUE_AND_IDS = "select * form MBQueueMessage where QueueName=:queue Id in (:ids)";

    @Override
    public MBQMessage get(String queueName, String id) {
        return null;
    }

    @Override
    public List<MBQMessage> get(String queueName, String seqKey, List<QueueStatus> status) {
        return null;
    }

    @Override
    public List<MBQMessage> pull(String queueName, List<String> ids) {
        return null;
    }

    @Override
    public List<MBQMessage> push(String queueName, List<QMessage> messages) {
        return null;
    }

    @Override
    public boolean updateStatus(String queueName, List<String> ids, QueueStatus status) {
        return false;
    }
}
