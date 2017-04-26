package org.pg.sr.distributedChat;

import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.pg.sr.distributedChat.chatting.ChatGUI;
import org.pg.sr.distributedChat.management.ManagementGUI;
import org.pg.sr.distributedChat.management.ManagementController;

public class Main extends ReceiverAdapter {
    public static void main(String[] args) throws Exception {
        System.setProperty("java.net.preferIPv4Stack","true");
        ManagementGUI gui = new ManagementGUI();
        ManagementController adapter = new ManagementController(gui);
//        try {
//            JChannel channel = new JChannel();
//            channel.setReceiver(new Main());
//            channel.setName("Nick1");
//            channel.connect("Cluster1");
//            channel.send(new Message(null, null, "Hello"));
//            Thread.sleep(100000);
//            channel.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void receive(Message msg) {
        System.out.println("Message from " + msg.getSrc() + ": " + new String(msg.getBuffer()));
    }

    @Override
    public void viewAccepted(View view) {
        System.out.println("View: " + view.getMembers());
    }
}
