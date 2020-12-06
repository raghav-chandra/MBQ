package com.rags.tools.mbq.queue.pending;

import com.rags.tools.mbq.queue.IdSeqKey;

import java.util.Iterator;
import java.util.List;

public interface PendingQ<T> {

    /**
     * Checks if queue is empty
     *
     * @return true if queue is empty
     */
    boolean isEmpty();

    /**
     * Checks size of the queue
     *
     * @return size
     */
    int size();

    /**
     * Removes all elements from the queue
     *
     * @param items elements
     * @return true if successful
     */
    boolean removeAll(List<T> items);

    /**
     * Returns an item at index
     *
     * @param index index
     * @return item
     */
    T get(int index);

    /**
     * Add item at the end of the queue
     *
     * @param item item
     */
    void addLast(T item);

    /**
     * Adds items in pushed order. places items at its right position
     *
     * @param item
     */
    void addInOrder(IdSeqKey item);

    /*
     * Adds items in pushed order. places items at its right position
     * @param items
     */
    void addInOrder(List<IdSeqKey> items);

    /**
     * Add an item in the queue
     *
     * @param item item to be added
     */
    void add(T item);

    /**
     * Add all items in the queue
     *
     * @param items all Items
     */
    void addAll(List<T> items);

    /**
     * Add an item in the queue to the front position
     *
     * @param item item to be added
     */
    void addFirst(T item);

    /**
     * Add all items in the queue to the front position
     *
     * @param items all Items
     */
    void addAllFirst(List<T> items);

    void lock();

    void unlock();

    boolean isLocked();

    /**
     * Returns all the elements found in the given items
     *
     * @param items given items
     * @return all found items
     */
    List<T> find(List<T> items);

    /**
     * Return queue Iterator
     */
    Iterator<T> iterator();
}
