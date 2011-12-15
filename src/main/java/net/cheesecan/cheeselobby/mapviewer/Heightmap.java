package net.cheesecan.cheeselobby.mapviewer;

import java.io.IOException;
import org.newdawn.slick.util.BufferedImageUtil;
import org.newdawn.slick.Color;
import org.newdawn.slick.opengl.Texture;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import static org.lwjgl.opengl.GL11.*;

/**
 * Represents a heightmap in the jpanel. Complete with all its textures. Whenever map is changed, just instantiate a new heightmap.
 * @author jahziah
 */
public class Heightmap {
    // Textures
    private final Texture heightmapTexture;
    private final Texture minimapTexture;
    private final Texture metalmapTexture;
    private Texture activeTexture;    // reference to one of the above
    // Container
    private int[][] data;
    // Constants
    public final static float tileSize = 1.4f;
    public final static float heightScale = 0.25f;

    public Heightmap(BufferedImage buf, BufferedImage heightmap, BufferedImage minimap, BufferedImage metalmap) throws IOException {
        Raster r = buf.getData();
        data = new int[buf.getWidth()][buf.getHeight()];
        for (int y = 0; y < buf.getHeight(); y++) {
            for (int x = 0; x < buf.getWidth(); x++) {
                float[] j = new float[3];
                r.getPixel(x, y, j);
                data[x][y] = (int) j[0];
            }
        }

        // Load textures
        heightmapTexture = BufferedImageUtil.getTexture("", heightmap);
        minimapTexture = BufferedImageUtil.getTexture("", minimap);
        metalmapTexture = BufferedImageUtil.getTexture("", metalmap);

        // Set default active texture
        activeTexture = minimapTexture;
        //activeTexture = heightmapTexture;
    }

    /**
     * Set the texture to be drawn on the heightmap.
     * @param name valid choices are 'height' and 'minimap'
     */
    public void setActiveTexture(String name) {
        if (name.equals("heightmap")) {
            activeTexture = heightmapTexture;
            activeTexture.bind();
        } else if (name.equals("minimap")) {
            activeTexture = minimapTexture;
            activeTexture.bind();
        } else if (name.equals("metalmap")) {
            activeTexture = metalmapTexture;
            activeTexture.bind();
        }
    }

    /**
     * Only draws if there is a active texture bound.
     */
    protected void draw() {
        glColor3d(1, 1, 1);
        Color.white.bind();
        activeTexture.bind();
        glBegin(GL_QUADS);

        int width = data[0].length;
        int height = data.length;
        for (int y = 0; y < width - 1; y++) {
            for (int x = 0; x < height - 1; x++) {
                glTexCoord2f((float) x / width, (float) y / height);
                glVertex3f(x * tileSize, y * tileSize, data[x][y] * heightScale);

                glTexCoord2f((float) (x + 1) / width, (float) y / height);
                glVertex3f((x + 1) * tileSize, y * tileSize, data[x + 1][y] * heightScale);

                glTexCoord2f((float) (x + 1) / width, (float) (y + 1) / height);
                glVertex3f((x + 1) * tileSize, (y + 1) * tileSize, data[x + 1][y + 1] * heightScale);

                glTexCoord2f((float) x / width, (float) (y + 1) / height);
                glVertex3f(x * tileSize, (y + 1) * tileSize, data[x][y + 1] *heightScale);
            }
        }
        glEnd();
    }
}