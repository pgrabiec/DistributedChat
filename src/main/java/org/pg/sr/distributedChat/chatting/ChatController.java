package org.pg.sr.distributedChat.chatting;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.pg.sr.distributedChat.channels.ChatJChannel;
import org.pg.sr.distributedChat.state.ChannelState;
import org.pg.sr.distributedChat.state.Client;
import org.pg.sr.distributedChat.state.State;
import org.pg.sr.distributedChat.util.StateListener;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;

import static com.sun.java.accessibility.util.AWTEventMonitor.addWindowListener;

public class ChatController extends ReceiverAdapter implements StateListener {
    private final ChatGUI gui;
    private JChannel chatChannel;
    private final JChannel managementChannel;
    private final State state;
    private final String chatName;

    public ChatController(ChatGUI gui, JChannel managementChannel, State state, String chatName) {
        this.gui = gui;
        this.managementChannel = managementChannel;
        this.state = state;
        this.chatName = chatName;

        state.addStateListener(this);
        try {
            this.chatChannel = new ChatJChannel(chatName, state.getMyClient().getName());
            chatChannel.setReceiver(this);
            chatChannel.connect(chatName);
        } catch (Exception e) {
            e.printStackTrace();
            gui.dispatchEvent(new WindowEvent(gui, WindowEvent.WINDOW_CLOSING));
        }

        gui.getLabelChannelName().setText(chatName);


        initListeners();

        registerJoining();

        stateChanged(state);

        gui.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                try {
                    cleanUpChannel();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                gui.dispose();
            }
        });
    }

    private void registerJoining() {
        ChatOperationProtos.ChatAction action =
                ChatOperationProtos.ChatAction.newBuilder()
                        .setAction(ChatOperationProtos.ChatAction.ActionType.JOIN)
                        .setChannel(chatName)
                        .setNickname(state.getMyClient().getName())
                        .build();
        try {
            managementChannel.send(null, action.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initListeners() {
        gui.getButtonLeave().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    cleanUpChannel();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                gui.dispatchEvent(new WindowEvent(gui, WindowEvent.WINDOW_CLOSING));
            }
        });

        gui.getButtonSend().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = gui.getTextMessageInput().getText();
                if (text.isEmpty()) {
                    return;
                }

                gui.getButtonSend().setEnabled(false);
                gui.getTextMessageInput().setText("");

                ChatOperationProtos.ChatMessage message = ChatOperationProtos.ChatMessage.newBuilder()
                        .setMessage(text).build();
                try {
                    chatChannel.send(null, message.toByteArray());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

                gui.getButtonSend().setEnabled(true);
            }
        });
    }

    private void cleanUpChannel() throws Exception {
        ChatOperationProtos.ChatAction action =
                ChatOperationProtos.ChatAction.newBuilder()
                        .setAction(ChatOperationProtos.ChatAction.ActionType.LEAVE)
                        .setChannel(chatName)
                        .setNickname(state.getMyClient().getName())
                        .build();
        managementChannel.send(null, action.toByteArray());
        state.unsubscribed(chatName);
    }

    @Override
    public void stateChanged(State newState) {
        ChannelState channelState = newState.getChannelState(chatName);
        if (channelState == null) {
            return;
        }
        String[] clients = new String[channelState.getClients().size()];
        int i=0;
        for (Client client : channelState.getClients()) {
            clients[i] = client.getName();
            i++;
        }
        SwingUtilities.invokeLater(() -> gui.getListClients().setListData(clients));
    }

    @Override
    public void receive(Message msg) {
        try {
            ChatOperationProtos.ChatMessage message =
                    ChatOperationProtos.ChatMessage.parseFrom(msg.getBuffer());
            String text = message.getMessage();
            String source = chatChannel.getName(msg.getSrc());

            SwingUtilities.invokeLater(() ->
                    gui.getListMessages().append(
                            source + " (" + new Date().toString() + "):\n" + ">>" + text + "\n"
                    )
            );
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void viewAccepted(View view) {
        state.chatViewChanged(view, chatChannel);
    }
}
