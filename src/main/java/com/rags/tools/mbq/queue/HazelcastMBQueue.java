package com.rags.tools.mbq.queue;

import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;

import java.util.List;
import java.util.Map;

/**
 * @author ragha
 * @since 29-12-2019
 */
public class HazelcastMBQueue extends AbstractMBQueue {
    @Override
    public MBQMessage get(String queueName, String id) {
        return null;
    }

    @Override
    public List<MBQMessage> get(String queueName, String seqKey, List<QueueStatus> status) {
        return null;
    }

    @Override
    public Map<String, List<String>> getAllPendingIds() {
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
