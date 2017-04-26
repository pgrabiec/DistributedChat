package org.pg.sr.distributedChat.util;

import org.pg.sr.distributedChat.state.State;

public interface StateListener {
    public void stateChanged(State newState);
}
