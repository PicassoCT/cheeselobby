/*
 *  Copyright 2010 Jahziah Wagner <jahziah[dot]wagner[at]gmail.com>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

/*
 * ChatFrame.java
 *
 * Created on Dec 30, 2010, 5:12:06 PM
 */
package net.cheesecan.cheeselobby.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import net.cheesecan.cheeselobby.SessionController;
import net.cheesecan.cheeselobby.io.SettingsFile;
import net.cheesecan.cheeselobby.lobby_connection.interfaces.ChatObserver;
import net.cheesecan.cheeselobby.tables.TablePacker;
import net.cheesecan.cheeselobby.session.ChatMessageType;
import net.cheesecan.cheeselobby.tables.LobbyTable;
import net.cheesecan.cheeselobby.tables.LobbyTableModel;
import net.cheesecan.cheeselobby.ui.components.IconRenderer;
import net.cheesecan.cheeselobby.session.User;
import net.cheesecan.cheeselobby.ui.interfaces.ChatControllerFacade;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.SubstanceConstants.TabCloseKind;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer.BooleanRenderer;
import org.pushingpixels.substance.api.tabbed.TabCloseCallback;

/**
 *
 * @author jahziah
 */
public class ChatFrame extends JInternalFrame implements ActionListener, ChangeListener, ChatObserver {

    // Members
    private ChatControllerFacade chatController;
    private SettingsFile settings;
    // Containers
    private HashMap<String, LobbyTableModel<User>> userTableModels;
    private HashMap<String, JTextPane> channels;
    private HashMap<String, JTextPane> privateConversations;

    /** Creates new form ChatFrame */
    public ChatFrame(ChatControllerFacade controller, SettingsFile settings) {
        chatController = controller;

        this.settings = settings;

        // Register as a chatObserver
        chatController.registerChatObserver(this);

        // Initialize userTableModels
        userTableModels = new HashMap<String, LobbyTableModel<User>>();

        // Initialize channels and pms
        channels = new HashMap<String, JTextPane>();
        privateConversations = new HashMap<String, JTextPane>();

        // Initialize server channel
        //channels.put(TasServer.SERVER_NAME, new JTextPane());

        initComponents();

        initServerUserTable();

        // Add server channel tab to tabbedPane
        _openChatPanel(settings.getServerName());
        //tabbedPane.addTab(TasServer.SERVER_NAME, channels.get(TasServer.SERVER_NAME));

        // Add a change listener to tabbedPane
        tabbedPane.addChangeListener(this);

        // Set icon
        setFrameIcon(new ImageIcon(getClass().getResource("/img/window/chat.png")));

        // Set title
        setTitle("Chat");

        // Make tabs closable
        TabCloseCallback closedTab = new TabCloseCallback() {

            @Override
            public TabCloseKind onAreaClick(JTabbedPane tabbedPane, int tabIndex, MouseEvent mouseEvent) {
                return TabCloseKind.NONE;
            }

            @Override
            public TabCloseKind onCloseButtonClick(JTabbedPane tabbedPane, int tabIndex, MouseEvent mouseEvent) {
                return closingTab(tabIndex);
            }

            @Override
            public String getAreaTooltip(JTabbedPane tabbedPane, int tabIndex) {
                return "";
            }

            @Override
            public String getCloseButtonTooltip(JTabbedPane tabbedPane, int tabIndex) {
                return "Click to close tab.";
            }
        };

        tabbedPane.putClientProperty(SubstanceLookAndFeel.TABBED_PANE_CLOSE_BUTTONS_PROPERTY, Boolean.TRUE);

        tabbedPane.putClientProperty(SubstanceLookAndFeel.TABBED_PANE_CLOSE_CALLBACK, closedTab);

        setDividerLocation();
        
        pingLabel.setToolTipText("Updated every " + SessionController.getMinPingDelay()/1000 + " seconds.");
    }

    private void initServerUserTable() {
        // Initialize data for table model
        LobbyTableModel<User> serverTableModel = new LobbyTableModel<User>(new ArrayList<User>(), new String[]{"Status", "Name", "Country", "Rank"}, 1);
        userTableModels.put(settings.getServerName(), serverTableModel);

        // Initialize table
        userTable = new LobbyTable(serverTableModel);

        // Initialize menu
        initPopupMenu();

        // Add
        usersScrollPane.setViewportView(userTable);
    }

