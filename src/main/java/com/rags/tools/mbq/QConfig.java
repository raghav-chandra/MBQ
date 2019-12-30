package com.rags.tools.mbq;

public class QConfig {
    private String pollingQueue;
    private String workerName;
    private int batch;
    private String qServerHost;
    private int qServerPort;
    private boolean isTest;

    public QConfig(String qServerHost, int qServerPort, String pollingQueue, String workerName, int batch, boolean isTest) {
        this.pollingQueue = pollingQueue;
        this.workerName = workerName;
        this.batch = batch;
        this.qServerHost = qServerHost;
        this.qServerPort = qServerPort;
        this.isTest = isTest;
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

    public boolean isTest() {
        return isTest;
    }
}
