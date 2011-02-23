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
package net.cheesecan.cheeselobby.unitsync;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.HashMap;
import unitsync.UnitsyncLibrary;

/**
 *
 * @author jahziah
 */
public class UnitSyncForJava {

    public class JMapInfo {

        private String author;
        private String description;
        private int tidalStrength;
        private int gravity;
        private float maxMetal;
        private int extractorRadius;
        private int minWind;
        private int maxWind;
        private int width;
        private int height;
        private int[] positionsX;
        private int[] positionsY;

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getExtractorRadius() {
            return extractorRadius;
        }

        public void setExtractorRadius(int extractorRadius) {
            this.extractorRadius = extractorRadius;
        }

        public int getGravity() {
            return gravity;
        }

        public void setGravity(int gravity) {
            this.gravity = gravity;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public float getMaxMetal() {
            return maxMetal;
        }

        public void setMaxMetal(float maxMetal) {
            this.maxMetal = maxMetal;
        }

        public int getMaxWind() {
            return maxWind;
        }

        public void setMaxWind(int maxWind) {
            this.maxWind = maxWind;
        }

        public int getMinWind() {
            return minWind;
        }

        public void setMinWind(int minWind) {
            this.minWind = minWind;
        }

        public int[] getPositionsX() {
            return positionsX;
        }

        public void setPositionsX(int[] positionsX) {
            this.positionsX = positionsX;
        }

        public int[] getPositionsY() {
            return positionsY;
        }

        public void setPositionsY(int[] positionsY) {
            this.positionsY = positionsY;
        }

        public int getTidalStrength() {
            return tidalStrength;
        }

        public void setTidalStrength(int tidalStrength) {
            this.tidalStrength = tidalStrength;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        @Override
        public String toString() {
            return "JMapInfo{" + "author=" + author + "description=" + description + "tidalStrength=" + tidalStrength + "gravity=" + gravity + "maxMetal=" + maxMetal + "extractorRadius=" + extractorRadius + "minWind=" + minWind + "maxWind=" + maxWind + "width=" + width + "height=" + height + "positionsX=" + positionsX + "positionsY=" + positionsY + '}';
        }
    }
    // Stores map name to index
    private HashMap<String, Integer> mapIndexes;    // mapname or archive name matched to its index as seen by unitsync
    private HashMap<Integer, String> mapChecksums;  // Every mapName/map archive name matched to map checksum

    public UnitSyncForJava() {
        // Initialize unitsync
        UnitsyncLibrary.Init(false, 0);

        // Initialize map indexes
        mapIndexes = new HashMap<String, Integer>();
        mapChecksums = new HashMap<Integer, String>();

        // Populate map indexes and sums
        int n = UnitsyncLibrary.GetMapCount();
        for (int i = 0; i < n; i++) {
            String archiveName = UnitsyncLibrary.GetMapName(i).getString(0);
            mapIndexes.put(archiveName, i);
            mapChecksums.put(UnitsyncLibrary.GetMapChecksumFromName(archiveName), archiveName);
            //System.out.println(archiveName + " hashes to " + UnitsyncLibrary.GetMapChecksumFromName(archiveName));
        }
    }

    private String GetError() {
        String error = "";
        try {
            while (true) {
                error += UnitsyncLibrary.GetNextError().getString(0) + "\n";
                System.err.println(error);
            }
        } catch (NullPointerException npe) {
        }
        return error;
    }

    public String mapChecksumToArchiveName(int checksum) throws IOException {
        String retVal = mapChecksums.get(checksum);

        if (retVal == null) {
            throw new IOException("Map does not exist.");
        }

        return retVal;
    }

    public static BufferedImage resize(BufferedImage image, int width, int height, int type) {
        BufferedImage resizedImage = new BufferedImage(width, height,
                type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        return resizedImage;
    }

    public BufferedImage getMinimap(String archiveName, int miplevel) throws IOException {
        try {
            int width = 1024 >> miplevel;
            int height = 1024 >> miplevel;
            short[] colours = UnitsyncLibrary.GetMinimap(archiveName, miplevel).getShortArray(0, width * height);
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_565_RGB);
            WritableRaster rast = img.getRaster();
            rast.setDataElements(0, 0, width, height, colours);
            return img;
        } catch (NullPointerException e) {
            throw new IOException(GetError());
        }
    }

    /**
     * Returns the dimensions of a infomap
     * @param filename
     * @return width, height
     */
    public int[] getInfoMapSize(String filename, String type) {
        // Get dimensions
        IntBuffer widthBuf = IntBuffer.allocate(1);
        IntBuffer heightBuf = IntBuffer.allocate(1);
        UnitsyncLibrary.GetInfoMapSize(filename, type, widthBuf, heightBuf);
        int width = widthBuf.get();
        int height = heightBuf.get();
        return new int[]{width, height};
    }

    /**
     * Retrieves the height map of the specified map as a byte array.
     * @param mapName name of the map
     * @return
     */
    public BufferedImage getInfoMap(String archiveName, String type) throws IOException {
        try {
            // Get dimensions
            int[] dims = getInfoMapSize(archiveName, type);
            int width = dims[0];
            int height = dims[1];

            // Filename
            Pointer pFilename = new Memory(archiveName.length() + 1); // c strings are null-terminated
            pFilename.setString(0, archiveName);

            // Type
            Pointer pType = new Memory(type.length() + 1);
            pType.setString(0, type);

            // Buffer
            int bufSize = width * height;  // java bytebuffer is 1 byte per cell
            Pointer pBuffer = new Memory(bufSize);

            // Fetch
            UnitsyncLibrary.GetInfoMap(pFilename, pType, pBuffer, 1);

            // Read data into array
            byte[] data = pBuffer.getByteArray(0, width * height);

            // Turn into a buffered img
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            WritableRaster rast = img.getRaster();
            rast.setDataElements(0, 0, width, height, data);

            return img;
        } catch (NullPointerException e) {
            throw new IOException(GetError());
        }
    }

    public boolean haveMap(int mapChecksum) {
        return mapChecksums.containsKey(mapChecksum);
    }

    /**
     * Retrieves the mapinfo object for the specified map
     * @param mapName
     */
    public JMapInfo getMapInfo(String archiveName) throws IOException {
        // Create a map info object
        JMapInfo mapInfo;

        // Initialize mapInfo
        mapInfo = new JMapInfo();
        return null;
    }
}
