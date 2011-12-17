package net.cheesecan.cheeselobby.ui.interfaces;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.cheesecan.cheeselobby.ui.components.ProgressElement;

/**
 *
 * @author jahziah
 */
public interface DownloaderFacade {
    public void downloadMap(String name, RefreshableObserver observer);
        
    Map<String, ProgressElement> progressContainer = Collections.synchronizedMap(new HashMap<String, ProgressElement>());
    
    public void notifyDownloadProgress(String filename, int progress, long bps);
    
    public void refreshProgressPane(ProgressElement pe);
    
    public void removeProgressElement(ProgressElement toRemove);
    
    public String getSavePath();
}
