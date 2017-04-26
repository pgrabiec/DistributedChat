package org.pg.sr.distributedChat.util;

public interface ListListener<E> {
    public void elementAdded(E element);
    public void elementRemoved(Object element);
}
