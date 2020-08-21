package com.rags.tools.mbq.client;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

public class Client {

    private static final int DEFAULT_BATCH = 5;

    private final String id;
    private final String name;
    private final String queueName;
    private final int batch;
    private String host;

    private String heartBeatId;

    public Client() {
        this(null, null, 1);
    }

    public Client(String name, String queueName, int batch) {
        this(null, name, queueName, batch, null);
    }

    public Client(String id, String name, String queueName, int batch, String host) {
        this.id = id;
        this.name = name;
        this.queueName = queueName;
        this.batch = batch;
        this.host = host;
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

    public String getHeartBeatId() {
        return heartBeatId;
    }

    public String getHost() {
        return host;
    }

    public Client setHost(String host) {
        this.host = host;
        return this;
    }

    public void setHeartBeatId(String heartBeatId) {
        this.heartBeatId = heartBeatId;
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

    @JsonIgnore
    public boolean isInValid() {
        return id == null || name == null || queueName == null || batch <= 0;
    }

    @JsonIgnore
    public boolean isInValidForRegistration() {
        return name == null || queueName == null || batch <= 0;
    }
}
