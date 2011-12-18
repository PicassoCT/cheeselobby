/*
 *  Copyright 2011 jahziah.
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
 * DownloaderFrame.java
 *
 * Created on Feb 26, 2011, 3:20:28 AM
 */
package net.cheesecan.cheeselobby.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import net.cheesecan.cheeselobby.io.SettingsFile;
import net.cheesecan.cheeselobby.io.downloader.DownloadThread;
import net.cheesecan.cheeselobby.io.downloader.DownloadThread.DownloadType;
import net.cheesecan.cheeselobby.ui.components.DownloadOption;
import net.cheesecan.cheeselobby.ui.components.ProgressElement;
import net.cheesecan.cheeselobby.ui.interfaces.BattleObserver;
import net.cheesecan.cheeselobby.ui.interfaces.DownloaderFacade;
import net.cheesecan.cheeselobby.unitsync.UnitSyncForJava;

/**
 *
 * @author jahziah
 */
public class DownloaderFrame extends JInternalFrame implements DownloaderFacade {

    public void notifyDownloadProgress(final String filename, final int progress, final long bps) {
        final DownloaderFrame thisRef = this;
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                ProgressElement pe = null;
                if (!progressContainer.containsKey(filename)) {
                    pe = new ProgressElement(filename, thisRef);
                    progressContainer.put(filename, pe);
                } else {
                    pe = progressContainer.get(filename);
                }

                pe.setProgress(progress, bps);
                refreshProgressPane(pe);

                if (progress == 100) {
                    unitsync.refreshMapHashes();
                }
            }
        });
    }

    public void refreshProgressPane(final ProgressElement pe) {
        if (pe.getPosition() == -1) { // if not attached, add it
            pe.setPosition(progressPanel.getComponentCount());
            progressPanel.add(pe.getPanel());
            progressPanel.validate();
        } else { // if already attached
            pe.refresh();
        }
    }

    public void removeProgressElement(ProgressElement toRemove) {
        progressPanel.remove(toRemove.getPosition());
        progressPanel.validate();
        progressPanel.repaint();
    }

    private void searchForMap(final String txt) {
        new Thread(new Runnable() {

            public void run() {
                mapsList.removeAll(); // clear previous searches

                String resource = DownloadThread.lookupResourceLocation(txt);

                if (resource == null) {
                    // TODO add some message to notify user that there were no hits
                    return;
                }

                // Itemize
                DownloadOption options[] = new DownloadOption[1];
                options[0] = new DownloadOption(txt, resource);
                mapsList.setListData(options);
                mapsList.setSelectedIndex(0); // auto-select first result
            }
        }).start();
    }
    private SettingsFile settings;
    private UnitSyncForJava unitsync;

    /** Creates new form DownloaderFrame */
    public DownloaderFrame(SettingsFile settings, UnitSyncForJava unitsync) {
        this.settings = settings;
        this.unitsync = unitsync;

        initComponents();
        postInitComponents();
        setLocation();
        setFrameBehaviour();
    }

    private void postInitComponents() {
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.PAGE_AXIS));

        searchField.setText("DeltaSiegeDry"); // testing only

        searchGroup.setSelected(mapRadioButton.getModel(), true);
        searchButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String txt = searchField.getText();
                if (!txt.isEmpty()) {

                    if (searchGroup.getSelection().equals(mapRadioButton.getModel())) {
                        searchForMap(txt);
                    } else {
                        System.out.println("Other downloads not yet implemented.");
                    }
                }
            }
        });

        downloadButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DownloadOption item = (DownloadOption) mapsList.getSelectedValue();

                if (mapRadioButton.isSelected()) {
                    downloadFile(item.toString(), item.getUrl(), DownloadType.Map, null);
                } else {
                    System.out.println("Widget and mod downloading not implemented");
                }
            }
        });
    }

    private void downloadFile(String name, String url, DownloadType type, BattleObserver observer) {
        if (type == DownloadType.Map) {
            new Thread(new DownloadThread(this, observer, name, url, DownloadType.Map)).start();
        } else {
            return;
        }
    }

    public String getSavePath() {
        return settings.getSpringDataDirectory();
    }

    private void setLocation() {
        // Compute size and location of frame
        pack();
        double x = NewMainFrame.getScreenSize().getWidth() / 2;
        double y = NewMainFrame.getScreenSize().getHeight() / 2;
        setLocation((int) x - getWidth() / 2, (int) ((int) y - getHeight() / 2));

        // Set icon
        setFrameIcon(new ImageIcon(getClass().getResource("/img/window/download.png")));

        // Set title
        setTitle("Downloader");

        setResizable(true);
    }

    private void setFrameBehaviour() {
        // Set frame behaviour
        setClosable(false);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(false);
    }

    public void refreshView() {
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        searchGroup = new ButtonGroup();
        splitPane = new JSplitPane();
        downloaderPanel = new JPanel();
        categorySelectorPane = new JTabbedPane();
        mapsScrollPane = new JScrollPane();
        mapsList = new JList();
        modsScrollPane = new JScrollPane();
        modsList = new JList();
        widgetsScrollPane = new JScrollPane();
        widgetsList = new JList();
        searchField = new JTextField();
        searchLabel = new JLabel();
        widgetRadioButton = new JRadioButton();
        modRadioButton = new JRadioButton();
        mapRadioButton = new JRadioButton();
        downloadButton = new JButton();
        searchButton = new JToggleButton();
        progressScrollPane = new JScrollPane();
        progressPanel = new JPanel();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        downloaderPanel.setPreferredSize(new Dimension(409, 487));

        mapsScrollPane.setViewportView(mapsList);

        categorySelectorPane.addTab("Maps", mapsScrollPane);

        modsScrollPane.setViewportView(modsList);

        categorySelectorPane.addTab("Mods", modsScrollPane);

        widgetsScrollPane.setViewportView(widgetsList);

        categorySelectorPane.addTab("Widgets", widgetsScrollPane);

        searchLabel.setText("Search:");

        searchGroup.add(widgetRadioButton);
        widgetRadioButton.setText("Widget");

        searchGroup.add(modRadioButton);
        modRadioButton.setText("Mod");

        searchGroup.add(mapRadioButton);
        mapRadioButton.setText("Map");

        downloadButton.setText("Download selected");

        searchButton.setText("Search");

        GroupLayout downloaderPanelLayout = new GroupLayout(downloaderPanel);
        downloaderPanel.setLayout(downloaderPanelLayout);
        downloaderPanelLayout.setHorizontalGroup(
            downloaderPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, downloaderPanelLayout.createSequentialGroup()
                .addContainerGap(353, Short.MAX_VALUE)
                .addComponent(searchButton))
            .addGroup(downloaderPanelLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(downloaderPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(downloaderPanelLayout.createParallelGroup(Alignment.LEADING)
                        .addComponent(categorySelectorPane, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 385, Short.MAX_VALUE)
                        .addGroup(Alignment.TRAILING, downloaderPanelLayout.createSequentialGroup()
                            .addComponent(mapRadioButton)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(modRadioButton)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(widgetRadioButton))
                        .addGroup(downloaderPanelLayout.createSequentialGroup()
                            .addComponent(searchLabel)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(searchField, GroupLayout.PREFERRED_SIZE, 274, GroupLayout.PREFERRED_SIZE))
                        .addComponent(downloadButton, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 385, Short.MAX_VALUE))
                    .addContainerGap()))
        );
        downloaderPanelLayout.setVerticalGroup(
            downloaderPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(downloaderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchButton)
                .addContainerGap(448, Short.MAX_VALUE))
            .addGroup(downloaderPanelLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(downloaderPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(downloaderPanelLayout.createParallelGroup(Alignment.BASELINE)
                        .addComponent(searchField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(searchLabel))
                    .addPreferredGap(ComponentPlacement.UNRELATED)
                    .addGroup(downloaderPanelLayout.createParallelGroup(Alignment.BASELINE)
                        .addComponent(widgetRadioButton)
                        .addComponent(modRadioButton)
                        .addComponent(mapRadioButton))
                    .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(categorySelectorPane, GroupLayout.PREFERRED_SIZE, 363, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(downloadButton)
                    .addContainerGap()))
        );

        splitPane.setLeftComponent(downloaderPanel);

        GroupLayout progressPanelLayout = new GroupLayout(progressPanel);
        progressPanel.setLayout(progressPanelLayout);
        progressPanelLayout.setHorizontalGroup(
            progressPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGap(0, 452, Short.MAX_VALUE)
        );
        progressPanelLayout.setVerticalGroup(
            progressPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGap(0, 485, Short.MAX_VALUE)
        );

        progressScrollPane.setViewportView(progressPanel);

        splitPane.setRightComponent(progressScrollPane);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(splitPane, GroupLayout.DEFAULT_SIZE, 869, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(splitPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JTabbedPane categorySelectorPane;
    private JButton downloadButton;
    private JPanel downloaderPanel;
    private JRadioButton mapRadioButton;
    private JList mapsList;
    private JScrollPane mapsScrollPane;
    private JRadioButton modRadioButton;
    private JList modsList;
    private JScrollPane modsScrollPane;
    private JPanel progressPanel;
    private JScrollPane progressScrollPane;
    private JToggleButton searchButton;
    private JTextField searchField;
    private ButtonGroup searchGroup;
    private JLabel searchLabel;
    private JSplitPane splitPane;
    private JRadioButton widgetRadioButton;
    private JList widgetsList;
    private JScrollPane widgetsScrollPane;
    // End of variables declaration//GEN-END:variables

    public void downloadMap(final String name, final BattleObserver observer) {
        new Thread(new Runnable() {
            public void run() {
                String url = DownloadThread.lookupResourceLocation(name); // spawn a new thread because this takes a while
                downloadFile(name, url, DownloadType.Map, observer);
                setVisible(true);
                toFront();
            }
        }).start();
        
    }
}
