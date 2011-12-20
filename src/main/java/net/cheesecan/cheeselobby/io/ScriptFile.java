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
package net.cheesecan.cheeselobby.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.ini4j.Wini;

/**
 *
 * @author jahziah
 */
public class ScriptFile {

    private Wini ini;

    public ScriptFile(String IP, int port, boolean isHost, String playerName, String myPasswd, String path) throws IOException {
        File file = new File(path);
        file.delete();
        boolean ok = file.createNewFile();

        if(!ok) {
            JOptionPane.showMessageDialog(null, "Couldn't create script.txt, "
                    + "is your spring data path set correctly?\n"
                    + "Do you have write access to the location?");
        }

        // Initialize
        ini = new Wini(file);

        // Write fields
        ini.put("GAME", "HostIP", IP+";");
        ini.put("GAME", "HostPort", port+";");
        ini.put("GAME", "IsHost", 0+";");   // TODO
        ini.put("GAME", "MyPlayerName", playerName+";");
        ini.put("GAME", "MyPasswd", myPasswd+";");
        ini.store(); // write to file

        // Time to add brackets to the file, the old fashioned way(TM)
        BufferedReader bfr = new BufferedReader(new FileReader(file));
        StringBuilder sb = new StringBuilder();

        // Read all lines into sb
        while(bfr.ready()) {
            String str = bfr.readLine();

            // Replace [GAME] with [GAME]\n{
            if(str.equals("[GAME]")) {
                str += "\n{";
            }

            sb.append(str);
            sb.append("\n");
        }

        // Add a } to the end of the file
        sb.append("}");

        // Write contents to file
        PrintWriter pr = new PrintWriter(file);
        pr.print(sb.toString());
        pr.close();
        // Finished
    }

    public static void main(String[] args) {
        try {
            ScriptFile s = new ScriptFile("213.164.111.22", 2255, false, "[PinK]Halcyon", "abc", "script.txt");
        } catch (IOException ex) {
            Logger.getLogger(ScriptFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


}
