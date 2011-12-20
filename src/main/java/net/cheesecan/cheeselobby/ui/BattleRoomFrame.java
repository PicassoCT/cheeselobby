/*
 *  Copyright 2011 Jahziah Wagner.
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
 * BattleRoomFrame.java
 *
 * Created on Jan 3, 2011, 2:27:50 AM
 */
package net.cheesecan.cheeselobby.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.text.DefaultCaret;
import net.cheesecan.cheeselobby.lobby_connection.interfaces.BattleRoomObserver;
import net.cheesecan.cheeselobby.io.SettingsFile;
import net.cheesecan.cheeselobby.session.ChatMessageType;
import net.cheesecan.cheeselobby.tables.BattleUserTableModel;
import net.cheesecan.cheeselobby.tables.LobbyTable;
import net.cheesecan.cheeselobby.ui.components.ColorRenderer;
import net.cheesecan.cheeselobby.ui.components.FactionRenderer;
import net.cheesecan.cheeselobby.ui.components.IconRenderer;
import net.cheesecan.cheeselobby.session.Battle;
import net.cheesecan.cheeselobby.session.User;
import net.cheesecan.cheeselobby.session.User.GameStatus;
import net.cheesecan.cheeselobby.ui.interfaces.BattleControllerFacade;
import net.cheesecan.cheeselobby.mapviewer.GraphicsPanel;
import net.cheesecan.cheeselobby.ui.components.MinimapDisplay;
import net.cheesecan.cheeselobby.ui.interfaces.DownloaderFacade;
import net.cheesecan.cheeselobby.unitsync.UnitSyncForJava;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer.BooleanRenderer;

/** TODO
 *  This class needs refactoring.
 * @author jahziah
 */
public class BattleRoomFrame extends JInternalFrame implements BattleRoomObserver, ActionListener {
    // Objects

    private BattleControllerFacade battleController;
    private SettingsFile settings;
    private DownloaderFacade downloader;
    // Panels
    private GraphicsPanel mapViewerPanel;
    // UnitSync!
    private UnitSyncForJava unitSync;
    // Fields
    private Battle battle;
    private User self;
    private long lastAutoUnspecAttempt;
    /**
     * The time in ms to wait before trying to unspec again.
     */
    private long autoUnspecAttemptInterval = 2000;

    /** Creates new form BattleRoomFrame */
    public BattleRoomFrame(BattleControllerFacade battleController, DownloaderFacade downloader, SettingsFile settings, UnitSyncForJava unitsync) {
        initComponents();

        this.unitSync = unitsync;
        this.battleController = battleController;
        this.settings = settings;
        this.downloader = downloader;

        initComponents();
        postInitComponents();
        setFrameBehaviour();
        init3DPreview();
        registerAsObserver();
    }

