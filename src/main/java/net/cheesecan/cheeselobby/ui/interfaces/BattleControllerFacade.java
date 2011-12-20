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
package net.cheesecan.cheeselobby.ui.interfaces;

import net.cheesecan.cheeselobby.lobby_connection.interfaces.BattleRoomObserver;

/**
 *
 * @author jahziah
 */
public interface BattleControllerFacade {

    /**
     * Register as a battle observer.
     */
    public void registerAsBattleObserver(BattleRoomObserver battleObserver);

    /**
     * Say something inside current battle.
     * @param str
     */
    public void sayBattle(String str);

    /**
     * Say something inside current battle.
     * @param str
     */
    public void sayExpressionBattle(String str);

    /**
     * Leave current battle.
     */
    public void leaveBattle();
    
    /**
     * Broadcast battle status.
     * @param battleStatus
     */
    public void sendMyBattleStatus(int battleStatus, int color);

    /**
     * Tells controller that we wish to start the game now.
     */
    public void startGame();

    /**
     * Get our own username.
     * @return
     */
    public String getUsername();
    
}
