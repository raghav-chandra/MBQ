package com.rags.tools.mbq.connection.rest.messagecodec;

import com.rags.tools.mbq.QueueStatus;

import java.util.List;

public class UpdateStatusRequest {

    private List<String> ids;
    private QueueStatus status;

    public List<String> getIds() {
        return ids;
    }

    public UpdateStatusRequest setIds(List<String> ids) {
        this.ids = ids;
        return this;
    }

    public QueueStatus getStatus() {
        return status;
    }

    public UpdateStatusRequest setStatus(QueueStatus status) {
        this.status = status;
        return this;
    }
}
