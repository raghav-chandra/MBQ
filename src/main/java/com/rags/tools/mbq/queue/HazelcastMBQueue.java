package com.rags.tools.mbq.queue;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author ragha
 * @since 29-12-2019
 */
public class HazelcastMBQueue extends AbstractMBQueue {

    private final HazelcastInstance instance;

    //TODO: Use Entry Processor for Atomicity of operations
    public HazelcastMBQueue(QConfig.ServerConfig serverConfig) {
        Config config = new Config();
        this.instance = Hazelcast.newHazelcastInstance(config);
    }

    @Override
    public MBQMessage get(String queueName, String id) {
        IMap<String, MBQMessage> iMap = this.instance.getMap(queueName);
        if (iMap != null) {
            return iMap.get(id);
        }
        return null;
    }

    @Override
    public List<MBQMessage> get(String queueName, String seqKey, List<QueueStatus> status) {
        IMap<String, MBQMessage> iMap = this.instance.getMap(queueName);
        if (iMap != null) {
            Predicate statusInQuery = Predicates.in("status", status.parallelStream()
                    .map(st -> (Comparable<QueueStatus>) queueStatus -> st == queueStatus ? 0 : 1).toArray(Comparable[]::new));
            Predicate seqKeyQuery = Predicates.equal("seqKey", seqKey);
            return new LinkedList<>(iMap.values(Predicates.and(statusInQuery, seqKeyQuery)));
        }
        return null;
    }

    @Override
    public Map<String, List<String>> getAllPendingIds() {
        Map<String, MapConfig> mapList = instance.getConfig().getMapConfigs();

        Map<String, List<String>> map = new ConcurrentHashMap<>();
        mapList.keySet().parallelStream().forEach(qName -> {
            IMap<String, MBQMessage> iMap = this.instance.getMap(qName);
            if (iMap != null) {
                List<MBQMessage> allMessages = new LinkedList<>(iMap.values(Predicates.equal("status", QueueStatus.PENDING)));
                allMessages.sort((m1, m2) -> Math.toIntExact(m1.getCreatedTimeStamp() - m2.getCreatedTimeStamp()));
                map.put(qName, allMessages.stream().map(MBQMessage::getId).collect(Collectors.toList()));
            }
        });

        return map;
    }

    @Override
    public List<MBQMessage> pull(String queueName, List<String> ids) {
        IMap<String, MBQMessage> iMap = this.instance.getMap(queueName);
        if (iMap != null) {
            return new LinkedList<>(iMap.getAll(new HashSet<>(ids)).values());
        }
        return null;
    }

    @Override
    public List<MBQMessage> push(String queueName, List<QMessage> messages) {
        IMap<String, MBQMessage> iMap = this.instance.getMap(queueName);
        if (iMap != null) {
            List<MBQMessage> mbqMessages = createMessages(messages, queueName);
            iMap.putAll(createMap(mbqMessages));
            return mbqMessages;
        }
        return null;
    }

    private Map<String, MBQMessage> createMap(Collection<MBQMessage> mbqMessages) {
        return mbqMessages.parallelStream().reduce(new HashMap<>(), (acc, msg) -> {
            acc.put(msg.getId(), msg);
            return acc;
        }, (map1, map2) -> map1);
    }

    @Override
    public boolean updateStatus(String queueName, List<String> ids, QueueStatus status) {
        IMap<String, MBQMessage> iMap = this.instance.getMap(queueName);
        if (iMap != null) {
            Collection<MBQMessage> allMessages = iMap.getAll(new HashSet<>(ids)).values();

            allMessages.parallelStream().forEach(msg -> msg.updateStatus(status));

            iMap.putAll(createMap(allMessages));
            return true;
        }
        return false;
    }

    @Override
    public void updateStatus(QueueStatus prevStatus, QueueStatus newStatus) {
        Map<String, MapConfig> mapList = instance.getConfig().getMapConfigs();

        mapList.keySet().parallelStream().forEach(qName -> {
            IMap<String, MBQMessage> iMap = this.instance.getMap(qName);
            if (iMap != null) {
                Collection<MBQMessage> allMessages = iMap.values(Predicates.equal("status", prevStatus));
                allMessages.parallelStream().forEach(msg -> msg.updateStatus(newStatus));
                iMap.putAll(createMap(allMessages));
            }
        });
    }
}
