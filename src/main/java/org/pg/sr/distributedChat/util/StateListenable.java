package org.pg.sr.distributedChat.util;

public interface StateListenable {
    public void addStateListener(StateListener listener);
    public void removeStateListener(StateListener listener);
}
