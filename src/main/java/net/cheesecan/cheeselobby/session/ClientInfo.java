package net.cheesecan.cheeselobby.session;

import java.security.NoSuchAlgorithmException;
import net.cheesecan.cheeselobby.session.MD5Base64Hash;

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
