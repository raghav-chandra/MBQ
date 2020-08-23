package com.rags.tools.mbq.connection.rest.messagecodec;

import java.util.List;

public class SearchRequest {
    private List<String> ids;

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public boolean isInValid() {
        return ids == null || ids.isEmpty();
    }
}
