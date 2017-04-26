package org.pg.sr.distributedChat.management;

import javax.swing.*;
import java.awt.*;

public class ManagementGUI extends JFrame {
    private final JLabel labelAllChannels = new JLabel("All Channels");
    private final JLabel labelMyNickname = new JLabel("My nickname");
    private final JLabel labelSubscribedChannels = new JLabel("Subscribed channels");
    private final JLabel labelChannelCreation = new JLabel("Channel creation");
    private final JLabel labelClientsInChannel = new JLabel("Clients");
    private final JButton buttonSetNickname = new JButton("Set Nickname");
    private final JButton buttonCreateChannel = new JButton("Create and join");
    private final JButton buttonJoin = new JButton("Join");
    private final JTextArea textCreateChannel = new JTextArea("");
    private final JTextArea textMyNickname = new JTextArea("");
    private final JList<String> listAllChannels = new JList<>();
    private final JList<String> listClientsInChannel = new JList<>();
    private final JList<String> listSubscribedChannels = new JList<>();

    private final JPanel contentPane = new JPanel(new GridBagLayout());

    public ManagementGUI() {
        super("Chat Console");

        setContentPane(contentPane);

        addComponentGridBag(labelAllChannels, 0, 0, 1, 1);
        addComponentGridBag(labelMyNickname, 1, 0, 1, 1);
        addComponentGridBag(listAllChannels, 0, 1, 1, 4);
        addComponentGridBag(textMyNickname, 1, 1, 1, 1);
        addComponentGridBag(buttonSetNickname, 1, 2, 1, 1);
        addComponentGridBag(labelSubscribedChannels, 1, 3, 1, 1);
        addComponentGridBag(listSubscribedChannels, 1, 4, 1, 3);
        addComponentGridBag(labelClientsInChannel, 0, 5, 1, 1);
        addComponentGridBag(listClientsInChannel, 0, 6, 1, 3);
        addComponentGridBag(labelChannelCreation, 1, 7, 1, 1);
        addComponentGridBag(textCreateChannel, 1, 8, 1, 1);
        addComponentGridBag(buttonJoin, 0, 9, 1, 1);
        addComponentGridBag(buttonCreateChannel, 1, 9, 1, 1);

        buttonCreateChannel.setEnabled(false);
        buttonJoin.setEnabled(false);
        textCreateChannel.setEnabled(false);

        textCreateChannel.setEditable(true);
        textCreateChannel.setPreferredSize(new Dimension(200, 20));

        textMyNickname.setEditable(true);
        textMyNickname.setPreferredSize(new Dimension(200, 20));

        listAllChannels.setPreferredSize(new Dimension(200, 400));

        listSubscribedChannels.setPreferredSize(new Dimension(200, 400));
        listSubscribedChannels.setEnabled(false);

        listClientsInChannel.setPreferredSize(new Dimension(200, 400));
        listClientsInChannel.setEnabled(false);

        listAllChannels.setEnabled(false);

        listAllChannels.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listClientsInChannel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSubscribedChannels.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


        setResizable(false);
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
        setEnabled(true);
    }

    private void addComponentGridBag(JComponent component, int gridx, int gridy, int gridwidth, int gridheight) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = gridx;
        c.gridy = gridy;
        c.gridwidth = gridwidth;
        c.gridheight = gridheight;
        c.insets = new Insets(3, 3, 3, 3);
        contentPane.add(component, c);
    }

    public JLabel getLabelAllChannels() {
        return labelAllChannels;
    }

    public JLabel getLabelMyNickname() {
        return labelMyNickname;
    }

    public JLabel getLabelSubscribedChannels() {
        return labelSubscribedChannels;
    }

    public JLabel getLabelChannelCreation() {
        return labelChannelCreation;
    }

    public JLabel getLabelClientsInChannel() {
        return labelClientsInChannel;
    }

    public JButton getButtonSetNickname() {
        return buttonSetNickname;
    }

    public JButton getButtonCreateChannel() {
        return buttonCreateChannel;
    }

    public JButton getButtonJoin() {
        return buttonJoin;
    }

    public JTextArea getTextCreateChannel() {
        return textCreateChannel;
    }

    public JTextArea getTextMyNickname() {
        return textMyNickname;
    }

    public JList<String> getListAllChannels() {
        return listAllChannels;
    }

    public JList<String> getListClientsInChannel() {
        return listClientsInChannel;
    }

    public JList<String> getListSubscribedChannels() {
        return listSubscribedChannels;
    }
}
