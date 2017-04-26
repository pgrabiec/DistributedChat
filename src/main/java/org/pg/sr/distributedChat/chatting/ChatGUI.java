package org.pg.sr.distributedChat.chatting;


import javax.swing.*;
import java.awt.*;

public class ChatGUI extends JFrame {
    private final JLabel labelChannelName = new JLabel();
    private final JButton buttonLeave = new JButton("Leave channel");
    private final JTextArea textMessages = new JTextArea();
    private final JScrollPane messagesScrollPane = new JScrollPane(textMessages);
    private final JLabel labelClients = new JLabel("Clients");
    private final JList<String> listClients = new JList<>();
    private final JTextArea textMessageInput = new JTextArea("");
    private final JButton buttonSend = new JButton("Send");



    private final JPanel contentPane = new JPanel(new GridBagLayout());

    public ChatGUI(String channelName) throws HeadlessException {
        super(channelName);

        setContentPane(contentPane);

        addComponentGridBag(labelChannelName, 0, 0, 1, 1);
        addComponentGridBag(buttonLeave, 1, 0, 1, 1);
        addComponentGridBag(messagesScrollPane, 0, 1, 1, 2);
        addComponentGridBag(labelClients, 1, 1, 1, 1);
        addComponentGridBag(listClients, 1, 2, 1, 1);
        addComponentGridBag(textMessageInput, 0, 3, 1, 1);
        addComponentGridBag(buttonSend, 1, 3, 1, 1);

        buttonLeave.setEnabled(true);
        textMessages.setEnabled(false);
        listClients.setEnabled(false);
        textMessageInput.setEnabled(true);
        textMessageInput.setEditable(true);
        buttonSend.setEnabled(true);

        Font font = new Font("Monospaced", Font.BOLD, 16);
        textMessages.setFont(font);
        Color color = new Color(90, 90, 90);
        textMessages.setDisabledTextColor(color);

        listClients.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        labelChannelName.setText(channelName);

        listClients.setPreferredSize(new Dimension(200, 400));
        messagesScrollPane.setPreferredSize(new Dimension(400, 500));
        textMessageInput.setPreferredSize(new Dimension(400, 100));

        setResizable(false);
        pack();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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

    public JLabel getLabelChannelName() {
        return labelChannelName;
    }

    public JButton getButtonLeave() {
        return buttonLeave;
    }

    public JTextArea getListMessages() {
        return textMessages;
    }

    public JLabel getLabelClients() {
        return labelClients;
    }

    public JList<String> getListClients() {
        return listClients;
    }

    public JTextArea getTextMessageInput() {
        return textMessageInput;
    }

    public JButton getButtonSend() {
        return buttonSend;
    }
}
