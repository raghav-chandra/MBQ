package com.rags.tools.mbq.stats.collector;

import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.queue.IdSeqKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MBQStatsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MBQStatsService.class);

    private MBQStatsCollector statsCollector;

    public MBQStatsService(String statsCollectorClass) {
        try {
            if (statsCollectorClass != null && !statsCollectorClass.isBlank()) {
                Class<?> aClass = Class.forName(statsCollectorClass);
                this.statsCollector = (MBQStatsCollector) aClass.getConstructor().newInstance();
            } else {
                this.statsCollector = new NoOpStatsCollector();
            }
        } catch (Throwable throwable) {
            LOGGER.warn("Failed while initializing Stats collector. Either class doesn't exist or class doesn't extends com.rags.tools.mbq.stats.collector.MBQStatsCollector. Falling back to No OpsCollector", throwable);
            this.statsCollector = new NoOpStatsCollector();
        }
    }

    public void collectConnectedClients(Client client) {
        handleError(() -> this.statsCollector.collectConnectedClients(client));
    }

    public void collectDisconnectedClients(Client client) {
        handleError(() -> this.statsCollector.collectDisconnectedClients(client));
    }

    public void collectPendingStats(String queueName, int noOfItems) {
        handleError(() -> this.statsCollector.collectPendingStats(queueName, noOfItems));
    }

    public void resetStats() {
        handleError(() -> this.statsCollector.resetStats());
    }

    public void collectClientProcessingStats(Client client, List<IdSeqKey> idSeqKeys) {
        handleError(() -> this.statsCollector.collectClientProcessingStats(client, idSeqKeys));
    }

    public void collectClientCompletedStats(Client client, List<IdSeqKey> idSeqKeys) {
        handleError(() -> this.statsCollector.collectClientCompletedStats(client, idSeqKeys));
    }

    public void collectClientRollbackStats(Client client, List<IdSeqKey> idSeqKeys) {
        handleError(() -> this.statsCollector.collectClientRollbackStats(client, idSeqKeys));
    }

    private void handleError(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            LOGGER.warn("Exception occurred while procecessing stats. Stats will have wrong values.", throwable);
        }
    }
}
