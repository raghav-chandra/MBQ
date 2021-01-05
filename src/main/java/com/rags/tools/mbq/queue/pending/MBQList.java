package com.rags.tools.mbq.queue.pending;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;

public class MBQList<E> extends LinkedList<E> {
    @Override
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<E> it = this.iterator();

        int len = c.size();
        while (it.hasNext()) {
            if (c.contains(it.next())) {
                it.remove();
                len--;
                modified = true;
            }
            if (len <= 0) {
                break;
            }
        }

        return modified;
    }
}
