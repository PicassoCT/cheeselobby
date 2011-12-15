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

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * @author jahziah
 */
public class SettingsFile {

    private String filename;
    private PropertiesConfiguration config;

    public SettingsFile() {
        filename = "cheeselobby.properties";

        // Does file exist, if not, create
        boolean exists = true;
        File file = new File(filename);
        if (!file.exists()) {
            try {
                exists = false;
                file.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(SettingsFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // Load file
        try {
            config = new PropertiesConfiguration(filename);
        } catch (ConfigurationException ex) {
            Logger.getLogger(SettingsFile.class.getName()).log(Level.SEVERE, null, ex);
        }

        // If did not exist
        if (!exists) {
            createFile();
        }
    }

    private void createFile() {
        setServerName("Official server");
        setServerHostname("taspringmaster.clan-sy.com");
        setServerPort(8200);
        setUsername("");
        setPassword("");
        setTheme("GraphiteGlassSkin");
        setSpringExePath("");
        setUnitSyncPath("");
        setSpringDataDirectory("");
        setLogging(true);
        setAutoJoinChannels("main\ncheeselobby");
        setLoginAutomatically(false);
        setNumAutoJoinChannels(2);
        save();
    }

    /*
     * Server properties
     */
    public String getServerName() {
        return config.getString("serverName");
    }

    public void setServerName(String name) {
        config.setProperty("serverName", name);
    }

    public String getServerHostname() {
        return config.getString("serverHost");
    }

    public void setServerHostname(String host) {
        config.setProperty("serverHost", host);
    }

    public int getServerPort() {
        return config.getInt("serverPort");
    }

    public void setServerPort(int port) {
        config.setProperty("serverPort", port);
    }

    /*
     * Lobby properties
     */
    public String getAutoJoinChannels() {
        int numAutoJoinChannels = 0;
        try {
            // Get number of autojoin channels
            numAutoJoinChannels = Integer.valueOf(config.getString("numAutoJoinChannels"));
        } catch (NumberFormatException e) {
            System.err.println("Bad format in cheeselobby.properties::numAutoJoinChannels.");
            System.exit(1);
        }

        // Create buffer
        String[] buf = new String[numAutoJoinChannels];
        String retVal = "";

        for (int i = 0; i < numAutoJoinChannels; i++) {
            buf[i] = config.getString("autojoinChannels" + i);
            retVal += buf[i] + "\n";
        }

        return retVal;
    }

    public int getNumAutoJoinChannels() {
        return Integer.valueOf(config.getString("numAutoJoinChannels"));
    }

    public void setNumAutoJoinChannels(Integer num) {
        config.setProperty("numAutoJoinChannels", num.toString());
    }

    public void setAutoJoinChannels(String data) {
        String[] autoJoinChannels = data.split("\n");

        for (int i = 0; i < autoJoinChannels.length; i++) {
            config.setProperty("autojoinChannels" + i, autoJoinChannels[i]);
        }

        // Sety num autojoinchannels
        setNumAutoJoinChannels(autoJoinChannels.length);
    }

    public boolean isLogging() {
        return Boolean.valueOf(config.getString("logging"));
    }

    public void setLogging(boolean logging) {
        config.setProperty("logging", String.valueOf(logging));
    }

    public String getUsername() {
        return config.getString("username");
    }

    public void setUsername(String username) {
        config.setProperty("username", username);
    }

    public String getPassword() {
        return config.getString("password");
    }

    public void setPassword(String password) {
        config.setProperty("password", password);
    }

    public boolean getLoginAutomatically() {
        return Boolean.valueOf(config.getString("loginAutomatically"));
    }

    public void setLoginAutomatically(boolean value) {
        config.setProperty("loginAutomatically", String.valueOf(value));
    }

    /*
     * Spring properties
     */
    public String getUnitSyncPath() {
        return config.getString("unitSyncPath");
    }

    public void setUnitSyncPath(String unitSyncPath) {
        config.setProperty("unitSyncPath", unitSyncPath);
    }

    public String getSpringExePath() {
        return config.getString("springExePath");
    }

    public void setSpringExePath(String springExePath) {
        config.setProperty("springExePath", springExePath);
    }

    public String getSpringDataDirectory() {
        return config.getString("springDatDirectory");
    }

    public void setSpringDataDirectory(String springDirectoryPath) {
        config.setProperty("springDatDirectory", springDirectoryPath);
    }

    public String getTheme() {
        return config.getString("theme");
    }

    public void setTheme(String theme) {
        config.setProperty("theme", theme);
    }

    public void save() {
        try {
            config.save();
        } catch (ConfigurationException ex) {
            Logger.getLogger(SettingsFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setDefaultChannel(String text) {
        config.setProperty("defaultChannel", text);
    }

    public String getDefaultChannel() {
        return config.getString("defaultChannel");
    }
}
