package com.rags.tools.mbq.queue.store;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.connection.rest.messagecodec.SearchRequest;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;
import com.rags.tools.mbq.queue.IdSeqKey;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author ragha
 * @since 29-12-2019
 */
public class HazelcastMBQDataStore extends AbstractMBQDataStore {

    private static HazelcastMBQDataStore INSTANCE;

    private final HazelcastInstance instance;

    //TODO: Use Entry Processor for Atomicity of operations
    public HazelcastMBQDataStore(QConfig.ServerConfig serverConfig) {
        //TODO: use url, user password to connect to the instance
        Config config = new Config();
        this.instance = Hazelcast.newHazelcastInstance(config);
    }

    public static synchronized HazelcastMBQDataStore getInstance(QConfig.ServerConfig config) {
        if (INSTANCE == null) {
            INSTANCE = new HazelcastMBQDataStore(config);
        }
        return INSTANCE;
    }

    @Override
    public List<MBQMessage> get(String queueName, List<String> ids) {
        IMap<String, MBQMessage> iMap = this.instance.getMap(queueName);
        if (iMap != null) {
            Map<String, MBQMessage> data = iMap.getAll(new HashSet<>(ids));
            if (data.isEmpty()) {
                iMap.values().forEach(d -> System.out.println(d.getId()));
            }
            return new LinkedList<>(data.values());
        }
        return Collections.emptyList();
    }

    @Override
    public Map<String, List<IdSeqKey>> getAllPendingItems() {
        Map<String, MapConfig> mapList = instance.getConfig().getMapConfigs();

        Map<String, List<IdSeqKey>> map = new ConcurrentHashMap<>();
        mapList.keySet().parallelStream().forEach(qName -> {
            IMap<String, MBQMessage> iMap = this.instance.getMap(qName);
            if (iMap != null) {
                List<MBQMessage> allMessages = new LinkedList<>(iMap.values(Predicates.equal("status", QueueStatus.PENDING)));
                allMessages.sort((m1, m2) -> Math.toIntExact(m1.getCreatedTimeStamp() - m2.getCreatedTimeStamp()));
                map.put(qName, allMessages.stream().map(i -> new IdSeqKey(i.getId(), i.getSeqKey(), i.getStatus(), i.getScheduledAt())).collect(Collectors.toList()));
            }
        });

        return map;
    }

    @Override
    public List<MBQMessage> push(String queueName, List<QMessage> messages) {
        IMap<String, MBQMessage> iMap = this.instance.getMap(queueName);
        if (iMap != null) {
            List<MBQMessage> mbqMessages = createMessages(messages, queueName);
            iMap.putAll(createMap(mbqMessages));
            return mbqMessages;
        }
        return new LinkedList<>();
    }

    private Map<String, MBQMessage> createMap(Collection<MBQMessage> mbqMessages) {
        return mbqMessages.parallelStream().reduce(new HashMap<>(), (acc, msg) -> {
            acc.put(msg.getId(), msg);
            return acc;
        }, (map1, map2) -> map1);
    }

    @Override
    public boolean updateStatus(String queueName, List<String> ids, QueueStatus status) {
        if (ids.isEmpty()) {
            return true;
        }
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

    @Override
    public List<MBQMessage> search(SearchRequest searchRequest) {
        Map<String, MapConfig> mapList = instance.getConfig().getMapConfigs();
        List<String> ids = searchRequest.getIds();
        List<String> queue = searchRequest.getQueues();
        QueueStatus status = searchRequest.getStatus();
        String seq = searchRequest.getSequence();

        return mapList.keySet().parallelStream()
                .filter(q -> !queue.isEmpty() && queue.contains(q))
                .flatMap(qName -> {
                    IMap<String, MBQMessage> iMap = this.instance.getMap(qName);
                    List<MBQMessage> messages = new LinkedList<>();
                    if (iMap != null) {
                        List<Predicate<?,?>> queries = new ArrayList<>();
                        if (ids != null && !ids.isEmpty()) {
                            queries.add(Predicates.in("id", ids.toArray(new String[0])));
                        }

                        if (status != null) {
                            queries.add(Predicates.equal("status", status));
                        }

                        if (seq != null) {
                            queries.add(Predicates.in("seqKey", seq));
                        }

                        messages.addAll(iMap.values(Predicates.and(queries.toArray(new Predicate[0]))));
                    }

                    return messages.stream();
                }).collect(Collectors.toList());
    }
}