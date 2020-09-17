package com.rags.tools.mbq.stats;

import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.queue.IdSeqKey;
import com.rags.tools.mbq.stats.collectors.MBQStatsCollector;
import com.rags.tools.mbq.stats.collectors.NoOpStatsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MBQStatsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MBQStatsService.class);

    private MBQStatsCollector statsCollector;

    private static MBQStatsService INSTANCE;

    public synchronized static MBQStatsService getInstance(String statsCollectorClass) {
        if (INSTANCE == null) {
            INSTANCE = new MBQStatsService(statsCollectorClass);
        }
        return INSTANCE;
    }

    private MBQStatsService(String statsCollectorClass) {
        try {
            if (statsCollectorClass != null && !statsCollectorClass.isBlank()) {
                Class<?> aClass = Class.forName(statsCollectorClass);
                this.statsCollector = (MBQStatsCollector) aClass.getConstructor().newInstance();
            } else {
                this.statsCollector = new NoOpStatsCollector();
            }
        } catch (Throwable throwable) {
            LOGGER.warn("Failed while initializing Stats collector. Either class doesn't exist or class doesn't implements com.rags.tools.mbq.stats.collector.MBQStatsCollector. Falling back to No OpsCollector", throwable);
            this.statsCollector = new NoOpStatsCollector();
        }
    }

    public void collectConnectedClients(Client client) {
        execute(() -> this.statsCollector.collectConnectedClients(client));
    }

    public void collectDisconnectedClients(Client client) {
        execute(() -> this.statsCollector.collectDisconnectedClients(client));
    }

    public void collectPendingStats(Client client, String queueName, int noOfItems) {
        execute(() -> this.statsCollector.collectPendingStats(client, queueName, noOfItems));
    }

    public void resetStats() {
        execute(() -> this.statsCollector.resetStats());
    }

    public void collectClientProcessingStats(Client client, List<IdSeqKey> idSeqKeys) {
        if (!idSeqKeys.isEmpty()) {
            execute(() -> this.statsCollector.collectClientProcessingStats(client, idSeqKeys));
        }
    }

    public void collectClientCompletedStats(Client client, List<IdSeqKey> idSeqKeys) {
        if (!idSeqKeys.isEmpty()) {
            execute(() -> this.statsCollector.collectClientCompletedStats(client, idSeqKeys));
        }
    }

    public void collectClientRollbackStats(Client client, List<IdSeqKey> idSeqKeys) {
        if (!idSeqKeys.isEmpty()) {
            execute(() -> this.statsCollector.collectClientRollbackStats(client, idSeqKeys));
        }
    }

    public void collectOldestItem(String queueName, IdSeqKey item) {
        execute(() -> this.statsCollector.markOldest(queueName, item));
    }

    public void collectInit(String queue, List<IdSeqKey> allItems) {
        if (!allItems.isEmpty()) {
            execute(() -> this.statsCollector.collectInit(queue, allItems));
        }
    }

    private void execute(Runnable runnable) {
        if (!(this.statsCollector instanceof NoOpStatsCollector)) {
            try {
                new Thread(runnable).start();
            } catch (Throwable throwable) {
                LOGGER.warn("Exception occurred while processing stats. Stats will have wrong values.", throwable);
            }
        }
    }

    public MBQStats getCollectedStats() {
        return statsCollector.getCollectedStats();
    }
}
