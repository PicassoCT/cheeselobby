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
package net.cheesecan.cheeselobby.ui;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import net.cheesecan.cheeselobby.SessionController;
import net.cheesecan.cheeselobby.io.SettingsFile;
import net.cheesecan.cheeselobby.ui.components.LobbyIcons;
import net.cheesecan.cheeselobby.ui.interfaces.Disconnectable;
import net.cheesecan.cheeselobby.unitsync.UnitSyncForJava;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;

/**
 *
 * @author jahziah
 */
public class NewMainFrame extends JFrame implements Disconnectable {

    // Shared
    public static LobbyIcons lobbyIcons = new LobbyIcons();
    // Members
    private JDesktopPane bg;
    public static final String lobbyName = "CheeseLobby Alpha";
    public static final float lobbyVersion = (float) 0.2;
    public static String title;
    private JPopupMenu popupMenu;
    private JMenuItem settingsMenu;
    private JMenuItem downloadMenu;
    private JMenuItem helpMenu;
    private JMenuItem aboutMenu;
    private JMenuItem logoutMenu;
    private JMenuItem exitMenu;
    // Objects
    private SettingsFile settings;
    private SessionController sessionController;
    private UnitSyncForJava unitSync;
    // GUI panels
    private LoginFrame login;
    private ChatFrame chat;
    private BattleListFrame battle;
    private BattleRoomFrame battleRoom;
    private SettingsDialog settingsDialog;
    private DownloaderFrame downloader;
    private AboutDialog about;

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new NewMainFrame();
            }
        });
    }

    public NewMainFrame() {
        // Initialize settings
        settings = new SettingsFile();

        // Initialize and start sessionController thread
        sessionController = new SessionController(settings, (Disconnectable) this);
        sessionController.start();

        // Initialize the GUI theme
        initializeTheme();

        // Setup GUI components
        createAndShowGUI();

        // Init unitsync
        readSettingsFile();

        // Init login window
        initLoginWindow();

        // Make visible
        setVisible(true);
    }

    /**
     * Sets a substance skin.
     */
    private void initializeTheme() {
        if (settings.getTheme() == null) {   // default skin
            SubstanceLookAndFeel.setSkin("org.pushingpixels.substance.api.skin.GraphiteGlassSkin");
        } else {
            SubstanceLookAndFeel.setSkin("org.pushingpixels.substance.api.skin." + settings.getTheme());
        }
    }

    private void readSettingsFile() {
        // Are required settings missing?
        if (settings.getUnitSyncPath().isEmpty() || settings.getSpringExePath().isEmpty() || settings.getSpringDataDirectory().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Welcome to CheeseLobby!\nThere are a few options that need to be set before you can begin playing.", lobbyName + " " + lobbyVersion, JOptionPane.INFORMATION_MESSAGE);

            settingsDialog.showAtCenterOfScreen();
        }
        // Load unitsync
        try {
            String os = System.getProperty("os.name");

            // if windows
            if (os.contains("Windows")) {
                String springDllPath = settings.getUnitSyncPath().replaceAll("unitsync.dll", 
                        "");
                Runtime.getRuntime().load(
                        springDllPath + "DevIL.dll");
                Runtime.getRuntime().load(
                        springDllPath + "ILU.dll");
                Runtime.getRuntime().load(
                        springDllPath + "SDL.dll");
            }

            Runtime.getRuntime().load(settings.getUnitSyncPath());
        } catch (UnsatisfiedLinkError e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(NewMainFrame.class.getName()).log(Level.SEVERE, null, e);
            System.exit(1);
        }
        unitSync = new UnitSyncForJava();
        unitSync.refreshMapHashes();
    }

    /**
     * Initializes the popup menu which appears when a user right-clicks on the background.
     */
    private void initPopupMenu() {
        final JPopupMenu menu = new JPopupMenu();
        settingsMenu = new JMenuItem("Settings");
        downloadMenu = new JMenuItem("Downloader");
        helpMenu = new JMenuItem("Help");
        aboutMenu = new JMenuItem("About");
        logoutMenu = new JMenuItem("Logout");
        exitMenu = new JMenuItem("Exit");
        menu.add(settingsMenu);
        menu.add(downloadMenu);
        menu.addSeparator();
        menu.add(helpMenu);
        menu.add(logoutMenu);
        menu.add(aboutMenu);
        menu.add(exitMenu);
        logoutMenu.setVisible(false);

        addMouseListener(new MouseListener() {

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
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            private void showPopup(MouseEvent e) {
                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        final NewMainFrame thisPtr = this;
        ActionListener menuListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == settingsMenu) {
                    settingsDialog.showAtCenterOfScreen();
                } else if (e.getSource() == helpMenu) {
                    helpMenuActionPerformed();
                } else if (e.getSource() == aboutMenu) {
                    about.setVisible(true);
                } else if (e.getSource() == exitMenu) {
                    exitMenuActionPerformed();
                } else if (e.getSource() == logoutMenu) {
                    logoutMenuActionPerformed();
                } else if (e.getSource() == downloadMenu) {
                    downloader.setVisible(true);
                }
            }

            private void logoutMenuActionPerformed() throws HeadlessException {
                int res = JOptionPane.showConfirmDialog(null, "Are you sure you want to disconnect?", "Confirm disconnect", JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.YES_OPTION) {
                    _disconnect(null, null, null);
                }
            }

            private void exitMenuActionPerformed() throws HeadlessException {
                int retVal = JOptionPane.showConfirmDialog(thisPtr, "Are you sure you want to exit?", "Confirm exit", JOptionPane.YES_NO_OPTION);
                if (retVal == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }

            private void helpMenuActionPerformed() {
                try {
                    try {
                        Desktop.getDesktop().browse(new URI("http://jahwag.github.com/cheeselobby/project-info.html"));
                    } catch (IOException ex) {
                        Logger.getLogger(NewMainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (URISyntaxException ex) {
                    Logger.getLogger(NewMainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        settingsMenu.addActionListener(menuListener);
        downloadMenu.addActionListener(menuListener);
        helpMenu.addActionListener(menuListener);
        aboutMenu.addActionListener(menuListener);
        logoutMenu.addActionListener(menuListener);
        exitMenu.addActionListener(menuListener);
    }

    /**
     * Constructs GUI then displays it.
     */
    public final void createAndShowGUI() {
        // Initialize settings dialog
        settingsDialog = new SettingsDialog(null, settings);

        // Initialize popup menu
        initPopupMenu();

        // Initialize members
        bg = new JDesktopPane();

        // Set decoration type of bg
        removeNotify();
        setUndecorated(true);
        addNotify();

        // Set title
        title = lobbyName + " " + lobbyVersion;
        setTitle(title);

        // Set members' properties
        bg.setPreferredSize(getScreenSize());
        setPreferredSize(getScreenSize());
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);

        // Add members
        add(bg);

        // Set what we do on close
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Closing");
            }
        });

        pack();
    }

    public static Dimension getScreenSize() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        int width = ge.getDefaultScreenDevice().getDisplayMode().getWidth();
        int height = ge.getDefaultScreenDevice().getDisplayMode().getHeight();
        return new Dimension(width, height);
    }

    private void initLoginWindow() {
        // Initialize other windows
        initializeWindows();

        // Initialize login panel
        login = new LoginFrame(sessionController, this, settings);

        bg.add(login);
        login.setVisible(true);
    }

    /**
     * Called by the login panel on event dispatch thread, to notify us that it's done.
     */
    protected void loginFinished() {
        disposeOfWindow(login);

        // Enable logout
        logoutMenu.setVisible(true);

        // Notify sessionController we are now ready
        sessionController.guiIsReady();

        // Tell GUI to log us into the channels in our perform list
        String[] performList = settings.getAutoJoinChannels().split("\n");
        for (String channel : performList) {
            sessionController.joinChannel(channel);
        }

        // Make visible
        battle.setVisible(true);
        chat.setVisible(true);
        
        bg.repaint();
    }

    private void initializeWindows() {
        // Initialize downloader
        downloader = new DownloaderFrame(settings, unitSync);
        bg.add(downloader);
        // Start chat frame
        chat = new ChatFrame(sessionController, settings);
        // Add chat frame to desktop pane
        bg.add(chat);
        // Start battle list frame
        battle = new BattleListFrame(sessionController, this, unitSync, downloader);
        // Add battle room frame
        bg.add(battle);
        // Initialize battleRoom
        battleRoom = new BattleRoomFrame(sessionController, downloader, settings, unitSync);
        bg.add(battleRoom);
        // Initialize about
        about = new AboutDialog(this);
    }

    private void disposeOfWindow(JInternalFrame window) {
        // Remove from desktop
        bg.remove(window);
        // Hide from view
        window.setVisible(false);
        // Dispose, that is, deallocate all memory assigned for this frame
        window.dispose();
    }

    @Override
    public void disconnect(final String previousName, final String newName, final String reason) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                _disconnect(previousName, newName, reason);
            }
        });
    }

    private void _disconnect(String previousName, String newName, String reason) {
        // Dispose of GUI windows
        disposeOfWindow(chat);
        disposeOfWindow(battle);
        disposeOfWindow(battleRoom);
        disposeOfWindow(downloader);

        // Repaint
        repaint();

        // Tell controller we want to _disconnect
        sessionController.disconnect();

        // Show login window 
        initLoginWindow();

        // Show popup if the user did not perform disconnect themselves
        if (reason != null) {
            JOptionPane.showMessageDialog(this, reason, "You were disconnected", JOptionPane.INFORMATION_MESSAGE);
        }

        // Set the renamed name if this was a /rename-caused disconnect
        if (newName != null) {
            login.changeAccountName(previousName, newName);
        }

    }
}