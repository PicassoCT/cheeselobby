/*
 *  Copyright 2011 Jahziah Wagner <jahziah[dot]wagner[at]gmail.com>.
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
 * BattleListFrame.java
 *
 * Created on Dec 24, 2010, 12:59:57 PM
 */
package net.cheesecan.cheeselobby.ui;

import java.awt.CardLayout;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;
import net.cheesecan.cheeselobby.lobby_connection.interfaces.BattleListObserver;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import net.cheesecan.cheeselobby.tables.TablePacker;
import net.cheesecan.cheeselobby.tables.LobbyTable;
import net.cheesecan.cheeselobby.tables.LobbyTableModel;
import net.cheesecan.cheeselobby.ui.components.IconRenderer;
import net.cheesecan.cheeselobby.ui.components.PasswordIconRenderer;
import net.cheesecan.cheeselobby.session.Battle;
import net.cheesecan.cheeselobby.session.User;
import net.cheesecan.cheeselobby.ui.components.GameTitleRenderer;
import net.cheesecan.cheeselobby.ui.components.MinimapDisplay;
import net.cheesecan.cheeselobby.ui.interfaces.BattleListControllerFacade;
import net.cheesecan.cheeselobby.ui.interfaces.DownloaderFacade;
import net.cheesecan.cheeselobby.unitsync.UnitSyncForJava;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer.BooleanRenderer;

/**
 *
 * @author jahziah
 */
public class BattleListFrame extends JInternalFrame implements ActionListener, BattleListObserver {

    private BattleListControllerFacade battleListController;
    private NewMainFrame parent;
    private UnitSyncForJava unitsync;
    private DownloaderFacade downloader;

    /** Creates new form BattleListFrame */
    public BattleListFrame(BattleListControllerFacade battleController, NewMainFrame parent, UnitSyncForJava unitSync, DownloaderFacade downloader) {
        this.battleListController = battleController;
        this.parent = parent;
        this.unitsync = unitSync;
        this.downloader = downloader;

        initComponents();
        postInitComponents();
        initBattlesTable();
        initBattleViewTable();
        registerAsObserver();
        setLocation();
    }

    private void postInitComponents() {
        // Set frame behaviour
        setTitle("Battles");

        // Set icon
        setFrameIcon(new ImageIcon(getClass().getResource("/img/window/blist.png")));

        this.addInternalFrameListener(new InternalFrameAdapter() {

            @Override
            public void internalFrameDeiconified(InternalFrameEvent e) {
                reveal();
            }
        });
        
        setPreferredSize(new Dimension((int)NewMainFrame.getScreenSize().getWidth(),
               800));
    }

