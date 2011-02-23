/*
 * SettingsDialog.java
 *
 * Created on Dec 23, 2010, 6:34:39 PM
 */
package net.cheesecan.cheeselobby.ui;

import net.cheesecan.cheeselobby.ui.components.SubstanceSkinComboBox;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.filechooser.FileFilter;
import net.cheesecan.cheeselobby.io.SettingsFile;
import org.pushingpixels.substance.api.skin.SkinInfo;

/**
 *
 * @author jahziah
 */
public class SettingsDialog extends JDialog implements ActionListener {
    // Appearance

    int width;
    int height;
    Color defaultFieldColor;
    private SettingsFile settings;
    private boolean restartRequired;

    /** Creates new form SettingsDialog */
    public SettingsDialog(NewMainFrame parent, SettingsFile settings) {
        super(parent, true);
        initComponents();

        this.settings = settings;
        getValues();

        // Some changes will require restarts
        restartRequired = false;
        ActionListener changeThatRequiresRestart = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                restartRequired = true;
            }
        };

        // Add a listener to the unitsync field to notify user that he needs to restart after changing it
        unitSyncPathField.addActionListener(changeThatRequiresRestart);
        // Same for themes
        skinComboBox.addActionListener(changeThatRequiresRestart);

        // Set window location
        setAppearance();
    }

    private void autoconfigureField(final JTextField field, String filename) {
        String path = locateFile(filename);
        if (path != null && new File(path).exists()) {
            field.setText(path);
        }
    }

    private void autoconfigureLinux() {
        autoconfigureField(unitSyncPathField, "libunitsync.so");
        autoconfigureField(springExeField, "spring");
        autoconfigureField(springExeField, "spring-multithreaded");
        File springDir = new File(System.getProperty("user.home") + "/.spring");
        if (springDir.exists()) {
            dataDirectoryField.setText(springDir.getAbsolutePath());
        }
    }

    private void autoconfigureWindows() {
        // Get the disk, C, D, etc
        char disk = System.getProperty("user.home").charAt(0);
        File springDataDir = new File(disk + ":/Program files/Spring/");
        // If it exists
        if (springDataDir.exists() && springDataDir.isDirectory()) {
            dataDirectoryField.setText(springDataDir.getAbsolutePath());
        }
        File springExe = null;
        File unitSync = null;
        for (File f : springDataDir.listFiles()) {
            if (f.getName().equals("spring.exe") || f.getName().equals("spring-multithreaded.exe")) {
                springExe = f;
            } else if (f.getName().equals("unitsync.dll")) {
                unitSync = f;
            }
        }
        // If we found the spring exe
        if (springExe != null) {
            springExeField.setText(springExe.getAbsolutePath());
        }
        if (unitSync != null) {
            unitSyncPathField.setText(unitSync.getAbsolutePath());
        }
    }

    private boolean mandatoryDataHasBeenProvided() {
        // If all input is sufficient to proceed
        boolean ok = validateUnitSyncField();
        ok &= validateSpringExeField();
        ok &= validateDataDirField();
        return ok;
    }

    private void saveSettings() throws HeadlessException {
        validatePerformListSyntax();
        // Save all settings to settingsFile
        settings.setAutoJoinChannels(autoJoinTextField.getText());
        if (loggingEnabled.isEnabled()) {
            settings.setLogging(true);
        } else {
            settings.setLogging(false);
        }
        String[] sel = ((SkinInfo) skinComboBox.getSelectedItem()).getClassName().split("\\."); // match period
        settings.setTheme(sel[sel.length - 1]);
        settings.setSpringExePath(springExeField.getText());
        settings.setUnitSyncPath(unitSyncPathField.getText());
        settings.setSpringDataDirectory(dataDirectoryField.getText());
        // Save updates to settings
        settings.save();
        // Display popup if the user changed unitsync path
        if (restartRequired) {
            JOptionPane.showConfirmDialog(this, "You will have to restart the application for all changes to take full effect.", "Restart may be required", JOptionPane.OK_OPTION);
        }
        closeSettingsDialog();
    }

    private boolean validateDataDirField() {
        if (dataDirectoryField.getText().isEmpty() || !new File(dataDirectoryField.getText()).exists()) {
            dataDirectoryField.setBackground(Color.red);
            return false;
        } else {
            dataDirectoryField.setBackground(defaultFieldColor);
            return true;
        }
    }

    private boolean validateSpringExeField() {
        if (springExeField.getText().isEmpty() || !new File(springExeField.getText()).exists()) {
            springExeField.setBackground(Color.red);
            return false;
        } else {
            springExeField.setBackground(defaultFieldColor);
            return true;
        }
    }

    private boolean validateUnitSyncField() {
        if (unitSyncPathField.getText().isEmpty() || !new File(unitSyncPathField.getText()).exists()) {
            unitSyncPathField.setBackground(Color.red);
            return false;
        } else {
            unitSyncPathField.setBackground(defaultFieldColor);
            return true;
        }
    }

    private boolean validatePerformListSyntax() {
        try {
            // Check perform list syntax
            BufferedReader reader = new BufferedReader(new StringReader(autoJoinTextField.getText()));
            LinkedList<String> sb = new LinkedList<String>();
            String str;
            while ((str = reader.readLine()) != null) {
                sb.add(str);
            }
            // Save perform list update
            // Update perform list in memory
            String[] retVal = sb.toArray(new String[sb.size()]);
            //            parent.setPerformList(retVal);

            return true;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Bad input in perform list");
            return true;
        }
    }

    private void closeSettingsDialog() {
        setVisible(false);
        //dispose();
    }

    /**
     * Asks the system to locate a file.
     */
    String locateFile(String filename) {
        try {
            Process p = Runtime.getRuntime().exec("locate " + filename);
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            return r.readLine();
        } catch (IOException ex) {
            Logger.getLogger(SettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void setAppearance() {
        // Compute size and location of frame
        pack();

        setTitle("Settings");
        setResizable(true);

        defaultFieldColor = springExeField.getBackground();
    }

    public void showAtCenterOfScreen() {
        // Show at the center of the screen
        setLocation(new Point((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - getWidth() / 2, (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 - getHeight() / 2));
        setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        loggingGroup = new ButtonGroup();
        jPanel1 = new JPanel();
        unitSyncLabel = new JLabel();
        unitSyncPathField = new JTextField();
        browseUnitSyncButton = new JButton();
        springExeLbl = new JLabel();
        springExeField = new JTextField();
        dataDirectoryField = new JTextField();
        dataDirectoryLabel = new JLabel();
        browseSpringExeButton = new JButton();
        dataDirBrowse = new JButton();
        jButton1 = new JButton();
        jPanel2 = new JPanel();
        skinLabel = new JLabel();
        skinComboBox = new SubstanceSkinComboBox();
        loggingEnabled = new JCheckBox();
        channelAutojoinLabel = new JLabel();
        channelJoinScrollPane = new JScrollPane();
        autoJoinTextField = new JTextArea();
        jPanel3 = new JPanel();
        saveButton = new JButton();
        cancelButton = new JButton();

        jPanel1.setBorder(BorderFactory.createTitledBorder("Spring options"));

        unitSyncLabel.setText("Unitsync path:");
        unitSyncLabel.setToolTipText("The directory that contains Unitsync. \nIf you don't know what this is, set it to the same as the spring data directory.");

        unitSyncPathField.addActionListener(this);

        browseUnitSyncButton.setText("Browse");
        browseUnitSyncButton.addActionListener(this);

        springExeLbl.setText("Spring executable path:");
        springExeLbl.setToolTipText("The directory that contains the spring executable(spring.exe).");

        dataDirectoryLabel.setText("Spring data directory:");
        dataDirectoryLabel.setToolTipText("The directory that contains maps, mods, script.txt etc.");

        browseSpringExeButton.setText("Browse");
        browseSpringExeButton.addActionListener(this);

        dataDirBrowse.setText("Browse");
        dataDirBrowse.addActionListener(this);

        jButton1.setText("Auto-configure");
        jButton1.setToolTipText("Tries to find all files for you. ");
        jButton1.addActionListener(this);

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(unitSyncLabel)
                    .addGroup(Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(unitSyncPathField, GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(browseUnitSyncButton))
                    .addComponent(springExeLbl)
                    .addGroup(Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(springExeField, GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(browseSpringExeButton))
                    .addComponent(dataDirectoryLabel)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(dataDirectoryField, GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(dataDirBrowse))
                    .addComponent(jButton1))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(unitSyncLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(unitSyncPathField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseUnitSyncButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(springExeLbl)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(springExeField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseSpringExeButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(dataDirectoryLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(dataDirectoryField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(dataDirBrowse))
                .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1))
        );

        jPanel2.setBorder(BorderFactory.createTitledBorder("Lobby options"));

        skinLabel.setText("Theme:");

        loggingEnabled.setText("Enable logging");

        channelAutojoinLabel.setText("Channel autojoin:");

        channelJoinScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        channelJoinScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        autoJoinTextField.setColumns(20);
        autoJoinTextField.setRows(5);
        autoJoinTextField.setText("main\ncheeselobby");
        channelJoinScrollPane.setViewportView(autoJoinTextField);

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(skinLabel)
                .addContainerGap(390, Short.MAX_VALUE))
            .addComponent(skinComboBox, 0, 443, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(loggingEnabled)
                .addContainerGap(318, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(channelAutojoinLabel)
                .addContainerGap())
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(channelJoinScrollPane, GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(skinLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(skinComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(loggingEnabled)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(channelAutojoinLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(channelJoinScrollPane, GroupLayout.PREFERRED_SIZE, 156, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        saveButton.setText("Save");
        saveButton.addActionListener(this);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(this);

        GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(saveButton)
                .addPreferredGap(ComponentPlacement.RELATED, 355, Short.MAX_VALUE)
                .addComponent(cancelButton))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(saveButton)
                    .addComponent(cancelButton)))
        );

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(jPanel1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == unitSyncPathField) {
            SettingsDialog.this.unitSyncPathFieldActionPerformed(evt);
        }
        else if (evt.getSource() == browseUnitSyncButton) {
            SettingsDialog.this.browseUnitSyncButtonActionPerformed(evt);
        }
        else if (evt.getSource() == browseSpringExeButton) {
            SettingsDialog.this.browseSpringExeButtonActionPerformed(evt);
        }
        else if (evt.getSource() == dataDirBrowse) {
            SettingsDialog.this.dataDirBrowseActionPerformed(evt);
        }
        else if (evt.getSource() == jButton1) {
            SettingsDialog.this.jButton1ActionPerformed(evt);
        }
        else if (evt.getSource() == saveButton) {
            SettingsDialog.this.saveButtonActionPerformed(evt);
        }
        else if (evt.getSource() == cancelButton) {
            SettingsDialog.this.cancelButtonActionPerformed(evt);
        }
    }// </editor-fold>//GEN-END:initComponents

    private void getValues() {
        autoJoinTextField.setText(settings.getAutoJoinChannels());
        unitSyncPathField.setText(settings.getUnitSyncPath());
        springExeField.setText(settings.getSpringExePath());
        dataDirectoryField.setText(settings.getSpringDataDirectory());
        skinComboBox.setSelectedItem("org.pushingpixels.substance.api.skin." + settings.getTheme());
    }

    private File findFile(String soughtFile, File startLocation) {
        File[] files = startLocation.listFiles();
        for (File f : files) {
            if (f.canRead() && f.getName().equals(soughtFile)) { // if we found the right file
                return f;
            } // else if f is a directory with more than 0 files in it
            else if (f.canRead() && f.isDirectory() && f.listFiles().length > 0) {
                findFile(soughtFile, f);    // recurse through directory
            }
        }
        // File was not found
        return null;
    }

    private File findFile(String soughtFile, File[] startLocation) {
        File result = null;
        for (File f : startLocation) {
            result = findFile(soughtFile, f);

            // If file was found
            if (result != null) {
                return result;
            }
        }
        // File was not found anywhere
        return null;
    }

    private void saveButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        if (mandatoryDataHasBeenProvided()) {
            saveSettings();
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    private void cancelButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        // If we have previously good settings to resort to using
        if (!settings.getSpringDataDirectory().isEmpty()
                && !settings.getUnitSyncPath().isEmpty()
                && !settings.getSpringExePath().isEmpty()) {
            closeSettingsDialog();
        } else {
            int result = JOptionPane.showOptionDialog(this, "To use CheeseLobby you must complete the highlighted fields.", "",
                    JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"Ok", "Exit the program"}, "Ok");
            // User wants to exit
            if (result == 1) {
                System.exit(0);
            }
        }
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void unitSyncPathFieldActionPerformed(ActionEvent evt) {//GEN-FIRST:event_unitSyncPathFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_unitSyncPathFieldActionPerformed

    private void browseUnitSyncButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_browseUnitSyncButtonActionPerformed
        // Open file chooser
        JFileChooser chooser = new JFileChooser();
        // Create filter
        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    return true;
                } else if (pathname.isFile() && pathname.getAbsolutePath().contains("unitsync")) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public String getDescription() {
                return "unitsync library";
            }
        };

        chooser.setFileHidingEnabled(false);
        chooser.setFileFilter(filter);
        int retval = chooser.showOpenDialog(this);

        String path;
        if (retval == JFileChooser.APPROVE_OPTION) {
            path = chooser.getSelectedFile().getAbsolutePath();
            unitSyncPathField.setText(path);
        }
    }//GEN-LAST:event_browseUnitSyncButtonActionPerformed

    private void browseSpringExeButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_browseSpringExeButtonActionPerformed
        // Open file chooser
        JFileChooser chooser = new JFileChooser();

        //chooser.setFileFilter(filter);
        chooser.setFileFilter(chooser.getAcceptAllFileFilter());
        chooser.setFileHidingEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int retval = chooser.showOpenDialog(this);

        String path;
        if (retval == JFileChooser.APPROVE_OPTION) {
            path = chooser.getSelectedFile().getAbsolutePath();
            springExeField.setText(path);
        }
    }//GEN-LAST:event_browseSpringExeButtonActionPerformed

    private void dataDirBrowseActionPerformed(ActionEvent evt) {//GEN-FIRST:event_dataDirBrowseActionPerformed
        // Open file chooser
        JFileChooser chooser = new JFileChooser();
        // Create filter
        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    return true;
                }

                return false;
            }

            @Override
            public String getDescription() {
                return "Spring directory";
            }
        };

        chooser.setFileHidingEnabled(false);
        chooser.setFileFilter(filter);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int retval = chooser.showOpenDialog(this);

        String path;
        if (retval == JFileChooser.APPROVE_OPTION) {
            path = chooser.getSelectedFile().getAbsolutePath();

            // Check for uikeys file in the path
            File dirName = new File(path);
            File[] subFiles = dirName.listFiles();
            boolean correct = false;
            for (File file : subFiles) {
                if (file.getName().equals("uikeys.txt")) {
                    correct = true;
                }
            }
            if (correct) {
                dataDirectoryField.setText(path);
            } else {
                JOptionPane.showMessageDialog(this, "Error: That directory does not contain uikeys.txt, so it cannot be the spring data directory.");
            }

        }
    }//GEN-LAST:event_dataDirBrowseActionPerformed

    private void jButton1ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String os = System.getProperty("os.name");

        // Find files in linux
        if (os.contains("Linux")) {
            autoconfigureLinux();
        } else if (os.contains("Windows")) {
            autoconfigureWindows();
        }
    }//GEN-LAST:event_jButton1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JTextArea autoJoinTextField;
    private JButton browseSpringExeButton;
    private JButton browseUnitSyncButton;
    private JButton cancelButton;
    private JLabel channelAutojoinLabel;
    private JScrollPane channelJoinScrollPane;
    private JButton dataDirBrowse;
    private JTextField dataDirectoryField;
    private JLabel dataDirectoryLabel;
    private JButton jButton1;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JPanel jPanel3;
    private JCheckBox loggingEnabled;
    private ButtonGroup loggingGroup;
    private JButton saveButton;
    private JComboBox skinComboBox;
    private JLabel skinLabel;
    private JTextField springExeField;
    private JLabel springExeLbl;
    private JLabel unitSyncLabel;
    private JTextField unitSyncPathField;
    // End of variables declaration//GEN-END:variables
}
