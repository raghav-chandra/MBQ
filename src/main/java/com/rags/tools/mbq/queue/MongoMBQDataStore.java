package com.rags.tools.mbq.queue;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;

import java.util.List;
import java.util.Map;

public class MongoMBQDataStore extends AbstractMBQDataStore {

    public MongoMBQDataStore(QConfig.ServerConfig config) {

    }

    @Override
    public List<MBQMessage> get(String queueName, List<String> ids) {
        return null;
    }

    @Override
    public List<MBQMessage> get(String queueName, String seqKey, List<QueueStatus> status) {
        return null;
    }

    @Override
    public Map<String, List<IdSeqKey>> getAllPendingIds() {
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

    @Override
    public void updateStatus(QueueStatus prevStatus, QueueStatus newStatus) {

    }
}