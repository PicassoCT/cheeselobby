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
import java.awt.event.MouseListener;
import net.cheesecan.cheeselobby.unitsync.UnitSyncForJava;

/**
 *
 * @author jahziah
 */
public class GraphicsPanel extends JPanel {

    public static Canvas canvas;
    private JGameEngine engine;
    protected static String heightmapPath;

    public GraphicsPanel() throws HeadlessException {
        heightmapPath = getClass().getResource("/3dpreview/heightmap.bmp").getPath();

        // Set size
        setSize(new Dimension(384, 384));

        // Make visible
        //pack();
        setVisible(true);

        // Initialize canvas
        canvas = new Canvas();

        // add canvas to panel
        add(canvas);

        // Configure canvas
        canvas.setSize(384, 384);
        canvas.setFocusable(true);
        canvas.setIgnoreRepaint(true);

        System.out.println("Finished setting up canvas.");

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if(engine != null) {
                    engine.setMouseIsInsideWindow(true);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if(engine != null) {
                    engine.setMouseIsInsideWindow(false);
                }
            }
        });

        // Repaint
        //repaint();
    }

    public JGameEngine getEngine() {
        return engine;
    }

    /**
     * Call this when GL window has not been initialized yet.
     */
    public void show(final UnitSyncForJava unitSync, final int mapChecksum) {
        // Don't show anything if mapname is a map that we don't have
        if (!unitSync.haveMap(mapChecksum)) {
            return;
        }

        if (engine == null) {
            // Initialize engine
            engine = new JGameEngine();

            // Start JGame thread
            engine.start();
        }
        // else engine is initialized already, so we just call MapPreview and tell it to remake its heightmap

        // Give engine task to perform
        engine.giveTask(new Runnable() {

            @Override
            public void run() {
                try {
                    engine.getGame().initHeightMap(unitSync, mapChecksum);
                } catch (IOException ex) {
                    Logger.getLogger(GraphicsPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

    }
}
