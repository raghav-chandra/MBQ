package com.rags.tools.mbq.qserver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rags.tools.mbq.QConfig;
import com.rags.tools.mbq.QueueStatus;
import com.rags.tools.mbq.client.Client;
import com.rags.tools.mbq.exception.MBQException;
import com.rags.tools.mbq.message.MBQMessage;
import com.rags.tools.mbq.message.QMessage;
import com.rags.tools.mbq.connection.rest.messagecodec.CommitRollbackRequest;
import com.rags.tools.mbq.connection.rest.messagecodec.PushRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

/**
 * @author ragha
 * @since 29-12-2019
 */
public class QueueServerProxy implements QueueServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueServer.class);

    private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

    private final String baseUrl;

    public QueueServerProxy(QConfig.ServerConfig config) {
        this.baseUrl = config.getUrl();
    }

    private byte[] post(String api, Object data) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(new ObjectMapper().writeValueAsString(data)))
                    .uri(URI.create(baseUrl + api))
                    .setHeader("User-Agent", "MBQueue Client v1.0.0") // add request header
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new MBQException("Exception occurred while executing request at MBQ Server", new MBQException(new String(response.body())));
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new MBQException("Exception occurred while connecting to the MBQ Server", e);
        }
    }

    @Override
    public Client registerClient(Client client) {
        int maxRetry = 10;
        while (maxRetry >= 0) {
            byte[] resp = this.post("mbq/registerClient", client);
            try {
                RestResponse<Client> clientResponse = new ObjectMapper().readValue(resp, new TypeReference<RestResponse<Client>>() {
                });
                if (clientResponse.isFailed()) {
                    System.out.println("Response : " + clientResponse.getMessage());
                    LOGGER.info("Client [{}] registration failed with message [{}], Retrying again", client, clientResponse.getMessage());
                    Thread.sleep(500);
                    maxRetry--;
                    continue;
                }
                return clientResponse.getData();
            } catch (IOException | InterruptedException e) {
                LOGGER.error("Client registration failed", e);
                maxRetry--;
            }
        }
        throw new MBQException("Couldn't register client. Max Retry reached");
    }

    @Override
    public List<MBQMessage> pull(Client client) {
        byte[] resp = this.post("mbq/pull", client);
        try {
            RestResponse<List<MBQMessage>> clientResponse = new ObjectMapper().readValue(resp, new TypeReference<RestResponse<List<MBQMessage>>>() {
            });
            validateResponse(clientResponse);
            return clientResponse.getData();
        } catch (IOException e) {
            throw new MBQException("Failed in parsing response from Server", e);
        }
    }

    private void validateResponse(RestResponse clientResponse) {
        if (!clientResponse.isSuccess()) {
            throw new MBQException(clientResponse.getMessage());
        }
    }

    public boolean commit(Client client, Map<QueueStatus, List<String>> processedIds, Map<String, List<QMessage>> messagesToBePushed) {
        byte[] resp = this.post("mbq/commit", new CommitRollbackRequest(client, processedIds, messagesToBePushed));
        try {
            RestResponse<Boolean> clientResponse = new ObjectMapper().readValue(resp, new TypeReference<RestResponse<Boolean>>() {
            });
            validateResponse(clientResponse);
            return clientResponse.getData();
        } catch (IOException e) {
            throw new MBQException("Failed in parsing response from Server", e);
        }
    }

    @Override
    public boolean rollback(Client client, Map<QueueStatus, List<String>> ids) {
        byte[] resp = this.post("mbq/push", new CommitRollbackRequest(client, ids, new HashMap<>()));
        try {
            RestResponse<Boolean> clientResponse = new ObjectMapper().readValue(resp, new TypeReference<RestResponse<Boolean>>() {
            });
            validateResponse(clientResponse);
            return clientResponse.getData();
        } catch (IOException e) {
            throw new MBQException("Failed in parsing response from Server", e);
        }
    }

    public List<MBQMessage> push(Client client, List<QMessage> messages) {
        byte[] resp = this.post("mbq/push", new PushRequest(client, messages));
        try {
            RestResponse<List<MBQMessage>> clientResponse = new ObjectMapper().readValue(resp, new TypeReference<RestResponse<List<MBQMessage>>>() {
            });
            validateResponse(clientResponse);
            return clientResponse.getData();
        } catch (IOException e) {
            throw new MBQException("Failed in parsing response from Server", e);
        }
    }

    public MBQMessage push(Client client, QMessage message) {
        List<MBQMessage> messages = push(client, Collections.singletonList(message));
        return messages != null && !messages.isEmpty() ? messages.get(0) : null;
    }

    public String ping(Client client) {
        byte[] resp = this.post("mbq/ping", client);
        try {
            RestResponse<String> clientResponse = new ObjectMapper().readValue(resp, new TypeReference<RestResponse<String>>() {
            });
            validateResponse(clientResponse);
            return clientResponse.getData();
        } catch (IOException e) {
            throw new MBQException("Failed in parsing response from Server", e);
        }
    }

    @Override
    public boolean update(List<String> ids, QueueStatus newStatus) {
        throw new UnsupportedOperationException("Update is unsupported from Client.");
    }
}