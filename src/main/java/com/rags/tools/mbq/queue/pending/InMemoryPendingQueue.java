package com.rags.tools.mbq.queue.pending;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class InMemoryPendingQueue<T> implements PendingQueue<T> {

    private final LinkedList<T> queue = new LinkedList<>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();


    @Override
    public boolean isEmpty() {
        try {
            lock.readLock().lock();
            return queue.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int size() {
        try {
            lock.readLock().lock();
            return queue.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean removeAll(List<T> items) {
        try {
            lock.writeLock().lock();
            return queue.removeAll(items);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public T get(int index) {
        try {
            lock.writeLock().lock();
            return queue.get(index);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void addLast(T item) {
        try {
            lock.writeLock().lock();
            this.queue.addLast(item);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void add(int index, T item) {
        try {
            lock.writeLock().lock();
            this.queue.add(index, item);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void add(T item) {
        try {
            lock.writeLock().lock();
            this.queue.add(item);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void addAll(List<T> items) {
        try {
            lock.writeLock().lock();
            queue.removeAll(items);
        } finally {
            lock.writeLock().unlock();
        }
    }
}