    private void initBattlesTable() {
        // Initialize user model for battles table
        battleModel = new LobbyTableModel<Battle>(new ArrayList<Battle>(), new String[]{"", "", "Name", "Creator", "", "Mod", "Map", "Players", "Spectators"}, 7);

        // Initialize battles table
        battlesTable = new LobbyTable(battleModel);

        // Add to scrollpane
        battlesScrollPane.setViewportView(battlesTable);

        // Add listener for mouse
        battlesTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    performLeftMouseClickAction(e);
                } // Right mouse click
                else if (SwingUtilities.isRightMouseButton(e)) {
                    performRightMouseClickAction(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // TODO 
            }

            private void performLeftMouseClickAction(MouseEvent e) {
                // Get value at row,col
                Battle b = getCurrentlySelectedBattle();

                if (b == null) {
                    return;
                }

                int battleId = b.getBattleId();

                // Set data in battle view model
                battleViewModel.setData(battleListController.getUsersInBattle(battleId));

                // Set map preview
                setMapPreview(b.getMapHash());

                // Set columns
                setColumnModelBattleView();
            }

            private void performRightMouseClickAction(MouseEvent e) {
            }
        });
        // TODO Add listener for keys (up/down)

    }

    private Battle getCurrentlySelectedBattle() {
        // Get battle id
        int row = battlesTable.getSelectedRow();

        // If row is -1 then view was reset TODO resolve this
        if (row == -1) {
            return null;
        }

        // Get value at row,col
        return ((Battle) ((LobbyTableModel) battlesTable.getModel()).getData().get(row));
    }

    private void initBattleViewTable() {
        // Initialize battle view model
        battleViewModel = new LobbyTableModel<User>(new ArrayList<User>(), new String[]{"Status", "Name", "Country", "Rank"}, 2);

        // Initialize battle view table
        battleViewTable = new LobbyTable(battleViewModel);

        // Add to scrollpane
        usersScrollPane.setViewportView(battleViewTable);
    }

    private void setLocation() {
        // Set location
        pack();
        setLocation(0, 0);
        setResizable(true);
    }

    private void setColumnModelBattleListTable() {
        // Set flag and rank columns
        battlesTable.getColumnModel().getColumn(0).setCellRenderer(new IconRenderer(NewMainFrame.lobbyIcons.getBattleStatusIcons()));
        battlesTable.getColumnModel().getColumn(1).setCellRenderer(new PasswordIconRenderer());
        battlesTable.getColumnModel().getColumn(2).setCellRenderer(new GameTitleRenderer());
        battlesTable.getColumnModel().getColumn(4).setCellRenderer(new IconRenderer(NewMainFrame.lobbyIcons.getRankIcons()));

        battlesTable.getColumnModel().getColumn(0).setMaxWidth(32);
        battlesTable.getColumnModel().getColumn(1).setMaxWidth(32);
        battlesTable.getColumnModel().getColumn(4).setMaxWidth(23);
        battlesTable.getColumnModel().getColumn(0).setWidth(32);
        battlesTable.getColumnModel().getColumn(1).setWidth(32);
        battlesTable.getColumnModel().getColumn(4).setWidth(23);

        // battlesTable.getColumnModel().getColumn(7).setMaxWidth(32);
        // battlesTable.getColumnModel().getColumn(7).setWidth(32);
        //battlesTable.getColumnModel().getColumn(8).setMaxWidth(32);
        //battlesTable.getColumnModel().getColumn(8).setWidth(32);
        battlesTable.setRowHeight(42);

        // If not yet deiconified
        if (isIcon()) {
            return;
        } else { // Else pack tables
            battlesTable.pack(TablePacker.ALL_ROWS, false);
        }
    }

    private void setColumnModelBattleView() {
        // Set flag and rank columns
        battleViewTable.getColumnModel().getColumn(0).setCellRenderer(new BooleanRenderer());
        battleViewTable.getColumnModel().getColumn(2).setCellRenderer(new IconRenderer(NewMainFrame.lobbyIcons.getCountryIcons()));
        battleViewTable.getColumnModel().getColumn(3).setCellRenderer(new IconRenderer(NewMainFrame.lobbyIcons.getRankIcons()));
        battleViewTable.setRowHeight(42);
        battleViewTable.pack(TablePacker.ALL_ROWS, true);
    }

    private void reveal() {
        // Pack tables
        pack();
        setColumnModelBattleListTable();
        setLocation();
    }

    private void setMapPreview(int mapChecksum) {
        ((MinimapDisplay) mapReviewLabel).setMinimap(mapChecksum);
    }

    private void _addBattle(Battle battle) {
        battleModel.addRow(battle);
        setColumnModelBattleListTable();
    }

    private void _removeBattle(Battle battle) {
        battleModel.removeRow(battle);
        setColumnModelBattleListTable();
    }

    private void joinBattle(int battleId) {
        // Ask battleController if said battle requires a password
        String psw = "";
        if (battleListController.isBattlePasswordRequired(battleId)) {
            psw = JOptionPane.showInputDialog(this, "Please enter password:", "Battle is password-protected", JOptionPane.QUESTION_MESSAGE);
        }

        // Join battle
        battleListController.joinBattle(battleId, psw);
    }

    /**
     * Register as an observer with the battle list controller.
     */
    private void registerAsObserver() {
        battleListController.registerBattleListObserver(this);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new JPanel();
        battlesScrollPane = new JScrollPane();
        jPanel2 = new JPanel();
        mapReviewLabel = new MinimapDisplay(downloader, this, unitsync, 128, 128);
        joinButton = new JButton();
        usersScrollPane = new JScrollPane();

        setIconifiable(true);
        setMaximizable(true);
        getContentPane().setLayout(new CardLayout());

        battlesScrollPane.setBorder(BorderFactory.createTitledBorder("Battle list"));
        battlesScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        battlesScrollPane.setPreferredSize(new Dimension(256, 256));

        joinButton.setText("Join");
        joinButton.addActionListener(this);

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(mapReviewLabel, GroupLayout.PREFERRED_SIZE, 128, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(joinButton))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addComponent(joinButton, GroupLayout.PREFERRED_SIZE, 327, GroupLayout.PREFERRED_SIZE)
            .addGroup(Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(199, Short.MAX_VALUE)
                .addComponent(mapReviewLabel, GroupLayout.PREFERRED_SIZE, 128, GroupLayout.PREFERRED_SIZE))
        );

        usersScrollPane.setBorder(BorderFactory.createTitledBorder("Battle peek"));
        usersScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        usersScrollPane.setToolTipText("Battle peek");
        usersScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        usersScrollPane.setPreferredSize(new Dimension(256, 256));

        usersScrollPane.setName("Battle peek");

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(usersScrollPane, GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addComponent(battlesScrollPane, GroupLayout.DEFAULT_SIZE, 535, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(battlesScrollPane, GroupLayout.PREFERRED_SIZE, 426, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(jPanel2, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(usersScrollPane, GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)))
        );

        getContentPane().add(jPanel1, "card2");
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == joinButton) {
            BattleListFrame.this.joinButtonActionPerformed(evt);
        }
    }// </editor-fold>//GEN-END:initComponents

    private void joinButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_joinButtonActionPerformed
        // Call parent and say we want to open battle frame
        //parent.openBattleRoomWindow();

        // Get battle id of selected row
        int row = battlesTable.getSelectedRow();

        // If row is -1 then selection got reset TODO make so this doesn't happen
        if (row == -1) {
            return;
        }

        // Get value at row,col
        int battleId = ((Battle) ((LobbyTableModel) battlesTable.getModel()).getData().get(row)).getBattleId();

        // Tell battleListController we want to join a battle
        joinBattle(battleId);
    }//GEN-LAST:event_joinButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JScrollPane battlesScrollPane;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JButton joinButton;
    private JLabel mapReviewLabel;
    private JScrollPane usersScrollPane;
    // End of variables declaration//GEN-END:variables
    LobbyTableModel<Battle> battleModel;
    LobbyTable battlesTable;
    LobbyTable battleViewTable;
    LobbyTableModel<User> battleViewModel;

    @Override
    public void addBattle(final Battle battle) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                _addBattle(battle);
            }
        });
    }

    @Override
    public void removeBattle(final Battle battle) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                _removeBattle(battle);
            }
        });
    }

    @Override
    public void updateBattle(final Battle battle) {
        // The quickest way to do update is to do a remove followed by an add
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                _removeBattle(battle);
                _addBattle(battle);
            }
        });
    }

    @Override
    public void joinFailed(final String reason) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                JOptionPane.showMessageDialog(null, reason, "Join battle failed", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public void fireRefreshFromDownloader() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                battlesTable.validate();
                battlesTable.repaint();
                toFront();
            }
        });
    }

    public Battle getRelevantBattle() {
        return getCurrentlySelectedBattle();
    }
}
