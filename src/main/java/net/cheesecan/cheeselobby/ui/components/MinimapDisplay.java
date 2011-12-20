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
package net.cheesecan.cheeselobby.ui.components;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import net.cheesecan.cheeselobby.session.Battle;
import net.cheesecan.cheeselobby.ui.interfaces.BattleObserver;
import net.cheesecan.cheeselobby.ui.interfaces.DownloaderFacade;
import net.cheesecan.cheeselobby.unitsync.UnitSyncForJava;

/**
 *
 * @author jahziah
 */
public class MinimapDisplay extends JLabel implements MouseListener {

    private boolean mapMissing = false;
    private DownloaderFacade downloader;
    private BattleObserver observer;
    private UnitSyncForJava unitsync;
    private static final String MAP_MISSING_SMALL = "/misc/missing128.png";
    private static final String MAP_MISSING_LARGE = "/misc/missing.png";
    private int width, height;

    public MinimapDisplay(DownloaderFacade downloader, BattleObserver observer, UnitSyncForJava unitsync, int width, int height) {
        this.downloader = downloader;
        this.observer = observer;
        this.unitsync = unitsync;
        this.width = width;
        this.height = height;

        // Set map review label size
        setMinimumSize(new Dimension(width, height));
        setPreferredSize(new Dimension(width, height));
        setMaximumSize(new Dimension(width, height));
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);

        addMouseListener(this);
    }

    private void clickMapDownload() {
        if (mapMissing) {
            Battle currentlySelectedBattle = observer.getRelevantBattle();
            int downloadMap = JOptionPane.showConfirmDialog(this, "Would you like to download this map?",
                    "Download '" + currentlySelectedBattle.getMapName() + "' ?", JOptionPane.YES_NO_OPTION);
            if (downloadMap == JOptionPane.YES_OPTION) {
                downloader.downloadMap(currentlySelectedBattle.getMapName(), observer); // tell downloader to download said map
            }
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
        clickMapDownload();
    }

    public void setMinimap(int mapChecksum) {
        ImageIcon icon = null;
        try {
            BufferedImage minimap = unitsync.getMinimap(unitsync.mapChecksumToArchiveName(mapChecksum), 1);
            // Resize to fit component
            minimap = UnitSyncForJava.resize(minimap, width, height, BufferedImage.TYPE_USHORT_565_RGB);
            // Put image into imageicon
            icon = new ImageIcon(minimap);
        } catch (IOException ex) {
            //Logger.getLogger(MinimapDisplay.class.getName()).log(Level.SEVERE, null, ex);
            icon = getMissingIcon();
        }

        setToolTipText(observer.getRelevantBattle().getMapName());
        super.setIcon(icon);
    }

    public ImageIcon getMissingIcon() {
        mapMissing = true;
        setToolTipText("Click to download map.");
        if (getWidth() <= 128) {
            return new ImageIcon(getClass().getResource(MAP_MISSING_SMALL));
        } else {
            return new ImageIcon(getClass().getResource(MAP_MISSING_LARGE));
        }

    }
}
