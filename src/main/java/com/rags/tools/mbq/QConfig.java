package com.rags.tools.mbq;

import com.rags.tools.mbq.queue.QueueType;

public class QConfig {
    private ClientConfig clientConfig;
    private ServerConfig serverConfig;

    private QConfig(ClientConfig clientConfig, ServerConfig serverConfig) {
        this.clientConfig = clientConfig;
        this.serverConfig = serverConfig;

    }

    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public static class Builder {
        private String pollingQueue;
        private String workerName;
        private int batch;
        private String url;
        private String user;
        private String password;
        private QueueType queueType;

        public Builder setPollingQueue(String pollingQueue) {
            this.pollingQueue = pollingQueue;
            return this;
        }

        public Builder setWorkerName(String workerName) {
            this.workerName = workerName;
            return this;
        }

        public Builder setBatch(int batch) {
            this.batch = batch;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setUser(String user) {
            this.user = user;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setQueueType(QueueType queueType) {
            this.queueType = queueType;
            return this;
        }

        public QConfig create() {
            ClientConfig clientConfig = new ClientConfig(pollingQueue, workerName, batch);
            ServerConfig serverConfig = new ServerConfig(url, user, password, queueType);
            return new QConfig(clientConfig, serverConfig);
        }
    }


    public static class ClientConfig {
        private final String pollingQueue;
        private final String workerName;
        private final int batch;

        ClientConfig(String pollingQueue, String workerName, int batch) {
            this.pollingQueue = pollingQueue;
            this.workerName = workerName;
            this.batch = batch;
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
    }

    public static class ServerConfig {
        private final String url;
        private final String user;
        private final String password;
        private final QueueType queueType;

        ServerConfig(String url, String user, String password, QueueType queueType) {
            this.url = url;
            this.user = user;
            this.password = password;
            this.queueType = queueType;
        }

        public String getUrl() {
            return url;
        }

        public String getUser() {
            return user;
        }

        public String getPassword() {
            return password;
        }

        public QueueType getQueueType() {
            return queueType;
        }
    }
}