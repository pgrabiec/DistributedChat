package org.pg.sr.distributedChat.state;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.pg.sr.distributedChat.util.StateListenable;
import org.pg.sr.distributedChat.util.StateListener;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * All public methods must be thread-safe
 * */
public class State implements StateListenable {
    private final List<StateListener> listeners = new LinkedList<>();
    private final Lock stateLock = new ReentrantLock();
    private final Map<String, ChannelState> channelsMap = new HashMap<>();
    private final Map<String, Client> clientsMap = new HashMap<>();
    private Client myClient = null;

    public ChatOperationProtos.ChatState getCurrentState() {
        stateLock.lock();
        try {
            ChatOperationProtos.ChatState.Builder builder =
                    ChatOperationProtos.ChatState.newBuilder();
            for (ChannelState channel : channelsMap.values()) {
                for (Client client : channel.getClients()) {
                    builder.addState(
                            ChatOperationProtos.ChatAction.newBuilder()
                                    .setAction(ChatOperationProtos.ChatAction.ActionType.JOIN)
                                    .setChannel(channel.getChannelName())
                                    .setNickname(client.getName())
                                    .build()
                    );
                }
            }
            return builder.build();
        } finally {
            stateLock.unlock();
        }
    }

    public void setCurrentState(ChatOperationProtos.ChatState state) {
        stateLock.lock();
        try {
            for (ChatOperationProtos.ChatAction action : state.getStateList()) {
                String channelName = action.getChannel();
                String nick = action.getNickname();
                ChatOperationProtos.ChatAction.ActionType actionType = action.getAction();

                if (actionType != ChatOperationProtos.ChatAction.ActionType.JOIN) {
                    System.out.println("[WARNING] ChatAction.ActionType: != JOIN + (" +
                            actionType.toString() + ")");
                    continue;
                }

                Client client = clientsMap.computeIfAbsent(nick, k -> new Client(nick));
                ChannelState channelState = channelsMap.computeIfAbsent(channelName, k -> new ChannelState(channelName));

                channelState.getClients().add(client);
            }
        } finally {
            stateLock.unlock();
        }
        triggerStateChanged();
    }

    public void applyChatAction(ChatOperationProtos.ChatAction action) {
        stateLock.lock();
        try {
            String channelName = action.getChannel();
            String nick = action.getNickname();
            ChatOperationProtos.ChatAction.ActionType actionType = action.getAction();

            Client client = clientsMap.computeIfAbsent(nick, k -> new Client(nick));
            ChannelState channelState = channelsMap.computeIfAbsent(channelName, k -> new ChannelState(channelName));

            switch (actionType) {
                case JOIN:
                    channelState.getClients().add(client);
                    if (client == myClient) {
                        channelState.setSubscribed(true);
                    }
                    break;
                case LEAVE:
                    channelState.getClients().remove(client);
                    if (client == myClient) {
                        channelState.setSubscribed(false);
                    }
            }
        } finally {
            stateLock.unlock();
        }
        triggerStateChanged();
    }

    public ChannelState getChannelState(String channelName) {
        stateLock.lock();
        try {
            ChannelState state = channelsMap.get(channelName);
            if (state != null) {
                return state.copy();
            }
            return null;
        } finally {
            stateLock.unlock();
        }
    }

    public void unsubscribed(String channelName) {
        stateLock.lock();
        try {
            ChannelState channelState = channelsMap.get(channelName);
            if (channelState != null) {
                channelState.getClients().remove(myClient);
                channelState.setSubscribed(false);
            }
        } finally {
            stateLock.unlock();
        }
        triggerStateChanged();
    }

    public void chatViewChanged(View newView, JChannel sourceChannel) {
        String channelName = sourceChannel.getName();
        ChannelState channelState = channelsMap.get(channelName);
        if (channelState == null) {
            return;
        }
        List<String> clients = channelState.getClientsList();

        stateLock.lock();
        try {
            List<String> missing = computeCrashedClients(newView, sourceChannel, clients);
            if (missing.isEmpty()) {
                return;
            }

            List<Client> clientsMissing = new ArrayList<>(missing.size());
            for (String clientName : missing) {
                clientsMissing.add(clientsMap.get(clientName));
            }

            channelState.getClients().removeAll(clientsMissing);
        } finally {
            stateLock.unlock();
        }
        triggerStateChanged();
    }

    public void managementChannelViewChanged(View newView, JChannel managementChannel) {
        stateLock.lock();
        try {
            List<String> missing = computeCrashedClients(newView, managementChannel, clientsMap.keySet());
            if (missing.isEmpty()) {
                return;
            }

            List<Client> clientsMissing = new ArrayList<>(missing.size());
            for (String clientName : missing) {
                clientsMissing.add(clientsMap.get(clientName));
            }

            for (ChannelState channelState : channelsMap.values()) {
                channelState.getClients().removeAll(clientsMissing);
            }

            for (Client client : clientsMissing) {
                clientsMap.remove(client.getName());
            }
        } finally {
            stateLock.unlock();
        }
        triggerStateChanged();
    }

    private List<String> computeCrashedClients(View currentView, JChannel viewSource, Collection<String> lastClients) {
        List<String> missing = new LinkedList<>();

        Set<String> newClients = new HashSet<>();
        for (Address address : currentView.getMembers()) {
            newClients.add(viewSource.getName(address));
        }

        for (String lastClient : lastClients) {
            if (!newClients.contains(lastClient)) {
                missing.add(lastClient);
            }
        }

        return missing;
    }

    public List<ChannelState> getAllChannels() {
        stateLock.lock();
        try {
            List<ChannelState> states = new ArrayList<>(channelsMap.size());

            for (ChannelState state : channelsMap.values()) {
                states.add(state.copy());
            }

            return states;
        } finally {
            stateLock.unlock();
        }
    }

    private void triggerStateChanged() {
        stateLock.lock();
        try {
            List<ChannelState> channels = new ArrayList<>();
            channels.addAll(channelsMap.values());
            for (ChannelState state : channels) {
                if (state.getClients().size() < 1) {
                    channelsMap.remove(state.getChannelName());
                }
            }
        } finally {
            stateLock.unlock();
        }
        for (StateListener listener : listeners) {
            listener.stateChanged(this);
        }
    }

    @Override
    public void addStateListener(StateListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeStateListener(StateListener listener) {
        listeners.remove(listener);
    }

    public Client getMyClient() {
        stateLock.lock();
        try {
            return myClient;
        } finally {
            stateLock.unlock();
        }
    }

    public void setMyClient(Client myClient) {
        stateLock.lock();
        try {
            this.myClient = myClient;
            clientsMap.put(myClient.getName(), myClient);
        } finally {
            stateLock.unlock();
        }
    }
}