    /**
     * User is trying to close a tab.
     * @param tabIndex tab index in tabbedPane
     */
    private TabCloseKind closingTab(int tabIndex) {
        String tabTitle = tabbedPane.getTitleAt(tabIndex);

        // Is this the server tab
        if (tabTitle.equals(settings.getServerName())) {
            return TabCloseKind.NONE;   // closing server tab is not allowed
        } // Is this a private conversation
        else if (privateConversations.containsKey(tabTitle)) {
            // Remove private conversation
            privateConversations.remove(tabTitle);

            return TabCloseKind.THIS;
        } // Is this a channel
        else if (channels.containsKey(tabTitle)) {
            // Remove channel
            channels.remove(tabTitle);

            // Remove channel user table model
            userTableModels.remove(tabTitle);

            // Send chat controller notice that it should issue a leave user message
            chatController.leaveChannel(tabTitle);

            return TabCloseKind.THIS;
        } // Else should never occur
        else {
            throw new UnsupportedOperationException("Tried to close tab at index " + tabIndex + " that is neither a channel or PM.");
        }
    }

    private void initPopupMenu() {
        menu = new JPopupMenu("");
        final JMenuItem openChat = new JMenuItem("Open chat");
        final JMenuItem joinSameBattle = new JMenuItem("Join same battle");

        // Create action listener
        ActionListener listener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // Get row selected
                int row = userTable.getSelectedRow();
                int col = 1;    //   1 is username column
                // Get username at that row
                String username = userTable.getModel().getValueAt(row, col).toString();

                if(e.getSource() == openChat) {
                    // Open chat if this is a valid user
                    if (username != null && chatController.isValidUsername(username)) {
                        openPrivateConversation(username);
                    }
                }
                else if(e.getSource() == joinSameBattle) {
                    chatController.joinSameBattleAsUser(username);
                }
            }
        };

        openChat.addActionListener(listener);
        joinSameBattle.addActionListener(listener);

        menu.add(openChat);
        menu.add(joinSameBattle);

        userTable.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            private void showPopup(MouseEvent e) {
                menu.show(e.getComponent(), e.getX(), e.getY());

                // Select row
                performRightMouseClickAction(e);
            }

            public void performRightMouseClickAction(MouseEvent e) {
                int row = userTable.rowAtPoint(e.getPoint());
                int col = userTable.columnAtPoint(e.getPoint());
                userTable.getSelectionModel().setSelectionInterval(row, row);
            }
        });
    }

    /**
     * This method should be used if you are calling openChatPanel from the EDT.
     */
    private void _openChatPanel(final String name) {
        // Initialize and set name of the new chat tab
        JTextPane newTab = new JTextPane();
        newTab.setName(name);
        newTab.setEditable(false);

        // make it autoscroll
        DefaultCaret caret = (DefaultCaret)newTab.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        // Initialize scrollpane
        JScrollPane container = new JScrollPane();
        container.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        container.setViewportView(newTab);

        // Add to channels hash map
        channels.put(name, newTab);

        // Create user table model for channel
        userTableModels.put(name, new LobbyTableModel<User>(new ArrayList<User>(), new String[]{"Status", "Name", "Country", "Rank"}, 1));

        // Add to tabbed pane
        tabbedPane.addTab(name, container);

        // Set tabbed pane to show that tab
        tabbedPane.setSelectedComponent(container);

        // Column model must be reconfigured
        setColumnModel();
    }

    private void openPrivateConversation(String otherParty) {
        // Add new text pane
        JTextPane newTab = new JTextPane();
        newTab.setName(otherParty);
        newTab.setEditable(false);
        privateConversations.put(otherParty, newTab);

        // Initialize scrollpane
        JScrollPane container = new JScrollPane();
        container.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        container.setViewportView(newTab);

        // Add text pane to tabbedPane
        tabbedPane.addTab(otherParty, container);

        // Column model must be reconfigured
        setColumnModel();

        // TODO make pm tab blink or something similar
    }

    private void say() {
        // If tabbedpane is not initialized
        if (tabbedPane == null) {
            return;
        }
        // Check where we are trying to do say
        String text = (String) chatBox.getSelectedItem();
        // If empty
        if (text.equals("")) {
            return;
        }
        // If we're sending a chat command
        // Check if the user did a "/", this is actually an application command
        if (text.charAt(0) == '/') {
            // trying to join a channel
            //System.out.println(chatBox.getText().substring(0, 5));

            if (text.substring(0, 5).toLowerCase().equals("/join")) {
                String channelName = text.substring(6, text.length()).toLowerCase();
                if (channelName.charAt(0) == '#') {  // remove the '#'
                    channelName = channelName.substring(1, channelName.length());
                }
                chatController.joinChannel(channelName);
            }
            else if(text.substring(0,7).equals("/rename")) {
                String name = text.substring(8, text.length());
                chatController.rename(name);
            }
        } // If we're trying to do say in the server tab, don't allow that unless it's a command
        else if (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()).equals(settings.getServerName())) {
            return;
        } // Else if this is a message sent in a proper channel or private conversation
        else {
            // If we are in a private conversation
            if (privateConversations.containsKey(tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()))) {
                chatController.sayPrivate(tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()), text);
            } else {  // We are in a regular channel
                chatController.say(text, tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()));
            }
        }
        chatBox.setSelectedItem("");
    }

    private static String timestampMessage(String msg) {
        // Add timestamp to msg
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String time = sdf.format(cal.getTime());

        String newMsg = "[" + time + "] " + msg;

        return newMsg;
    }

    private void setColumnModel() {
        // Set flag and rank columns
        userTable.getColumnModel().getColumn(0).setCellRenderer(new IconRenderer(NewMainFrame.lobbyIcons.getPlayerStatusIcons()));
        userTable.getColumnModel().getColumn(2).setCellRenderer(new IconRenderer(NewMainFrame.lobbyIcons.getCountryIcons()));
        userTable.getColumnModel().getColumn(3).setCellRenderer(new IconRenderer(NewMainFrame.lobbyIcons.getRankIcons()));

        userTable.getColumnModel().getColumn(0).setMaxWidth(32);
        userTable.getColumnModel().getColumn(2).setMaxWidth(16);
        userTable.getColumnModel().getColumn(3).setMaxWidth(23);
        userTable.setRowHeight(42);

        //userTable.pack(TablePacker.ALL_ROWS, true);
    }

    private void setDividerLocation() {
        pack();

        // Set divider location
        double factor = getWidth() - userTable.getColumnModel().getTotalColumnWidth();
        splitPane.setDividerLocation((int) factor);

        // If not yet deiconified
        if (isIcon()) {
            return;
        } else { // Else pack tables
            userTable.pack(TablePacker.ALL_ROWS, false);
        }

        int diff = userTable.getColumnModel().getTotalColumnWidth() - userTable.getWidth();

        setSize(getWidth() + diff, getHeight());

        setResizable(true);

        setPosition();
    }

    private void setPosition() {
         // Compute size and location of frame
        double x = NewMainFrame.getScreenSize().getWidth();
        double y = NewMainFrame.getScreenSize().getHeight();
        setLocation((int) x -getWidth(), 0);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sendButton = new JButton();
        splitPane = new JSplitPane();
        tabbedPane = new JTabbedPane();
        usersScrollPane = new JScrollPane();
        chatBox = new JComboBox();
        pingLabel = new JLabel();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setIconifiable(true);
        setMaximizable(true);

        sendButton.setText("Send");
        sendButton.setPreferredSize(new Dimension(25, 30));
        sendButton.addActionListener(this);
        getContentPane().add(sendButton, BorderLayout.CENTER);

        splitPane.setDividerLocation(500);
        splitPane.setPreferredSize(new Dimension(16, 400));

        tabbedPane.addChangeListener(this);
        splitPane.setLeftComponent(tabbedPane);
        splitPane.setRightComponent(usersScrollPane);

        getContentPane().add(splitPane, BorderLayout.NORTH);

        chatBox.setEditable(true);
        chatBox.setPreferredSize(new Dimension(700, 28));
        chatBox.addActionListener(this);
        getContentPane().add(chatBox, BorderLayout.WEST);

        pingLabel.setText("Ping:");
        getContentPane().add(pingLabel, BorderLayout.EAST);

        pack();
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == sendButton) {
            ChatFrame.this.sendButtonActionPerformed(evt);
        }
        else if (evt.getSource() == chatBox) {
            ChatFrame.this.chatBoxActionPerformed(evt);
        }
    }

    public void stateChanged(javax.swing.event.ChangeEvent evt) {
        if (evt.getSource() == tabbedPane) {
            ChatFrame.this.tabbedPaneStateChanged(evt);
        }
    }// </editor-fold>//GEN-END:initComponents

    private void sendButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_sendButtonActionPerformed
        // Get text on chat box
        say();
    }//GEN-LAST:event_sendButtonActionPerformed

    private void chatBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_chatBoxActionPerformed
        // JComboBox does not registerChatObserver low-level events such as keyboard events, so we have to use a different approach
        // http://download.oracle.com/javase/tutorial/uiswing/events/eventsandcomponents.html
        // There are two types of events here: comboBoxEdited and comboBoxChanged
        // We are listening for comboBoxEdited
        if (evt.getActionCommand().equals("comboBoxEdited")) {
            sendButtonActionPerformed(null);
        }
    }//GEN-LAST:event_chatBoxActionPerformed

    private void tabbedPaneStateChanged(ChangeEvent evt) {//GEN-FIRST:event_tabbedPaneStateChanged
        // Get user table model associated with current tab
        LobbyTableModel<User> cur = userTableModels.get(tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()));

        // If it is null then we are in a private conversation, so we need to hide the user table
        if (cur == null) {
            usersScrollPane.setVisible(false);
            userTable.setVisible(false);
        } else { // Else we changed tabbedPane to show a channel
            // Set user table
            userTable.setModel(cur);

            // Column model must be reconfigured
            setColumnModel();

            // Make sure scroll pane is enabled
            if (!usersScrollPane.isVisible()) {
                usersScrollPane.setVisible(true);
                setDividerLocation();
                userTable.setVisible(true);
            }
        }
    }//GEN-LAST:event_tabbedPaneStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JComboBox chatBox;
    private JLabel pingLabel;
    private JButton sendButton;
    private JSplitPane splitPane;
    private JTabbedPane tabbedPane;
    private JScrollPane usersScrollPane;
    // End of variables declaration//GEN-END:variables
    LobbyTable userTable;
    private JPopupMenu menu;

    @Override
    public String getActiveChannel() {
        String currentChannelName = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
        return currentChannelName;
    }

    @Override
    public void openChatPanel(final String name) {
        // Caller must wait for this call to finish or risk trying to access an un-initialized table
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    _openChatPanel(name);
                }
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(ChatFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(ChatFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void closeChannelTab(String channel) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
            }
        });
    }

    @Override
    public void messageOfTheDay(final String line) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                appendToTextPane(settings.getServerName(), channels.get(settings.getServerName()), line, ChatMessageType.MOTD, 0);
            }
        });
    }

    @Override
    public void setTopic(final String channel, final String author, final long changedTime, final String topic) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                System.out.println("***Settings channel topic***");
                appendToTextPane(author, channels.get(channel), topic, ChatMessageType.Topic, changedTime);
            }
        });
    }

    @Override
    public void notifyChatMessage(final String channel, final String sender, final String msg, boolean isExpression) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                appendToTextPane(sender, channels.get(channel), msg, ChatMessageType.AnotherUserSaid, 0);
            }
        });
    }

    @Override
    public void notifyPrivateChatMessage(final String ourselves, final String otherParty, final String msg, final boolean echo) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                // If this is a brand new private conversation, create a new pane for it
                if (!privateConversations.containsKey(otherParty)) {
                    openPrivateConversation(otherParty);
                }
                // mesage is from us
                if (echo) {
                    appendToTextPane(ourselves, privateConversations.get(otherParty), msg, ChatMessageType.PrivateMessageFromAnotherUser, 0);
                } else {  // message is not from us
                    appendToTextPane(otherParty, privateConversations.get(otherParty), msg, ChatMessageType.PrivateMessageFromMe, 0);
                }

            }
        });
    }

    @Override
    public void notifyBroadcastMessage(String channel, String message) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                //appendToTextPane(sender, "", msg, ChatMessageType.AnotherUserSaid);
            }
        });
    }

    protected static void appendToTextPane(String sender, JTextPane pane, String message, ChatMessageType messageType, long changedTime) {
        if (pane == null) {
            System.err.println("Tried to append text to pane that does not exist.");
            return;
        }

        // Format the line
        String msg;
        if (sender.equals("")) {
            msg = timestampMessage(message);
        } else {
            msg = timestampMessage(sender + "> " + message);
        }

        // Get color schemes
        SubstanceColorScheme colScheme = SubstanceLookAndFeel.getCurrentSkin().getColorScheme(pane, ComponentState.DEFAULT);
        SimpleAttributeSet style = new SimpleAttributeSet();

        // Set string
        String text = pane.getText() + msg + "\n";

        switch (messageType) {
            case PrivateMessageFromAnotherUser:
                StyleConstants.setForeground(style, colScheme.getUltraLightColor().brighter());
                break;
            case PrivateMessageFromMe:
                StyleConstants.setForeground(style, colScheme.getUltraLightColor().brighter());
                break;
            case AnotherUserSaid:
                StyleConstants.setForeground(style, colScheme.getUltraLightColor().brighter());
                break;
            case ISaid:
                StyleConstants.setForeground(style, colScheme.getUltraLightColor().brighter());
                break;
            case MOTD:
                StyleConstants.setForeground(style, colScheme.shiftBackground(Color.magenta, 0.25).getLightColor());
                break;
            case ServerMessage:
                StyleConstants.setForeground(style, colScheme.shiftBackground(Color.orange, 0.55).getLightColor());
                break;
            case Topic:
                StyleConstants.setForeground(style, colScheme.shiftBackground(Color.white, 0.75).getLightColor());
                Calendar cur = Calendar.getInstance();
                cur.setTimeInMillis(changedTime);
                msg = "*Topic: " + message + "*\nSet by " + sender + " on " + cur.getTime().toLocaleString() + " GMT " + cur.getTime().getTimezoneOffset() / 60 + ".";
                break;
            case Expression:
                StyleConstants.setForeground(style, colScheme.shiftBackground(Color.magenta, 0.50).getLightColor());
                break;
        }

        // Insert string
        if (messageType == ChatMessageType.MOTD) { // we don't append MOTD in the same way
            pane.setText(text);
        } else {  // not a MOTD
            try {
                pane.getDocument().insertString(pane.getDocument().getLength(), msg + "\n", style);
            } catch (BadLocationException ex) {
                Logger.getLogger(ChatFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // Scroll down on the text pane's scroll pane
        

        // Repaint
        pane.repaint();
    }

    @Override
    public void addUser(final String channel, final User user) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                // Check that we are still in said channel
                if (!channels.containsKey(channel)) {
                    return;
                }

                // Get table for that channel and add user to it
                userTableModels.get(channel).addRow(user);

                // Column model must be reconfigured
                setColumnModel();
            }
        });
    }

    @Override
    public void removeUser(final String channel, final User user, final String reason) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                // Check that we are still in said channel
                if (!channels.containsKey(channel)) {
                    return;
                }

                // Get table for that channel and remove user from it
                userTableModels.get(channel).removeRow(user);

                // Print a notice in the channel if the user left a reason
                if (!reason.equals("")) {
                    appendToTextPane("", channels.get(channel), user.getName() + " left the channel( " + reason + " )", ChatMessageType.ServerMessage, 0);
                }

                // Column model must be reconfigured
                setColumnModel();
            }
        });
    }

    @Override
    public void notifyLatency(final long latency) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                pingLabel.setText("Ping: " + latency + "ms");
            }
        });
    }

    @Override
    public void joinFailed(final String reason) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                appendToTextPane("Server", channels.get(settings.getServerName()),
                        reason, ChatMessageType.ServerMessage, System.currentTimeMillis());
            }
        });
    }

}
