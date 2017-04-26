package org.pg.sr.distributedChat.state;

import java.util.*;

public class ChannelState {
    private final Set<Client> clients = new HashSet<>();
    private final String channelName;
    private boolean subscribed = false;

    public ChannelState(String name) {
        this.channelName = name;
    }

    public String getChannelName() {
        return channelName;
    }

    public Set<Client> getClients() {
        return clients;
    }

    public ChannelState copy() {
        ChannelState copy = new ChannelState(channelName);
        copy.getClients().addAll(clients);
        copy.setSubscribed(subscribed);
        return copy;
    }

    public List<String> getClientsList() {
        List<String> clientsList = new ArrayList<>(clients.size());
        for (Client client : clients) {
            clientsList.add(client.getName());
        }
        return clientsList;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }
}
