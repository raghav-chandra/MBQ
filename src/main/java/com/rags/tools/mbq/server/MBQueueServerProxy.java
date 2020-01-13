package com.rags.tools.mbq.server;

import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * @author ragha
 * @since 29-12-2019
 */
public class MBQueueServerProxy implements MBQueueServer {

    private static final String Q_END_POINT = "mbQueue";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    public MBQueueServerProxy(QConfig config) {

    }

    private String post(String api) {
        HttpRequest request = HttpRequest.newBuilder()
//                .POST(HttpRequest.BodyPublishers.ofString(Json.encode()))
                .uri(URI.create("https://httpbin.org/post" + api))
                .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                .header("Content-Type", "application/x-www-form-urlencoded")
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
    public Client registerClient(Client config) {
        String resp = post("register");
        return Json.decodeValue(Buffer.buffer(resp), Client.class);
    }

    @Override
    public List<MBQMessage> pull(Client client) {
        String resp = post("pull");
        return Json.decodeValue(Buffer.buffer(resp), List.class);
    }

    public boolean commit(Client client, List<String> ids) {
        String resp = post("commit");
        return Json.decodeValue(Buffer.buffer(resp), Boolean.class);
    }

    @Override
    public boolean rollback(Client client, List<String> ids) {
        String resp = post("rollback");
        return Json.decodeValue(Buffer.buffer(resp), Boolean.class);
    }

    public List<MBQMessage> push(Client client, List<QMessage> messages) {
        String resp = post("pushAll");
        return Json.decodeValue(Buffer.buffer(resp), List.class);
    }

    public MBQMessage push(Client client, QMessage message) {
        String resp = post("push");
        return Json.decodeValue(Buffer.buffer(resp), MBQMessage.class);
    }

    public String ping(Client client) {
        post("heartbeat");
        return null;
    }
}