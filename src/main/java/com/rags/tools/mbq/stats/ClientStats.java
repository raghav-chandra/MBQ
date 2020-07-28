package com.rags.tools.mbq.stats;

import com.rags.tools.mbq.QueueStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class ClientStats {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientStats.class);

    private final String id;
    private final List<String> processing;
    private long completed;
    private long markedError;
    private long markedHeld;
    private long pushed;

    public ClientStats(String id) {
        this.id = id;
        this.processing = new LinkedList<>();
    }

    public String getId() {
        return id;
    }

    public List<String> getProcessing() {
        return processing;
    }

    public long getCompleted() {
        return completed;
    }

    public long getMarkedError() {
        return markedError;
    }

    public long getMarkedHeld() {
        return markedHeld;
    }

    public void addProcessed(QueueStatus status, List<String> items) {
        if (status == QueueStatus.COMPLETED) {
            this.processing.removeAll(items);
            this.completed += items.size();
        } else if (status == QueueStatus.ERROR) {
            this.markedError += items.size();
        } else if (status == QueueStatus.HELD) {
            this.markedHeld += items.size();
        } else if (status == QueueStatus.PROCESSING) {
            this.processing.addAll(items);
        }
        LOGGER.debug("Client Id : {}. Stats -> Processing : {}, Completed :{}, Errored :{}, Held : {}", id, processing.size(), completed, markedError, markedHeld);
    }

    public void addPending(int noOfPending) {
        this.pushed += noOfPending;
    }

    public void removeProcessing(List<String> items) {
        this.processing.removeAll(items);
        LOGGER.debug("Client Id : {}. Stats -> Processing : {}, Completed :{}, Errored :{}, Held : {}", id, processing.size(), completed, markedError, markedHeld);
    }
}
