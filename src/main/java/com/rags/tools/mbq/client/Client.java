package com.rags.tools.mbq.client;

public class Client {

    private static final int DEFAULT_BATCH = 5;

    private String id;
    private String name;
    private String queueName;
    private String host;
    private int batch;

    public Client() {
    }

    public Client(String id, String name, String queueName, String host, int batch) {
        this.id = id;
        this.name = name;
        this.queueName = queueName;
        this.host = host;
        this.batch = batch;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getQueueName() {
        return queueName;
    }

    public String getHost() {
        return host;
    }

    public int getBatch() {
        return batch <= 0 ? DEFAULT_BATCH : batch;
    }

    @Override
    public String toString() {
        return "id : " + id + ", name : " + name + ", queueName : " + queueName + ", batch=" + batch;
    }
}
