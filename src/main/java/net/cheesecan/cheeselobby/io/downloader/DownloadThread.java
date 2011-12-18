package net.cheesecan.cheeselobby.io.downloader;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.cheesecan.cheeselobby.ui.interfaces.BattleObserver;
import net.cheesecan.cheeselobby.ui.interfaces.DownloaderFacade;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author jahziah
 */
public class DownloadThread implements Runnable {

    private int progress; // 1 - 100 %
    private String location, filename;

    public int getProgress() {
        return progress;
    }

    public enum DownloadType {

        Map,
        Mod,
        Widget
    }

    public static String lookupResourceLocation(String fileName) {
        OutputStream outputStream = null;
        try {
            URL u = new URL("http://zero-k.info/ContentService.asmx");
            URLConnection uc = u.openConnection();
            HttpURLConnection http = (HttpURLConnection) uc;

            http.setDoOutput(true);
            http.setDoInput(true);
            http.setRequestMethod("POST");
            http.setRequestProperty("SOAPAction", "http://tempuri.org/DownloadFile");
            http.setRequestProperty("Content-type", "text/xml");
            outputStream = http.getOutputStream();

            Writer w = new OutputStreamWriter(outputStream);

            w.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n");
            w.write("<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">\r\n");
            w.write("  <soap12:Body>\r\n");
            w.write("    <DownloadFile xmlns=\"http://tempuri.org/\">\r\n");
            w.write("      <internalName>" + fileName + "</internalName>\r\n");
            w.write("    </DownloadFile>\r\n");
            w.write("  </soap12:Body>\r\n");
            w.write("</soap12:Envelope>");

            w.flush();
            w.close();

            InputStream reply = http.getInputStream();

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(reply);
            NodeList childNodes = doc.getFirstChild().getFirstChild().getFirstChild().getChildNodes();


            for (int j = 0; j < childNodes.getLength(); j++) {
                Node node = childNodes.item(j);
                if (node.getNodeName().equals("links")) {
                    return childNodes.item(j).getChildNodes().item(0).getFirstChild().getNodeValue();
                }
            }


            return null;

        } catch (Exception ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    private DownloaderFacade downloader;
    private DownloadType type;
    private BattleObserver observer;

    public DownloadThread(DownloaderFacade downloader, BattleObserver observer, String filename, String location, DownloadType type) {
        this.downloader = downloader;
        this.observer = observer;

        this.location = location; //lookupResourceLocation(filename);
        this.filename = filename;
        this.type = type;
    }

    public void run() {
        if (location == null) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    JOptionPane.showMessageDialog(null, "Sorry, the map could not be located. Try a manual search instead.", "Download failed", JOptionPane.OK_OPTION);
                }
            });
            return;
        }

        try {
            // Get filename with extension e.g. *.sd7
            String filenameWithExt = location.split("/")[location.split("/").length - 1];

            // Open URL
            URL file = new URL(location);
            HttpURLConnection http = (HttpURLConnection) file.openConnection();
            int byteSize = http.getContentLength();
            //System.out.println("Remaining: " + byteSize);

            // Get input stream
            BufferedInputStream is = new BufferedInputStream(http.getInputStream());

            // Get output stream
            FileOutputStream out = null;
            if (type == DownloadType.Map) {
                out = new FileOutputStream(downloader.getSavePath() + "/maps/" + filenameWithExt);
            }

            // Write inputstream to outputstream
            long last = System.currentTimeMillis();
            long bytesRead = 0;
            long bps = 0;

            int len = 0;
            byte[] c = new byte[4096]; // hold four kilobyte

            while ((len = is.read(c)) != -1) {
                out.write(c, 0, len);
                bytesRead += len;

                // Notify GUI if 0.1 s has elapsed
                if (System.currentTimeMillis() - last > 500) {
                    // Note download speed
                    double elapsed = ((double) (System.currentTimeMillis() - last)) / 1000;
                    bps = (int) ((double) bytesRead / elapsed);

                    // reset byte counter
                    bytesRead = 0;

                    // set last time read
                    last = System.currentTimeMillis();

                    // Notify observer(GUI)
                    progress = (int) (((double) out.getChannel().position() / (double) byteSize) * 100);
                    downloader.notifyDownloadProgress(filename, progress, bps);
                }
            }
            // Close both streams when we are done
            is.close();
            out.close();

            // Notify user
            System.out.println("Finished downloading from " + location);
            downloader.notifyDownloadProgress(filename, 100, -1);

            if (observer != null) {
                observer.fireRefreshFromDownloader();
            }

        } catch (Exception ex) {
            Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
