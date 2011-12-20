/*
 *  Copyright 2011 Jahziah Wagner.
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
package net.cheesecan.cheeselobby.session;

import java.security.NoSuchAlgorithmException;

/**
 *
 * @author jahziah
 */
public class ClientInfo {
    private String name;
    private String passwordMd5Base64;
    private int cpuFrequency;
    private String localHost;

    public ClientInfo(String name, String passwordPlaintext) throws NoSuchAlgorithmException {
        this.name = name;
        
        // Get password hash & discard the password plaintext immediately
        passwordMd5Base64 = MD5Base64Hash.encrypt(passwordPlaintext);

        // Get cpu frequency
        // In Linux/Unix we could parse the output of 'cat /proc/cpuinfo | grep 'cpu MHz''
        // But in Windows??
        cpuFrequency = 3000;
        
        // Retrieve IP/Hostname
        localHost = "*"; // NetworkInterface.getByName("ppp0").
    }
    
    public int getCpuFrequency() {
        return cpuFrequency;
    }

    public String getLocalHost() {
        return localHost;
    }

    public String getName() {
        return name;
    }

    public String getPasswordMd5Base64() {
        return passwordMd5Base64;
    }
}
