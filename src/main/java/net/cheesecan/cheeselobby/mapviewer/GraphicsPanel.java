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
package net.cheesecan.cheeselobby.mapviewer;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.MouseAdapter;
import net.cheesecan.cheeselobby.unitsync.UnitSyncForJava;

/**
 *
 * @author jahziah
 */
public class GraphicsPanel extends JPanel {

    public static Canvas canvas;
    private JGameEngine engine;
    private final int height, width;

    public GraphicsPanel(int width, int height) throws HeadlessException {
        this.width = width;
        this.height = height;
        // Set size
        setSize(new Dimension(width, height));

        // Make visible
        //pack();
        setVisible(true);

        // Initialize canvas
        canvas = new Canvas();

        // add canvas to panel
        add(canvas);

        // Configure canvas
        canvas.setSize(width, height);
        canvas.setFocusable(true);
        canvas.setIgnoreRepaint(true);

        System.out.println("Successfully attached 3D previewer canvas.");

        canvas.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                if (engine != null) {
                    engine.setMouseIsInsideWindow(true);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (engine != null) {
                    engine.setMouseIsInsideWindow(false);
                }
            }
        });

        // Repaint
        //repaint();
    }

    public boolean engineIsUninitialized() {
        return engine == null;
    }

    /**
     * Call this when GL window has not been initialized yet.
     */
    public void init() {
        setVisible(true);
        if (engine == null) {
            // Initialize engine
            engine = new JGameEngine(width, height);

            // Start JGame thread
            engine.start();
        }
        // else engine is initialized already, so we just call MapPreview and tell it to remake its heightmap
    }

    public void show(final UnitSyncForJava unitSync, final int mapChecksum) {
        // Don't show anything if mapname is a map that we don't have
        if (!unitSync.haveMap(mapChecksum)) {
            System.out.println("Don't have this map.");
            return;
        }

        // Give engine task to perform
        engine.giveTask(new Runnable() {

            @Override
            public void run() {
                try {
                    engine.getGame().initHeightMap(unitSync, mapChecksum);
                    engine.setPaused(false);
                    setVisible(true);
                    canvas.setVisible(true);
                    System.out.println("Initialized 3D preview heightmap.");
                } catch (IOException ex) {
                    Logger.getLogger(GraphicsPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    @Override
    public void hide() {
        super.hide();
        engine.setPaused(true);
        canvas.setVisible(false);
        System.out.println("Hiding 3D preview.");
    }

    public void setMinimapActive() {
        engine.giveTask(new Runnable() {

            public void run() {
                System.out.println("Showing minimap.");
                engine.getGame().getMap().setActiveTexture("minimap");
            }
        });
    }

    public void setHeightmapActive() {
        engine.giveTask(new Runnable() {

            public void run() {
                System.out.println("Showing heightmap.");
                engine.getGame().getMap().setActiveTexture("heightmap");
            }
        });
    }
    
    public void setMetalmapActive() {
        engine.giveTask(new Runnable() {

            public void run() {
                System.out.println("Showing metalmap.");
                engine.getGame().getMap().setActiveTexture("metalmap");
            }
        });
    }
}
