package com.rags.tools.mbq.qserver;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;
import com.rags.tools.mbq.server.rest.messagecodec.CommitRollbackRequest;
import com.rags.tools.mbq.server.rest.messagecodec.PushRequest;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author ragha
 * @since 29-12-2019
 */
public class MBQueueServerProxy implements MBQueueServer {

    private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

    private final String baseUrl;

    public MBQueueServerProxy(QConfig.ServerConfig config) {
        this.baseUrl = config.getUrl();
    }

    private String post(String api, Object data) {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(Json.encode(data)))
                .uri(URI.create(baseUrl + api))
                .setHeader("User-Agent", "MBQueue Client v1.0.0") // add request header
                .header("Content-Type", "application/json")
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new MBQException("Exception occurred while executing request at MBQ Server", new MBQException(response.body()));
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new MBQException("Exception occurred while connecting to the MBQ Server", e);
        }
    }

    @Override
    public Client registerClient(Client client) {
        String resp = post("mbq/registerClient", client);
        return Json.decodeValue(Buffer.buffer(resp), Client.class);
    }

    @Override
    public List<MBQMessage> pull(Client client) {
        String resp = post("mbq/pull", client);
        return Json.decodeValue(Buffer.buffer(resp), List.class);
    }

    public boolean commit(Client client, List<String> ids) {
        String resp = post("mbq/commit", new CommitRollbackRequest(client, ids));
        return Json.decodeValue(Buffer.buffer(resp), Boolean.class);
    }

    @Override
    public boolean rollback(Client client, List<String> ids) {
        String resp = post("mbq/rollback", new CommitRollbackRequest(client, ids));
        return Json.decodeValue(Buffer.buffer(resp), Boolean.class);
    }

    public List<MBQMessage> push(Client client, List<QMessage> messages) {
        String resp = post("mbq/push", new PushRequest(client, messages));
        return Json.decodeValue(Buffer.buffer(resp), List.class);
    }

    public MBQMessage push(Client client, QMessage message) {
        List<MBQMessage> messages = push(client, Collections.singletonList(message));
        return messages != null && !messages.isEmpty() ? messages.get(0) : null;
    }

    public String ping(Client client) {
        return post("mbq/ping", client);
    }
}