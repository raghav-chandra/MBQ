package com.rags.tools.mbq.client;

import java.util.Objects;

public class Client {

    private static final int DEFAULT_BATCH = 5;

    private String id;
    private String name;
    private String queueName;
    private int batch;

    public Client(String id) {
        this(id, null, null, 10);
    }

    public Client(String name, String queueName, int batch) {
        this.name = name;
        this.queueName = queueName;
        this.batch = batch;
    }


    public Client(String id, String name, String queueName, int batch) {
        this.id = id;
        this.name = name;
        this.queueName = queueName;
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

    public int getBatch() {
        return batch <= 0 ? DEFAULT_BATCH : batch;
    }

    @Override
    public String toString() {
        return "id : " + id + ", name : " + name + ", queueName : " + queueName + ", batch=" + batch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Client)) return false;
        Client client = (Client) o;
        return Objects.equals(id, client.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public boolean isValid() {
        return id != null && name != null && queueName != null && batch > 0;
    }
}
