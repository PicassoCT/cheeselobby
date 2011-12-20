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

package net.cheesecan.cheeselobby.lobby_connection.interfaces;

import net.cheesecan.cheeselobby.session.Battle;
import net.cheesecan.cheeselobby.ui.interfaces.BattleObserver;

/**
 *
 * @author jahziah
 */
public interface BattleListObserver extends BattleObserver {

      /**
     * Add battle to a battle table
     * @param name
     * @param battle
     */
    public void addBattle(Battle battle);

    /**
     * Remove a battle from battle table based on user object.
     * @param name
     * @param battle
     */
    public void removeBattle(Battle battle);

    /**
     * Update information on an existing battle.
     * @param battle
     */
    public void updateBattle(Battle battle);

    /**
     * Notifies window that the join attempted just now failed.
     */
    public void joinFailed(String reason);
}
