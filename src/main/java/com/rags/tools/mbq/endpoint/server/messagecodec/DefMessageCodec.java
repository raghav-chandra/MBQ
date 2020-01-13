package com.rags.tools.mbq.endpoint.server.messagecodec;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;

public class DefMessageCodec<S> implements MessageCodec<S, S> {
    @Override
    public void encodeToWire(Buffer buffer, S item) {
        String json = Json.encode(item);
        int length = json.getBytes().length;
        buffer.appendInt(length);
        buffer.appendString(json);
    }

    @Override
    public S decodeFromWire(int position, Buffer buffer) {
        int pos = position;
        int length = buffer.getInt(position);

        String json = buffer.getString(pos += 4, pos += length);
        return (S) Json.decodeValue(json);
    }

    @Override
    public S transform(S item) {
        return item;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}