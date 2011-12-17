package net.cheesecan.cheeselobby.ui.interfaces;

import net.cheesecan.cheeselobby.session.Battle;

/**
 *
 * @author jahziah
 */
public interface BattleObserver {
    /**
     * 
     * @return selected or current battle.
     */
    public Battle getRelevantBattle();
    
    public void fireRefreshFromDownloader();
}
