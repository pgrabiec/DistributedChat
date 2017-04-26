package org.pg.sr.distributedChat.util;

import java.util.LinkedList;
import java.util.List;

public class ListenableLinkedList<E> extends LinkedList<E> implements ListenableList<E> {
    private final List<ListListener<E>> listeners = new LinkedList<>();

    @Override
    public void addListListener(ListListener<E> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListListener(ListListener<E> listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean add(E e) {
        boolean added = super.add(e);
        for (ListListener<E> listListener : listeners) {
            listListener.elementAdded(e);
        }
        return added;
    }

    @Override
    public boolean remove(Object o) {
        boolean removed = super.remove(o);
        for (ListListener<E> listListener : listeners) {
            listListener.elementRemoved(o);
        }
        return removed;
    }
}
