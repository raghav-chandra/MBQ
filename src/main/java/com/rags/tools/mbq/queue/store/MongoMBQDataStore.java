package com.rags.tools.mbq.queue.store;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.connection.rest.messagecodec.SearchRequest;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;
import com.rags.tools.mbq.queue.IdSeqKey;

import java.util.List;
import java.util.Map;

public class MongoMBQDataStore extends AbstractMBQDataStore {

    private static MongoMBQDataStore INSTANCE;

    public MongoMBQDataStore(QConfig.ServerConfig config) {

    }

    public static synchronized MongoMBQDataStore getInstance(QConfig.ServerConfig config) {
        if (INSTANCE == null) {
            INSTANCE = new MongoMBQDataStore(config);
        }
        return INSTANCE;
    }

    @Override
    public List<MBQMessage> get(String queueName, List<String> ids) {
        return null;
    }

    @Override
    public Map<String, List<IdSeqKey>> getAllPendingItems() {
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

    @Override
    public List<MBQMessage> search(SearchRequest searchRequest) {
        return null;
    }

    @Override
    public List<MBQMessage> get(List<String> ids) {
        return null;
    }
}
