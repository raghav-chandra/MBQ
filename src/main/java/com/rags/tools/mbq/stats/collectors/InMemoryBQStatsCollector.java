package com.rags.tools.mbq.stats.collectors;

import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.queue.IdSeqKey;
import com.rags.tools.mbq.stats.MBQStats;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class InMemoryBQStatsCollector implements MBQStatsCollector {

    private static final int QUEUE_SIZE = 100000;
    private final Queue<StatsItem> statsQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);

    private final MBQStats stats = new MBQStats();

    public InMemoryBQStatsCollector() {
        new Timer("StatsBQConsumer-Main").schedule(new TimerTask() {
            @Override
            public void run() {
                while (true) {
                    consumeStats(statsQueue.poll());
                    /*if (statsQueue.size() >= QUEUE_SIZE / 2) {
                        Thread thread = new Thread(() -> {
                            while (statsQueue.size() >= QUEUE_SIZE / 2) {
                                consumeStats(statsQueue.poll());
                            }
                        });
                        thread.setName("StatsBQConsumer-Overloaded");
                        thread.start();
                    }*/
                }
            }
        }, 1000);
    }

    private void consumeStats(StatsItem item) {
        if (item != null) {
            switch (item.type) {
                case REGISTER_CLIENT:
                    stats.addClient(item.client);
                    break;
                case DEREGISTER_CLIENT:
                    stats.removeClient(item.client);
                    break;
                case PENDING:
                    stats.addPendingItemStats(item.client, item.queueName, item.pending);
                    break;
                case PROCESSING:
                    stats.addClientProcessingStats(item.client, item.idSeqKeys);
                    break;
                case COMPLETED:
                    stats.addClientCompletedStats(item.client, item.idSeqKeys);
                    break;
                case ROLLED_BACK:
                    stats.addClientRollbackStats(item.client, item.idSeqKeys);
                    break;
                case ERRORED:
                    stats.addClientErrorStats(item.client, item.idSeqKeys);
                    break;
            }
        }
    }

    @Override
    public void collectConnectedClients(Client client) {
        this.statsQueue.add(new StatsItem(StatsType.REGISTER_CLIENT, client));
    }

    @Override
    public void collectDisconnectedClients(Client client) {
        this.statsQueue.add(new StatsItem(StatsType.DEREGISTER_CLIENT, client));
    }

    @Override
    public void collectPendingStats(Client client, String queueName, int noOfItems) {
        this.statsQueue.add(new StatsItem(StatsType.PENDING, client, queueName, noOfItems));
    }

    @Override
    public void resetStats() {
        this.stats.reset();
    }

    @Override
    public void collectClientProcessingStats(Client client, List<IdSeqKey> idSeqKeys) {
        if (!idSeqKeys.isEmpty()) {
            this.statsQueue.add(new StatsItem(StatsType.PROCESSING, client, idSeqKeys));
        }
    }

    @Override
    public void collectClientCompletedStats(Client client, List<IdSeqKey> idSeqKeys) {
        if (!idSeqKeys.isEmpty()) {
            Map<QueueStatus, StatsType> statusMap = Map.of(QueueStatus.PENDING, StatsType.ROLLED_BACK, QueueStatus.COMPLETED, StatsType.COMPLETED, QueueStatus.ERROR, StatsType.ERRORED);
            Map<StatsType, List<IdSeqKey>> map = Map.of(StatsType.ROLLED_BACK, new ArrayList<>(), StatsType.COMPLETED, new ArrayList<>(), StatsType.ERRORED, new ArrayList<>());
            for (IdSeqKey idSeqKey : idSeqKeys) {
                map.get(statusMap.get(idSeqKey.getStatus())).add(idSeqKey);
                if (idSeqKey.getStatus() == QueueStatus.ERROR) {
                    map.get(StatsType.ROLLED_BACK).add(idSeqKey);
                }
            }
            List<StatsItem> statItems = new LinkedList<>();
            map.forEach((statsType, items) -> statItems.add(new StatsItem(statsType, client, items)));
            this.statsQueue.addAll(statItems);
        }
    }

    @Override
    public void collectClientRollbackStats(Client client, List<IdSeqKey> idSeqKeys) {
        if (!idSeqKeys.isEmpty()) {
            this.statsQueue.add(new StatsItem(StatsType.ROLLED_BACK, client, idSeqKeys));
        }
    }

    @Override
    public void markOldest(String queueName, IdSeqKey item) {
        this.stats.markOldest(queueName, item);
    }

    @Override
    public MBQStats getCollectedStats() {
        return this.stats;
    }

    static class StatsItem implements Serializable {
        Client client;
        List<IdSeqKey> idSeqKeys;
        StatsType type;
        String queueName;
        int pending;

        public StatsItem(StatsType type, Client client) {
            this.client = client;
            this.type = type;
        }

        public StatsItem(StatsType type, Client client, List<IdSeqKey> idSeqKeys) {
            this.client = client;
            this.type = type;
            this.idSeqKeys = idSeqKeys;
        }

        public StatsItem(StatsType type, Client client, String queueName, int pending) {
            this.type = type;
            this.client = client;
            this.queueName = queueName;
            this.pending = pending;
        }
    }


    enum StatsType {
        REGISTER_CLIENT, DEREGISTER_CLIENT, PENDING, PROCESSING, COMPLETED, ROLLED_BACK, ERRORED
    }
}