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
package net.cheesecan.cheeselobby.unitsync;

/**
 *
 * @author jahziah
 */
public class Extractor {

    /*
    static UnitSyncForJava unitSync;

    private static BufferedImage resize(BufferedImage image, int width, int height, int type) {
        BufferedImage resizedImage = new BufferedImage(width, height,
                type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        return resizedImage;
    }

    public void getHeightmap() {
        
    }

    public static void main(String[] args) {
        Runtime.getRuntime().load("/usr/lib/spring/libunitsync.so");
        unitSync = new UnitSyncForJava();

        String mapName = "DeltaSiegeDry";

        try {
            // Get image
            BufferedImage result = unitSync.getInfoMap(mapName, "height");

            // Resize image with same proportions
            float ratio = (float)result.getHeight() / (float)result.getWidth();
            result = resize(result, 256, 256, BufferedImage.TYPE_BYTE_GRAY);

            // Write image
            ImageIO.write(result, "BMP", new File("/home/jahziah/NetBeansProjects_github/net.springrts.cc_CheeseLobby_jar_1.0-SNAPSHOT/heightmap.bmp"));

            // Do same for minimap
            BufferedImage minimap = unitSync.getMinimap(mapName, 0);

            // Write minimap
            ImageIO.write(minimap, "PNG", new File("/home/jahziah/NetBeansProjects_github/net.springrts.cc_CheeseLobby_jar_1.0-SNAPSHOT/minimap.png"));

        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }


    }
     * 
     */
}
