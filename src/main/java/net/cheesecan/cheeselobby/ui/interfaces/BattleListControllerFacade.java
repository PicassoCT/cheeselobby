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

import java.util.List;
import net.cheesecan.cheeselobby.lobby_connection.interfaces.BattleListObserver;
import net.cheesecan.cheeselobby.session.User;

/**
 *
 * @author jahziah
 */
public interface BattleListControllerFacade {

    /**
     * Register as a new battle observer.
     * @param battleObserver
     */
    public void registerBattleListObserver(BattleListObserver battleObserver);

    /**
     * Get the users in a certain battle.
     * @param battleId
     * @return
     */
    public List<User> getUsersInBattle(int battleId);

    /**
     * Tell that we want to join a battle.
     * @param battleId battle to join
     * @return true if we succeeded, otherwise false
     */
    public boolean joinBattle(int battleId, String psw);

     /**
     * Leave current battle.
     */
    public void leaveBattle();

    /**
     * Ask controller if battle requires a password.
     * @return
     */
    public boolean isBattlePasswordRequired(int battleId);
}
