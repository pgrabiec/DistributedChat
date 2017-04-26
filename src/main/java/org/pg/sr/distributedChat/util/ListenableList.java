package org.pg.sr.distributedChat.util;

import java.util.List;

public interface ListenableList<E> extends List<E> {
    public void addListListener(ListListener<E> listener);
    public void removeListListener(ListListener<E> listener);
}
