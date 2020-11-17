package com.rags.tools.mbq.connection.rest.verticle;

import com.rags.tools.mbq.connection.rest.ErrorMessage;
import com.rags.tools.mbq.connection.rest.RequestType;
import com.rags.tools.mbq.connection.rest.messagecodec.EventBusRequest;
import com.rags.tools.mbq.connection.rest.messagecodec.SearchRequest;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.queue.store.MBQDataStore;
import com.rags.tools.mbq.queue.store.MBQDataStoreInstance;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;

public class DataStoreVerticle extends CommonVerticle {
    @Override
    public void start() {

        EventBus eventBus = getVertx().eventBus();
        MBQDataStore dataStore = MBQDataStoreInstance.createOrGet(getServerConfig(config()));

        WorkerExecutor workers = getVertx().createSharedWorkerExecutor("DataStoreWorker", 5);

        eventBus.<EventBusRequest<SearchRequest>>consumer(RequestType.LOOKUP_ITEMS.name(), pullHandler -> {
            SearchRequest request = pullHandler.body().getReqObj();
            if (request == null || request.isInValid()) {
                pullHandler.fail(ErrorMessage.INVALID_SEARCH_REQUEST.getCode(), ErrorMessage.INVALID_SEARCH_REQUEST.getMessage());
            } else {
                workers.executeBlocking(workerHandler -> {
                    List<MBQMessage> messages = dataStore.search(request);
                    workerHandler.complete(new JsonArray(messages.stream().map(JsonObject::mapFrom).collect(Collectors.toList())));
                }, resHandler(pullHandler, ErrorMessage.FAILED_SEARCH_REQUEST));
            }
        });

        eventBus.<EventBusRequest<List<String>>>consumer(RequestType.GET_ITEMS.name(), pullHandler -> {
            List<String> ids = pullHandler.body().getReqObj();
            if (ids == null || ids.isEmpty()) {
                pullHandler.fail(ErrorMessage.INVALID_SEARCH_REQUEST.getCode(), ErrorMessage.INVALID_SEARCH_REQUEST.getMessage());
            } else {
                workers.executeBlocking(workerHandler -> {
                    List<MBQMessage> messages = dataStore.get(ids);
                    workerHandler.complete(new JsonArray(messages.stream().map(d -> JsonObject.mapFrom(d).put("message", new String(d.getMessage()))).collect(Collectors.toList())));
                }, resHandler(pullHandler, ErrorMessage.FAILED_SEARCH_REQUEST));
            }
        });
    }

}
