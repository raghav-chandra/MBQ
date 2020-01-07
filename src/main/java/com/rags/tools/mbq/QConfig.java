package com.rags.tools.mbq;

import com.rags.tools.mbq.queue.QueueType;

public class QConfig {
    private String pollingQueue;
    private String workerName;
    private int batch;
    private String qServerHost;
    private int qServerPort;
    private QueueType queueType;

    public QConfig(String qServerHost, int qServerPort, String pollingQueue, String workerName, int batch, QueueType queueType) {
        this.pollingQueue = pollingQueue;
        this.workerName = workerName;
        this.batch = batch;
        this.qServerHost = qServerHost;
        this.qServerPort = qServerPort;
        this.queueType = queueType;
    }

    public String getPollingQueue() {
        return pollingQueue;
    }

    public String getWorkerName() {
        return workerName;
    }

    public int getBatch() {
        return batch;
    }

    public String getqServerHost() {
        return qServerHost;
    }

    public int getqServerPort() {
        return qServerPort;
    }

    public QueueType getQueueType() {
        return queueType;
    }
}
