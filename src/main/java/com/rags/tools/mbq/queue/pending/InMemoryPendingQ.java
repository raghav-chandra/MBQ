package com.rags.tools.mbq.queue.pending;

import com.rags.tools.mbq.queue.IdSeqKey;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class InMemoryPendingQ implements PendingQ<IdSeqKey> {

    private final LinkedList<IdSeqKey> queue = new LinkedList<>();

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean removeAll(List<IdSeqKey> items) {
        return queue.removeAll(items);
    }

    @Override
    public IdSeqKey get(int index) {
        return queue.get(index);
    }

    @Override
    public void addLast(IdSeqKey item) {
        this.queue.addLast(item);
    }

    @Override
    public void add(int index, IdSeqKey item) {
        this.queue.add(index, item);
    }

    @Override
    public void addInOrder(IdSeqKey item) {
        if (queue.isEmpty() || queue.getFirst().getId().compareTo(item.getId()) >= 0) {
            queue.addFirst(item);
        } else {
            int ind = 0;
            while (ind < queue.size() && queue.get(ind++).getId().compareTo(item.getId()) <= 0) ;
            queue.add(ind - 1, item);
        }
    }

    @Override
    public void addInOrder(List<IdSeqKey> items) {
        items.forEach(this::addInOrder);
    }

    @Override
    public void add(IdSeqKey item) {
        this.queue.add(item);
    }

    @Override
    public void addAll(List<IdSeqKey> items) {
        queue.addAll(items);
    }

    @Override
    public void addFirst(IdSeqKey item) {
        queue.addFirst(item);
    }

    @Override
    public void addAllFirst(List<IdSeqKey> items) {
        items.forEach(queue::addFirst);
    }

    @Override
    public void lock() {
//        LOCK.tryLock();
    }

    @Override
    public void unlock() {
//        LOCK.unlock();
    }

    @Override
    public boolean isLocked() {
//        return LOCK.isLocked();
        return false;
    }

    @Override
    public List<IdSeqKey> find(List<IdSeqKey> items) {
        return queue.stream().filter(items::contains).collect(Collectors.toList());
    }
}