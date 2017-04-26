package org.pg.sr.distributedChat.chatting;

import org.jgroups.JChannel;
import org.pg.sr.distributedChat.channels.ChatJChannel;
import org.pg.sr.distributedChat.state.State;

public class ChatChannelBuilder {
    private final State state;
    private final String channelName;
    private final JChannel managementChannel;

    public ChatChannelBuilder(State state, String channelName, JChannel managementChannel) {
        this.state = state;
        this.channelName = channelName;
        this.managementChannel = managementChannel;
    }

    public void createAndShowChat() {
        new Thread(() -> {
            try {
                JChannel chatChannel = new ChatJChannel(channelName, state.getMyClient().getName());
                chatChannel.connect(channelName);

                ChatGUI gui = new ChatGUI(channelName);
                ChatController adapter = new ChatController(
                        gui,
                        managementChannel,
                        state,
                        channelName
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
