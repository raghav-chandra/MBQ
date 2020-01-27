package com.rags.tools.mbq.queue.pending;

import java.util.List;

public interface PendingQueue<T> {

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
     * Add Items  in the queue at given index
     *
     * @param index index
     * @param item  item to be added
     */
    void add(int index, T item);

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

    void lock();
    void unlock();
    boolean isLocked();
}
