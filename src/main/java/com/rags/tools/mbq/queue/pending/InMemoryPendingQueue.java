package com.rags.tools.mbq.queue.pending;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class InMemoryPendingQueue<T> implements PendingQueue<T> {

    private final LinkedList<T> queue = new LinkedList<>();

    public final ReentrantLock LOCK = new ReentrantLock();

    @Override
    public boolean isEmpty() {
/*
        try {
            lock.readLock().lock();
*/
            return queue.isEmpty();
/*
        } finally {
            lock.readLock().unlock();
        }
*/
    }

    @Override
    public int size() {
/*
        lock.readLock().lock();
        try {
*/
            return queue.size();
/*
        } finally {
            lock.readLock().unlock();
        }
*/
    }

    @Override
    public boolean removeAll(List<T> items) {
/*
        lock.writeLock().lock();
        try {
*/
            return queue.removeAll(items);
/*
        } finally {
            lock.writeLock().unlock();
        }
*/
    }

    @Override
    public T get(int index) {
/*
        lock.writeLock().lock();
        try {
*/
            return queue.get(index);
/*
        } finally {
            lock.writeLock().unlock();
        }
*/
    }

    @Override
    public void addLast(T item) {
/*
        lock.writeLock().lock();
        try {
*/
            this.queue.addLast(item);
/*
        } finally {
            lock.writeLock().unlock();
        }
*/
    }

    @Override
    public void add(int index, T item) {
/*
        lock.writeLock().lock();
        try {
*/
            this.queue.add(index, item);
/*
        } finally {
            lock.writeLock().unlock();
        }
*/
    }

    @Override
    public void add(T item) {
/*
        lock.writeLock().lock();
        try {
*/
            this.queue.add(item);
/*
        } finally {
            lock.writeLock().unlock();
        }
*/
    }

    @Override
    public void addAll(List<T> items) {
/*
        lock.writeLock().lock();
        try {
*/
            queue.addAll(items);

/*
        } finally {
            lock.writeLock().unlock();
        }
*/
    }

    @Override
    public void lock() {
        LOCK.lock();
    }

    @Override
    public void unlock() {
        LOCK.unlock();
    }
}

