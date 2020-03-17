package com.rags.tools.mbq.queue.pending;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class InMemoryPendingQ<T> implements PendingQ<T> {

    private final LinkedList<T> queue = new LinkedList<>();

    public final ReentrantLock LOCK = new ReentrantLock();

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean removeAll(List<T> items) {
        return queue.removeAll(items);
    }

    @Override
    public T get(int index) {
        return queue.get(index);
    }

    @Override
    public void addLast(T item) {
        this.queue.addLast(item);
    }

    @Override
    public void add(int index, T item) {
        this.queue.add(index, item);
    }

    @Override
    public void add(T item) {
        this.queue.add(item);
    }

    @Override
    public void addAll(List<T> items) {
        queue.addAll(items);
    }

    @Override
    public void addFirst(T item) {
        queue.addFirst(item);
    }

    @Override
    public void addAllFirst(List<T> items) {
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
    public List<T> find(List<T> items) {
        return queue.stream().filter(items::contains).collect(Collectors.toList());
    }
}