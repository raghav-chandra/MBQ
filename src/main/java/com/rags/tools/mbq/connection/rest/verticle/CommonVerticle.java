package com.rags.tools.mbq.connection.rest.verticle;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.queue.QueueType;
import com.rags.tools.mbq.connection.rest.ErrorMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class CommonVerticle extends AbstractVerticle {
    QConfig.ServerConfig getServerConfig(JsonObject config) {
        QueueType queueType = QueueType.valueOf(config.getString("queue.type"));
        QConfig.Builder builder = new QConfig.Builder()
                .setQueueType(queueType)
                .setStatsCollectorClass(config.getString("stats.collector", "com.rags.tools.mbq.stats.collectors.NoOpStatsCollector"));
        switch (queueType) {
            case SINGLE_JVM_INMEMORY:
                break;
            case SINGLE_JVM_RDB:
            case SINGLE_JVM_HAZELCAST:
            case SINGLE_JVM_MONGO_DB:
                builder.setUrl(config.getString("url"))
                        .setUser(config.getString("username"))
                        .setPassword(config.getString("password"))
                        .setDbDriver(config.getString("driver"))
                        .setValidationQuery(config.getString("validationQuery"))
                        .setMaxxConn(Math.max(config.getInteger("maxxConn", 1), 1));
                break;
            default:
                throw new MBQException("Queue Type is not supported");
        }
        return builder.create().getServerConfig();
    }

    Handler<AsyncResult<Object>> resHandler(Message handler, ErrorMessage messagePullFailed) {
        return resHandler -> {
            if (resHandler.succeeded()) {
                handler.reply(resHandler.result());
            } else {
                handler.fail(messagePullFailed.getCode(), messagePullFailed.getMessage() + resHandler.cause().getMessage());
            }
        };
    }
}