    private void postInitComponents() {
        // make chat autoscroll
        DefaultCaret caret = (DefaultCaret) chatTextPane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        overviewTabbedPane.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (overviewTabbedPane.getSelectedComponent() != previewPanel) {
                    disableMapViewer();
                }
            }
        });
    }

    private void disableMapViewer() {
        mapViewerComboBox.setSelectedItem("Off");   // triggers listener to disable viewer
    }

    private void setLocation() {
        // Compute size and location of frame
        pack();
        double x = NewMainFrame.getScreenSize().getWidth() / 2;
        double y = NewMainFrame.getScreenSize().getHeight() / 2;
        setLocation((int) x - getWidth() / 2, (int) ((int) y - getHeight() / 2));

        // Set icon
        setFrameIcon(new ImageIcon(getClass().getResource("/img/window/battle.png")));

        // Set title
        setTitle("Battle room");

        setResizable(true);
    }

    private void setFrameBehaviour() {
        // Set frame behaviour
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        // setTitle("Battle room");
    }

    private void registerAsObserver() {
        battleController.registerAsBattleObserver(this);
    }

    private void initUserTable() {
        // Init model
        userTableModel = new BattleUserTableModel(new ArrayList<User>(), new String[]{"Status", "Ingame", "Faction", "Color", "Country", "Rank",
                    "Name", "Team", "Ally", "CPU"});

        // Init table
        userTable = new LobbyTable(userTableModel);

        // Add table to scrollpane
        usersScrollPane.setViewportView(userTable);

        // Set column model
        setColumnModel();
    }

    private void showBattle(Battle battle) {
        this.battle = battle;

        // Init user table
        initUserTable();

        // Load minimap
        ((MinimapDisplay) minimapLabel).setMinimap(battle.getMapHash());

        // Make battle window visible
        reveal();

        // Set color
        setColor();

        // Set default close operation to leave
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addInternalFrameListener(new InternalFrameAdapter() {

            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                leave();
            }
        });
    }

    private void setColumnModel() {
        // Set flag and rank columns
        userTable.getColumnModel().getColumn(0).setCellRenderer(new IconRenderer(NewMainFrame.lobbyIcons.getStatusIcons()));
        userTable.getColumnModel().getColumn(1).setCellRenderer(new BooleanRenderer());
        userTable.getColumnModel().getColumn(2).setCellRenderer(new FactionRenderer());
        userTable.getColumnModel().getColumn(3).setCellRenderer(new ColorRenderer());
        userTable.getColumnModel().getColumn(4).setCellRenderer(new IconRenderer(NewMainFrame.lobbyIcons.getCountryIcons()));
        userTable.getColumnModel().getColumn(5).setCellRenderer(new IconRenderer(NewMainFrame.lobbyIcons.getRankIcons()));

        // Resize table columns
        // TODO these are magic numbers
        userTable.getColumnModel().getColumn(0).setMaxWidth(42);
        userTable.getColumnModel().getColumn(1).setMaxWidth(20);
        userTable.getColumnModel().getColumn(2).setMaxWidth(50);
        userTable.getColumnModel().getColumn(3).setMaxWidth(50);
        userTable.getColumnModel().getColumn(4).setMaxWidth(25);
        userTable.getColumnModel().getColumn(5).setMaxWidth(23);
        userTable.getColumnModel().getColumn(7).setMaxWidth(30);
        userTable.getColumnModel().getColumn(8).setMaxWidth(30);
        userTable.getColumnModel().getColumn(9).setMaxWidth(30);
        userTable.setRowHeight(42);

        //userTable.pack(TablePacker.ALL_ROWS, true);
    }

    private void constructInitializeAndAdd3DPreview() throws HeadlessException {
        System.out.println(previewPanel.getWidth() + " " + previewPanel.getHeight());

        // Initialize map viewer panel
        mapViewerPanel = new GraphicsPanel(368, 320);

        // Add
        previewPanel.add(mapViewerPanel);
    }

    // TODO 3d viewer is broken(not that bad)
    private void init3DPreview() {
        constructInitializeAndAdd3DPreview();

        // Add a change listener to the tabbed pane
        mapViewerComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String title = mapViewerComboBox.getSelectedItem().toString();

                if (title.equals("Off")) {
                    if (!mapViewerPanel.engineIsUninitialized()) {
                        mapViewerPanel.hide();
                    }
                    return;
                }

                System.out.println(title);

                // If engine is not running yet because we still don't have the map
                if (mapViewerPanel.engineIsUninitialized()) {
                    mapViewerPanel.init();
                }

                if (!title.equals("Off")) {
                    // Conditional initialization of OpenGL canvas in the panel if not already initialized
                    mapViewerPanel.show(unitSync, battle.getMapHash());
                }

                if (title.equals("Textured")) {
                    mapViewerPanel.setMinimapActive();
                } else if (title.equals("Heightmap")) {
                    mapViewerPanel.setHeightmapActive();
                } else if (title.equals("Metalmap")) {
                    mapViewerPanel.setMetalmapActive();
                }
            }
        });
    }

    /**
     * Called when battle window is opened to be shown.
     */
    public void reveal() {
        // Set num players label
        playerNumbersLabel.setText(battle.getNumPlayers() + "/" + battle.getNumPlayers() + battle.getNumSpectators());

        setLocation();

        setVisible(true);
    }

    /**
     * Find a color to use.
     */
    private void setColor() {
        colorButton.setBackground(getOurUser().getColorByColor());
    }

    private void _addUser(User user) {
        userTableModel.addRow(user);
        playerNumbersLabel.setText(battle.getNumPlayers() + "/" + battle.getNumPlayers() + battle.getNumSpectators());
    }

    private void _removeUser(User user) {
        userTableModel.removeRow(user);
        playerNumbersLabel.setText(battle.getNumPlayers() + "/" + battle.getNumPlayers() + battle.getNumSpectators());
    }

    private void leave() {
        System.out.println("Leaving battle.");

        // Notify battleController we are leaving
        battleController.leaveBattle();

        // disable mapViewer
        disableMapViewer();

        // Hide frame until we want to use it next time
        setVisible(false);
    }

    protected void startGame() {
        battleController.startGame();
    }

    /**
     * Used by the auto-unspec feature. Called whenever a seat becomes empty.
     * TODO:
     * Make sure we don't spam unspec status messages so that server kicks us for battle flooding.
     * This means we need a timer.
     */
    private void battleIsSeatEmpty() {
        // Check that we are not hammering the server
        if ((System.currentTimeMillis() - lastAutoUnspecAttempt) < autoUnspecAttemptInterval) {
            return;
        }
        // Check if we are too low rank to unspec
        if (battle.getRankLimit() > self.getRank()) {
            return; // do nothing
        }
        // If we can rightly attempt to unspec, and the auto-unspec button selected
        if (autoUnspecButton.isSelected()) {
            // Try to unspec
            spectatorCheckboxActionPerformed(null);
            // Note this as our latest attempt
            lastAutoUnspecAttempt = System.currentTimeMillis();
        }
    }

    private void spectate() {
        getOurUser().setGameStatus(GameStatus.SPECTATING);
        // Enable auto-unspec
        autoUnspecButton.setEnabled(true);
        // Disable other buttons
        setGUIButtonsEnabled(false);
    }

    private void unspectate() {
        getOurUser().setGameStatus(GameStatus.UNREADY);
        // Disable auto-unspec
        autoUnspecButton.setEnabled(false);
        // Enable other buttons
        setGUIButtonsEnabled(true);
    }

    /**
     * Called when host force us to spectate.
     */
    private void forceSpectator() {
        spectatorCheckbox.setSelected(true);
        spectate();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rightPanel = new JPanel();
        spectatorCheckbox = new JCheckBox();
        teamComboBox = new JComboBox();
        teamLabel = new JLabel();
        allyLabel = new JLabel();
        allyComboBox = new JComboBox();
        colorLabel = new JLabel();
        colorButton = new JButton();
        factionLabel = new JLabel();
        factionComboBox = new JComboBox();
        readyCheckBox = new JCheckBox();
        playerNumbersLabel = new JLabel();
        leaveButton = new JButton();
        startButton = new JButton();
        autoUnspecButton = new JCheckBox();
        leftPanel = new JPanel();
        chatBox = new JComboBox();
        chatScrollPane = new JScrollPane();
        chatTextPane = new JTextPane();
        sendButton = new JButton();
        usersScrollPane = new JScrollPane();
        overviewTabbedPane = new JTabbedPane();
        minimapLabel = new MinimapDisplay(downloader, this, unitSync, 384, 384);
        previewPanel = new JPanel();
        mapViewerComboBox = new JComboBox();
        mapSettings = new JPanel();
        gameSettingsLabel = new JLabel();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        rightPanel.setPreferredSize(new Dimension(130, 346));

        spectatorCheckbox.setText("Spectate");
        spectatorCheckbox.addActionListener(this);

        teamComboBox.setModel(new DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8" }));
        teamComboBox.setToolTipText("Team");
        teamComboBox.setName("Team"); // NOI18N
        teamComboBox.addActionListener(this);

        teamLabel.setText("Team:");

        allyLabel.setText("Ally:");

        allyComboBox.setModel(new DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16" }));
        allyComboBox.addActionListener(this);

        colorLabel.setText("Color:");

        colorButton.addActionListener(this);

        factionLabel.setText("Faction:");

        factionComboBox.setModel(new DefaultComboBoxModel(new String[] { "ARM", "CORE" }));
        factionComboBox.addActionListener(this);

        readyCheckBox.setText("I'm ready");
        readyCheckBox.addActionListener(this);

        leaveButton.setText("Leave");
        leaveButton.addActionListener(this);

        startButton.setText("Start");
        startButton.addActionListener(this);

        autoUnspecButton.setText("Auto-unspec");
        autoUnspecButton.setToolTipText("Automatically unspecs immediately whenever a seat becomes free.");
        autoUnspecButton.setEnabled(false);
        autoUnspecButton.addActionListener(this);

        GroupLayout rightPanelLayout = new GroupLayout(rightPanel);
        rightPanel.setLayout(rightPanelLayout);
        rightPanelLayout.setHorizontalGroup(
            rightPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rightPanelLayout.createParallelGroup(Alignment.LEADING)
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addComponent(colorLabel)
                        .addContainerGap(82, Short.MAX_VALUE))
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addComponent(spectatorCheckbox)
                        .addContainerGap(35, Short.MAX_VALUE))
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addComponent(readyCheckBox, GroupLayout.PREFERRED_SIZE, 696, GroupLayout.PREFERRED_SIZE)
                        .addGap(71, 71, 71))
                    .addGroup(Alignment.TRAILING, rightPanelLayout.createSequentialGroup()
                        .addGroup(rightPanelLayout.createParallelGroup(Alignment.TRAILING)
                            .addComponent(leaveButton, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)
                            .addComponent(startButton, GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE))
                        .addGap(679, 679, 679))
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addGroup(rightPanelLayout.createParallelGroup(Alignment.TRAILING)
                            .addComponent(colorButton, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                            .addComponent(teamComboBox, Alignment.LEADING, 0, 78, Short.MAX_VALUE)
                            .addComponent(teamLabel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                            .addComponent(factionLabel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                            .addGroup(Alignment.LEADING, rightPanelLayout.createParallelGroup(Alignment.TRAILING, false)
                                .addComponent(allyComboBox, Alignment.LEADING, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(factionComboBox, Alignment.LEADING, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(allyLabel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE))
                        .addGap(592, 592, 592)
                        .addComponent(playerNumbersLabel, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addComponent(autoUnspecButton)
                        .addContainerGap(13, Short.MAX_VALUE))))
        );
        rightPanelLayout.setVerticalGroup(
            rightPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rightPanelLayout.createParallelGroup(Alignment.LEADING)
                    .addComponent(playerNumbersLabel, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
                    .addComponent(factionLabel))
                .addGap(2, 2, 2)
                .addComponent(factionComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(allyLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(allyComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(teamLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(teamComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(colorLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(colorButton, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(spectatorCheckbox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(readyCheckBox)
                .addPreferredGap(ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addComponent(autoUnspecButton)
                .addGap(18, 18, 18)
                .addComponent(leaveButton)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(startButton))
        );

        getContentPane().add(rightPanel, BorderLayout.EAST);

        leftPanel.setPreferredSize(new Dimension(786, 256));

        chatBox.setEditable(true);
        chatBox.addActionListener(this);

        chatScrollPane.setViewportView(chatTextPane);

        sendButton.setText("Send");
        sendButton.addActionListener(this);

        GroupLayout leftPanelLayout = new GroupLayout(leftPanel);
        leftPanel.setLayout(leftPanelLayout);
        leftPanelLayout.setHorizontalGroup(
            leftPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addGroup(leftPanelLayout.createParallelGroup(Alignment.LEADING)
                    .addGroup(Alignment.TRAILING, leftPanelLayout.createSequentialGroup()
                        .addComponent(chatBox, 0, 1098, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(sendButton, GroupLayout.PREFERRED_SIZE, 71, GroupLayout.PREFERRED_SIZE))
                    .addComponent(chatScrollPane, GroupLayout.DEFAULT_SIZE, 1175, Short.MAX_VALUE))
                .addContainerGap())
        );
        leftPanelLayout.setVerticalGroup(
            leftPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, leftPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chatScrollPane, GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(leftPanelLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(chatBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(sendButton))
                .addContainerGap())
        );

        getContentPane().add(leftPanel, BorderLayout.SOUTH);
        getContentPane().add(usersScrollPane, BorderLayout.CENTER);

        overviewTabbedPane.setMinimumSize(new Dimension(384, 384));
        overviewTabbedPane.setPreferredSize(new Dimension(384, 384));
        overviewTabbedPane.addTab("Minimap", minimapLabel);

        mapViewerComboBox.setModel(new DefaultComboBoxModel(new String[] { "Off", "Textured", "Heightmap", "Metalmap" }));

        GroupLayout previewPanelLayout = new GroupLayout(previewPanel);
        previewPanel.setLayout(previewPanelLayout);
        previewPanelLayout.setHorizontalGroup(
            previewPanelLayout.createParallelGroup(Alignment.LEADING)
            .addComponent(mapViewerComboBox, 0, 376, Short.MAX_VALUE)
        );
        previewPanelLayout.setVerticalGroup(
            previewPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, previewPanelLayout.createSequentialGroup()
                .addContainerGap(360, Short.MAX_VALUE)
                .addComponent(mapViewerComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );

        overviewTabbedPane.addTab("3D Preview", previewPanel);

        GroupLayout mapSettingsLayout = new GroupLayout(mapSettings);
        mapSettings.setLayout(mapSettingsLayout);
        mapSettingsLayout.setHorizontalGroup(
            mapSettingsLayout.createParallelGroup(Alignment.LEADING)
            .addGap(0, 376, Short.MAX_VALUE)
        );
        mapSettingsLayout.setVerticalGroup(
            mapSettingsLayout.createParallelGroup(Alignment.LEADING)
            .addGap(0, 385, Short.MAX_VALUE)
        );

        overviewTabbedPane.addTab("Map information", mapSettings);
        overviewTabbedPane.addTab("Game settings", gameSettingsLabel);

        getContentPane().add(overviewTabbedPane, BorderLayout.LINE_START);

        pack();
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == spectatorCheckbox) {
            BattleRoomFrame.this.spectatorCheckboxActionPerformed(evt);
        }
        else if (evt.getSource() == teamComboBox) {
            BattleRoomFrame.this.teamComboBoxActionPerformed(evt);
        }
        else if (evt.getSource() == allyComboBox) {
            BattleRoomFrame.this.allyComboBoxActionPerformed(evt);
        }
        else if (evt.getSource() == colorButton) {
            BattleRoomFrame.this.colorButtonActionPerformed(evt);
        }
        else if (evt.getSource() == factionComboBox) {
            BattleRoomFrame.this.factionComboBoxActionPerformed(evt);
        }
        else if (evt.getSource() == readyCheckBox) {
            BattleRoomFrame.this.readyCheckBoxActionPerformed(evt);
        }
        else if (evt.getSource() == leaveButton) {
            BattleRoomFrame.this.leaveButtonActionPerformed(evt);
        }
        else if (evt.getSource() == startButton) {
            BattleRoomFrame.this.startButtonActionPerformed(evt);
        }
        else if (evt.getSource() == autoUnspecButton) {
            BattleRoomFrame.this.autoUnspecButtonActionPerformed(evt);
        }
        else if (evt.getSource() == chatBox) {
            BattleRoomFrame.this.chatBoxActionPerformed(evt);
        }
        else if (evt.getSource() == sendButton) {
            BattleRoomFrame.this.sendButtonActionPerformed(evt);
        }
    }// </editor-fold>//GEN-END:initComponents

    private void colorButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_colorButtonActionPerformed
        // Show color chooser
        Color retval = JColorChooser.showDialog(this, "Choose your player color", colorButton.getBackground());

        // Set button color
        colorButton.setBackground(retval);

        // Update our users color
        getOurUser().setColor(retval);

        // Notify battleController
        battleController.sendMyBattleStatus(getOurUser().getBattleStatus(), getOurUser().getColor());
    }//GEN-LAST:event_colorButtonActionPerformed

    private void leaveButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_leaveButtonActionPerformed
        leave();
    }//GEN-LAST:event_leaveButtonActionPerformed

    private void startButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        startGame();
    }//GEN-LAST:event_startButtonActionPerformed

    private void sendButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_sendButtonActionPerformed
        // Get typed msg
        String msg = chatBox.getSelectedItem().toString();

        // If contains a /me then it is sayExpressions
        if (msg.startsWith("/me")) {
            battleController.sayExpressionBattle(msg.substring(3));
        } else {    // this is a regular message
            battleController.sayBattle(msg);
        }

        // Clear box
        chatBox.setSelectedItem("");
    }//GEN-LAST:event_sendButtonActionPerformed

    private void readyCheckBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_readyCheckBoxActionPerformed
        // Update our users game status
        if (readyCheckBox.isSelected()) {
            getOurUser().setGameStatus(GameStatus.READY);
        } else {
            getOurUser().setGameStatus(GameStatus.UNREADY);
        }

        // Notify battle observer
        battleController.sendMyBattleStatus(getOurUser().getBattleStatus(), getOurUser().getColor());
    }//GEN-LAST:event_readyCheckBoxActionPerformed

    private void spectatorCheckboxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_spectatorCheckboxActionPerformed
        // Update our users game status
        if (spectatorCheckbox.isSelected()) {
            spectate();
        } else {
            unspectate();
        }
        // Notify battle observer
        battleController.sendMyBattleStatus(getOurUser().getBattleStatus(), getOurUser().getColor());
    }//GEN-LAST:event_spectatorCheckboxActionPerformed

    private void teamComboBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_teamComboBoxActionPerformed
        // Update our users ready status
        getOurUser().setTeam(Integer.valueOf(teamComboBox.getSelectedItem().toString()));

        // Notify battle observer
        battleController.sendMyBattleStatus(getOurUser().getBattleStatus(), getOurUser().getColor());
    }//GEN-LAST:event_teamComboBoxActionPerformed

    private void allyComboBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_allyComboBoxActionPerformed
        // Update our users ready status
        getOurUser().setAlly(Integer.valueOf(allyComboBox.getSelectedItem().toString()));

        // Notify battle observer
        battleController.sendMyBattleStatus(getOurUser().getBattleStatus(), getOurUser().getColor());
    }//GEN-LAST:event_allyComboBoxActionPerformed

    private void factionComboBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_factionComboBoxActionPerformed
        // Update our users ready status
        getOurUser().setFaction(Integer.valueOf(factionComboBox.getSelectedIndex()));

        // Notify battle observer
        battleController.sendMyBattleStatus(getOurUser().getBattleStatus(), getOurUser().getColor());
    }//GEN-LAST:event_factionComboBoxActionPerformed

    private void chatBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_chatBoxActionPerformed
        if (evt.getActionCommand().equals("comboBoxEdited")) {
            sendButtonActionPerformed(null);
        }
    }//GEN-LAST:event_chatBoxActionPerformed

    private void autoUnspecButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_autoUnspecButtonActionPerformed
        // Check if we can already unspec immediately
        spectatorCheckboxActionPerformed(null);
    }//GEN-LAST:event_autoUnspecButtonActionPerformed

    private void setGUIButtonsEnabled(boolean enabled) {
        readyCheckBox.setEnabled(enabled);
        allyComboBox.setEnabled(enabled);
        teamComboBox.setEnabled(enabled);
        factionComboBox.setEnabled(enabled);
        colorButton.setEnabled(enabled);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JComboBox allyComboBox;
    private JLabel allyLabel;
    private JCheckBox autoUnspecButton;
    private JComboBox chatBox;
    private JScrollPane chatScrollPane;
    private JTextPane chatTextPane;
    private JButton colorButton;
    private JLabel colorLabel;
    private JComboBox factionComboBox;
    private JLabel factionLabel;
    private JLabel gameSettingsLabel;
    private JButton leaveButton;
    private JPanel leftPanel;
    private JPanel mapSettings;
    private JComboBox mapViewerComboBox;
    private JLabel minimapLabel;
    private JTabbedPane overviewTabbedPane;
    private JLabel playerNumbersLabel;
    private JPanel previewPanel;
    private JCheckBox readyCheckBox;
    private JPanel rightPanel;
    private JButton sendButton;
    private JCheckBox spectatorCheckbox;
    private JButton startButton;
    private JComboBox teamComboBox;
    private JLabel teamLabel;
    private JScrollPane usersScrollPane;
    // End of variables declaration//GEN-END:variables
    private LobbyTable userTable;
    private BattleUserTableModel userTableModel;

    @Override
    public void displayBattle(final Battle battle) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                // Show battle
                showBattle(battle);

                // Update column model
                setColumnModel();
            }
        });
    }

    @Override
    public void addUser(final User user) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                _addUser(user);
                // Update column model
                setColumnModel();
            }
        });
    }

    @Override
    public void removeUser(final User user) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                _removeUser(user);
                // Notify that seat is empty
                battleIsSeatEmpty();
                // Update column model
                setColumnModel();
            }
        });
    }

    @Override
    public void updateUser(final User user, final GameStatus previousMode) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                _removeUser(user);
                _addUser(user);

                // If the user changed from player to spectator, the auto-unspec feature should know
                if (previousMode == GameStatus.UNREADY || previousMode == GameStatus.READY && user.isSpectator()) {
                    battleIsSeatEmpty();
                }

                // If this is our own user, and our status was forcibly changed, update GUI to match
                if (user.getName().equals(battleController.getUsername())) {
                    if (user.isSpectator()) {
                        // Force us to spectate
                        forceSpectator();
                    }
                }

                // Update column model
                setColumnModel();
            }
        });
    }

    @Override
    public void closeBattle() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                ///throw new UnsupportedOperationException("Not supported yet.");
            }
        });
    }

    @Override
    public void setOwnUser(User self) {
        this.self = self;
    }

    private User getOurUser() {
        return self;
    }

    @Override
    public void said(final String sender, final String msg) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                ChatFrame.appendToTextPane(sender, chatTextPane, msg, ChatMessageType.AnotherUserSaid, 0);
            }
        });
    }

    @Override
    public void saidExpression(final String sender, final String msg) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                ChatFrame.appendToTextPane(sender, chatTextPane, msg, ChatMessageType.Expression, 0);
            }
        });
    }

    @Override
    public void showErrorMessage(final String msg, final String title) {
        final BattleRoomFrame thisRef = this;
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                JOptionPane.showMessageDialog(thisRef, msg, title, JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    @Override
    public void kickedFromBattle() {
        // Show boot msg
        JOptionPane.showMessageDialog(this, "You were kicked from the battle.");

        // Hide frame until we want to use it next time
        setVisible(false);
    }

    public Battle getRelevantBattle() {
        return battle;
    }

    public void fireRefreshFromDownloader() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                ((MinimapDisplay) minimapLabel).setMinimap(battle.getMapHash());
                //minimapLabel.validate();
                //minimapLabel.repaint();
                toFront();
            }
        });
    }
}
