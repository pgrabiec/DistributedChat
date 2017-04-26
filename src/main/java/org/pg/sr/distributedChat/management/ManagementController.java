package org.pg.sr.distributedChat.management;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.pg.sr.distributedChat.channels.ManagementJChannel;
import org.pg.sr.distributedChat.chatting.ChatChannelBuilder;
import org.pg.sr.distributedChat.state.ChannelState;
import org.pg.sr.distributedChat.state.Client;
import org.pg.sr.distributedChat.state.State;
import org.pg.sr.distributedChat.util.StateListener;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

public class ManagementController extends ReceiverAdapter implements StateListener {
    private final State state = new State();
    private JChannel managementChannel;
    private final ManagementGUI gui;

    private String selectedChannel = null;

    public ManagementController(ManagementGUI gui) {
        this.gui = gui;
        state.addStateListener(this);
        initListeners();
    }

    private void initListeners() {
        gui.getListAllChannels().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                String channelName = gui.getListAllChannels().getSelectedValue();
                if (channelName == null) {
                    SwingUtilities.invokeLater(() -> {
                        gui.getListClientsInChannel().setListData(new String[0]);
                        gui.getLabelClientsInChannel().setText("Clients");
                    });
                    return;
                }

                selectedChannel = channelName;

                ChannelState channelState = state.getChannelState(channelName);
                if (channelState == null) {
                    return;
                }

                String[] clients = new String[channelState.getClients().size()];
                int i=0;
                for (Client client : channelState.getClients()) {
                    clients[i] = client.getName();
                    i++;
                }

                SwingUtilities.invokeLater(() -> {
                    gui.getListClientsInChannel().setListData(
                            clients
                    );
                    gui.getLabelClientsInChannel().setText("Clients in " + channelName);
                });
            }
        });

        gui.getButtonJoin().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedChannel == null) {
                    return;
                }

                joinChannel(selectedChannel);
            }
        });

        gui.getButtonSetNickname().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nick = gui.getTextMyNickname().getText();
                if (nick.isEmpty()) {
                    return;
                }
                gui.getButtonSetNickname().setEnabled(false);
                gui.getTextMyNickname().setEnabled(false);

                nicknameSelected(nick);
            }
        });

        gui.getButtonCreateChannel().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String channelName = gui.getTextCreateChannel().getText();
                if (!newChannelNameValid(channelName)) {
                    return;
                }

                gui.getButtonCreateChannel().setEnabled(false);
                gui.getTextCreateChannel().setText("");

                joinChannel(channelName);

                gui.getButtonCreateChannel().setEnabled(true);
            }
        });
    }

    private boolean newChannelNameValid(String channelName) {
        if (state.getChannelState(channelName) != null) {
            return false;
        }

        String[] numberStrings = channelName.split("[.]");
        if (numberStrings.length != 4) {
            return false;
        }

        int[] numbers = new int[4];
        try {
            for (int i=0; i<4; i++) {
                numbers[i] = Integer.parseInt(numberStrings[i]);
            }
        } catch (NumberFormatException e) {
            return false;
        }

        if (numbers[0] < 224 || numbers[0] > 239) {
            return false;
        }

        for (int i=1; i<4; i++) {
            if (numbers[i] < 0 || numbers[i] > 255) {
                return false;
            }
        }

        return true;
    }

    private void nicknameSelected(String nick) {
        state.setMyClient(new Client(nick));

        gui.getListAllChannels().setEnabled(true);
        gui.getTextCreateChannel().setEnabled(true);
        gui.getButtonJoin().setEnabled(true);
        gui.getButtonCreateChannel().setEnabled(true);
        gui.getListAllChannels().setEnabled(true);

        try {
            managementChannel = new ManagementJChannel(nick);
            managementChannel.setReceiver(this);
            managementChannel.connect("ChatManagement321321");
            managementChannel.getState(null, 0);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void receive(Message msg) {
        try {
            ChatOperationProtos.ChatAction action = ChatOperationProtos.ChatAction.parseFrom(msg.getBuffer());
            state.applyChatAction(action);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getState(OutputStream output) throws Exception {
        output.write(state.getCurrentState().toByteArray());
        output.flush();
    }

    @Override
    public void setState(InputStream input) throws Exception {
        List<Byte> bytes = new ArrayList<>(256);

        byte[] buf = new byte[256];
        int readCount;

        do {
            readCount = input.read(buf);
            for (int i=0; i<readCount; i++) {
                bytes.add(buf[i]);
            }
        } while (readCount > 0);

        byte[] rawBytes = new byte[bytes.size()];
        for (int i=0; i<bytes.size(); i++) {
            rawBytes[i] = bytes.get(i);
        }

        ChatOperationProtos.ChatState receivedState = ChatOperationProtos.ChatState.parseFrom(
                rawBytes
        );

        this.state.setCurrentState(receivedState);
    }

    @Override
    public void viewAccepted(View view) {
        state.managementChannelViewChanged(view, managementChannel);
    }

    @Override
    public void stateChanged(State newState) {
        List<ChannelState> newChannels = newState.getAllChannels();

        String[] channels = new String[newChannels.size()];

        List<String> subscribed = new LinkedList<>();

        for (int i=0; i<newChannels.size(); i++) {
            channels[i] = newChannels.get(i).getChannelName();
            if (newChannels.get(i).isSubscribed()) {
                subscribed.add(newChannels.get(i).getChannelName());
            }
        }

        String[] subscribedChannels = subscribed.toArray(new String[0]);

        SwingUtilities.invokeLater(() -> {
            gui.getListAllChannels().setListData(channels);
            if (selectedChannel != null) {
                gui.getListAllChannels().setSelectedValue(selectedChannel, true);
            }
            gui.getListSubscribedChannels().setListData(subscribedChannels);
        });

    }


    private void joinChannel(String channelName) {
        ChannelState channelState = state.getChannelState(channelName);

        if (channelState != null) {
            if (channelState.isSubscribed()) {
                return;
            }
        }

        new ChatChannelBuilder(state, channelName, managementChannel).createAndShowChat();
    }
}
