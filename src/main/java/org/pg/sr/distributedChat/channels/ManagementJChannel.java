package org.pg.sr.distributedChat.channels;

import org.jgroups.JChannel;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.*;
import org.jgroups.stack.ProtocolStack;

public class ManagementJChannel extends JChannel {
    public ManagementJChannel(String nick) throws Exception {
        super(false);
        ProtocolStack stack = new ProtocolStack();
        this.setProtocolStack(stack);
        stack.addProtocol(new UDP())
                .addProtocol(new PING())
                .addProtocol(new MERGE3())
                .addProtocol(new FD_SOCK())
                .addProtocol(new FD_ALL().setValue("timeout", 12000).setValue("interval", 3000))
                .addProtocol(new VERIFY_SUSPECT())
                .addProtocol(new BARRIER())
                .addProtocol(new NAKACK2())
                .addProtocol(new UNICAST3())
                .addProtocol(new STABLE())
                .addProtocol(new GMS())
                .addProtocol(new UFC())
                .addProtocol(new MFC())
                .addProtocol(new FRAG2())
                .addProtocol(new STATE_TRANSFER())
                .addProtocol(new FLUSH());
        stack.init();
        this.setName(nick);
    }
}
