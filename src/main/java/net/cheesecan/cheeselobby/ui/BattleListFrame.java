/*
 * BattleListFrame.java
 *
 * Created on Dec 24, 2010, 12:59:57 PM
 */
package net.cheesecan.cheeselobby.ui;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.IOException;
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import net.cheesecan.cheeselobby.tables.TablePacker;
import net.cheesecan.cheeselobby.tables.LobbyTable;
import net.cheesecan.cheeselobby.tables.LobbyTableModel;
import net.cheesecan.cheeselobby.ui.components.IconRenderer;
import net.cheesecan.cheeselobby.ui.components.PasswordIconRenderer;
import net.cheesecan.cheeselobby.session.Battle;
import net.cheesecan.cheeselobby.session.User;
import net.cheesecan.cheeselobby.ui.interfaces.BattleListControllerFacade;
import net.cheesecan.cheeselobby.ui.interfaces.DownloaderFacade;
import net.cheesecan.cheeselobby.unitsync.UnitSyncForJava;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer.BooleanRenderer;

/**
 *
 * @author jahziah
 */
public class BattleListFrame extends JInternalFrame implements ActionListener, BattleListObserver {
    private static final String MAP_MISSING = "/misc/missing128.png";

    private BattleListControllerFacade battleListController;
    private NewMainFrame parent;
    private UnitSyncForJava unitSync;
    private boolean mapMissing = false;
    private DownloaderFacade downloader;

    /** Creates new form BattleListFrame */
    public BattleListFrame(BattleListControllerFacade battleController, NewMainFrame parent, UnitSyncForJava unitSync, DownloaderFacade downloader) {
        this.downloader = downloader;
        this.battleListController = battleController;
        this.parent = parent;
        this.unitSync = unitSync;

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

        // Set map review label size
        mapReviewLabel.setSize(new Dimension(128, 128));
        mapReviewLabel.setMinimumSize(new Dimension(128, 128));
        mapReviewLabel.setPreferredSize(new Dimension(128, 128));
        mapReviewLabel.setMaximumSize(new Dimension(128, 128));
        mapReviewLabel.setHorizontalAlignment(SwingConstants.LEFT);
        mapReviewLabel.setVerticalAlignment(SwingConstants.BOTTOM);

        this.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameDeiconified(InternalFrameEvent e) {
                reveal();
            }
        });
        
        mapReviewLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                clickMapDownload();
            }
        });
    }
    
    private void clickMapDownload() {
        if(mapMissing) {
            int downloadMap = JOptionPane.showConfirmDialog(this, "Would you like to download this map?", "Download missing map", JOptionPane.YES_NO_OPTION);
            if(downloadMap == JOptionPane.YES_OPTION) {
                Battle currentlySelectedBattle = getCurrentlySelectedBattle();
                downloader.downloadMap(currentlySelectedBattle.getMapName()); // tell downloader to download said map
            }
        }
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
                
                if(b == null) {
                    return;
                }
                
                int battleId = b.getBattleId();

                // Set data in battle view model
                battleViewModel.setData(battleListController.getUsersInBattle(battleId));

                // Set map preview
                setMapPreview(b.getMapHash(), b.getMapName());

                // Set columns
                setColumnModelBattleView();
            }

            private void performRightMouseClickAction(MouseEvent e) {
                throw new UnsupportedOperationException("Not yet implemented");
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
       // double x = Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2;
        //double y = Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2;
        setLocation(0,0);
        setResizable(true);
    }

    private void setColumnModelBattleListTable() {
        // Set flag and rank columns
        battlesTable.getColumnModel().getColumn(0).setCellRenderer(new IconRenderer(NewMainFrame.lobbyIcons.getBattleStatusIcons()));
        battlesTable.getColumnModel().getColumn(1).setCellRenderer(new PasswordIconRenderer());
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
        if(isIcon()) {
            return;
        }
        else { // Else pack tables
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

    private void setMapPreview(int mapChecksum, String mapName) {
        // TODO add caching to improve performance
        try {
            mapReviewLabel.setIcon(new ImageIcon(unitSync.getMinimap(unitSync.mapChecksumToArchiveName(mapChecksum), 3)));
            mapMissing = true;
        } catch (IOException ex) {
            // Does not have said map
            mapReviewLabel.setIcon(new ImageIcon(getClass().getResource(MAP_MISSING)));
            mapMissing = true;
        }
        mapReviewLabel.setToolTipText(mapName);
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
        mapReviewLabel = new JLabel();
        joinButton = new JButton();
        usersScrollPane = new JScrollPane();

        setIconifiable(true);
        setMaximizable(true);
        getContentPane().setLayout(new CardLayout());

        battlesScrollPane.setBorder(BorderFactory.createTitledBorder("Battle list"));
        battlesScrollPane.setPreferredSize(new Dimension(256, 256));

        mapReviewLabel.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
        mapReviewLabel.setMaximumSize(new Dimension(128, 128));
        mapReviewLabel.setMinimumSize(new Dimension(128, 128));
        mapReviewLabel.setPreferredSize(new Dimension(128, 128));

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
                .addComponent(usersScrollPane, GroupLayout.DEFAULT_SIZE, 1148, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addComponent(battlesScrollPane, GroupLayout.DEFAULT_SIZE, 1335, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(battlesScrollPane, GroupLayout.PREFERRED_SIZE, 426, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(jPanel2, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(usersScrollPane, GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)))
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
}