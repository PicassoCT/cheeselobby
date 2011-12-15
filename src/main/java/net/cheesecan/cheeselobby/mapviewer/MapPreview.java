package net.cheesecan.cheeselobby.mapviewer;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import net.cheesecan.cheeselobby.unitsync.UnitSyncForJava;
import java.awt.image.ShortLookupTable;
import java.awt.image.LookupOp;
import java.awt.image.BufferedImageOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author jahziah
 */
public final class MapPreview {

    // Camera
    protected int camx;
    protected int camy;
    protected float camrotation;
    protected float zoom;
    // Constants
    public final int mapWidth = 512;
    public final int mapHeight = 512;
    // Objects
    private Heightmap map;
    //private UnitSyncForJava unitSync;

    public MapPreview() {
        camx = mapWidth * 2/3 + 10;
        camy = mapHeight * 2/3 - 10;
        camrotation = 0;
        zoom = 1;
    }

    public void initHeightMap(UnitSyncForJava unitSync, int mapChecksum) throws IOException {
//        this.unitSync = unitSync;

        // Get heightmap
        BufferedImage heightmap = unitSync.getInfoMap(unitSync.mapChecksumToArchiveName(mapChecksum), "height");
        heightmap = flipHorizontally(heightmap); 
        // Resize heightmap
        heightmap = UnitSyncForJava.resize(heightmap, 256, 256, BufferedImage.TYPE_BYTE_GRAY);
        // Get minimap
        BufferedImage minimap = unitSync.getMinimap(unitSync.mapChecksumToArchiveName(mapChecksum), 0);
        minimap = flipHorizontally(minimap); 
        // Get metalmap
        BufferedImage metalmap = unitSync.getInfoMap(unitSync.mapChecksumToArchiveName(mapChecksum), "metal");
        heightmap = flipHorizontally(heightmap); 
        
        // Create a heightmap
        map = new Heightmap(invertImage(heightmap), heightmap, minimap, metalmap);
    }

    private BufferedImage flipHorizontally(BufferedImage bufferedImage) {
        // Flip the image horizontally
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-bufferedImage.getWidth(null), 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return bufferedImage = op.filter(bufferedImage, null);
    }

    public Heightmap getMap() {
        return map;
    }

    private BufferedImage invertImage(final BufferedImage src) {
        short[] invertTable;
        invertTable = new short[256];
        for (int i = 0; i < 256; i++) {
            invertTable[i] = (short) (255 - i);
        }

        final int w = src.getWidth();
        final int h = src.getHeight();
        final BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        final BufferedImageOp invertOp = new LookupOp(new ShortLookupTable(0, invertTable), null);
        return invertOp.filter(src, dst);
    }

    private void updateCamera() {
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glTranslated(camx, camy, 0);

        glRotatef(35.264f, 1.0f, 0.0f, 0.0f);
        glRotatef(180.0f, 0.0f, 0.0f, 1.0f);

        // Rotate camera
        glTranslatef(camx, camy, 0);
        glRotatef(camrotation, 0, 0, 1);
        glTranslatef(-camx, -camy, 0);

        // Scale
        //glTranslated(-camx, -camy, 0);
        //glScalef(zoom, zoom, zoom);
        //glTranslated(camx, camy, 0);

        // TODO pivot points are messed up because of viewing angle rotations
    }

    /**
     * Paint will not run before initHeightMap has been performed.
     */
    protected void paint() {
        if (map == null) {
            return;
        }

        // Update our camera
        updateCamera();
        // Have child objects draw themselves
        map.draw();
    }
}
