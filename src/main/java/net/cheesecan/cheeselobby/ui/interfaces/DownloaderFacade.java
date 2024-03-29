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
package net.cheesecan.cheeselobby.ui.interfaces;

import net.cheesecan.cheeselobby.lobby_connection.interfaces.BattleObserver;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.cheesecan.cheeselobby.ui.components.ProgressElement;

/**
 *
 * @author jahziah
 */
public interface DownloaderFacade {
    public void downloadMap(String name, BattleObserver observer);
        
    Map<String, ProgressElement> progressContainer = Collections.synchronizedMap(new HashMap<String, ProgressElement>());
    
    public void notifyDownloadProgress(String filename, int progress, long bps);
    
    public void refreshProgressPane(ProgressElement pe);
    
    public void removeProgressElement(ProgressElement toRemove);
    
    public String getSavePath();
}
