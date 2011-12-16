package net.cheesecan.cheeselobby.io.torrent;

import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author jahziah
 */
public class TorrentHandler {

    public TorrentHandler() {
    }

    public static void downloadMap(String url) {
        File file = null;
        try {
            // Save torrent to hd
            URL u = new URL(url);
            String[] split = u.getFile().split("/");
            file = new File(split[split.length - 1]);
            System.out.println("Saving file " + file.getName());
            FileUtils.copyURLToFile(u, file);

            // Open torrent
            Torrent torrent = Torrent.load(file, new File("./"));
            System.out.println(torrent.getName());
            SharedTorrent t = new SharedTorrent(torrent, new File("/home/jahziah/NetBeansProjects"));
        } catch (IOException ex) {
            Logger.getLogger(TorrentHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
