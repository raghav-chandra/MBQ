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
        private String dbDriver;
        private int maxxConn;
        private String validationQuery;

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

        public Builder setDbDriver(String dbDriver) {
            this.dbDriver = dbDriver;
            return this;
        }

        public Builder setMaxxConn(int maxxConn) {
            this.maxxConn = maxxConn;
            return this;
        }

        public Builder setValidationQuery(String validationQuery) {
            this.validationQuery = validationQuery;
            return this;
        }

        public Builder clone() {
            return new Builder().setBatch(batch).setWorkerName(workerName).setPollingQueue(pollingQueue)
                    .setDbDriver(dbDriver).setUrl(url).setUser(user).setPassword(password).setQueueType(queueType)
                    .setValidationQuery(validationQuery).setMaxxConn(maxxConn);
        }

        public QConfig create() {
            ClientConfig clientConfig = new ClientConfig(pollingQueue, workerName, batch);
            ServerConfig serverConfig = new ServerConfig(queueType, url, user, password, dbDriver, validationQuery, maxxConn);
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
        private final String dbDriver;
        private final String validationQuery;
        private final String url;
        private final String user;
        private final String password;
        private final QueueType queueType;
        private final int maxConn;

        private ServerConfig(QueueType queueType, String url, String user, String password, String dbDriver, String validationQuery, int maxConn) {
            this.url = url;
            this.user = user;
            this.password = password;
            this.queueType = queueType;
            this.dbDriver = dbDriver;
            this.validationQuery = validationQuery;
            this.maxConn = maxConn <= 0 ? 1 : maxConn;
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

        public String getDbDriver() {
            return dbDriver;
        }

        public String getValidationQuery() {
            return validationQuery;
        }

        public int getMaxConn() {
            return maxConn;
        }
    }
}